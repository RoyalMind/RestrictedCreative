package solutions.nuhvel.spigot.rc.storage.config.config.limitations;

import de.exlll.configlib.annotation.ConfigurationElement;

@ConfigurationElement
public final class ItemLimitation {
    public boolean dropping = false;
    public boolean pickingUp = false;
    public boolean throwing = false;
}
