package solutions.nuhvel.spigot.rc;

import de.exlll.configlib.configs.yaml.BukkitYamlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import solutions.nuhvel.spigot.rc.storage.config.config.PluginConfig;
import solutions.nuhvel.spigot.rc.storage.config.messages.PluginMessages;
import solutions.nuhvel.spigot.rc.storage.database.BlockRepository;
import solutions.nuhvel.spigot.rc.storage.database.Database;
import solutions.nuhvel.spigot.rc.utils.Utils;
import solutions.nuhvel.spigot.rc.utils.helpers.CommandHandler;
import solutions.nuhvel.spigot.rc.utils.helpers.ListenerHandler;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;

public class RestrictedCreative extends JavaPlugin {
    public PluginConfig config;
    public PluginMessages messages;
    public ListenerHandler listenerHandler;
    private CommandHandler commandHandler;
    private Utils utils;
    private Database database;
    private BlockRepository blockRepository;

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
        setUtils(new Utils(this));

        loadConfig();
        loadMessages();

        commandHandler = new CommandHandler(this);
        listenerHandler = new ListenerHandler(this);

        database = new Database(this, config.database.type);
        blockRepository = new BlockRepository(this, database);
    }

    public void reloadConfigs() {
        loadConfig();
        loadMessages();

        // Reload command messages, aliases etc as well
        commandHandler = new CommandHandler(this);
    }

    public static Plugin getInstance() {
        return Bukkit.getPluginManager().getPlugin("RestrictedCreative");
    }

    public Utils getUtils() {
        return utils;
    }

    public void setUtils(Utils utils) {
        this.utils = utils;
    }

    private void loadConfig() {
        Path configPath = new File(getDataFolder(), "config.yml").toPath();

        BukkitYamlConfiguration.BukkitYamlProperties properties = BukkitYamlConfiguration.BukkitYamlProperties.builder()
                                                                                                              .setPrependedComments(
                                                                                                                      Arrays.asList(
                                                                                                                              "Check out default config: link"
                                                                                                                              // TODO
                                                                                                                      ))
                                                                                                              .build();
        config = new PluginConfig(configPath, properties);
        config.loadAndSave();
    }

    private void loadMessages() {
        Path configPath = new File(getDataFolder(), "messages.yml").toPath();

        BukkitYamlConfiguration.BukkitYamlProperties properties = BukkitYamlConfiguration.BukkitYamlProperties.builder()
                                                                                                              .setPrependedComments(
                                                                                                                      Arrays.asList(
                                                                                                                              "Check out default messages: link"
                                                                                                                              // TODO
                                                                                                                      ))
                                                                                                              .build();
        messages = new PluginMessages(configPath, properties);
        messages.loadAndSave();
    }
}
