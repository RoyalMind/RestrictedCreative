package solutions.nuhvel.spigot.rc.storage.config.config;

import de.exlll.configlib.annotation.Comment;
import de.exlll.configlib.configs.yaml.BukkitYamlConfiguration;
import solutions.nuhvel.spigot.rc.storage.config.config.commands.PluginCommands;
import solutions.nuhvel.spigot.rc.storage.config.config.confiscate.ConfiscateConfig;
import solutions.nuhvel.spigot.rc.storage.config.config.database.DatabaseConnection;
import solutions.nuhvel.spigot.rc.storage.config.config.disable.DisableConfig;
import solutions.nuhvel.spigot.rc.storage.config.config.limitations.LimitationsConfig;
import solutions.nuhvel.spigot.rc.storage.config.config.miscellaneous.MiscellaneousConfig;
import solutions.nuhvel.spigot.rc.storage.config.config.system.SystemConfig;
import solutions.nuhvel.spigot.rc.storage.config.config.tracking.TrackingConfig;

import java.nio.file.Path;

public final class PluginConfig extends BukkitYamlConfiguration {
    public boolean debug = false;
    @Comment({"", "Database type is either MYSQL or SQLITE"})
    public DatabaseConnection database = new DatabaseConnection();
    @Comment({"", "Configure the plugin's commands"})
    public PluginCommands commands = new PluginCommands();

    @Comment({"", "General settings"})
    public SystemConfig system = new SystemConfig();

    @Comment({"", "Choose what is tracked", "Tracking WordEdit events require WorldEdit to be installed",
            "Purge times are in days"})
    public TrackingConfig tracking = new TrackingConfig();

    @Comment({"", "Creative limitations"})
    public LimitationsConfig limitations = new LimitationsConfig();

    @Comment({"", "Confiscate from creative hands"})
    public ConfiscateConfig confiscate = new ConfiscateConfig();

    @Comment({"", "Disable interacting with items/blocks"})
    public DisableConfig disable = new DisableConfig();

    @Comment({"", "Miscellaneous features"})
    public MiscellaneousConfig miscellaneous = new MiscellaneousConfig();

    public PluginConfig(Path path, BukkitYamlProperties properties) {
        super(path, properties);
    }
}
