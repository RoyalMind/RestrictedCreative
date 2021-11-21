package solutions.nuhvel.spigot.rc.listeners.player;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.utils.helpers.PreconditionChecker;

public class PlayerItemListener implements Listener {
    private final RestrictedCreative plugin;

    public PlayerItemListener(RestrictedCreative plugin) {
        this.plugin = plugin;
    }

    /*
     * Called when a player empties a bucket
     */
    // HIGHEST required for WorldGuard and similar plugins
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent e) {
        Player p = e.getPlayer();
        Block b = e.getBlockClicked().getRelative(e.getBlockFace());
        Material m = b.getType();

        if (new PreconditionChecker(plugin, p)
                .isWorldAllowed(p.getWorld().getName())
                .isTrackingAllowed(m)
                .isTracked(b)
                .anyFailed())
            return;

        // Waterloggable blocks don't drop when filled with water
        if (e.getBlockClicked().getBlockData() instanceof Waterlogged)
            return;

        // This prevents players from pouring liquids directly onto tracked items
        // and thus getting the drops
        e.setCancelled(true);
    }

    /*
     * Thrown when a player drops an item from their inventory
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        Material m = e.getItemDrop().getItemStack().getType();

        if (new PreconditionChecker(plugin, p)
                .isWorldAllowed(p.getWorld().getName())
                .isGameMode(GameMode.CREATIVE)
                .isDroppingForbidden(m)
                .anyFailed())
            return;

        e.setCancelled(true);
        p.updateInventory();
        plugin.messagingUtils.sendMessage(p, true, plugin.messages.disabled.dropping);
    }

    /*
     * Thrown when a entity picks an item up from the ground
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerPickupItem(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player p))
            return;

        Material m = e.getItem().getItemStack().getType();

        if (new PreconditionChecker(plugin, p)
                .isWorldAllowed(p.getWorld().getName())
                .isGameMode(GameMode.CREATIVE)
                .isPickupForbidden(m)
                .anyFailed())
            return;

        e.setCancelled(true);
    }
}
