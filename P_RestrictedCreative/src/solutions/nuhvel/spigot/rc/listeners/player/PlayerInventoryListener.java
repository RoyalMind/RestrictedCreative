package solutions.nuhvel.spigot.rc.listeners.player;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.utils.helpers.ArmorHelper;
import solutions.nuhvel.spigot.rc.utils.helpers.ConfiscationHelper;
import solutions.nuhvel.spigot.rc.utils.helpers.PreconditionChecker;

public class PlayerInventoryListener implements Listener {
    private final RestrictedCreative plugin;

    public PlayerInventoryListener(RestrictedCreative plugin) {
        this.plugin = plugin;
    }

    /*
     * This event is called when a player in creative mode puts down or picks up an
     * item in their inventory / hotbar and when they drop items from their
     * Inventory while in creative mode.
     *
     * e.getAction() is always PLACE_ALL for some reason
     *
     * e.getClick() is always CREATIVE
     */
    @EventHandler(ignoreCancelled = true)
    public void onInventoryCreative(InventoryCreativeEvent e) {
        Player p = (Player) e.getWhoClicked();
        Inventory i = e.getClickedInventory();
        ItemStack is = e.getCursor();

        if (new PreconditionChecker(plugin, p).isWorldAllowed(p.getWorld().getName()).anyFailed())
            return;

        // Creative armor
        if (plugin.config.miscellaneous.armor.enabled && !p.hasPermission("rc.bypass.creative.armor")) {
            // Armor exists check
            if (!armorIsEquipped(p.getInventory().getArmorContents())) {
                new ArmorHelper(plugin).equipArmor(p);

                plugin.messagingUtils.sendMessage(p, true, plugin.messages.disabled.armor);
                e.setResult(Result.DENY);
                return;
            }
        }

        // Middle-click check
        if (plugin.config.confiscate.middleClick.enabled && !p.hasPermission("rc.bypass.confiscate.middle-click")) {
            Material m = is.getType();

            // If it's a block (not an item) and it's not excluded
            if (m.isBlock() && !isExcludedFromConfiscating(m)) {
                // Replaces it with a new item, but without NBT data
                e.setCursor(new ItemStack(is.getType(), is.getAmount()));
            }
        }

        if (i != null) {
            InventoryHolder h = i.getHolder();

            // Check if clicked inventory isn't player's own inventory
            if (i.getType() != InventoryType.PLAYER && (h instanceof Player) &&
                    ((Player) h).getName().equalsIgnoreCase(p.getName()))
                return;
        }

        // Checks if cursor item exists
        // Must be after middle-click check, because "is" needs specific cursor itemstack
        if (is.getType() == Material.AIR)
            is = e.getCurrentItem();

        if (is == null || is.getType() == Material.AIR)
            return;

        // Confiscate
        if (new ConfiscationHelper(plugin).shouldConfiscate(p, is)) {
            e.setCurrentItem(new ItemStack(Material.AIR));
            e.setCursor(new ItemStack(Material.AIR));
            e.setResult(Result.DENY);
            e.setCancelled(true);
        }
    }

    /*
     * Represents a player related inventory event
     */
    @EventHandler(ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent e) {
        Player p = (Player) e.getPlayer();

        if (new PreconditionChecker(plugin, p)
                .isWorldAllowed(p.getWorld().getName())
                .isGameMode(GameMode.CREATIVE)
                .anyFailed() && !plugin.config.limitations.interaction.inventories)
            return;

        // No need to control bypassed players
        if (p.hasPermission("rc.bypass.limit.interact.inventories") ||
                p.hasPermission("rc.bypass.limit.interact.inventories." + e.getView().getType()))
            return;

        e.setCancelled(true);
        plugin.messagingUtils.sendMessage(p, true, plugin.messages.disabled.container);
    }

    private boolean armorIsEquipped(ItemStack[] armorContents) {
        for (ItemStack is : armorContents)
            if (is == null)
                return false;

        return true;
    }

    private boolean isExcludedFromConfiscating(Material m) {
        return plugin.config.confiscate.middleClick.excluded.contains(m) || m == Material.AIR;
    }
}
