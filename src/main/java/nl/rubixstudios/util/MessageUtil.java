package nl.rubixstudios.util;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author Djorr
 * @created 15/08/2022 - 23:50
 * @project TicketsRMC
 */
public class MessageUtil {

    public static void deletedHistory(TextChannel textChannel, int amount) {
        final List<Message> messageHistory = textChannel.getHistory().retrievePast(amount).complete();
        if (!messageHistory.isEmpty()) {
            messageHistory.forEach(message -> {
                if (message != null) message.delete().queue();
            });
        }
    }

    public static String someStringFilter(final String string, final String allowedCharacters) {
        return string.replaceAll("[^" + allowedCharacters + "]", "").replace(" ", "");
    }

    private static DateTimeFormatter date = DateTimeFormatter.ofPattern("dd. MMM yyyy hh:mm:ss");

    public static String formatTime(LocalDateTime localDateTime){
        LocalDateTime time = LocalDateTime.from(localDateTime.atOffset(ZoneOffset.UTC));
        return time.format(date) + " UTC";
    }
}
