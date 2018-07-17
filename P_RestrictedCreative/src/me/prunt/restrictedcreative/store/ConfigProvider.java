package me.prunt.restrictedcreative.store;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.Utils;

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
	return ChatColor.translateAlternateColorCodes('&', getConfig().getString(path));
    }

    public boolean isEnabled(String path) {
	return getConfig().getBoolean(path);
    }

    public List<String> getStringList(String path) {
	return getConfig().getStringList(path);
    }

    public List<Material> getMaterialList(String path) {
	List<Material> list = new ArrayList<>();

	for (String m : getConfig().getStringList(path)) {
	    try {
		list.add(Material.valueOf(m));
	    } catch (IllegalArgumentException e) {
		Utils.log("Skipped item: " + e.getMessage());
	    }
	}

	return list;
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

    public boolean isNone(String... list) {
	for (String str : list) {
	    if (!getMessage(str).equalsIgnoreCase(""))
		return false;
	}

	return true;
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