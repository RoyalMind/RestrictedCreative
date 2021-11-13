package solutions.nuhvel.spigot.rc.storage.config.config.tracking;

import de.exlll.configlib.annotation.ConfigurationElement;

@ConfigurationElement
public final class InventoriesPurge {
    public boolean enabled = true;
    public int survival;
    public int creative;
}
