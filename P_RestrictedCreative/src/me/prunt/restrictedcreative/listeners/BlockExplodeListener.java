package me.prunt.restrictedcreative.listeners;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.storage.DataHandler;

public class BlockExplodeListener implements Listener {
    private Main main;

    public BlockExplodeListener(Main main) {
	this.main = main;
    }

    private Main getMain() {
	return this.main;
    }

    /*
     * Called when a block explodes
     */
    // HIGHEST required for WorldGuard and similar plugins
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockExplode(BlockExplodeEvent e) {
	// No need to control blocks in disabled worlds
	if (getMain().getUtils().isDisabledWorld(e.getBlock().getWorld().getName()))
	    return;

	// Loops through all broken blocks
	for (Block b : e.blockList()) {
	    if (getMain().getUtils().isExcluded(b.getType()))
		continue;

	    if (!DataHandler.isTracked(b))
		continue;

	    DataHandler.breakBlock(b, null);
	}
    }

    /*
     * Called when an entity explodes
     */
    // HIGHEST required for WorldGuard and similar plugins
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent e) {
	// No need to control blocks in disabled worlds
	if (getMain().getUtils().isDisabledWorld(e.getEntity().getWorld().getName()))
	    return;

	// Loops through all broken blocks
	for (Block bl : e.blockList()) {
	    if (getMain().getUtils().isExcluded(bl.getType()))
		continue;

	    if (!DataHandler.isTracked(bl))
		continue;

	    DataHandler.breakBlock(bl, null);
	}
    }
}
