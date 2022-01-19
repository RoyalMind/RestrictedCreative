package solutions.nuhvel.spigot.rc.storage.config.config.tracking;

import de.exlll.configlib.annotation.ConfigurationElement;

@ConfigurationElement
public final class TrackingConfig {
    public TrackingWorldEdit worldedit = new TrackingWorldEdit();
    public TrackingInventories inventories = new TrackingInventories();
    public TrackingBlocks blocks = new TrackingBlocks();
}
