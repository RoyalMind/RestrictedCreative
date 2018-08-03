package me.prunt.restrictedcreative.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.storage.DataHandler;
import me.prunt.restrictedcreative.utils.MaterialHandler;

public class PlayerItemListener implements Listener {
    private Main main;

    public PlayerItemListener(Main main) {
	this.main = main;
    }

    private Main getMain() {
	return this.main;
    }

    /*
     * Called when a player empties a bucket
     */
    // HIGHEST required for WorldGuard and similar plugins
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent e) {
	Player p = e.getPlayer();
	Block b = e.getBlockClicked();
	Material m = b.getType();

	// No need to control buckets in disabled worlds
	if (getMain().getUtils().isDisabledWorld(p.getWorld().getName()))
	    return;

	// No need to control bypassed players
	if (p.hasPermission("rc.bypass.tracking.blocks") || p.hasPermission("rc.bypass.tracking.blocks." + m))
	    return;

	// The clicked block is a regular block,
	// not a placed item (torch, pressure plate etc)
	if (MaterialHandler.isOccluding(m))
	    // Gets the block on the side on which player clicked on,
	    // that's where the liquid will be placed
	    b = e.getBlockClicked().getRelative(e.getBlockFace());

	// This prevents players from pouring liquids directly onto tracked items
	// and thus getting the drops
	if (DataHandler.isTracked(b))
	    e.setCancelled(true);
    }

    /*
     * Thrown when a player drops an item from their inventory
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent e) {
	Player p = e.getPlayer();

	// No need to control drops in disabled worlds
	if (getMain().getUtils().isDisabledWorld(p.getWorld().getName()))
	    return;

	// No need to control non-creative players
	if (p.getGameMode() != GameMode.CREATIVE)
	    return;

	// No need to control disabled features
	if (!getMain().getSettings().isEnabled("limit.item.drop"))
	    return;

	// No need to control bypassed players
	if (p.hasPermission("rc.bypass.limit.item.drop")
		|| p.hasPermission("rc.bypass.limit.item.drop." + e.getItemDrop().getItemStack().getType()))
	    return;

	e.setCancelled(true);
	getMain().getUtils().sendMessage(p, true, "disabled.general");
    }

    /*
     * Thrown when a entity picks an item up from the ground
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerPickupItem(EntityPickupItemEvent e) {
	if (!(e.getEntity() instanceof Player))
	    return;

	Player p = (Player) e.getEntity();

	// No need to control pickups in disabled worlds
	if (getMain().getUtils().isDisabledWorld(p.getWorld().getName()))
	    return;

	// No need to control non-creative players
	if (p.getGameMode() != GameMode.CREATIVE)
	    return;

	// No need to control disabled features
	if (!getMain().getSettings().isEnabled("limit.item.pickup"))
	    return;

	// No need to control bypassed players
	if (p.hasPermission("rc.bypass.limit.item.pickup")
		|| p.hasPermission("rc.bypass.limit.item.pickup." + e.getItem().getItemStack().getType()))
	    return;

	e.setCancelled(true);
    }
}
