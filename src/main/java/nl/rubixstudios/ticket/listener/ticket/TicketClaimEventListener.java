package nl.rubixstudios.ticket.listener.ticket;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import nl.rubixstudios.TicketBot;
import nl.rubixstudios.data.Language;
import nl.rubixstudios.ticket.TicketController;
import nl.rubixstudios.ticket.object.Ticket;
import nl.rubixstudios.util.ChannelPermissionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * @author Djorr
 * @created 19/08/2022 - 19:34
 * @project TicketsRMC
 */
public class TicketClaimEventListener extends ListenerAdapter {

    private final TicketController ticketController;

    public TicketClaimEventListener () {
        this.ticketController = TicketBot.getInstance().getTicketController();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        final Button button = event.getButton();
        if (button.getId() == null) return;

        if (!button.getId().equals("claim-ticket")) return;

        final TextChannel textChannel = event.getChannel().asTextChannel();

        final Ticket ticket = this.ticketController.getTicketManager().getTicketByChannel(textChannel);
        if (ticket == null) return;

        if (!hasPermission(event.getUser(), ticket)) {
            event.reply(Language.TICKET_PERMISSIONS_NO_PERMS)
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (ticket.isClaimed()) {
            event.reply(Language.TICKET_CLAIMED_ALREADY)
                    .setEphemeral(true)
                    .queue();
            return;
        }

        ticket.setClaimed(true);
        ticket.setClaimedUserId(event.getUser().getId());
        ticket.saveTicket();

        final TextChannelManager channelManager = textChannel.getManager();
        channelManager.putMemberPermissionOverride(Long.parseLong(ticket.getCreatedByUserId()),
                Arrays.asList(Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES),
                null);

        ChannelPermissionUtil.getAllAvailableRoles().forEach(role -> {
            channelManager.putRolePermissionOverride(role.getIdLong(),
                    Arrays.asList(Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY),
                    null);
        });
        channelManager.queue();

        event.reply(Language.TICKET_CLAIMED_MESSAGE
                        .replace("<userId>", event.getUser().getId()))
                .queue();
    }

    public boolean hasPermission(User requester, Ticket ticket) {
        final boolean isTicketCreator = requester.getId().equals(ticket.getCreatedByUserId());
        final boolean isFriendOfTicketCreator = ticket.getUsersThatCanAccessChannel().stream().anyMatch(memberId -> memberId.equals(requester.getId()));

        return !isTicketCreator && !isFriendOfTicketCreator;
    }
}
