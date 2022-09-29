package nl.rubixstudios.ticket.object;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import nl.rubixstudios.TicketBot;
import nl.rubixstudios.ticket.object.ticketbuilder.TicketCategoryType;
import nl.rubixstudios.ticket.object.ticketbuilder.TicketQuestion;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Djorr
 * @created 15/08/2022 - 11:26
 * @project TicketsRMC
 */

@Getter
@Setter
public class Ticket {

    private final String createdByUserId;
    private final String createdByUserName;

    private final String ticketChannelId;
    private final String ticketChannelName;

    private String ticketCategoryType;

    private List<String> usersThatCanAccessChannel;
    private List<TicketQuestion> ticketQuestions;

    private boolean onHold;
    private boolean openLaten;

    private boolean closed;
    private String closedByUserId;

    private boolean claimed;
    private String claimedUserId;

    public Ticket(User user, TextChannel ticketChannel, String ticketName) {
        this.createdByUserId = user.getId();
        this.createdByUserName = user.getName();

        this.ticketChannelId = ticketChannel.getId();
        this.ticketChannelName = ticketName;

        this.usersThatCanAccessChannel = new ArrayList<>();
        this.ticketQuestions = new ArrayList<>();
    }

    public Ticket(Member member, TextChannel ticketChannel, String ticketName) {
        this.createdByUserId = member.getId();
        this.createdByUserName = member.getUser().getName();

        this.ticketChannelId = ticketChannel.getId();
        this.ticketChannelName = ticketName;

        this.usersThatCanAccessChannel = new ArrayList<>();
        this.ticketQuestions = new ArrayList<>();
    }

    public void addTicketQuestion(List<TicketQuestion> ticketQuestions) {
        this.ticketQuestions.clear();
        ticketQuestions.forEach(ticketQuestion -> {
            ticketQuestion.clearAnswers();

            this.ticketQuestions.add(ticketQuestion);
        });
    }

    public boolean allQuestionsAreAnswered() {
        return this.ticketQuestions.stream().filter(ticketQuestion -> ticketQuestion.getAnswer() == null).count() == 0;
    }

    public TicketCategoryType getTicketCategoryType() {
        return TicketBot.getInstance().getTicketController().getTicketManager().getTicketCategoryType(this.ticketCategoryType);
    }

    public void saveTicket() {
        TicketBot.getInstance().getTicketController().getTicketManager().saveTickets(false);
    }
}
