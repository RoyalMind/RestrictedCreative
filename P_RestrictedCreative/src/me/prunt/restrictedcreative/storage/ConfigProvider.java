package me.prunt.restrictedcreative.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.base.Charsets;

import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.utils.Utils;

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
		return ChatColor.translateAlternateColorCodes('&', getString(path));
	}

	public String getString(String path) {
		return getConfig().getString(path) == null ? "" : getConfig().getString(path);
	}

	public int getInt(String path) {
		return getConfig().getInt(path);
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

	public boolean isNone(String... paths) {
		for (String path : paths) {
			if (!getMessage(path).equalsIgnoreCase(""))
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

			// Update config with new paths and values
			config.options().copyDefaults(true);
			config.setDefaults(YamlConfiguration
					.loadConfiguration(new InputStreamReader(getMain().getResource(name), Charsets.UTF_8)));
			config.save(file);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}

		return config;
	}

	private void loadConfig() {
		if (isDefault()) {
			InputStreamReader reader = new InputStreamReader(getMain().getResource(name), Charsets.UTF_8);
			YamlConfiguration defaults = YamlConfiguration.loadConfiguration(reader);
			
			// Update config with new paths and values
			getMain().getConfig().options().copyDefaults(true);
			getMain().getConfig().setDefaults(defaults);
			getMain().saveConfig();

			getMain().reloadConfig();

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
