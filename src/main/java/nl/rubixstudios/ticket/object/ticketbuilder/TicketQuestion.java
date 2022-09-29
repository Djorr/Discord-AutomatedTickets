package nl.rubixstudios.ticket.object.ticketbuilder;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Djorr
 * @created 16/08/2022 - 12:45
 * @project TicketsRMC
 */

@Getter
@Setter
public class TicketQuestion {

    private final int questionId;
    private final String question;
    private final boolean yesOrNoBool;

    private String questionMessageId;
    private String answer;

    public TicketQuestion(int questionId, String question, boolean yesOrNoBool) {
        this.questionId = questionId;
        this.question = question;

        this.yesOrNoBool = yesOrNoBool;
    }

    public void clearAnswers() {
        this.questionMessageId = null;
        this.answer = null;
    }

}
