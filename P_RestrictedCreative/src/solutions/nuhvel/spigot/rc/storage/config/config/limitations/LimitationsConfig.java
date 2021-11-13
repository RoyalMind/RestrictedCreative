package solutions.nuhvel.spigot.rc.storage.config.config.limitations;

import de.exlll.configlib.annotation.ConfigurationElement;

import java.util.List;

@ConfigurationElement
public final class LimitationsConfig {
    public MovingLimitation moving = new MovingLimitation();
    public RegionLimitation regions = new RegionLimitation();
    public ItemLimitation items = new ItemLimitation();
    public List<String> commands = List.of("(.*)(buy|sell)(.*)", "(.*)(chest)(.*)");
    public boolean receivingDamage = false;
}
