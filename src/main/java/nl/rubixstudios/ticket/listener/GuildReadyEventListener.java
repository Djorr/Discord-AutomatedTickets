package nl.rubixstudios.ticket.listener;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import nl.rubixstudios.TicketBot;
import nl.rubixstudios.data.Config;
import nl.rubixstudios.data.Language;
import nl.rubixstudios.ticket.TicketController;
import nl.rubixstudios.ticket.command.CommandManager;
import nl.rubixstudios.ticket.listener.ticket.TicketAnswerEventListener;
import nl.rubixstudios.ticket.listener.ticket.TicketClaimEventListener;
import nl.rubixstudios.ticket.listener.ticket.TicketCloseEventListener;
import nl.rubixstudios.ticket.listener.ticket.TicketOpenEventListener;
import nl.rubixstudios.util.MessageUtil;
import nl.rubixstudios.util.embed.EmbedUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Djorr
 * @created 15/08/2022 - 23:49
 * @project TicketsRMC
 */
public class GuildReadyEventListener extends ListenerAdapter {

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        long SETTING_UP_TIME = System.currentTimeMillis();
        System.out.println("Setting up all discord embeds..");

        TicketBot.getInstance().ticketController = new TicketController();
        this.setupEventsAfterGuildReady();

        this.setupTestEmbed();

        System.out.println("Setted up all discord embes. Took <time>ms.".replace("<time>", "" + (System.currentTimeMillis() - SETTING_UP_TIME)));
    }

    private void setupEventsAfterGuildReady() {
        // Tickets
        TicketBot.getInstance().discordJda.addEventListener(new TicketOpenEventListener());
        TicketBot.getInstance().discordJda.addEventListener(new TicketCloseEventListener());
        TicketBot.getInstance().discordJda.addEventListener(new TicketAnswerEventListener());
        TicketBot.getInstance().discordJda.addEventListener(new TicketClaimEventListener());

        TicketBot.getInstance().discordJda.addEventListener(new CommandManager());
    }

    private void setupTestEmbed() {
        final TextChannel textChannel = TicketBot.getInstance().getDiscordJda().getTextChannelById(Config.EMBED_CHANNEL);
        if (textChannel == null) return;

        MessageUtil.deletedHistory(textChannel, 100);

        final String informationDesc = getStringBuilderToString(convertList(Language.TICKET_CREATE_INFORMATION_MESSAGE));

        textChannel.sendMessageEmbeds(EmbedUtil.createEmbedMessage(
                Language.TICKET_CREATE_INFORMATION_TITEL,
                informationDesc
        )).queue();

        final String reactionDesc = getStringBuilderToString(convertList(Language.TICKET_CREATE_REACTION_MESSAGE));

        textChannel.sendMessageEmbeds(EmbedUtil.createEmbedMessage(
                Language.TICKET_CREATE_REACTION_TITEL,
                reactionDesc
        ))
        .setActionRows(this.getTicketCreationButtons())
        .queue();
    }

    private ActionRow getTicketCreationButtons() {
        final List<Button> buttons = new ArrayList<>();

        final TicketController ticketController = TicketBot.getInstance().getTicketController();

        ticketController.getTicketManager().getTicketCategories().forEach(ticketCategory -> {
            buttons.add(Button.primary(ticketCategory.getCategoryIdentifier(), ticketCategory.getCategoryName()));
        });

        return ActionRow.of(buttons);
    }

    private List<String> convertList(List<String> messages) {
        final List<String> entries = new ArrayList<>();

        if (messages.isEmpty()) return entries;

        for (final String message : messages) {
            entries.add(message + "\n");
        }

        return entries;
    }

    private String getStringBuilderToString(List<String> data) {
        final StringBuilder sb = new StringBuilder();
        for (String s : data) {
            sb.append(s);
            sb.append("\t");
        }
        return sb.toString();
    }


}
