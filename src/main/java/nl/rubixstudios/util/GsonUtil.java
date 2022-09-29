package nl.rubixstudios.util;

import com.google.gson.reflect.TypeToken;
import nl.rubixstudios.ticket.object.Ticket;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author Djorr
 * @created 15/08/2022 - 11:47
 * @project TicketsRMC
 */
public class GsonUtil {
    public static final Type TICKET_TYPE = new TypeToken<List<Ticket>>(){}.getType();
    public static final Type BLACKLIST_TYPE = new TypeToken<List<String>>(){}.getType();
}
