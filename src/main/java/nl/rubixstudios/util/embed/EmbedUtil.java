package nl.rubixstudios.util.embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import nl.rubixstudios.data.Config;

import javax.annotation.Nullable;
import java.awt.*;

/**
 * @author Djorr
 * @created 15/08/2022 - 23:28
 * @project TicketsRMC
 */
public class EmbedUtil {

    public static MessageEmbed createEmbedMessage(@Nullable String title,
                                               @Nullable String description) {
        final EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setAuthor(Config.BOT_NAME);

        if (title != null) embedBuilder.setTitle(title);
        embedBuilder.setColor(Color.cyan);

        if (description != null) embedBuilder.setDescription(description);

        embedBuilder.setFooter(Config.BOT_NAME);
        return embedBuilder.build();
    }

    public static MessageEmbed createEmptyEmbed(@Nullable String title,
                                                  @Nullable String description) {
        final EmbedBuilder embedBuilder = new EmbedBuilder();

        if (title != null) embedBuilder.setTitle(title);
        embedBuilder.setColor(Color.cyan);

        if (description != null) embedBuilder.setDescription(description);
        return embedBuilder.build();
    }
}
