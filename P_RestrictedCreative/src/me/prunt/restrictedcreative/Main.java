package me.prunt.restrictedcreative;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.WorldEdit;

import me.prunt.restrictedcreative.commands.MainCommand;
import me.prunt.restrictedcreative.commands.SwitchCommand;
import me.prunt.restrictedcreative.listeners.BlockBreakListener;
import me.prunt.restrictedcreative.listeners.BlockChangeListener;
import me.prunt.restrictedcreative.listeners.BlockExplodeListener;
import me.prunt.restrictedcreative.listeners.BlockPistonListener;
import me.prunt.restrictedcreative.listeners.BlockPlaceListener;
import me.prunt.restrictedcreative.listeners.BlockUpdateListener;
import me.prunt.restrictedcreative.listeners.ChunkListener;
import me.prunt.restrictedcreative.listeners.EntityCreateListener;
import me.prunt.restrictedcreative.listeners.EntityDamageListener;
import me.prunt.restrictedcreative.listeners.PlayerInteractListener;
import me.prunt.restrictedcreative.listeners.PlayerInventoryListener;
import me.prunt.restrictedcreative.listeners.PlayerItemListener;
import me.prunt.restrictedcreative.listeners.PlayerMiscListener;
import me.prunt.restrictedcreative.listeners.WorldEditListener;
import me.prunt.restrictedcreative.storage.ConfigProvider;
import me.prunt.restrictedcreative.storage.Database;
import me.prunt.restrictedcreative.storage.SyncData;
import me.prunt.restrictedcreative.storage.handlers.BlockHandler;
import me.prunt.restrictedcreative.storage.handlers.InventoryHandler;
import me.prunt.restrictedcreative.storage.handlers.PermissionHandler;
import me.prunt.restrictedcreative.utils.Utils;

public class Main extends JavaPlugin {
	public static boolean DEBUG = true;
	public static boolean EXTRADEBUG = false;

	private Database database;

	private Utils utils;

	private ConfigProvider config;
	private ConfigProvider messages;

	private static FixedMetadataValue fmv;

	@Override
	public void onEnable() {
		setFMV(new FixedMetadataValue(getInstance(), "true"));
		setUtils(new Utils(this));

		loadConfigs();
		registerCommands();
		registerListeners();
		loadData();
	}

	@Override
	public void onDisable() {
		for (Player p : getServer().getOnlinePlayers()) {
			getUtils().saveInventory(p);
		}

		// Save data for the last time
		final List<String> fAdd = new ArrayList<>(BlockHandler.addToDatabase);
		final List<String> fDel = new ArrayList<>(BlockHandler.removeFromDatabase);
		new SyncData(this, fAdd, fDel, true).run();

		getDB().closeConnection();
	}

	/**
	 * Load settings and messages from config files
	 */
	public void loadConfigs() {
		this.config = new ConfigProvider(this, "config.yml");
		this.messages = new ConfigProvider(this, "messages.yml");
	}

	/**
	 * Reload settings and messages from config files, don't overwrite
	 */
	public void reloadConfigs() {
		this.config.reload();
		this.messages.reload();

		// Reload command messages, aliases etc as well
		registerCommands();
	}

	/**
	 * Register event listeners
	 */
	public void registerListeners() {
		if (Utils.isInstalled("WorldEdit") && getSettings().isEnabled("tracking.worldedit.enabled"))
			WorldEdit.getInstance().getEventBus().register(new WorldEditListener(this));

		// In case of plugin reload
		HandlerList.unregisterAll(this);

		getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);
		getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
		getServer().getPluginManager().registerEvents(new BlockUpdateListener(this), this);
		getServer().getPluginManager().registerEvents(new BlockChangeListener(this), this);
		getServer().getPluginManager().registerEvents(new BlockExplodeListener(this), this);
		getServer().getPluginManager().registerEvents(new BlockPistonListener(this), this);

		getServer().getPluginManager().registerEvents(new ChunkListener(), this);

		getServer().getPluginManager().registerEvents(new EntityDamageListener(this), this);
		getServer().getPluginManager().registerEvents(new EntityCreateListener(this), this);

		getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
		getServer().getPluginManager().registerEvents(new PlayerInventoryListener(this), this);
		getServer().getPluginManager().registerEvents(new PlayerItemListener(this), this);
		getServer().getPluginManager().registerEvents(new PlayerMiscListener(this), this);
	}

	/**
	 * Register plugin commands
	 */
	private void registerCommands() {
		// Register commands
		for (Entry<String, Map<String, Object>> entry : getDescription().getCommands().entrySet()) {
			String name = entry.getKey();

			PluginCommand cmd = getCommand(name);
			cmd.setExecutor(getExecutor(name));
			cmd.setPermissionMessage(getMessages().getMessage("no-permission"));
			cmd.setDescription(getSettings().getMessage("commands." + name + ".description"));
			cmd.setUsage(getSettings().getMessage("commands." + name + ".usage"));
		}

		// Using old way of handling aliases... TODO
		PermissionHandler.setUsingOldAliases(true);
	}

	/**
	 * Load data from database
	 */
	private void loadData() {
		setDB(new Database(this));

		BlockHandler.setUsingSQLite(getSettings().getString("database.type").equalsIgnoreCase("sqlite"));
		InventoryHandler.setForceGamemodeEnabled(Utils.isForceGamemodeEnabled());

		// Tracked blocks
		getDB().executeUpdate(
				"CREATE TABLE IF NOT EXISTS " + getDB().getBlocksTable() + " (block VARCHAR(255), UNIQUE (block))");

		// Tracked inventories
		if (BlockHandler.isUsingSQLite()) {
			ResultSet rs = getDB().executeQuery(
					"SELECT name FROM sqlite_master WHERE type='table' AND name='" + getDB().getInvsTable() + "'");

			boolean tableExists = false;
			try {
				tableExists = rs.next();
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			String tableName = getDB().getInvsTable();
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
							+ getDB().getInvsTable() + " GROUP BY player");
					stm.addBatch("DROP TABLE " + getDB().getInvsTable());
					stm.addBatch("ALTER TABLE " + tableName + " RENAME TO " + getDB().getInvsTable());
					stm.executeBatch();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} else {
			getDB().executeUpdate("CREATE TABLE IF NOT EXISTS " + getDB().getInvsTable()
					+ " (player VARCHAR(36), type TINYINT(1), storage TEXT, armor TEXT, extra TEXT, effects TEXT, xp BIGINT, lastused BIGINT(11), UNIQUE (player))");
		}

		if (getSettings().isEnabled("general.saving.inventories.enabled")) {
			long survival = Instant.now().getEpochSecond()
					- 86400 * getSettings().getInt("general.saving.inventories.purge.survival");
			long creative = Instant.now().getEpochSecond()
					- 86400 * getSettings().getInt("general.saving.inventories.purge.creative");

			getDB().executeUpdate("DELETE FROM " + getDB().getInvsTable() + " WHERE type = 0 AND lastused < " + survival
					+ " OR type = 1 AND lastused < " + creative);
		}

		if (getSettings().isEnabled("general.loading.use-old-system")) {
			BlockHandler.loadFromDatabaseOld(this);
		} else {
			BlockHandler.loadFromDatabaseNew(this);
		}

		BlockHandler.startDataSync(this);
	}

	private CommandExecutor getExecutor(String name) {
		switch (name) {
		case "rc":
			return new MainCommand(this);
		case "creative":
			return new SwitchCommand(this, GameMode.CREATIVE);
		case "survival":
			return new SwitchCommand(this, GameMode.SURVIVAL);
		case "adventure":
			return new SwitchCommand(this, GameMode.ADVENTURE);
		case "spectator":
			return new SwitchCommand(this, GameMode.SPECTATOR);
		default:
			return null;
		}
	}

	/**
	 * @return RestrictedCreative plugin instance
	 */
	public static Plugin getInstance() {
		return Bukkit.getPluginManager().getPlugin("RestrictedCreative");
	}

	/**
	 * @return Settings provider
	 */
	public ConfigProvider getSettings() {
		return this.config;
	}

	/**
	 * @return Messages provider
	 */
	public ConfigProvider getMessages() {
		return this.messages;
	}

	/**
	 * @return FixedMetadataValue used by RestrictedCreative
	 */
	public static FixedMetadataValue getFMV() {
		return fmv;
	}

	/**
	 * @param fmv FixedMetadataValue used by RestrictedCreative
	 */
	public static void setFMV(FixedMetadataValue fmv) {
		Main.fmv = fmv;
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
}
