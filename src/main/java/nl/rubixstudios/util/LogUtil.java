package nl.rubixstudios.util;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ContextException;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import nl.rubixstudios.TicketBot;
import nl.rubixstudios.util.embed.EmbedUtil;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Djorr
 * @created 20/08/2022 - 01:07
 * @project TicketsRMC
 */
public class LogUtil {

    public static void sendTranscript(@Nullable String targetUserId, TextChannel ticketToTranscript, TextChannel logChannel){
        final Guild guild = ticketToTranscript.getGuild();

        final MessageHistory history = ticketToTranscript.getHistory();
        while(history.retrievePast(100).complete().size() > 0);

        final List<String> transcripts = new LinkedList<>();
        final List<String> transcriptFile = new LinkedList<>();
        for(Message message : history.getRetrievedHistory()){
            String timestamp = MessageUtil.formatTime(LocalDateTime.from(message.getTimeCreated()));
            transcripts.add(String.format(
                    "`[%s]` **%s**: %s",
                    timestamp,
                    message.getAuthor().getName(),
                    message.getContentDisplay()
            ));
            transcriptFile.add(String.format(
                    "[%s] %s: %s",
                    timestamp,
                    message.getAuthor().getName(),
                    message.getContentDisplay().replace("\n", "\r\n")
            ));
        }
        Collections.reverse(transcripts);
        Collections.reverse(transcriptFile);

        final List<String> transcriptsMsg = new LinkedList<>();
        final List<String> messageList = new LinkedList<>();

        for(String message : transcripts){
            if(messageList.stream().mapToInt(String::length).sum() + messageList.size() + message.length() + 1 > 1992){
                transcriptsMsg.add(String.join("\n", messageList));
                messageList.clear();
            }
            messageList.add(message);
        }
        if(messageList.size() > 0){
            transcriptsMsg.add(String.join("\n", messageList));
        }

        final StringBuilder builder = new StringBuilder();
        for(String message : transcriptFile){
            builder.append(message).append("\r\n");
        }

        InputStream is;
        try {
            is = new ByteArrayInputStream(builder.toString().getBytes("UTF-8"));
        }catch (Exception ex){
            is = null;
        }

        final InputStream input = is;

        // Sent to player in dm
        if (targetUserId != null) {
            final User targetUser = TicketBot.getInstance().getDiscordJda().retrieveUserById(targetUserId).complete();
            if (targetUser != null) {
                targetUser.openPrivateChannel()
                        .flatMap(channel -> {
                            assert input != null;
                            return channel.sendMessageEmbeds(EmbedUtil.createEmbedMessage(String.format(
                                            "The ticket `%s` in the guild `%s` was closed!\n" +
                                                    "Here's a transcript of the chat:",
                                            ticketToTranscript.getName(),
                                            guild.getName()
                                    ), null))
                                    .addFile(input, String.format(
                                            "%s.txt",
                                            ticketToTranscript.getName()));
                        })
                        .queue(null, new ErrorHandler()
                                .handle(ErrorResponse.CANNOT_SEND_TO_USER,
                                        (ex) -> System.out.println("Cannot send message to user")));
            }
        }

        // Send in log channel
        if (logChannel != null) {
            logChannel.sendMessageEmbeds(EmbedUtil.createEmbedMessage(String.format(
                    "The ticket `%s` was closed!\n" +
                            "Here's a transcript of the chat:",
                    ticketToTranscript.getName()
            ), null)).queue();

            if (input != null) {
                logChannel.sendFile(input, String.format(
                        "%s.txt",
                        ticketToTranscript.getName()
                )).queue();
            }
        }
    }
}
