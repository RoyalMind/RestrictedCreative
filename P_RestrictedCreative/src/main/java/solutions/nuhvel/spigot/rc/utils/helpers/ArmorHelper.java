package solutions.nuhvel.spigot.rc.utils.helpers;

import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.storage.config.config.miscellaneous.ArmorMaterial;
import solutions.nuhvel.spigot.rc.utils.minecraft.MaterialHandler;

import java.util.List;

public class ArmorHelper {
    private final RestrictedCreative plugin;

    public ArmorHelper(RestrictedCreative plugin) {
        this.plugin = plugin;
    }

    public void equipArmor(Player p) {
        ArmorMaterial type = plugin.config.miscellaneous.armor.type;
        List<ItemStack> armorList = MaterialHandler.getArmorList(type);

        if (type == ArmorMaterial.LEATHER) {
            var color = getColorFromHex(plugin.config.miscellaneous.armor.color);

            for (ItemStack is : armorList) {
                LeatherArmorMeta lam = (LeatherArmorMeta) is.getItemMeta();
                if (lam == null)
                    continue;

                lam.setColor(color);
                is.setItemMeta(lam);
            }
        }

        ItemStack[] list = new ItemStack[armorList.size()];
        list = armorList.toArray(list);

        p.getInventory().setArmorContents(list);
        p.updateInventory();
    }

    public static Color getColorFromHex(String colorStr) {
        return Color.fromRGB(
                Integer.valueOf(colorStr.substring(1, 3), 16),
                Integer.valueOf(colorStr.substring(3, 5), 16),
                Integer.valueOf(colorStr.substring(5, 7), 16));
    }
}
