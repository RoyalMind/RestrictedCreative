package solutions.nuhvel.spigot.rc.storage.config.config.confiscate;

import de.exlll.configlib.annotation.ConfigurationElement;

@ConfigurationElement
public final class ConfiscateLimitation {
    public boolean invalidItems = true;
    public ConfiscateMiddleClick middleClick = new ConfiscateMiddleClick();
    public ConfiscateItems items = new ConfiscateItems();
}
