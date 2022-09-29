package nl.rubixstudios.ticket.listener.ticket;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import nl.rubixstudios.TicketBot;
import nl.rubixstudios.data.Language;
import nl.rubixstudios.ticket.TicketController;
import nl.rubixstudios.ticket.object.Ticket;
import nl.rubixstudios.ticket.object.ticketbuilder.TicketQuestion;
import nl.rubixstudios.util.ChannelPermissionUtil;
import nl.rubixstudios.util.MessageUtil;
import nl.rubixstudios.util.QuestionUtil;
import nl.rubixstudios.util.embed.EmbedUtil;
import nl.rubixstudios.util.sorter.QuestionsSorter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Djorr
 * @created 16/08/2022 - 19:19
 * @project TicketsRMC
 */
public class TicketAnswerEventListener extends ListenerAdapter {

    private TicketController ticketController;

    public TicketAnswerEventListener() {
        this.ticketController = TicketBot.getInstance().getTicketController();
    }

    @Override
    public void onGenericMessage(@NotNull GenericMessageEvent event) {
        if (!(event.getChannel() instanceof TextChannel)) return;
        final TextChannel textChannel = event.getChannel().asTextChannel();

        final Ticket ticket = this.ticketController.getTicketManager().getTicketByChannel(textChannel);
        if (ticket == null) return;
        if (ticket.allQuestionsAreAnswered()) return;

        final List<Message> history = textChannel.getHistory().retrievePast(2).complete();
        if (history.isEmpty()) return;

        final Message message = history.get(0);
        if (message.getAuthor().isBot()) return;
        if (!message.getAuthor().getId().equals(ticket.getCreatedByUserId())) return;

        // Change Permission
        final TextChannelManager channelManager = textChannel.getManager();
        channelManager.putMemberPermissionOverride(Long.parseLong(ticket.getCreatedByUserId()),
                Arrays.asList(Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY),
                Arrays.asList(Permission.MESSAGE_SEND));
        channelManager.queue();

        final TicketQuestion ticketQuestion = QuestionUtil.getFirstQuestionWithEmptyAnswer(ticket.getTicketQuestions(), history.get(1).getId());
        if (ticketQuestion == null) return;

        this.ticketController.saveAnswer(message, ticketQuestion);
        ticket.saveTicket();

        if (ticket.allQuestionsAreAnswered()) {
            this.sendOverzicht(textChannel, ticket);
            return;
        }

        this.ticketController.sendQuestion(textChannel, ticket);
    }

    private void sendOverzicht(TextChannel textChannel, Ticket ticket) {
        final String description = this.getStringBuilderToString(getAnswersList(ticket));

        MessageUtil.deletedHistory(textChannel, (2 * ticket.getTicketQuestions().size()) + 1);

        // Change Permission
        final TextChannelManager channelManager = textChannel.getManager();
        channelManager.putMemberPermissionOverride(Long.parseLong(ticket.getCreatedByUserId()),
                Arrays.asList(Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY),
                Arrays.asList(Permission.MESSAGE_SEND));

        ChannelPermissionUtil.getAllAvailableRoles().forEach(role -> channelManager.putRolePermissionOverride(role.getIdLong(),
                Arrays.asList(Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY),
                Arrays.asList(Permission.MESSAGE_SEND)));

        channelManager.queue();

        textChannel.sendMessageEmbeds(EmbedUtil.createEmptyEmbed(
                Language.TICKET_ANSWER_TITEL,
                description
        )).setActionRows(ActionRow.of(
                Button.danger("claim-ticket", "Claim ticket")
        )).queue();

    }

    private List<String> getAnswersList(Ticket ticket) {
        final List<String> entries = new ArrayList<>();

        final List<TicketQuestion> ticketQuestions = ticket.getTicketQuestions();
        if (ticketQuestions.isEmpty()) return entries;

        ticketQuestions.sort(new QuestionsSorter());

        for (int i = 0; i < ticketQuestions.size(); i++) {
            final TicketQuestion ticketQuestion = ticketQuestions.get(i);
            entries.add("**" + ticketQuestion.getQuestion() + "**\n");
            entries.add(ticketQuestion.getAnswer() + "\n");
            if (i != ticketQuestions.size()) {
                entries.add("\n");
            }
        }

        return entries;
    }

    private String getStringBuilderToString(List<String> data) {
        final StringBuilder sb = new StringBuilder();
        for (String s : data) {
            sb.append(s);
            sb.append("\t");
        }
        return sb.toString();
    }
}
