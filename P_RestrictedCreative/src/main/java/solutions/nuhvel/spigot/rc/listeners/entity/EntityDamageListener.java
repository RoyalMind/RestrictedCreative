package solutions.nuhvel.spigot.rc.listeners.entity;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.storage.handlers.TrackableHandler;
import solutions.nuhvel.spigot.rc.utils.helpers.ArmorStandHelper;
import solutions.nuhvel.spigot.rc.utils.helpers.PreconditionChecker;

public class EntityDamageListener implements Listener {
    private final RestrictedCreative plugin;

    public EntityDamageListener(RestrictedCreative plugin) {
        this.plugin = plugin;
    }

    /*
     * Called when an entity is damaged by an entity
     */
    // HIGHEST required for WorldGuard and similar plugins
    // BEWARE - it was changed to HIGH in 2.3, but PlotSquared needs HIGHEST
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        Entity en = e.getEntity();

        if (new PreconditionChecker(plugin).isWorldAllowed(en.getWorld().getName()).anyFailed())
            return;

        // Item frame
        if (en instanceof ItemFrame frame) {
            ItemStack is = frame.getItem();

            // The item isn't going to pop off
            if (is.getType() == Material.AIR)
                return;

            // Item frame doesn't contain creative items
            if (!TrackableHandler.hasTrackedItem(frame))
                return;

            TrackableHandler.removeItem(frame);
        }

        // Armor stand
        if (en instanceof ArmorStand a) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (!TrackableHandler.isTracked(a, slot))
                    continue;

                EntityEquipment inv = a.getEquipment();
                if (inv == null)
                    continue;

                ItemStack air = new ItemStack(Material.AIR);

                ArmorStandHelper.removeItemFromArmorStand(a, slot, inv, air);
            }
        }

        // PVP and PVE
        if (e.getDamager() instanceof Player p) {
            // Only need to control creative players
            if (p.getGameMode() == GameMode.CREATIVE) {
                // PVE except armor stands
                if (en instanceof LivingEntity && !(en instanceof ArmorStand)) {
                    // Only need to control enabled features and non-bypassed players
                    if (new PreconditionChecker(plugin, p).isPlayerVersusEntityAllowed(en.getType()).anyFailed()) {
                        e.setCancelled(true);
                        plugin.messagingUtils.sendMessage(p, true, plugin.messages.disabled.pve);
                        return;
                    }
                }

                // PVP
                else if (en instanceof Player) {
                    // Only need to control enabled features and non-bypassed players
                    if (new PreconditionChecker(plugin, p).isPlayerVersusPlayerAllowed().anyFailed()) {
                        e.setCancelled(true);
                        plugin.messagingUtils.sendMessage(p, true, plugin.messages.disabled.pvp);
                        return;
                    }
                }
            }
        }

        // No need to control non-tracked entities
        if (new PreconditionChecker(plugin).isTracked(en).anyFailed())
            return;

        // Remove armor stands etc.
        en.remove();
        e.setCancelled(true);
    }

    /*
     * Raised when a vehicle is destroyed, which could be caused by either a player
     * or the environment. This is not raised if the boat is simply 'removed' due to
     * other means.
     */
    // HIGHEST required for PlotSquared and similar plugins
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onVehicleDestroy(VehicleDestroyEvent e) {
        Entity en = e.getVehicle();

        // No need to control entities in disabled worlds
        if (new PreconditionChecker(plugin).isWorldAllowed(en.getWorld().getName()).isTracked(en).anyFailed())
            return;

        // Remove boats, carts etc.
        en.remove();
        e.setCancelled(true);
    }

    /*
     * Triggered when a hanging entity is removed
     */
    // HIGHEST required for WorldGuard and similar plugins
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onHangingBreak(HangingBreakEvent e) {
        Entity en = e.getEntity();

        if (new PreconditionChecker(plugin).isWorldAllowed(en.getWorld().getName()).anyFailed())
            return;

        if (new PreconditionChecker(plugin).isTracked(en).anyFailed()) {
            if (en instanceof ItemFrame frame && TrackableHandler.hasTrackedItem(frame)) {
                en.remove();
                e.setCancelled(true);
            }

            return;
        }

        if (en instanceof ItemFrame frame && !frame.getItem().getType().isAir() &&
                !TrackableHandler.hasTrackedItem(frame)) {
            // Drop item frame content in the ground
            en.getWorld().dropItem(en.getLocation(), frame.getItem());
            en.remove();
        }

        en.remove();
        e.setCancelled(true);
    }
}
