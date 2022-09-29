package nl.rubixstudios.util.gson;

import com.google.gson.*;
import nl.rubixstudios.ticket.object.Ticket;

import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author Djorr
 * @created 19/08/2022 - 20:37
 * @project TicketsRMC
 */
public class TicketTypeAdapter implements JsonSerializer<List<Ticket>>, JsonDeserializer<List<Ticket>> {

    @Override
    public JsonElement serialize(List<Ticket> list, Type type, JsonSerializationContext context) {
        JsonArray array = new JsonArray();

        list.forEach(ticket -> array.add(context.serialize(ticket, ticket.getClass())));

        return array;
    }

    @Override
    public List<Ticket> deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonArray array = json.getAsJsonArray();
        List<Ticket> ticketlist = new ArrayList<>();

        Ticket ticket;

        for(JsonElement element : array) {
            ticket = context.deserialize(element.getAsJsonObject(), Ticket.class);
            ticketlist.add(ticket);
        }

        return ticketlist;
    }
}