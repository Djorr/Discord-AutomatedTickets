package nl.rubixstudios.ticket.listener.ticket;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import nl.rubixstudios.TicketBot;
import nl.rubixstudios.data.Language;
import nl.rubixstudios.ticket.TicketController;
import nl.rubixstudios.ticket.object.Ticket;
import nl.rubixstudios.ticket.object.ticketbuilder.TicketCategory;
import nl.rubixstudios.ticket.object.ticketbuilder.TicketCategoryType;
import nl.rubixstudios.util.MessageUtil;
import nl.rubixstudios.util.embed.EmbedUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.EnumSet;
import java.util.List;


/**
 * @author Djorr
 * @created 15/08/2022 - 23:39
 * @project TicketsRMC
 */
public class TicketOpenEventListener extends ListenerAdapter {

    private final TicketController ticketController;

    public TicketOpenEventListener() {
        this.ticketController = TicketBot.getInstance().getTicketController();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        final Button button = event.getButton();
        if (button.getId() == null) return;

        final TicketCategory ticketCategory = this.ticketController.getTicketManager().getTicketCategoryStartsWith(button.getId());
        if (ticketCategory == null) return;

        final List<TicketCategoryType> ticketCategoryTypes = ticketCategory.getTicketCategoryTypes();
        if (ticketCategoryTypes == null) return;

        if (this.ticketController.getTicketManager().hasPlayerMaxLimitTickets(event.getUser())) {
            event.reply(Language.TICKET_ALREADY_OPEN).setEphemeral(true).queue();
            return;
        }

        final TextChannel ticketChannel = this.ticketController.openTicketChannel(event, ticketCategory);
        if (ticketChannel == null) return;

        this.sendCategoriesInTicketChannel(ticketChannel, ticketCategoryTypes);
    }

    private void sendCategoriesInTicketChannel(TextChannel ticketChannel, List<TicketCategoryType> ticketCategoryTypes) {
        final SelectMenu.Builder chooseTicketType = SelectMenu.create("ticket-dropdown");
        chooseTicketType.setPlaceholder(Language.TICKET_CHOOSE_CATEGORY_TITEL);

        ticketCategoryTypes.forEach(type -> chooseTicketType.addOption(type.getTicketDisplayName(), type.getTicketIdentifier(), "", Emoji.fromUnicode("U+1F4D8")));
        chooseTicketType.setRequiredRange(1, 1);

        ticketChannel.sendMessageEmbeds(EmbedUtil.createEmbedMessage(
                        Language.TICKET_CHOOSE_TYPE_INFORMATION_TITEL,
                        Language.TICKET_CHOOSE_TYPE_INFORMATION_MESSAGE
                )).setActionRows(ActionRow.of(chooseTicketType.build()))
                .queue();
    }

    @Override
    public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
        final SelectMenu selectMenu = event.getComponent();
        if (selectMenu.getId() == null) return;

        if (!selectMenu.getId().equals("ticket-dropdown")) return;

        final TextChannel textChannel = event.getChannel().asTextChannel();
        final Ticket ticket = this.ticketController.getTicketManager().getTicketByChannel(textChannel);
        if (ticket == null) return;

        if (!event.getUser().getId().equals(ticket.getCreatedByUserId())) {
            event.reply(Language.TICKET_ONLY_OWNER_OF_TICKET).setEphemeral(true).queue();
            return;
        }

        MessageUtil.deletedHistory(textChannel, 1);

        final String chosenOption = event.getValues().get(0);
        if (chosenOption == null) return;

        final TicketCategoryType ticketCategoryType = this.ticketController.getTicketManager().getTicketCategoryType(chosenOption);
        if (ticketCategoryType == null) return;

        ticket.setTicketCategoryType(this.ticketController.getTicketManager().getTicketCategoryType(chosenOption).getTicketIdentifier());
        ticket.addTicketQuestion(ticketCategoryType.getTicketTicketQuestions());
        ticket.saveTicket();

        this.changeChannelToCategoryType(textChannel, ticket);

        textChannel.sendMessageEmbeds(EmbedUtil.createEmptyEmbed(
                Language.REACT_TO_RECEIVE_SUPPORT,
                null
        )).queue();

        this.ticketController.sendQuestion(textChannel, ticket);
    }

    private void changeChannelToCategoryType(TextChannel textChannel, Ticket ticket) {
        final TicketCategoryType ticketCategoryType = ticket.getTicketCategoryType();
        if (ticketCategoryType == null) return;

        final TextChannelManager textChannelManager = textChannel.getManager();
        ticketCategoryType.getTicketTagRoles().forEach(role -> textChannelManager.putRolePermissionOverride(role.getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_HISTORY), null));
        textChannelManager.setName(ticketCategoryType.getTicketPrefix() + "-" + ticket.getCreatedByUserName());
        textChannelManager.queue();
    }
}
