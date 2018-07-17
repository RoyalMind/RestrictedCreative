package me.prunt.restrictedcreative;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.WorldEdit;

import me.prunt.restrictedcreative.listeners.BlockPlaceListener;
import me.prunt.restrictedcreative.listeners.WEListener;
import me.prunt.restrictedcreative.store.ConfigProvider;
import me.prunt.restrictedcreative.store.DataHandler;
import me.prunt.restrictedcreative.store.Database;
import me.prunt.restrictedcreative.utils.AliasManager;
import me.prunt.restrictedcreative.utils.Utils;

public class Main extends JavaPlugin {
    /**
     * Database handler
     */
    private Database database;

    /**
     * Config provider for general settings
     */
    private ConfigProvider config;
    /**
     * Config provider for translatable messages
     */
    private ConfigProvider messages;

    /**
     * FixedMetadataValue used by RestrictedCreative
     */
    private static FixedMetadataValue fmv;

    /*
     * (non-Javadoc)
     * 
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {
	setFMV(new FixedMetadataValue(getInstance(), "true"));

	loadConfig();
	registerListeners();
	registerCommands();
	loadData();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
     */
    @Override
    public void onDisable() {

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

	getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), getInstance());
    }

    /**
     * Register plugin commands
     */
    private void registerCommands() {
	// Register commands
	for (Entry<String, Map<String, Object>> entry : getDescription().getCommands().entrySet()) {
	    PluginCommand cmd = getCommand(entry.getKey());
	    cmd.setExecutor(new Commands(this));
	    cmd.setPermissionMessage(getMessages().getMessage("no-permission"));
	    cmd.setDescription(getSettings().getMessage("commands." + entry.getKey() + ".description"));

	    // Register aliases
	    AliasManager aliasManager = new AliasManager(this);
	    if (!aliasManager.setAdditionalAliases(cmd,
		    getSettings().getStringList("commands." + entry.getKey() + ".aliases"))) {
		DataHandler.setUsingOldAliases(true);
		System.out.println("" + ChatColor.RED + ChatColor.BOLD + "Unable to access CommandMap for: "
			+ ChatColor.RESET + cmd.getName() + ChatColor.YELLOW + ChatColor.BOLD
			+ ". Plugin will use old (agressive) method to enforce aliases!");
	    }
	}

    }

    /**
     * Load data from database
     */
    private void loadData() {
	setDB(new Database(this, getSettings().getString("database.type")));

    }

    /**
     * @param name
     *                 World name
     * @return Whether the plugin is disabled in the given world
     */
    public boolean isDisabledWorld(String name) {
	return getSettings().getStringList("general.worlds.disable-plugin").contains(name);
    }

    /**
     * @param name
     *                 World name
     * @return Whether tracking and saving is disabled in the given world
     */
    public boolean isBypassedWorld(String name) {
	return getSettings().getStringList("general.worlds.bypass").contains(name);
    }

    /**
     * @param m
     *              Material type
     * @return Whether the given type should be excluded from tracking
     */
    public boolean isExcluded(Material m) {
	return getSettings().getMaterialList("tracking.blocks.exclude").contains(m);
    }

    /**
     * @param m
     *              Material type
     * @return Whether the given type should be disabled from placing by creative
     *         players
     */
    public boolean isDisabledPlacing(Material m) {
	return getSettings().getMaterialList("disable.placing").contains(m);
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

    /**
     * @param sender
     *                   Player to send the message
     * @param prefix
     *                   Whether to include a prefix in the message
     * @param string
     *                   Strings to send to the player
     */
    public void sendMessage(CommandSender sender, boolean prefix, String... string) {
	if (getSettings().isNone(string))
	    return;

	String msg = "";
	for (String path : string)
	    msg += getSettings().getMessage(path);

	sender.sendMessage(msg);
    }
}
