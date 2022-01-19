package solutions.nuhvel.spigot.rc.storage.config.config;

import de.exlll.configlib.annotation.Comment;
import de.exlll.configlib.configs.yaml.BukkitYamlConfiguration;
import solutions.nuhvel.spigot.rc.storage.config.config.database.DatabaseConnection;
import solutions.nuhvel.spigot.rc.storage.config.config.disable.DisableConfig;
import solutions.nuhvel.spigot.rc.storage.config.config.limitations.LimitationsConfig;
import solutions.nuhvel.spigot.rc.storage.config.config.miscellaneous.MiscellaneousConfig;
import solutions.nuhvel.spigot.rc.storage.config.config.system.SystemConfig;
import solutions.nuhvel.spigot.rc.storage.config.config.tracking.TrackingConfig;

import java.nio.file.Path;

public final class RcConfig extends BukkitYamlConfiguration {
    public boolean debug = false;
    @Comment({"", "Database type is either MYSQL or SQLITE"})
    public DatabaseConnection database = new DatabaseConnection();

    @Comment({"", "General settings"})
    public SystemConfig system = new SystemConfig();

    @Comment({"", "Choose what is tracked", "Tracking WordEdit events require WorldEdit to be installed",
            "Purge times are in days"})
    public TrackingConfig tracking = new TrackingConfig();

    @Comment({"", "Creative mode limitations"})
    public LimitationsConfig limitations = new LimitationsConfig();

    @Comment({"", "Disable interacting with items/blocks"})
    public DisableConfig disable = new DisableConfig();

    @Comment({"", "Miscellaneous features"})
    public MiscellaneousConfig miscellaneous = new MiscellaneousConfig();

    public RcConfig(Path path, BukkitYamlProperties properties) {
        super(path, properties);
    }
}
