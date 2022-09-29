package nl.rubixstudios.util;

import nl.rubixstudios.ticket.object.Ticket;
import nl.rubixstudios.ticket.object.ticketbuilder.TicketQuestion;
import nl.rubixstudios.util.sorter.QuestionsSorter;

import java.util.List;

/**
 * @author Djorr
 * @created 16/08/2022 - 12:48
 * @project TicketsRMC
 */
public class QuestionUtil {

    public static TicketQuestion sendQuestionInChannel(Ticket ticket) {
        final List<TicketQuestion> ticketQuestions = ticket.getTicketQuestions();
        return getNextQuestion(ticketQuestions);
    }

    public static TicketQuestion getNextQuestion(List<TicketQuestion> ticketQuestions) {
        ticketQuestions.sort(new QuestionsSorter());

        return ticketQuestions.stream().filter(ticketQuestion -> ticketQuestion.getQuestionMessageId() == null && ticketQuestion.getAnswer() == null).findFirst().orElse(null);
    }

    public static TicketQuestion getFirstQuestionWithEmptyAnswer(List<TicketQuestion> ticketQuestions) {
        ticketQuestions.sort(new QuestionsSorter());

        return ticketQuestions.stream().filter(ticketQuestion -> ticketQuestion.getAnswer() == null).findFirst().orElse(null);
    }

    public static TicketQuestion getFirstQuestionWithEmptyAnswer(List<TicketQuestion> ticketQuestions, String messageId) {
        ticketQuestions.sort(new QuestionsSorter());

        return ticketQuestions.stream().filter(ticketQuestion -> ticketQuestion.getQuestionMessageId().equals(messageId)).findFirst().orElse(null);
    }


}
