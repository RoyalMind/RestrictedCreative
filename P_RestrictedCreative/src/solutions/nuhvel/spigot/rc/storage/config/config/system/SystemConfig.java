package solutions.nuhvel.spigot.rc.storage.config.config.system;

import de.exlll.configlib.annotation.ConfigurationElement;

@ConfigurationElement
public final class SystemConfig {
    public SystemWorlds worlds = new SystemWorlds();
    public boolean delayLogin = true;
}
