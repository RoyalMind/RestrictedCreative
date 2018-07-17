package me.prunt.restrictedcreative;

import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.WorldEdit;

import me.prunt.restrictedcreative.listeners.WEListener;
import me.prunt.restrictedcreative.store.ConfigProvider;

public class Main extends JavaPlugin {
    private ConfigProvider config;
    private ConfigProvider messages;

    @Override
    public void onEnable() {
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
	if (Utils.isInstalled("WorldEdit") && getSettings().isEnabled(""))
	    WorldEdit.getInstance().getEventBus().register(new WEListener(this));
    }

    private void registerCommands() {

    }

    private void loadData() {

    }

    public ConfigProvider getSettings() {
	return this.config;
    }

    public ConfigProvider getMessages() {
	return this.messages;
    }
}
