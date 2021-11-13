package solutions.nuhvel.spigot.rc;

import de.exlll.configlib.configs.yaml.BukkitYamlConfiguration;
import solutions.nuhvel.spigot.rc.storage.config.config.PluginConfig;
import solutions.nuhvel.spigot.rc.storage.config.config.database.DatabaseType;
import solutions.nuhvel.spigot.rc.storage.config.messages.PluginMessages;
import solutions.nuhvel.spigot.rc.storage.database.Database;
import solutions.nuhvel.spigot.rc.storage.database.DataSyncRunnable;
import solutions.nuhvel.spigot.rc.storage.handlers.BlockHandler;
import solutions.nuhvel.spigot.rc.storage.handlers.InventoryHandler;
import solutions.nuhvel.spigot.rc.utils.Utils;
import solutions.nuhvel.spigot.rc.utils.helpers.CommandHandler;
import solutions.nuhvel.spigot.rc.utils.helpers.ListenerHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RestrictedCreative extends JavaPlugin {
	private Database database;
	private Utils utils;

	public PluginConfig config;
	public PluginMessages messages;

	private CommandHandler commandHandler;
	public ListenerHandler listenerHandler;

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
	public void onEnable() {
		setUtils(new Utils(this));

		loadConfig();
		loadMessages();

		commandHandler = new CommandHandler(this);
		listenerHandler = new ListenerHandler(this);

		loadData();
	}

	@Override
	public void onDisable() {
		for (Player p : getServer().getOnlinePlayers()) {
			getUtils().saveInventory(p);
		}

		// Save data for the last time
		final Set<String> fAdd = new HashSet<>(BlockHandler.addToDatabase);
		final Set<String> fDel = new HashSet<>(BlockHandler.removeFromDatabase);
		new DataSyncRunnable(this, fAdd, fDel, true).run();

		getDB().closeConnection();
	}

	public void reloadConfigs() {
		loadConfig();
		loadMessages();

		// Reload command messages, aliases etc as well
		commandHandler = new CommandHandler(this);
	}

	private void loadData() {
		setDB(new Database(this, null));

		BlockHandler.setUsingSQLite(config.database.type == DatabaseType.SQLITE);
		InventoryHandler.setForceGamemodeEnabled(Utils.isForceGamemodeEnabled());

		// Tracked blocks
		getDB().executeUpdate("CREATE TABLE IF NOT EXISTS " + getDB().getBlocksTable() +
				"`x` BIGINT NOT NULL, " +
				"`y` BIGINT NOT NULL, " +
				"`z` BIGINT NOT NULL, " +
				"`world` VARCHAR(100) NOT NULL, " +
				"`chunk_x` INT NOT NULL, " +
				"`chunk_z` INT NOT NULL, " +
				"`owner` VARCHAR(36) NOT NULL, " +
				"UNIQUE `x_y_z_world` (`x`, `y`, `z`, `world`))");

		// Tracked inventories
		if (BlockHandler.isUsingSQLite()) {
			String tableName = getDB().getInventoryTable();
			ResultSet rs = getDB()
					.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'");

			boolean tableExists = false;
			try {
				tableExists = rs.next();
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			if (tableExists)
				tableName += "_tmp";

			getDB().executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName
					+ " (player VARCHAR(36), type TINYINT(1), storage TEXT, armor TEXT, extra TEXT, effects TEXT, xp BIGINT, lastused BIGINT(11), UNIQUE (player))");

			if (tableExists) {
				// Retain only the latest inventory from each player
				try {
					Statement stm = getDB().getConnection().createStatement();
					stm.addBatch("INSERT INTO " + tableName
							+ " SELECT player, type, storage, armor, extra, effects, xp, MAX(lastused) as lastused FROM "
							+ getDB().getInventoryTable() + " GROUP BY player");
					stm.addBatch("DROP TABLE " + getDB().getInventoryTable());
					stm.addBatch(
							"ALTER TABLE " + tableName + " RENAME TO " + getDB().getInventoryTable());
					stm.executeBatch();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} else {
			getDB().executeUpdate("CREATE TABLE IF NOT EXISTS " + getDB().getInventoryTable()
					+ " (player VARCHAR(36), type TINYINT(1), storage TEXT, armor TEXT, extra TEXT, effects TEXT, xp BIGINT, lastused BIGINT(11), UNIQUE (player))");
		}

		// Purge old database entries
		if (config.tracking.inventories.purge.enabled) {
			long survival = Instant.now().getEpochSecond()
					- 86400L * config.tracking.inventories.purge.survival;
			long creative = Instant.now().getEpochSecond()
					- 86400L * config.tracking.inventories.purge.creative;

			getDB().executeUpdate(
					"DELETE FROM " + getDB().getInventoryTable() + " WHERE type = 0 AND lastused < "
							+ survival + " OR type = 1 AND lastused < " + creative);
		}

		BlockHandler.usingAdvancedLoading = true;
		BlockHandler.loadFromDatabaseAdvanced(this);

		BlockHandler.startDataSync(this);
	}

	public static Plugin getInstance() {
		return Bukkit.getPluginManager().getPlugin("RestrictedCreative");
	}

	public Database getDB() {
		return database;
	}

	public void setDB(Database database) {
		this.database = database;
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
				.setPrependedComments(Arrays.asList(
						"Check out default config: link" // TODO
				))
				.build();
		config = new PluginConfig(configPath, properties);
		config.loadAndSave();
	}

	private void loadMessages() {
		Path configPath = new File(getDataFolder(), "messages.yml").toPath();

		BukkitYamlConfiguration.BukkitYamlProperties properties = BukkitYamlConfiguration.BukkitYamlProperties.builder()
				.setPrependedComments(Arrays.asList(
						"Check out default messages: link" // TODO
				))
				.build();
		messages = new PluginMessages(configPath, properties);
		messages.loadAndSave();
	}

}
