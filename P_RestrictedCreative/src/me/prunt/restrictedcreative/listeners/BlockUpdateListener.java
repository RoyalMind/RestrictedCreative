package me.prunt.restrictedcreative.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.material.Attachable;
import org.bukkit.material.MaterialData;

import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.storage.DataHandler;
import me.prunt.restrictedcreative.storage.MaterialHandler;

@SuppressWarnings("deprecation")
public class BlockUpdateListener implements Listener {
    private Main main;

    BlockFace[] horisontal = { BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH };

    public BlockUpdateListener(Main main) {
	this.main = main;
    }

    private Main getMain() {
	return this.main;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockUpdate(BlockPhysicsEvent e) {
	Block b = e.getBlock();
	BlockData bd = b.getBlockData();
	MaterialData md = b.getState().getData();

	// No need to control blocks in disabled worlds
	if (main.isDisabledWorld(b.getWorld().getName()))
	    return;

	// No need to control excluded blocks
	if (getMain().isExcluded(b.getType()))
	    return;

	// No need to control non-tracked blocks
	if (!DataHandler.isCreative(b))
	    return;

	/* Top blocks */
	if (MaterialHandler.needsBlockBelow(b)) {

	}

	/* Rail */
	if (bd instanceof Rail) {
	    Block bl = b.getRelative(BlockFace.DOWN);

	    // If the block below the rail isn't solid
	    // or rail is on slope, but there's no block to support on
	    if (!isSolid(bl) || !isSlopeOk(b)) {
		e.setCancelled(true);
		DataHandler.breakBlock(b, null);
		return;
	    }
	}

	/* Chorus */
	else if (b.getType() == Material.CHORUS_PLANT) {
	    if (willDrop(b)) {
		DataHandler.breakBlock(b, null);
		return;
	    }
	}

	/* Attachable */
	else if (md instanceof Attachable) {
	    Attachable at = (Attachable) b.getState().getData();
	    Block bl = b.getRelative(at.getAttachedFace());

	    // Trapdoors can hold their own
	    if (bd instanceof TrapDoor)
		return;

	    // If the block (to which the first block is attached to) isn't solid
	    if (!bl.getType().isSolid() || bl.getType() == Material.PISTON || bl.getType() == Material.STICKY_PISTON) {
		e.setCancelled(true);
		DataHandler.breakBlock(b, null);
		return;
	    }

	}

	// If it should be on top of sth
	if (main.top.contains(b.getType())) {
	    // If it's a growing block
	    if (main.growers.contains(b.getType())) {
		// If the block (to which the first block is attached to) isn't solid
		if (!b.getRelative(BlockFace.DOWN).getType().isSolid()
			|| b.getRelative(BlockFace.DOWN).getType() == Material.PISTON_BASE
			|| b.getRelative(BlockFace.DOWN).getType() == Material.PISTON_STICKY_BASE) {
		    e.setCancelled(true);
		    main.breakBlock(b, null);
		    return;
		}
		// If there is something under the growing plant

		// If it's a cactus
		if (b.getType() == Material.CACTUS) {
		    // If there's a block besides the cactus
		    if (b.getRelative(BlockFace.EAST).getType() != Material.AIR
			    || b.getRelative(BlockFace.WEST).getType() != Material.AIR
			    || b.getRelative(BlockFace.NORTH).getType() != Material.AIR
			    || b.getRelative(BlockFace.SOUTH).getType() != Material.AIR) {
			e.setCancelled(true);
			main.breakBlock(b, null);
			return;
		    }

		    // It's a sugar cane
		} else {
		    // Blocked to prevent breaking when there's no water
		    // nearby
		    e.setCancelled(true);
		}

		return;
	    }

	    Block block = b.getRelative(BlockFace.DOWN);

	    // If it's a water lily
	    if (b.getType() == Material.WATER_LILY) {
		// If the block under it isn't water
		if (block.getType() != Material.STATIONARY_WATER) {
		    e.setCancelled(true);
		    main.breakBlock(b, null);
		    return;
		}
	    }

	    // If the block (to which the first block is attached to) isn't solid
	    if (!block.getType().isSolid() || block.getType() == Material.PISTON_BASE
		    || block.getType() == Material.PISTON_STICKY_BASE) {
		e.setCancelled(true);
		main.breakBlock(b, null);
		return;
	    }
	}
    }

    private boolean isSlopeOk(Block b) {
	Rail rail = (Rail) b.getBlockData();

	switch (rail.getShape()) {
	case ASCENDING_EAST:
	    return b.getRelative(BlockFace.EAST).getType().isSolid();
	case ASCENDING_NORTH:
	    return b.getRelative(BlockFace.NORTH).getType().isSolid();
	case ASCENDING_SOUTH:
	    return b.getRelative(BlockFace.SOUTH).getType().isSolid();
	case ASCENDING_WEST:
	    return b.getRelative(BlockFace.WEST).getType().isSolid();
	default:
	    return true;
	}
    }

    private boolean willDrop(Block b) {
	boolean isVerticalEmpty = b.getRelative(BlockFace.UP).getType() == Material.AIR
		|| b.getRelative(BlockFace.DOWN).getType() == Material.AIR;

	List<Block> horisontals = getValidHorisontalChoruses(b);

	if (!isBelowOk(b) && horisontals.isEmpty())
	    return true;

	if (!horisontals.isEmpty() && !isVerticalEmpty)
	    return true;

	return false;
    }

    private boolean isBelowOk(Block b) {
	return b.getRelative(BlockFace.DOWN).getType() == Material.END_STONE
		|| b.getRelative(BlockFace.DOWN).getType() == Material.CHORUS_PLANT;
    }

    private boolean isSolid(Block b) {
	return b.getType().isSolid() && b.getType() != Material.PISTON && b.getType() != Material.STICKY_PISTON;
    }

    private List<Block> getValidHorisontalChoruses(Block b) {
	List<Block> list = new ArrayList<>();

	for (BlockFace bf : horisontal) {
	    Block bl = b.getRelative(bf);

	    if (bl.getType() == Material.CHORUS_PLANT && isBelowOk(bl)) {
		list.add(bl);
		break;
	    }
	}

	return list;
    }
}
