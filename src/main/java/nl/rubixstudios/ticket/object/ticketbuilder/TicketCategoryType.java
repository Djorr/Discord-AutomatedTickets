package nl.rubixstudios.ticket.object.ticketbuilder;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Role;
import nl.rubixstudios.TicketBot;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Djorr
 * @created 16/08/2022 - 15:08
 * @project TicketsRMC
 */

@Getter
@Setter
public class TicketCategoryType {

    private final String ticketIdentifier;
    private final String ticketPrefix;
    private final String ticketDisplayTopic;
    private final String ticketDisplayName;

    private final List<Role> ticketTagRoles;
    private List<TicketQuestion> ticketTicketQuestions;

    public TicketCategoryType(String ticketIdentifier, String ticketPrefix, String ticketDisplayTopic, String ticketDisplayName, List<String> ticketTagRoleIds) {
        this.ticketIdentifier = ticketIdentifier;
        this.ticketPrefix = ticketPrefix;
        this.ticketDisplayTopic = ticketDisplayTopic;
        this.ticketDisplayName = ticketDisplayName;

        this.ticketTagRoles = new ArrayList<>();
        this.setupTagRoles(ticketTagRoleIds);

        this.ticketTicketQuestions = new ArrayList<>();
    }

    private void setupTagRoles(List<String> tagRoleIds) {
        tagRoleIds.forEach(roleId -> {
            final Role role = TicketBot.getInstance().getDiscordJda().getRoleById(roleId);
            if (role == null) return;

            this.ticketTagRoles.add(role);
        });
    }
}
