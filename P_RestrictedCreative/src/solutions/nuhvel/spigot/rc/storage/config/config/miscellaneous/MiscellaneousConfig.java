package solutions.nuhvel.spigot.rc.storage.config.config.miscellaneous;

import de.exlll.configlib.annotation.ConfigurationElement;
import solutions.nuhvel.spigot.rc.storage.config.config.confiscate.ConfiscateItems;

@ConfigurationElement
public final class MiscellaneousConfig {
    public ArmorColorConfig armor = new ArmorColorConfig();
    public ConfiscateItems items = new ConfiscateItems();
    public boolean invalidItems = true;
}
