package solutions.nuhvel.spigot.rc;

import de.exlll.configlib.configs.yaml.BukkitYamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import solutions.nuhvel.spigot.rc.storage.config.config.RcConfig;
import solutions.nuhvel.spigot.rc.storage.config.messages.RcMessages;
import solutions.nuhvel.spigot.rc.storage.database.BlockRepository;
import solutions.nuhvel.spigot.rc.storage.database.Database;
import solutions.nuhvel.spigot.rc.storage.handlers.InventoryHandler;
import solutions.nuhvel.spigot.rc.storage.handlers.TrackableHandler;
import solutions.nuhvel.spigot.rc.utils.MessagingUtils;
import solutions.nuhvel.spigot.rc.utils.minecraft.ServerUtils;
import solutions.nuhvel.spigot.rc.utils.Utils;
import solutions.nuhvel.spigot.rc.utils.handlers.CommandHandler;
import solutions.nuhvel.spigot.rc.utils.handlers.ListenerHandler;

import java.io.File;
import java.util.List;

public class RestrictedCreative extends JavaPlugin {
    public RcConfig config;
    public RcMessages messages;
    public Database database;
    public TrackableHandler trackableHandler;
    public ListenerHandler listenerHandler;
    public MessagingUtils messagingUtils;
    public BlockRepository blockRepository;
    private CommandHandler commandHandler;
    private Utils utils;

    // TODO:
	/*You just need to add all the new blocks and it works 99%
	The only bug so far is like the new berry bush? drop themselves but nothing else.
	Players are able to pickup items with gamemode creative from the ground, multiply the hoppers placed
			(who have blocks in them), and then place multiple times on the ground.
	Inventories lost on server crash
	Inventories lost on server stop (plugin compatibility issue probably, try specifying event priority)
	https://www.youtube.com/watch?v=A6wtcS9fomg
	When players rapidly place falling sand on our server, they seem to be spammed with "You can't do that in creative"

	If you build this, you put the door in the space and open / close the trapdoor the door will drop:
	Side view:
	block | block     | block
	------|-----------|------
	block | door      | block
	------|-----------|------
	block | door      | block
	------|-----------|------
	block | trapdoor  | block*/

    @Override
    public void onDisable() {
        for (Player p : getServer().getOnlinePlayers()) {
            getUtils().saveInventory(p);
        }

        blockRepository.saveAndClose();

        database.closeConnection();
    }

    @Override
    public void onEnable() {
        utils = new Utils(this);
        messagingUtils = new MessagingUtils(this);

        loadConfig();
        loadMessages();

        database = new Database(this, config.database.type);
        blockRepository = new BlockRepository(this, database);

        trackableHandler = new TrackableHandler(this, blockRepository);
        commandHandler = new CommandHandler(this);
        listenerHandler = new ListenerHandler(this);

        InventoryHandler.setForceGamemodeEnabled(ServerUtils.isForceGamemodeEnabled());
    }

    public void reload() {
        loadConfig();
        loadMessages();

        // Reload command messages, aliases etc as well
        commandHandler = new CommandHandler(this);
        // Reload listeners, because they can be toggled by the config
        listenerHandler.registerListeners();
    }

    public Utils getUtils() {
        return utils;
    }

    private void loadConfig() {
        var configPath = new File(getDataFolder(), "config.yml").toPath();

        var properties = BukkitYamlConfiguration.BukkitYamlProperties
                .builder()
                .setPrependedComments(List.of("Check out default config: link"
                        // TODO
                ))
                .build();
        config = new RcConfig(configPath, properties);
        config.loadAndSave();
    }

    private void loadMessages() {
        var configPath = new File(getDataFolder(), "messages.yml").toPath();

        var properties = BukkitYamlConfiguration.BukkitYamlProperties
                .builder()
                .setPrependedComments(List.of("Check out default messages: link"
                        // TODO
                ))
                .build();
        messages = new RcMessages(configPath, properties);
        messages.loadAndSave();
    }
}
