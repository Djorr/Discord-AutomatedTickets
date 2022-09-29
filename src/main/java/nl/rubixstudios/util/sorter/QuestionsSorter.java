package nl.rubixstudios.util.sorter;

import nl.rubixstudios.ticket.object.ticketbuilder.TicketQuestion;

import java.util.Comparator;

/**
 * @author Djorr
 * @created 16/08/2022 - 19:25
 * @project TicketsRMC
 */
public class QuestionsSorter implements Comparator<TicketQuestion> {

    @Override
    public int compare(TicketQuestion o1, TicketQuestion o2) {
        return Integer.compare(o1.getQuestionId(), o2.getQuestionId());
    }

}
