package solutions.nuhvel.spigot.rc.storage.config.config.tracking;

import de.exlll.configlib.annotation.ConfigurationElement;
import de.exlll.configlib.annotation.ElementType;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

@ConfigurationElement
public final class TrackingBlocks {
    public boolean enabled = true;
    public boolean notify = false;

    public int syncInterval = 6000;

    @ElementType(Material.class)
    public List<Material> excluded = new ArrayList<>();
}
