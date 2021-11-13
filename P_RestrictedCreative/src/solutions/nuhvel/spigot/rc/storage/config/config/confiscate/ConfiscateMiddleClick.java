package solutions.nuhvel.spigot.rc.storage.config.config.confiscate;

import de.exlll.configlib.annotation.ConfigurationElement;
import de.exlll.configlib.annotation.ElementType;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ConfigurationElement
public final class ConfiscateMiddleClick {
    public boolean enabled = true;

    @ElementType(Material.class)
    public List<Material> excluded = List.of(Material.PLAYER_HEAD);
}
