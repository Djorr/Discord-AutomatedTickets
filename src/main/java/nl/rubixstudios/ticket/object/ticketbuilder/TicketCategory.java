package nl.rubixstudios.ticket.object.ticketbuilder;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Djorr
 * @created 16/08/2022 - 15:08
 * @project TicketsRMC
 */

@Getter
@Setter
public class TicketCategory {

    private final String categoryIdentifier;
    private final String categoryId;
    private final String categoryName;

    private final List<TicketCategoryType> ticketCategoryTypes;

    public TicketCategory(String categoryIdentifier, String categoryId, String categoryName) {
        this.categoryIdentifier = categoryIdentifier;
        this.categoryId = categoryId;
        this.categoryName = categoryName;

        this.ticketCategoryTypes = new ArrayList<>();
    }
}
