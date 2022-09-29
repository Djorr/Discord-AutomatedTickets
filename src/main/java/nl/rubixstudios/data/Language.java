package nl.rubixstudios.data;

import nl.rubixstudios.TicketBot;

import java.util.List;

/**
 * @author Djorr
 * @created 20/08/2022 - 01:41
 * @project TicketsRMC
 */
public class Language {

    public static String TICKET_PERMISSIONS_NO_PERMS;

    public static String REACT_TO_RECEIVE_SUPPORT;

    public static String TICKET_ALREADY_OPEN;

    public static String TICKET_CHOOSE_CATEGORY_TITEL;
    public static List<String> TICKET_CHOOSE_CATEGORY_MESSAGE;

    public static String TICKET_CREATE_INFORMATION_TITEL;
    public static List<String> TICKET_CREATE_INFORMATION_MESSAGE;

    public static String TICKET_CREATE_REACTION_TITEL;
    public static List<String> TICKET_CREATE_REACTION_MESSAGE;

    public static String TICKET_ANSWER_TITEL;

    public static String TICKET_CLAIMED_ALREADY;
    public static String TICKET_CLAIMED_MESSAGE;

    public static String TICKET_REMOVE_DELETE;

    public static String TICKET_ONLY_OWNER_OF_TICKET;

    public static String TICKET_CHOOSE_TYPE_INFORMATION_TITEL;
    public static String TICKET_CHOOSE_TYPE_INFORMATION_MESSAGE;

    public Language() {
        final ConfigFileUtil language = TicketBot.getInstance().getLanguageFile();

        TICKET_PERMISSIONS_NO_PERMS = language.getFile().getString("TICKET.PERMISSIONS.NO_PERMISSION");

        REACT_TO_RECEIVE_SUPPORT = language.getFile().getString("TICKET.REACT_SUPPORT");

        TICKET_ONLY_OWNER_OF_TICKET = language.getFile().getString("TICKET.ONLY_OWNER_OF_TICKET_CAN_DO_THIS");

        TICKET_ALREADY_OPEN = language.getFile().getString("TICKET.ALREADY_TICKET_OPEN");

        TICKET_CHOOSE_CATEGORY_TITEL = language.getFile().getString("TICKET.EMBED.CHOOSE_CATEGORY.TITEL");
        TICKET_CHOOSE_CATEGORY_MESSAGE = language.getFile().getStringList("TICKET.EMBED.CHOOSE_CATEGORY.MESSAGE");

        TICKET_CREATE_INFORMATION_TITEL = language.getFile().getString("TICKET.EMBED.CREATE.INFORMATION.TITEL");
        TICKET_CREATE_INFORMATION_MESSAGE = language.getFile().getStringList("TICKET.EMBED.CREATE.INFORMATION.MESSAGE");

        TICKET_CREATE_REACTION_TITEL = language.getFile().getString("TICKET.EMBED.CREATE.REACTION.TITEL");
        TICKET_CREATE_REACTION_MESSAGE = language.getFile().getStringList("TICKET.EMBED.CREATE.REACTION.MESSAGE");

        TICKET_ANSWER_TITEL = language.getFile().getString("TICKET.EMBED.ANSWER.REACTION.TITEL");

        TICKET_CLAIMED_ALREADY = language.getFile().getString("TICKET.EMBED.STAFF_CLAIMED.REACTION.IS_ALREADY_CLAIMED");
        TICKET_CLAIMED_MESSAGE = language.getFile().getString("TICKET.EMBED.STAFF_CLAIMED.REACTION.MESSAGE");

        TICKET_REMOVE_DELETE = language.getFile().getString("TICKET.EMBED.REMOVE.REACTION.MESSAGE");

        TICKET_CHOOSE_TYPE_INFORMATION_TITEL = language.getFile().getString("TICKET.EMBED.CHOOSE_CATEGORY.INFORMATION.TITEL");
        TICKET_CHOOSE_TYPE_INFORMATION_MESSAGE = language.getFile().getString("TICKET.EMBED.CHOOSE_CATEGORY.INFORMATION.MESSAGE");
    }
}
