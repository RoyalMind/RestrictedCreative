package solutions.nuhvel.spigot.rc.utils.helpers;

import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import solutions.nuhvel.spigot.rc.storage.handlers.TrackableHandler;

public class ArmorStandHelper {
    public static void removeItemFromArmorStand(ArmorStand a, EquipmentSlot slot, EntityEquipment inv, ItemStack air) {
        switch (slot) {
            case CHEST -> inv.setChestplate(air);
            case FEET -> inv.setBoots(air);
            case HEAD -> inv.setHelmet(air);
            case LEGS -> inv.setLeggings(air);
            case HAND -> inv.setItemInMainHand(air);
            case OFF_HAND -> inv.setItemInOffHand(air);
            default -> {
            }
        }

        TrackableHandler.removeTracking(a, slot);
    }
}
