package nl.rubixstudios.ticket.command;

import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildAvailableEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import nl.rubixstudios.TicketBot;
import nl.rubixstudios.ticket.command.commands.TicketCommand;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Djorr
 * @created 16/08/2022 - 17:26
 * @project TicketsRMC
 */
public class CommandManager extends ListenerAdapter {

    public CommandManager() {
        this.registerCommands();
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        event.getGuild().updateCommands().addCommands(this.updateCommands()).queue();
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        event.getGuild().updateCommands().addCommands(this.updateCommands()).queue();
    }

    @Override
    public void onGuildAvailable(@NotNull GuildAvailableEvent event) {
        event.getGuild().updateCommands().addCommands(this.updateCommands()).queue();
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        event.getJDA().updateCommands().addCommands(this.updateCommands()).queue();
    }

    public List<CommandData> updateCommands() {
        final List<CommandData> commandData = new ArrayList<>();

        final OptionData discordUser = new OptionData(OptionType.USER, "user", "De discord gebruiker", true);
        final OptionData discordRole = new OptionData(OptionType.ROLE, "role", "De role", true);

        final OptionData channelName = new OptionData(OptionType.CHANNEL, "channel", "De nieuwe kanaal naam", true);

        final OptionData reason = new OptionData(OptionType.STRING, "reason", "Vul een reden in", true);

        final SubcommandData addCommandData = new SubcommandData("add", "Voeg een user aan een ticket").addOptions(discordUser);
        final SubcommandData removeCommandData = new SubcommandData("remove", "Verwijder een user aan een ticket").addOptions(discordUser);
        final SubcommandData renameCommandData = new SubcommandData("rename", "Verrander de channel naam").addOptions(channelName);
        final SubcommandData assistCommandData = new SubcommandData("assist", "Vraag om assist van een role").addOptions(discordRole);
        final SubcommandData openLatenCommandData = new SubcommandData("openlaten", "Laat het ticket open");
        final SubcommandData onHoldCommandData = new SubcommandData("onhold", "Zet het ticket on hold");
        final SubcommandData closeCommandData = new SubcommandData("close", "Sluit het ticket");
        final SubcommandData forceCloseCommandData = new SubcommandData("forceclose", "Forceer sluiten van het ticket");
        final SubcommandData blacklistCommandData = new SubcommandData("blacklist", "Blacklist iemand van de tickets").addOptions(discordUser, reason);

        commandData.add(Commands.slash("ticket", "Ticket command")
                .addSubcommands(addCommandData, removeCommandData, renameCommandData, assistCommandData,
                        openLatenCommandData, onHoldCommandData, closeCommandData, forceCloseCommandData, blacklistCommandData));

        return commandData;
    }

    private void registerCommands() {
        final TicketBot mainInstance = TicketBot.getInstance();

        mainInstance.getDiscordJda().addEventListener(new TicketCommand());
    }
}
