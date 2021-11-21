package solutions.nuhvel.spigot.rc.storage.config.config.disable;

import de.exlll.configlib.annotation.ConfigurationElement;
import de.exlll.configlib.annotation.ElementType;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ConfigurationElement
public final class DisableConfig {
    @ElementType(Material.class)
    public List<Material> placing = Arrays.asList(Material.BEDROCK, Material.END_PORTAL_FRAME, Material.TNT, Material.SCAFFOLDING);
    @ElementType(Material.class)
    public List<Material> breaking = Arrays.asList(Material.BEDROCK, Material.END_PORTAL_FRAME);

    public DisableInteracting interacting = new DisableInteracting();
}
