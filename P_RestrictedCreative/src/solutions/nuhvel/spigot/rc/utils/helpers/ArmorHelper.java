package solutions.nuhvel.spigot.rc.utils.helpers;

import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.storage.config.config.miscellaneous.ArmorMaterial;
import solutions.nuhvel.spigot.rc.utils.MaterialHandler;

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
            var rgb = java.awt.Color.decode(plugin.config.miscellaneous.armor.color).getRGB();
            var color = Color.fromRGB(rgb);

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
}
