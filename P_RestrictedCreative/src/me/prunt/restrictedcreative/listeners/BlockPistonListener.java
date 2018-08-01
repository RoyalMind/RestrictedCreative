package me.prunt.restrictedcreative.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.storage.DataHandler;

public class BlockPistonListener implements Listener {
    private Main main;

    public BlockPistonListener(Main main) {
	this.main = main;
    }

    private Main getMain() {
	return this.main;
    }

    /*
     * Called when a piston retracts
     */
    // HIGHEST required for WorldGuard and similar plugins
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockPull(BlockPistonRetractEvent e) {
	// No need to control blocks in disabled worlds
	if (getMain().isDisabledWorld(e.getBlock().getWorld().getName()))
	    return;

	// Non-sticky piston doesn't pull anything
	if (!e.isSticky())
	    return;

	// Loop through pulled blocks
	for (Block b : e.getBlocks()) {
	    if (!DataHandler.isTracked(b))
		return;

	    if (b.getPistonMoveReaction() == PistonMoveReaction.BREAK)
		DataHandler.breakBlock(b, null, true);
	}
    }

    /*
     * Called when a piston extends
     */
    // HIGHEST required for WorldGuard and similar plugins
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockPush(BlockPistonExtendEvent e) {
	// No need to control blocks in disabled worlds
	if (getMain().isDisabledWorld(e.getBlock().getWorld().getName()))
	    return;

	List<Block> newBlocks = new ArrayList<>();
	List<Block> oldBlocks = new ArrayList<>();

	// Loop through pushed blocks
	for (Block b : e.getBlocks()) {
	    if (!DataHandler.isTracked(b))
		return;

	    if (b.getPistonMoveReaction() == PistonMoveReaction.BREAK)
		DataHandler.breakBlock(b, null, true);

	    oldBlocks.add(b);
	    newBlocks.add(b.getRelative(e.getDirection()));
	}

	// Remove old blocks first
	for (Block b : oldBlocks) {
	    DataHandler.removeTracking(b);
	}
	// Add new blocks afterwards
	for (Block b : newBlocks) {
	    DataHandler.setAsTracked(b);
	}
    }
}
