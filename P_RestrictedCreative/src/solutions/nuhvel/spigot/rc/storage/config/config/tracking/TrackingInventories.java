package solutions.nuhvel.spigot.rc.storage.config.config.tracking;

import de.exlll.configlib.annotation.ConfigurationElement;

@ConfigurationElement
public final class TrackingInventories {
    public boolean enabled = true;
    public InventoriesPurge purge = new InventoriesPurge();
}
