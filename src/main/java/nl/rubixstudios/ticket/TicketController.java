package nl.rubixstudios.ticket;

import lombok.Getter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import nl.rubixstudios.TicketBot;
import nl.rubixstudios.data.Config;
import nl.rubixstudios.data.Language;
import nl.rubixstudios.ticket.object.Ticket;
import nl.rubixstudios.ticket.object.ticketbuilder.TicketCategory;
import nl.rubixstudios.ticket.object.ticketbuilder.TicketCategoryType;
import nl.rubixstudios.ticket.object.ticketbuilder.TicketQuestion;
import nl.rubixstudios.util.ChannelPermissionUtil;
import nl.rubixstudios.util.LogUtil;
import nl.rubixstudios.util.QuestionUtil;
import nl.rubixstudios.util.embed.EmbedUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.simpleyaml.configuration.ConfigurationSection;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Djorr
 * @created 15/08/2022 - 11:26
 * @project TicketsRMC
 */

@Getter
public class TicketController {

    @Getter private static TicketController instance;

    private final TicketManager ticketManager;


    public TicketController() {
        this.ticketManager = new TicketManager();

        this.loadTicketBuilder();

    }

    public void disable() {
        this.ticketManager.disable();
    }

    public TextChannel openTicketChannel(ButtonInteractionEvent event, TicketCategory ticketCategory) {
        final Category category = TicketBot.getInstance().getDiscordJda().getCategoryById(ticketCategory.getCategoryId());
        if (category == null) return null;

        final Member member = event.getMember();
        if (member == null) return null;

        final ChannelAction<TextChannel> channelChannelAction = this.createdTicketChannel(category, member);

        final TextChannel textChannel = channelChannelAction.complete();
        if (textChannel == null) {
            System.out.println("textChannel = null: " + this.getClass().getName());
            return null;
        }

        // Add channel to tickets cache
        this.ticketManager.addTicketToCache(event.getInteraction().getUser(), textChannel);

        event.reply("<@<user-id>> succesvol een ticket aangemaakt! (<#<channel-id>>)"
                        .replace("<user-id>", event.getUser().getId())
                        .replace("<channel-id>", textChannel.getId()))
                .setEphemeral(true)
                .queue();

        this.sendCreatedMessage(event.getUser(), textChannel);

        return textChannel;
    }

    private ChannelAction<TextChannel> createdTicketChannel(Category category, Member member) {
        final ChannelAction<TextChannel> channelChannelAction = category.createTextChannel("ticket-" + member.getUser().getName());;

        channelChannelAction.clearPermissionOverrides();
        channelChannelAction.addPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL), EnumSet.of(Permission.MESSAGE_SEND));
        final Role role = TicketBot.getInstance().getDiscordJda().getRolesByName("@everyone", true).get(0);
        channelChannelAction.addPermissionOverride(role, null, EnumSet.of(Permission.VIEW_CHANNEL));

        return channelChannelAction;
    }

    private void sendCreatedMessage(User user, TextChannel textChannel) {
        final String instructions =
                "<@<user-id>>, heeft een ticket geopend!\n".replace("<user-id>", user.getId());

        textChannel.sendMessageEmbeds(EmbedUtil.createEmptyEmbed(
                        "Ticket",
                        instructions))
                .setActionRows(ActionRow.of(
                        net.dv8tion.jda.api.interactions.components.buttons.Button.danger("ticket-close", "Sluit de ticket.")
                ))
                .queue();
    }

    public void preCloseTicket(TextChannel textChannel, Ticket ticket) {
        if (ticket.getTicketCategoryType() != null) {
            final TextChannelManager textChannelManager = textChannel.getManager();
            ticket.getTicketCategoryType().getTicketTagRoles().forEach(role -> {
                textChannelManager.putPermissionOverride(role, null, EnumSet.of(Permission.MESSAGE_SEND)).queue();
            });
        }

        textChannel.sendMessageEmbeds(EmbedUtil.createEmptyEmbed(
                "**Weet je zeker dat je de ticket wilt sluiten?**",
                null
        )).setActionRow(
                net.dv8tion.jda.api.interactions.components.buttons.Button.danger("ticket-close-yes", "Sluit ticket."),
                Button.primary("ticket-close-no", "Annuleer")
        ).queue();
    }

    public void closeTicketChannelByEvent(@NotNull ButtonInteractionEvent event, TextChannel textChannel, Ticket ticket) {
        event.reply("Ticket gesloten door <@<userId>>".replace("<userId>", event.getUser().getId()))
                .queue(m ->
                        textChannel.sendMessageEmbeds(EmbedUtil.createEmptyEmbed(
                                "Check the ticket.",
                                null
                        )).setActionRow(
                                Button.danger("ticket-remove", "Verwijder ticket.")
                        ).queue());



        final Category category = TicketBot.getInstance().getDiscordJda().getCategoryById(Config.CATEGORY_CLOSED_TICKETS);

        final TextChannelManager textChannelManager = textChannel.getManager();
        textChannelManager.setParent(category);
        textChannelManager.setName("closed-ticket-" + ticket.getCreatedByUserName());

        final TextChannelManager channelManager = textChannel.getManager();
        channelManager.putMemberPermissionOverride(Long.parseLong(ticket.getCreatedByUserId()),
                null,
                Arrays.asList(Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES));

        channelManager.queue();
    }

    public void removeTicketFromCache(TextChannel textChannel, TicketCategoryType ticketCategoryType) {
        if (ticketCategoryType != null) {
            this.ticketManager.removeTicketFromCache(textChannel, ticketCategoryType);
        } else {
            this.ticketManager.removeTicketFromCache(textChannel);
        }
    }

    public void removeTicketChannel(ButtonInteractionEvent event, TextChannel textChannel) {
        event.reply(Language.TICKET_REMOVE_DELETE).queue();

        final TextChannel logChannel = TicketBot.getInstance().getDiscordJda().getTextChannelById(Config.LOG_CHANNEL);
        if (logChannel != null) {
            LogUtil.sendTranscript(null, textChannel, logChannel);
        }

        textChannel.delete().queueAfter(5, TimeUnit.SECONDS);
    }

    // Ticket Builder

    private void loadTicketBuilder() {
        final String categoryPath = "TICKET.CATEGORIES";
        final ConfigurationSection categorySection = this.getConfigurationSectionByPath(categoryPath);
        if (!doesSectionExists(categorySection, categoryPath)) return;

        categorySection.getKeys(false).forEach(category -> {
            final TicketCategory ticketCategory = this.createTicketCategory(categorySection, category);

            final String typePath = "<path1>.<path2>.TYPES"
                    .replace("<path1>", "" + categorySection.getCurrentPath())
                    .replace("<path2>", category);

            final ConfigurationSection typeSection = this.getConfigurationSectionByPath(typePath);
            if (!doesSectionExists(typeSection, typePath)) return;

            typeSection.getKeys(false).forEach(categoryType -> {
                final TicketCategoryType ticketCategoryType = this.createTicketCategoryType(typeSection, categoryType);

                final String questionPath ="<path1>.<path2>.TICKET_QUESTIONS"
                        .replace("<path1>", typeSection.getCurrentPath())
                        .replace("<path2>", categoryType);

                final ConfigurationSection questionSection = this.getConfigurationSectionByPath(questionPath);
                if (!doesSectionExists(questionSection, questionPath)) return;

                questionSection.getKeys(false).forEach(question -> {
                    final TicketQuestion ticketQuestionObj = this.createTicketQuestion(questionSection, question);
                    ticketCategoryType.getTicketTicketQuestions().add(ticketQuestionObj);

                    this.ticketManager.getTicketCategoryTypes().add(ticketCategoryType);
                });
                ticketCategory.getTicketCategoryTypes().add(ticketCategoryType);
            });

            this.ticketManager.getTicketCategories().add(ticketCategory);
        });
    }

    private boolean doesSectionExists(ConfigurationSection section, String path) {
        if (section != null) return true;
        System.out.println("Error on config.yml, the section '<path>' does not exist!"
                .replace("<path>", path)
        );
        return false;
    }

    private ConfigurationSection getConfigurationSectionByPath(String path) {
        return TicketBot.getInstance().getConfigFile().getFile().getConfigurationSection(path);
    }

    private TicketCategory createTicketCategory(ConfigurationSection categorySection, String startPath) {
        final String categoryIdentifier = categorySection.getString(startPath + ".CATEGORY_IDENTIFIER");
        final String categoryId = categorySection.getString(startPath + ".CATEGORY_ID");
        final String categoryName = categorySection.getString(startPath + ".CATEGORY_NAME");

        return new TicketCategory(categoryIdentifier, categoryId, categoryName);
    }

    private TicketCategoryType createTicketCategoryType(ConfigurationSection typeSection, String startPath) {
        final String ticketIdentifier = typeSection.getString(startPath + ".TICKET_IDENTIFIER");
        final String ticketPrefix = typeSection.getString(startPath + ".TICKET_PREFIX");
        final String ticketDisplayTopic = typeSection.getString(startPath + ".TICKET_DISPLAY_TOPIC");
        final String ticketDisplayName = typeSection.getString(startPath + ".TICKET_DISPLAY_NAME");
        final List<String> ticketTagroles = typeSection.getStringList(startPath + ".TICKET_TAG_ROLES");

        return new TicketCategoryType(ticketIdentifier, ticketPrefix, ticketDisplayTopic, ticketDisplayName, ticketTagroles);
    }

    private TicketQuestion createTicketQuestion(ConfigurationSection questionSection, String startPath) {
        final String ticketQuestion = questionSection.getString(startPath + ".QUESTION");
        final boolean ticketIsYesOrNoQuestion = questionSection.getBoolean(startPath + ".IS_YES_OR_NO_QUESTION");

        return new TicketQuestion(Integer.parseInt(startPath), ticketQuestion, ticketIsYesOrNoQuestion);
    }

    // Handle Questions

    public void sendQuestion(TextChannel textChannel, Ticket ticket) {
        final TicketQuestion ticketQuestion = QuestionUtil.sendQuestionInChannel(ticket);
        if (ticketQuestion == null) return;

        textChannel.sendMessageEmbeds(EmbedUtil.createEmptyEmbed(
                ticketQuestion.getQuestion(),
                null
        )).queue(message -> {
            ticketQuestion.setQuestionMessageId(message.getId());
        });

        final TextChannelManager channelManager = textChannel.getManager();
        channelManager.putMemberPermissionOverride(Long.parseLong(ticket.getCreatedByUserId()),
                Arrays.asList(Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MESSAGE_SEND),
                null);

        channelManager.queue();
    }

    public void saveAnswer(Message message, TicketQuestion ticketQuestion) {
        final String messageContent = message.getContentDisplay();
        ticketQuestion.setAnswer(messageContent);
    }
}
