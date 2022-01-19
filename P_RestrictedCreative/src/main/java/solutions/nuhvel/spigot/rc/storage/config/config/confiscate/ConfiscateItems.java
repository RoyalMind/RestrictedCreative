package solutions.nuhvel.spigot.rc.storage.config.config.confiscate;

import de.exlll.configlib.annotation.ConfigurationElement;
import de.exlll.configlib.annotation.ElementType;
import org.bukkit.Material;

import java.util.List;

@ConfigurationElement
public final class ConfiscateItems {
    public boolean enabled = true;

    @ElementType(Material.class)
    public List<Material> materials = List.of(Material.BARRIER, Material.CHAIN_COMMAND_BLOCK, Material.COMMAND_BLOCK,
            Material.COMMAND_BLOCK_MINECART, Material.REPEATING_COMMAND_BLOCK, Material.SPAWNER, Material.DRAGON_EGG,
            Material.RED_MUSHROOM_BLOCK, Material.BROWN_MUSHROOM_BLOCK, Material.DIRT_PATH, Material.STRUCTURE_BLOCK,
            Material.STRUCTURE_VOID, Material.VOID_AIR, Material.KNOWLEDGE_BOOK, Material.DEBUG_STICK, Material.JIGSAW);

    public List<String> names = List.of("Cash");
    public List<String> lores = List.of("$");
}
