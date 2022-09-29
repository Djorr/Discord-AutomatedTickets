package nl.rubixstudios.data;

import nl.rubixstudios.TicketBot;
import org.simpleyaml.configuration.ConfigurationSection;

import java.util.List;

/**
 * @author Djorr
 * @created 15/08/2022 - 14:05
 * @project TicketsRMC
 */
public class Config {

    public static String BOT_NAME;
    public static String BOT_TOKEN;

    public static String EMBED_CHANNEL;
    public static String LOG_CHANNEL;

    public static String CATEGORY_OPEN_TICKETS;
    public static String CATEGORY_CLOSED_TICKETS;
    public static String CATEGORY_OPEN_LATEN_TICKETS;
    public static String CATEGORY_ON_HOLD_TICKETS;

    public static List<String> STAFF_ROLES;

    public Config() {
        final ConfigFileUtil config = TicketBot.getInstance().getConfigFile();

        BOT_NAME = config.getFile().getString("BOT.NAME");
        BOT_TOKEN = config.getFile().getString("BOT.TOKEN");

        EMBED_CHANNEL = config.getFile().getString("TICKET.TICKET_EMBED_CHANNEL");
        LOG_CHANNEL = config.getFile().getString("TICKET.TICKET_LOG_CHANNEL");

        CATEGORY_OPEN_TICKETS = config.getFile().getString("TICKET.IMPORTANT_CATEGORIES.OPEN");
        CATEGORY_CLOSED_TICKETS = config.getFile().getString("TICKET.IMPORTANT_CATEGORIES.CLOSED");
        CATEGORY_OPEN_LATEN_TICKETS = config.getFile().getString("TICKET.IMPORTANT_CATEGORIES.OPEN_LATEN");
        CATEGORY_ON_HOLD_TICKETS = config.getFile().getString("TICKET.IMPORTANT_CATEGORIES.ON_HOLD");

        STAFF_ROLES = config.getFile().getStringList("TICKET.STAFF_ROLES");
    }
}
