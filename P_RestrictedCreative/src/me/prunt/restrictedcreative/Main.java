package me.prunt.restrictedcreative;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.WorldEdit;

import me.prunt.restrictedcreative.commands.MainCommand;
import me.prunt.restrictedcreative.listeners.BlockBreakListener;
import me.prunt.restrictedcreative.listeners.BlockChangeListener;
import me.prunt.restrictedcreative.listeners.BlockExplodeListener;
import me.prunt.restrictedcreative.listeners.BlockPistonListener;
import me.prunt.restrictedcreative.listeners.BlockPlaceListener;
import me.prunt.restrictedcreative.listeners.BlockUpdateListener;
import me.prunt.restrictedcreative.listeners.EntityCreateListener;
import me.prunt.restrictedcreative.listeners.EntityDamageListener;
import me.prunt.restrictedcreative.listeners.WEListener;
import me.prunt.restrictedcreative.storage.ConfigProvider;
import me.prunt.restrictedcreative.storage.DataHandler;
import me.prunt.restrictedcreative.storage.Database;
import me.prunt.restrictedcreative.storage.SyncData;
import me.prunt.restrictedcreative.utils.AliasManager;
import me.prunt.restrictedcreative.utils.Utils;

public class Main extends JavaPlugin {
    private Database database;

    private Utils utils;

    private ConfigProvider config;
    private ConfigProvider messages;

    private static FixedMetadataValue fmv;

    @Override
    public void onEnable() {
	setFMV(new FixedMetadataValue(getInstance(), "true"));
	setUtils(new Utils(this));

	loadConfig();
	registerListeners();
	registerCommands();
	loadData();
    }

    @Override
    public void onDisable() {
	for (Player p : getServer().getOnlinePlayers()) {
	    String name = p.getWorld().getName();

	    if (p.getGameMode() == GameMode.CREATIVE)
		continue;
	    if (getUtils().isDisabledWorld(name))
		continue;

	    // TODO save player's data
	}

	final List<String> fadd = new ArrayList<>(DataHandler.addToDatabase);
	final List<String> fdel = new ArrayList<>(DataHandler.removeFromDatabase);
	getServer().getScheduler().runTask(this, new SyncData(this, fadd, fdel));

	getDB().closeConnection();
    }

    /**
     * Load settings and messages from config files
     */
    public void loadConfig() {
	this.config = new ConfigProvider(this, "config.yml");
	this.messages = new ConfigProvider(this, "messages.yml");
    }

    /**
     * Register event listeners
     */
    private void registerListeners() {
	if (Utils.isInstalled("WorldEdit") && getSettings().isEnabled("tracking.worldedit.enabled"))
	    WorldEdit.getInstance().getEventBus().register(new WEListener(this));

	getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);
	getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
	getServer().getPluginManager().registerEvents(new BlockUpdateListener(this), this);
	getServer().getPluginManager().registerEvents(new BlockChangeListener(this), this);
	getServer().getPluginManager().registerEvents(new BlockExplodeListener(this), this);
	getServer().getPluginManager().registerEvents(new BlockPistonListener(this), this);

	getServer().getPluginManager().registerEvents(new EntityDamageListener(this), this);
	getServer().getPluginManager().registerEvents(new EntityCreateListener(this), this);
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

	    // Register aliases
	    AliasManager aliasManager = new AliasManager(this);
	    if (!aliasManager.setAdditionalAliases(cmd, getSettings().getStringList("commands." + name + ".aliases"))) {
		DataHandler.setUsingOldAliases(true);
		Utils.log("" + ChatColor.RED + ChatColor.BOLD + "Unable to access CommandMap for: " + ChatColor.RESET
			+ cmd.getName() + ChatColor.YELLOW + ChatColor.BOLD
			+ ". Plugin will use old (agressive) method to enforce aliases!");
	    }
	}

    }

    /**
     * Load data from database
     */
    private void loadData() {
	setDB(new Database(this, getSettings().getString("database.type")));

	if (getSettings().getString("database.type").equalsIgnoreCase("mysql")) {
	    getDB().executeUpdate(
		    "CREATE TABLE IF NOT EXISTS " + getDB().getTableName() + " (block VARCHAR(255), UNIQUE (block))");
	} else if (getSettings().getString("database.type").equalsIgnoreCase("sqlite")) {
	    DataHandler.setUsingSQLite(true);
	    getDB().executeUpdate(
		    "CREATE TABLE IF NOT EXISTS " + getDB().getTableName() + " (block VARCHAR(255) UNIQUE)");
	}

	DataHandler.loadFromDatabase(this);
	DataHandler.startDataSync(this);
    }

    private CommandExecutor getExecutor(String name) {
	switch (name) {
	case "rc":
	    return new MainCommand(this);
	case "creative":
	    return null;
	case "survival":
	    return null;
	case "adventure":
	    return null;
	case "spectator":
	    return null;
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
     * @param fmv
     *                FixedMetadataValue used by RestrictedCreative
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
