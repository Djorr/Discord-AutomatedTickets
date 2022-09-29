package nl.rubixstudios.ticket;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import nl.rubixstudios.TicketBot;
import nl.rubixstudios.ticket.object.Ticket;
import nl.rubixstudios.ticket.object.ticketbuilder.TicketCategory;
import nl.rubixstudios.ticket.object.ticketbuilder.TicketCategoryType;
import nl.rubixstudios.util.FileUtil;
import nl.rubixstudios.util.GsonUtil;
import nl.rubixstudios.util.embed.EmbedUtil;

import java.io.File;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Djorr
 * @created 15/08/2022 - 11:41
 * @project TicketsRMC
 */

@Getter
public class TicketManager {

    @Getter private static TicketManager instance;


    private final List<TicketCategory> ticketCategories;
    private final List<TicketCategoryType> ticketCategoryTypes;

    private final File ticketsDir;

    private final File ticketFile;
    private final List<Ticket> tickets;

    private final File ticketBlacklistFile;
    private final List<String> ticketBlacklistedMembers;

    public TicketManager() {
        instance = this;

        this.ticketCategories = new ArrayList<>();
        this.ticketCategoryTypes = new ArrayList<>();

        this.ticketsDir = new File(FileSystems.getDefault().getPath(".").toAbsolutePath().toString(), "ticket-data");
        if (!this.ticketsDir.exists()) this.ticketsDir.mkdir();

        this.ticketFile = FileUtil.getOrCreateFile(new File(FileSystems.getDefault().getPath("./ticket-data/").toAbsolutePath().toString()), "ticket.json");
        this.tickets = new ArrayList<>();

        this.ticketBlacklistFile = FileUtil.getOrCreateFile(new File(FileSystems.getDefault().getPath("./ticket-data/").toAbsolutePath().toString()), "ticket-blacklist.json");
        this.ticketBlacklistedMembers = new ArrayList<>();

        this.loadTickets(true);
        this.loadBlacklistedMember(true);
    }

    public void disable() {
        this.saveTickets(true);
        this.saveBlacklistedMembers(true);
    }

    private void loadTickets(boolean log) {
        final String content = FileUtil.readWholeFile(this.ticketFile);
        if (content == null) return;

        this.tickets.clear();

        final List<Ticket> tickets = TicketBot.getInstance().getGson().fromJson(content, GsonUtil.TICKET_TYPE);
        this.tickets.addAll(tickets);

        if (log) {
            System.out.println("Tickets:");
            System.out.println("- Loaded <amount> tickets.".replace("<amount>", "" + this.tickets.size()));
        }
    }

    private void loadBlacklistedMember(boolean log) {
        final String content = FileUtil.readWholeFile(this.ticketBlacklistFile);
        if (content == null) return;

        this.ticketBlacklistedMembers.clear();

        final List<String> ticketBlacklistedMembers = TicketBot.getInstance().getGson().fromJson(content, GsonUtil.BLACKLIST_TYPE);
        this.ticketBlacklistedMembers.addAll(ticketBlacklistedMembers);

        if (log) {
            System.out.println("- Loaded <amount>  blacklisted members.".replace("<amount>", "" + this.ticketBlacklistedMembers.size()));
        }
    }

    public void saveTickets(boolean log) {
        FileUtil.writeString(this.ticketFile, TicketBot.getInstance().getGson().toJson(this.tickets, GsonUtil.TICKET_TYPE));

        if (log) {
            System.out.println("Tickets:");
            System.out.println("- Saved <amount> tickets.".replace("<amount>", "" + this.tickets.size()));
        }
    }

    public void saveBlacklistedMembers(boolean log) {
        FileUtil.writeString(this.ticketBlacklistFile, TicketBot.getInstance().getGson().toJson(this.ticketBlacklistedMembers, GsonUtil.BLACKLIST_TYPE));

        if (log) {
            System.out.println("- Saved <amount> blacklisted members.".replace("<amount>", "" + this.ticketBlacklistedMembers.size()));
        }
    }

    public void addTicketToCache(User user, TextChannel textChannel) {
        final Ticket ticket = this.createTicketByType(user, textChannel);
        this.tickets.add(ticket);

        this.saveTickets(false);
    }

    public void removeTicketFromCache(TextChannel textChannel, TicketCategoryType ticketCategoryType) {
        final Ticket ticket = this.getTicketByChannelAndType(textChannel, ticketCategoryType);
        this.tickets.remove(ticket);

        this.saveTickets(false);
    }

    public void removeTicketFromCache(TextChannel textChannel) {
        final Ticket ticket = this.getTicketByChannel(textChannel);
        this.tickets.remove(ticket);

        this.saveTickets(false);
    }

    public void removeTicketFromCacheByTicket(Ticket ticket) {
        this.tickets.remove(ticket);

        this.saveTickets(false);
    }

    private Ticket createTicketByType(User user, TextChannel textChannel) {
        return new Ticket(user, textChannel, "ticket-" + user.getName());
    }

    public Ticket getTicketByChannelAndType(TextChannel textChannel, TicketCategoryType ticketCategoryType) {
        return this.tickets.stream().filter(ticket -> ticket.getTicketChannelId().equals(textChannel.getId()) && ticket.getTicketCategoryType().getTicketIdentifier().equals(ticketCategoryType.getTicketIdentifier())).findFirst().orElse(null);
    }

    public Ticket getTicketByChannel(TextChannel textChannel) {
        return this.tickets.stream().filter(ticket -> ticket.getTicketChannelId().equals(textChannel.getId())).findFirst().orElse(null);
    }

    public List<Ticket> getAllTicketsOfUser(User user) {
        return this.tickets.stream().filter(ticket -> ticket.getCreatedByUserId().equals(user.getId()) && !ticket.isClosed()).collect(Collectors.toList());
    }

    public boolean hasPlayerMaxLimitTickets(User user) {
        return this.getAllTicketsOfUser(user).size() >= 1;
    }

    // Builder

    public TicketCategory getTicketCategoryStartsWith(String name) {
        return this.ticketCategories.stream().filter(ticketCategory -> ticketCategory.getCategoryIdentifier().startsWith(name)).findFirst().orElse(null);
    }

    public TicketCategoryType getTicketCategoryType(String name) {
        return this.ticketCategoryTypes.stream().filter(ticketCategoryType -> ticketCategoryType.getTicketIdentifier().equals(name)).findFirst().orElse(null);
    }

    // Blacklist

    public void addMemberToBlacklist(TextChannel textChannel, Member requester, Member victim) {
        if (isMemberBlacklisted(victim)) {
            textChannel.sendMessage("<@<requesterId>>"
                    .replace("<requesterId>", requester.getId())
            ).setEmbeds(EmbedUtil.createEmptyEmbed("<@<victimId>> staat al in de ticket blacklist!"
                            .replace("<victimId>", victim.getId())
                    , null)).queue();
            return;
        }

        this.ticketBlacklistedMembers.add(victim.getId());
    }

    public void removeMemberFromBlacklist(TextChannel textChannel, Member requester, Member victim) {
        if (!isMemberBlacklisted(victim)) {
            textChannel.sendMessage("<@<requesterId>>"
                    .replace("<requesterId>", requester.getId())
            ).setEmbeds(EmbedUtil.createEmptyEmbed("<@<victimId>> staat niet in de ticket blacklist!"
                            .replace("<victimId>", victim.getId())
                    , null)).queue();
            return;
        }

        this.ticketBlacklistedMembers.add(victim.getId());
    }

    public boolean isMemberBlacklisted(Member victim) {
        return this.ticketBlacklistedMembers.stream().anyMatch(memberId -> victim.getId().equals(memberId));
    }
}
