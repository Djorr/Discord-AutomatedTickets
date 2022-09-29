package nl.rubixstudios.util;

import net.dv8tion.jda.api.entities.Role;
import nl.rubixstudios.TicketBot;
import nl.rubixstudios.data.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Djorr
 * @created 20/08/2022 - 15:21
 * @project TicketsRMC
 */
public class ChannelPermissionUtil {


    public static List<Role> getAllAvailableRoles() {
        final List<Role> roles = new ArrayList<>();

        Config.STAFF_ROLES.forEach(roleStr -> {
            final Role role = TicketBot.getInstance().getDiscordJda().getRoleById(roleStr);
            if (role == null) return;

            roles.add(role);
        });

        return roles;
    }
}
