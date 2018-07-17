package me.prunt.restrictedcreative;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.WorldEdit;

import me.prunt.restrictedcreative.listeners.BlockPlaceListener;
import me.prunt.restrictedcreative.listeners.WEListener;
import me.prunt.restrictedcreative.store.ConfigProvider;

public class Main extends JavaPlugin {
    private ConfigProvider config;
    private ConfigProvider messages;

    private static FixedMetadataValue fmv;

    @Override
    public void onEnable() {
	setFMV(new FixedMetadataValue(getInstance(), "true"));

	loadConfig();
	registerListeners();
	registerCommands();
	loadData();
    }

    @Override
    public void onDisable() {

    }

    private void loadConfig() {
	this.config = new ConfigProvider(this, "config.yml");
	this.messages = new ConfigProvider(this, "messages.yml");
    }

    private void registerListeners() {
	if (Utils.isInstalled("WorldEdit") && getSettings().isEnabled("tracking.worldedit.enabled"))
	    WorldEdit.getInstance().getEventBus().register(new WEListener(this));

	getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), getInstance());
    }

    private void registerCommands() {

    }

    private void loadData() {

    }

    public boolean isDisabledWorld(String name) {
	return getSettings().getStringList("general.worlds.disable-plugin").contains(name);
    }

    public boolean isBypassedWorld(String name) {
	return getSettings().getStringList("general.worlds.bypass").contains(name);
    }

    public boolean isExcluded(Material m) {
	return getSettings().getMaterialList("tracking.blocks.exclude").contains(m);
    }

    public boolean isDisabledPlacing(Material m) {
	return getSettings().getMaterialList("disable.placing").contains(m);
    }

    public static Plugin getInstance() {
	return Bukkit.getPluginManager().getPlugin("RestrictedCreative");
    }

    public ConfigProvider getSettings() {
	return this.config;
    }

    public ConfigProvider getMessages() {
	return this.messages;
    }

    public static FixedMetadataValue getFMV() {
	return fmv;
    }

    public static void setFMV(FixedMetadataValue fmv) {
	Main.fmv = fmv;
    }

    public void sendMessage(Player p, boolean prefix, String... paths) {
	if (getSettings().isNone(paths))
	    return;

	String msg = "";
	for (String path : paths)
	    msg += getSettings().getMessage(path);

	p.sendMessage(msg);
    }
}
