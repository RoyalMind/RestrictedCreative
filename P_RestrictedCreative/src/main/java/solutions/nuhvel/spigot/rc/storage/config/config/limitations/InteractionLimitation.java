package solutions.nuhvel.spigot.rc.storage.config.config.limitations;

import de.exlll.configlib.annotation.ConfigurationElement;

@ConfigurationElement
public final class InteractionLimitation {
    public boolean inventories = true;
    public boolean entities = true;
    public boolean breeding = true;
    public boolean slimefun = true;
}
