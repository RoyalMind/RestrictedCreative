package solutions.nuhvel.spigot.rc.utils.helpers;

import org.bukkit.entity.Player;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.utils.minecraft.ServerUtils;

public class SwitchingHelper {
    private final RestrictedCreative plugin;

    public SwitchingHelper(RestrictedCreative plugin) {
        this.plugin = plugin;
    }

    public void setCreative(Player p, boolean toCreative) {
        // Permissions
        if (plugin.config.miscellaneous.permissions.enabled && !p.hasPermission("rc.bypass.creative.permissions"))
            new PermissionsHelper(plugin).setPermissions(p, toCreative);

        // Groups
        if (ServerUtils.isInstalled("Vault") && plugin.config.miscellaneous.groups.enabled &&
                !p.hasPermission("rc.bypass.creative.groups"))
            new PermissionsHelper(plugin).setGroups(p, toCreative);

        // Inventory
        if (plugin.config.tracking.inventories.enabled && !p.hasPermission("rc.bypass.tracking.inventory"))
            new InventoryHelper(plugin).separateInventory(p, toCreative);

        // Armor
        if (toCreative && plugin.config.miscellaneous.armor.enabled && !p.hasPermission("rc.bypass.creative.armor"))
            new ArmorHelper(plugin).equipArmor(p);
    }
}
