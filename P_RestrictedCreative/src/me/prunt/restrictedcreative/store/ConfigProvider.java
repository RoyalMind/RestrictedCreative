package me.prunt.restrictedcreative.store;

import java.io.File;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.prunt.restrictedcreative.Main;

public class ConfigProvider {
    private Main main;
    private FileConfiguration config;
    private String name;

    public ConfigProvider(Main main, String name) {
	setMain(main);
	setName(name);
	loadConfig();
    }

    /* Configuration */

    public String getMessage(String path) {
	String msg = getConfig().getString(path);

	// If the user wished to remove the message
	if (msg.equalsIgnoreCase("none"))
	    return "";

	// Convert formatting to readable format
	return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public boolean isEnabled(String path) {
	return getConfig().getBoolean(path);
    }

    /* Methods */

    public void reload() {
	if (isDefault()) {
	    getMain().saveDefaultConfig();
	    getMain().reloadConfig();
	} else {
	    setConfig(loadCustomConfig(getName()));
	}
    }

    private FileConfiguration loadCustomConfig(String name) {
	File file = new File(getMain().getDataFolder(), name);

	if (!file.exists()) {
	    file.getParentFile().mkdirs();
	    getMain().saveResource(name, false);
	}

	FileConfiguration config = new YamlConfiguration();
	try {
	    config.load(file);
	} catch (IOException | InvalidConfigurationException e) {
	    e.printStackTrace();
	}

	return config;
    }

    private void loadConfig() {
	if (isDefault()) {
	    getMain().saveDefaultConfig();
	    setConfig(getMain().getConfig());
	} else {
	    setConfig(loadCustomConfig(getName()));
	}
    }

    private boolean isDefault() {
	return getName() == "config.yml";
    }

    private boolean isNone(String... list) {
	for (String str : list) {
	    if (!str.equalsIgnoreCase(""))
		return false;
	}

	return true;
    }

    /* Getters & setters */

    private Main getMain() {
	return main;
    }

    private void setMain(Main main) {
	this.main = main;
    }

    public FileConfiguration getConfig() {
	return config;
    }

    public void setConfig(FileConfiguration config) {
	this.config = config;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }
}
