package nl.rubixstudios;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import nl.rubixstudios.data.Config;
import nl.rubixstudios.data.ConfigFileUtil;
import nl.rubixstudios.data.Language;
import nl.rubixstudios.ticket.TicketController;
import nl.rubixstudios.ticket.listener.GuildReadyEventListener;
import nl.rubixstudios.util.GsonUtil;
import nl.rubixstudios.util.gson.TicketTypeAdapter;

import javax.security.auth.login.LoginException;
import java.lang.reflect.Modifier;

/**
 * @author Djorr
 * @created 15/08/2022 - 11:10
 * @project TicketsRMC
 */

@Getter
public class TicketBot {

    @Getter public static TicketBot instance;

    public JDA discordJda;
    public Gson gson;

    @Setter private ConfigFileUtil configFile;
    @Setter private ConfigFileUtil languageFile;

    public TicketController ticketController;

    public static void main(String[] args) {
        new TicketBot();
    }

    public TicketBot() {
        instance = this;

        this.configFile = new ConfigFileUtil("config.yml");
        this.languageFile = new ConfigFileUtil("language.yml");

        new Config();
        new Language();

        this.startBot();
        this.registerGson();

        this.setupEventListeners();
    }

    private void startBot() {
        try {
            final Activity activity = Activity.of(Activity.ActivityType.WATCHING, Config.BOT_NAME);
            this.discordJda = JDABuilder.createDefault(Config.BOT_TOKEN)
                    .enableIntents(GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MEMBERS)
                    .setActivity(activity)
                    .setStatus(OnlineStatus.DO_NOT_DISTURB)
                    .build();
        } catch (LoginException ex) {
            ex.printStackTrace();
        }
    }

    private void setupEventListeners() {
        // Ready Bot
        this.discordJda.addEventListener(new GuildReadyEventListener());
    }

    /**
     * This register the gson
     */
    public void registerGson() {
        this.gson = new GsonBuilder().setPrettyPrinting().serializeNulls()
                .enableComplexMapKeySerialization().excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC)
                .registerTypeAdapter(GsonUtil.TICKET_TYPE, new TicketTypeAdapter())
                .create();
    }
}
