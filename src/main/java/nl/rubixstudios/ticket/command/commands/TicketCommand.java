package nl.rubixstudios.ticket.command.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import nl.rubixstudios.TicketBot;
import nl.rubixstudios.data.Config;
import nl.rubixstudios.data.Language;
import nl.rubixstudios.ticket.TicketController;
import nl.rubixstudios.ticket.object.Ticket;
import nl.rubixstudios.util.ChannelPermissionUtil;
import nl.rubixstudios.util.embed.EmbedUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * @author Djorr
 * @created 16/08/2022 - 17:26
 * @project TicketsRMC
 */
public class TicketCommand extends ListenerAdapter {

    private final TicketController ticketController;
    private final JDA discordJda;

    public TicketCommand() {
        this.ticketController = TicketBot.getInstance().getTicketController();
        this.discordJda = TicketBot.getInstance().getDiscordJda();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        final Member requester = event.getMember();
        if (requester == null) return;

        final TextChannel textChannel = event.getChannel().asTextChannel();

        final String commandName = event.getName();
        if (!commandName.equals("ticket")) return;
        if (event.getSubcommandName() == null) return;

        final String subCommandName = event.getSubcommandName();
        switch (subCommandName) {
            case "add": {
                final Member target = this.getMember(event);
                if (target == null) return;

                this.addMemberOrRoleToTicket(event, requester, target, textChannel);
                break;
            }
            case "remove": {
                final Member target = this.getMember(event);
                if (target == null) return;

                this.removeMemberOrRoleFromTicket(event, requester, target, textChannel);
                break;
            }
            case "rename": {
                final String name = this.getNewChannelName(event);
                if (name == null) return;

                this.renameTicket(event, requester, name, textChannel);
                break;
            }
            case "assist": {
                final Role role = this.getRole(event);
                if (role == null) return;

                this.assistRoleToTicket(event, requester, role, textChannel);
                break;
            }
            case "openlaten": {
                final Ticket ticket = this.getTicketByTextChannel(textChannel);
                if (ticket == null) return;

                this.stayTicketOpen(event, requester, ticket, textChannel);
                break;
            }
            case "onhold": {
                final Ticket ticket = this.getTicketByTextChannel(textChannel);
                if (ticket == null) return;

                this.onHoldTicket(event, requester, ticket, textChannel);
                break;
            }
            case "close": {
                final Ticket ticket = this.getTicketByTextChannel(textChannel);
                if (ticket == null) return;

                this.closeTicket(event, requester, ticket, textChannel, false);
                break;
            }
            case "forceclose": {
                final Ticket ticket = this.getTicketByTextChannel(textChannel);
                if (ticket == null) return;

                this.closeTicket(event, requester, ticket, textChannel, true);
                break;
            }
            case "blacklist": {
                final Member target = this.getMember(event);
                if (target == null) return;

                this.toggleBlackListOnUser(textChannel, requester, target);
                break;
            }
        }
    }

    private void addMemberOrRoleToTicket(@NotNull SlashCommandInteractionEvent event, Member requester, Member target, TextChannel ticketChannel) {
        final Ticket ticket = this.ticketController.getTicketManager().getTicketByChannel(ticketChannel);
        if (ticket == null) return;
        if (!hasPermission(requester, ticket)) {
            event.reply(Language.TICKET_PERMISSIONS_NO_PERMS).setEphemeral(true).queue();
            return;
        }
        event.reply("Zie onderstaand bericht").setEphemeral(true).queue();

        ticketChannel.sendMessageEmbeds(
                EmbedUtil.createEmptyEmbed("<requester> heeft <target> toegevoegd aan het ticket!"
                                .replace("<requester>", requester.getUser().getName())
                                .replace("<target>", target.getUser().getName())
                        , null)
        ).queue();

        final TextChannelManager channelManager = ticketChannel.getManager();
        channelManager.putMemberPermissionOverride(target.getIdLong(),
                Arrays.asList(Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES),
                null);

        channelManager.queue();

    }

    private void removeMemberOrRoleFromTicket(@NotNull SlashCommandInteractionEvent event, Member requester, Member target, TextChannel ticketChannel) {
        final Ticket ticket = this.ticketController.getTicketManager().getTicketByChannel(ticketChannel);
        if (ticket == null) return;
        if (!hasPermission(requester, ticket)) {
            event.reply(Language.TICKET_PERMISSIONS_NO_PERMS).setEphemeral(true).queue();
            return;
        }
        event.reply("Zie onderstaand bericht").setEphemeral(true).queue();

        final TextChannelManager channelManager = ticketChannel.getManager();
        channelManager.putMemberPermissionOverride(target.getIdLong(),
                null,
                Arrays.asList(Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES));

        channelManager.queue();

        ticketChannel.sendMessageEmbeds(
                EmbedUtil.createEmptyEmbed("<requester> heeft <target> verwijderd uit het ticket!"
                                .replace("<requester>", requester.getUser().getName())
                                .replace("<target>", target.getUser().getName())
                        , null)
        ).queue();
    }

    private void renameTicket(@NotNull SlashCommandInteractionEvent event, Member requester, String name, TextChannel ticketChannel) {
        final Ticket ticket = this.ticketController.getTicketManager().getTicketByChannel(ticketChannel);
        if (ticket == null) return;
        if (!hasPermission(requester, ticket)) {
            event.reply(Language.TICKET_PERMISSIONS_NO_PERMS).setEphemeral(true).queue();
            return;
        }

        final TextChannelManager channelManager = ticketChannel.getManager();
        channelManager.setName(name).queue();

        ticketChannel.sendMessageEmbeds(
                EmbedUtil.createEmptyEmbed("<requester> heeft de naam van het kanaal verranderd naar <name>!"
                                .replace("<requester>", requester.getUser().getName())
                                .replace("<name>", name)
                        , null)
        ).queue();
    }

    private void assistRoleToTicket(@NotNull SlashCommandInteractionEvent event, Member requester, Role targetRole, TextChannel textChannel) {
        final Ticket ticket = this.ticketController.getTicketManager().getTicketByChannel(textChannel);
        if (ticket == null) return;
        if (!hasPermission(requester, ticket)) {
            event.reply(Language.TICKET_PERMISSIONS_NO_PERMS).setEphemeral(true).queue();
            return;
        }
        event.reply("Zie onderstaand bericht").setEphemeral(true).queue();

        final TextChannelManager channelManager = textChannel.getManager();
        channelManager.putRolePermissionOverride(targetRole.getIdLong(),
                Arrays.asList(Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES),
                null);

        channelManager.queue();

        textChannel.sendMessage("<@&<roleId>>".replace("<roleId>", targetRole.getId())).setEmbeds(
                EmbedUtil.createEmptyEmbed("<requester> heeft het ticket overgedragen aan <roleId>!"
                                .replace("<requester>", requester.getUser().getName())
                                .replace("<roleId>", targetRole.getName())
                        , null)
        ).queue();
    }

    private void stayTicketOpen(@NotNull SlashCommandInteractionEvent event, Member requester, Ticket ticket, TextChannel ticketChannel) {
        if (!hasPermission(requester, ticket)) {
            event.reply(Language.TICKET_PERMISSIONS_NO_PERMS).setEphemeral(true).queue();
            return;
        }

        final Category openLatenCategory = this.discordJda.getCategoryById(Config.CATEGORY_OPEN_LATEN_TICKETS);
        if (openLatenCategory == null) return;

        ticket.setOpenLaten(true);

        final TextChannelManager channelManager = ticketChannel.getManager();
        channelManager.setName("openlaten-" + ticket.getCreatedByUserName());
        channelManager.setParent(openLatenCategory);
        channelManager.putMemberPermissionOverride(Long.parseLong(ticket.getCreatedByUserId()),
                null,
                Arrays.asList(Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES));

        channelManager.queue();

        event.reply("Zie onderstaand bericht").setEphemeral(true).queue();
        ticketChannel.sendMessageEmbeds(
                EmbedUtil.createEmptyEmbed("<requester> heeft het ticket als open laten gemarkeerd!"
                                .replace("<requester>", requester.getUser().getName())
                        , null)
        ).queue();
    }

    private void onHoldTicket(@NotNull SlashCommandInteractionEvent event, Member requester, Ticket ticket, TextChannel ticketChannel) {
        if (!hasPermission(requester, ticket)) {
            event.reply(Language.TICKET_PERMISSIONS_NO_PERMS).setEphemeral(true).queue();
            return;
        }

        final Category onHoldCategory = this.discordJda.getCategoryById(Config.CATEGORY_ON_HOLD_TICKETS);
        if (onHoldCategory == null) return;

        ticket.setOnHold(true);

        final TextChannelManager channelManager = ticketChannel.getManager();
        channelManager.setName("onhold-" + ticket.getCreatedByUserName());
        channelManager.setParent(onHoldCategory);
        channelManager.putMemberPermissionOverride(Long.parseLong(ticket.getCreatedByUserId()),
                null,
                Arrays.asList(Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES));

        channelManager.queue();

        event.reply("Zie onderstaand bericht").setEphemeral(true).queue();
        ticketChannel.sendMessageEmbeds(
                EmbedUtil.createEmptyEmbed("<requester> heeft het ticket als on-hold gemarkeerd!"
                                .replace("<requester>", requester.getUser().getName())
                        , null)
        ).queue();
    }

    private void closeTicket(@NotNull SlashCommandInteractionEvent event, Member requester, Ticket ticket, TextChannel ticketChannel, boolean forced) {
        if (!hasPermission(requester, ticket)) {
            event.reply(Language.TICKET_PERMISSIONS_NO_PERMS).setEphemeral(true).queue();
            return;
        }
        event.reply("Zie onderstaand bericht").setEphemeral(true).queue();

        if (forced) {
            ticketChannel.sendMessageEmbeds(
                    EmbedUtil.createEmptyEmbed("<requester> heeft het ticket geforceerd gesloten!"
                                    .replace("<requester>", requester.getUser().getName())
                            , null)
            ).queue(m ->
                    ticketChannel.sendMessageEmbeds(EmbedUtil.createEmptyEmbed(
                            "Check the ticket.",
                            null
                    )).setActionRow(
                            Button.danger("ticket-remove", "Verwijder ticket.")
                    ).queue());


            final TextChannelManager channelManager = ticketChannel.getManager();
            final Category category = TicketBot.getInstance().getDiscordJda().getCategoryById(Config.CATEGORY_CLOSED_TICKETS);
            channelManager.setParent(category);
            channelManager.setName("closed-ticket-" + ticket.getCreatedByUserName());
            channelManager.putMemberPermissionOverride(Long.parseLong(ticket.getCreatedByUserId()),
                    null,
                    Arrays.asList(Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY, Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES));

            channelManager.queue();
            return;
        }

        this.ticketController.preCloseTicket(ticketChannel, ticket);
    }

    private void toggleBlackListOnUser(TextChannel textChannel, Member requester, Member targetUser) {
        if (this.ticketController.getTicketManager().isMemberBlacklisted(targetUser)) {
            this.ticketController.getTicketManager().removeMemberFromBlacklist(textChannel, requester, targetUser);
            return;
        }
        this.ticketController.getTicketManager().addMemberToBlacklist(textChannel, requester, targetUser);
    }

    // Checks

    private Member getMember(@NotNull SlashCommandInteractionEvent event) {
        final OptionMapping userMap = event.getOption("user");
        if (userMap == null) {
            event.reply("You must give a user!")
                    .setEphemeral(true)
                    .queue();
            return null;
        }

        return userMap.getAsMember();
    }

    private Role getRole(@NotNull SlashCommandInteractionEvent event) {
        final OptionMapping roleMap = event.getOption("role");
        if (roleMap == null) {
            event.reply("You must give a role!")
                    .setEphemeral(true)
                    .queue();
            return null;
        }

        return roleMap.getAsRole();
    }

    private String getNewChannelName(@NotNull SlashCommandInteractionEvent event) {
        final OptionMapping nameMap = event.getOption("name");
        if (nameMap == null) {
            event.reply("You must give a new channel name!")
                    .setEphemeral(true)
                    .queue();
            return null;
        }

        return nameMap.getAsString();
    }

    private Ticket getTicketByTextChannel(TextChannel textChannel) {
        return this.ticketController.getTicketManager().getTicketByChannel(textChannel);
    }

    public boolean hasPermission(Member requester, Ticket ticket) {
        final boolean isTicketCreator = requester.getId().equals(ticket.getCreatedByUserId());
        final boolean isFriendOfTicketCreator = ticket.getUsersThatCanAccessChannel().contains(requester);

        return !isTicketCreator && !isFriendOfTicketCreator;
    }
}
