package me.prunt.restrictedcreative.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.storage.DataHandler;

public class BlockChangeListener implements Listener {
    private Main main;

    public BlockChangeListener(Main main) {
	this.main = main;
    }

    private Main getMain() {
	return this.main;
    }

    /*
     * Represents events with a source block and a destination block, currently only
     * applies to liquid (lava and water) and teleporting dragon eggs.
     *
     * If a Block From To event is cancelled, the block will not move (the liquid
     * will not flow).
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockChange(BlockFromToEvent e) {
	Block b = e.getToBlock();

	// No need to control blocks in disabled worlds
	if (getMain().getUtils().isDisabledWorld(b.getWorld().getName()))
	    return;

	// No need to control excluded blocks
	if (getMain().getUtils().isExcluded(b.getType()))
	    return;

	// Waterloggable blocks won't be affected by liquids
	if (b.getBlockData() instanceof Waterlogged)
	    return;

	// No need to control non-tracked blocks
	if (!DataHandler.isTracked(b))
	    return;

	if (Main.DEBUG)
	    System.out.println("onBlockChange: " + b.getType());

	// Removes, because otherwise a liquid would destroy it and drop the block
	e.setCancelled(true);
	DataHandler.breakBlock(b, null);
    }

    /*
     * Called when any Entity, excluding players, changes a block.
     */
    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
	Block b = e.getBlock();
	Entity en = e.getEntity();

	// No need to control blocks in disabled worlds
	if (getMain().getUtils().isDisabledWorld(b.getWorld().getName()))
	    return;

	// No need to control excluded blocks
	if (getMain().getUtils().isExcluded(b.getType()))
	    return;

	// No need to control non-tracked blocks and entities
	if (!DataHandler.isTracked(b) && !DataHandler.isTracked(en))
	    return;

	// Lily pad broken by boat
	if (b.getType() == Material.LILY_PAD) {
	    DataHandler.breakBlock(b, null);
	    return;
	}

	// Falling block transforming into regular block and vice versa
	else if (e.getEntityType() == EntityType.FALLING_BLOCK) {
	    // Regular block starts to fall and becomes FALLING_BLOCK
	    if (e.getTo() == Material.AIR) {
		DataHandler.removeTracking(b);
		DataHandler.setAsTracked(en);

		((FallingBlock) en).setDropItem(false);
	    }

	    // FALLING_BLOCK is transforming into regular block
	    else {
		DataHandler.setAsTracked(b);
	    }
	}
    }
}
