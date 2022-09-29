package nl.rubixstudios.ticket.listener.ticket;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import nl.rubixstudios.TicketBot;
import nl.rubixstudios.data.Config;
import nl.rubixstudios.data.Language;
import nl.rubixstudios.ticket.TicketController;
import nl.rubixstudios.ticket.object.Ticket;
import nl.rubixstudios.util.LogUtil;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

/**
 * @author Djorr
 * @created 15/08/2022 - 23:23
 * @project TicketsRMC
 */
public class TicketCloseEventListener extends ListenerAdapter {

    private final TicketController ticketController;

    public TicketCloseEventListener() {
        this.ticketController = TicketBot.getInstance().getTicketController();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        final Button button = event.getButton();
        if (button.getId() == null) return;

        final TextChannel textChannel = event.getChannel().asTextChannel();

        if (button.getId().equals("ticket-close")) {
            final Ticket ticket = this.ticketController.getTicketManager().getTicketByChannel(textChannel);
            if (ticket == null) {
                this.ticketController.removeTicketChannel(event, textChannel);
                return;
            }

            if (!this.hasPermission(event.getUser(), ticket)) return;

            this.ticketController.preCloseTicket(textChannel, ticket);
        } else if (button.getId().equals("ticket-close-yes")) {
            final Ticket ticket = this.ticketController.getTicketManager().getTicketByChannel(textChannel);
            if (ticket == null) return;

            if (!this.hasPermission(event.getUser(), ticket)) return;
            this.ticketController.closeTicketChannelByEvent(event, textChannel, ticket);

            ticket.setClosed(true);
            ticket.setClosedByUserId(event.getUser().getId());
            ticket.saveTicket();

            final TextChannel logChannel = TicketBot.getInstance().getDiscordJda().getTextChannelById(Config.LOG_CHANNEL);
            if (logChannel != null) {
                LogUtil.sendTranscript(ticket.getCreatedByUserId(), textChannel, logChannel);
            }
        } else if (button.getId().equals("ticket-close-no")) {
            final Ticket ticket = this.ticketController.getTicketManager().getTicketByChannel(textChannel);
            if (ticket == null) return;

            if (!this.hasPermission(event.getUser(), ticket)) return;

            textChannel.retrieveMessageById(event.getInteraction().getMessage().getId()).queue(m -> {
                if (m != null) m.delete().queue();
            });

            if (ticket.getTicketCategoryType() != null) {
                final TextChannelManager textChannelManager = textChannel.getManager();
                ticket.getTicketCategoryType().getTicketTagRoles().forEach(role -> {
                    textChannelManager.putPermissionOverride(role, EnumSet.of(Permission.MESSAGE_SEND), null).queue();
                });
            }
        } else if (button.getId().equals("ticket-remove")) {
            final Ticket ticket = this.ticketController.getTicketManager().getTicketByChannel(textChannel);
            if (ticket == null) return;

            this.ticketController.getTicketManager().removeTicketFromCacheByTicket(ticket);
            this.ticketController.removeTicketChannel(event, textChannel);
        }
    }

    private boolean hasPermission(User user, Ticket ticket) {
        final boolean matchedCreator = user.getId().equals(ticket.getCreatedByUserId());
        final boolean isNotCreator = ticket.getUsersThatCanAccessChannel().contains(user.getId());

        if (matchedCreator && (ticket.isOnHold() || ticket.isOpenLaten() || ticket.isClosed())) {
            return false;
        } else if (isNotCreator) {
            return false;
        }
        return true;
    }
}
