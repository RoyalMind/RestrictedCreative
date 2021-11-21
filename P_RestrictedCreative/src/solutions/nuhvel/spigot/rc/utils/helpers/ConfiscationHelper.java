package solutions.nuhvel.spigot.rc.utils.helpers;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.utils.minecraft.ServerUtils;

import java.util.List;
import java.util.Map;

public class ConfiscationHelper {
    private final RestrictedCreative plugin;

    public ConfiscationHelper(RestrictedCreative plugin) {
        this.plugin = plugin;
    }

    public boolean shouldConfiscate(Player p, ItemStack is) {
        if (is == null)
            return false;

        Material m = is.getType();

        // Invalid items
        if (plugin.config.confiscate.invalidItems && !p.hasPermission("rc.bypass.confiscate.invalid-items")) {
            if ((ServerUtils.isInstalled("ProtocolLib") && isInvalidNBT(is)) || isInvalid(is))
                return true;
        }

        if (!plugin.config.confiscate.items.enabled)
            return false;

        if (!p.hasPermission("rc.bypass.confiscate.items.material") &&
                !p.hasPermission("rc.bypass.confiscate.items.material." + m))
            if (isInvalid(m))
                return true;

        if (!p.hasPermission("rc.bypass.confiscate.items.name"))
            if (isBadName(is))
                return true;

        if (!p.hasPermission("rc.bypass.confiscate.items.lore"))
            return isBadLore(is);

        return false;
    }

    private boolean isInvalid(Material m) {
        return plugin.config.confiscate.items.materials.contains(m);
    }

    private boolean isInvalidNBT(ItemStack is) {
        // No need to control disabled features
        if (!plugin.config.confiscate.invalidItems)
            return false;

        if (is == null || is.getType() == Material.AIR)
            return false;

        try {
            NbtCompound nc = (NbtCompound) NbtFactory.fromItemTag(is);

            if (nc.containsKey("CustomPotionEffects") || nc.containsKey("StoredEnchantments") ||
                    nc.containsKey("HideFlags") || nc.containsKey("Unbreakable") ||
                    nc.containsKey("AttributeModifiers"))
                return true;
        } catch (Exception ex) {
            return false;
        }

        return false;
    }

    private boolean isInvalid(ItemStack is) {
        // No need to control disabled features
        if (!plugin.config.confiscate.invalidItems)
            return false;

        if (is == null || is.getType() == Material.AIR)
            return false;

        ItemMeta im = is.getItemMeta();

        // Displayname length check
        if (im != null && im.getDisplayName().length() > 30)
            return true;

        // Enchantments check
        for (Map.Entry<Enchantment, Integer> ench : is.getEnchantments().entrySet())
            if (ench.getValue() < 1 || ench.getValue() > ench.getKey().getMaxLevel())
                return true;

        return false;
    }

    // Check if item name is bad
    private boolean isBadName(ItemStack is) {
        if (is == null || is.getItemMeta() == null)
            return false;

        for (String name : plugin.config.confiscate.items.names) {
            ItemMeta im = is.getItemMeta();
            if (im == null)
                return false;

            String dn = im.getDisplayName();
            if (dn.contains(name))
                return true;
        }

        return false;
    }

    // Check if item lore is bad
    private boolean isBadLore(ItemStack is) {
        for (String name : plugin.config.confiscate.items.lores) {
            ItemMeta im = is.getItemMeta();
            if (im == null)
                return false;

            List<String> lores = im.getLore();
            if (lores == null)
                return false;

            for (String lore : lores) {
                if (lore.contains(name))
                    return true;
            }
        }

        return false;
    }
}
