package solutions.nuhvel.spigot.rc.storage.config.config.limitations;

import de.exlll.configlib.annotation.ConfigurationElement;

import java.util.ArrayList;
import java.util.List;

@ConfigurationElement
public final class RegionLimitation {
    public OwnershipLimitation ownership = new OwnershipLimitation();
    public List<String> whitelisted = new ArrayList<>();
}
