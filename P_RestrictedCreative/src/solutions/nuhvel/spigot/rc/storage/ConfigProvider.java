package solutions.nuhvel.spigot.rc.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.base.Charsets;

import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.utils.Utils;

public class ConfigProvider {
	private RestrictedCreative restrictedCreative;
	private FileConfiguration config;
	private String name;

	public ConfigProvider(RestrictedCreative restrictedCreative, String name) {
		setMain(restrictedCreative);
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
		loadConfig();
	}

	public boolean isNone(String... paths) {
		for (String path : paths) {
			if (!getMessage(path).equalsIgnoreCase(""))
				return false;
		}

		return true;
	}

	public void saveConfig() {
		try {
			File file = new File(getMain().getDataFolder(), getName());
			getConfig().save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadConfig() {
		File file = new File(getMain().getDataFolder(), getName());

		if (!file.exists()) {
			file.getParentFile().mkdirs();
			getMain().saveResource(getName(), false);
		}

		YamlConfiguration config = new YamlConfiguration();

		try {
			config.load(file);

			InputStream stream = getMain().getResource(getName());
			InputStreamReader reader = new InputStreamReader(stream, Charsets.UTF_8);
			YamlConfiguration defaults = YamlConfiguration.loadConfiguration(reader);

			// Update config with new paths and values
			config.options().copyDefaults(true);
			config.setDefaults(defaults);

			config.save(file);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}

		setConfig(config);
	}

	/* Getters & setters */

	private RestrictedCreative getMain() {
		return restrictedCreative;
	}

	private void setMain(RestrictedCreative restrictedCreative) {
		this.restrictedCreative = restrictedCreative;
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
