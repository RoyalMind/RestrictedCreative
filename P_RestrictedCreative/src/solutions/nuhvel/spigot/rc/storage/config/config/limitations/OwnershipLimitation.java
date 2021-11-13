package solutions.nuhvel.spigot.rc.storage.config.config.limitations;

import de.exlll.configlib.annotation.ConfigurationElement;

@ConfigurationElement
public final class OwnershipLimitation {
    public boolean enabled = false;
    public boolean allowMembers = false;
}
