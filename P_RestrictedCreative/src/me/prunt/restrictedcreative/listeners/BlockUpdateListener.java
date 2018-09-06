package me.prunt.restrictedcreative.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.storage.DataHandler;
import me.prunt.restrictedcreative.utils.MaterialHandler;

public class BlockUpdateListener implements Listener {
    private Main main;

    BlockFace[] horisontal = { BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH };

    public BlockUpdateListener(Main main) {
	this.main = main;
    }

    private Main getMain() {
	return this.main;
    }

    /*
     * Thrown when a block physics check is called
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockUpdate(BlockPhysicsEvent e) {
	Block b = e.getBlock();
	BlockData bd = b.getBlockData();

	// No need to control blocks in disabled worlds
	if (getMain().getUtils().isDisabledWorld(b.getWorld().getName()))
	    return;

	// No need to control excluded blocks
	if (getMain().getUtils().isExcluded(b.getType()))
	    return;

	// No need to control non-tracked blocks
	if (!DataHandler.isTracked(b))
	    return;

	/* Rail */
	if (bd instanceof Rail) {
	    Block bl = b.getRelative(BlockFace.DOWN);

	    // If the block below the rail is solid
	    // and if rail is on slope, there's a block to support on
	    if (isSolid(bl) && isSlopeOk(b))
		return;

	    e.setCancelled(true);
	    DataHandler.breakBlock(b, null);
	    return;
	}

	/* Chorus */
	else if (b.getType() == Material.CHORUS_PLANT) {
	    if (!willChorusDrop(b))
		return;

	    e.setCancelled(true);
	    DataHandler.breakBlock(b, null, true);
	    return;
	}

	/* Top blocks */
	if (MaterialHandler.needsBlockBelow(b)) {
	    Block bl = b.getRelative(BlockFace.DOWN);
	    Material m = b.getType();
	    Material ma = bl.getType();

	    // Needs to be checked BEFORE isSolid()
	    if (m == Material.LILY_PAD && ma != Material.WATER) {
		e.setCancelled(true);
		DataHandler.breakBlock(b, null);
		return;
	    }

	    // Needs to be checked BEFORE isSolid()
	    if (m == Material.CACTUS && isCactusOk(b))
		return;

	    // Needs to be checked BEFORE isSolid()
	    if (m == Material.SUGAR_CANE && isSugarCaneOk(b))
		return;

	    if (isSolid(bl))
		return;

	    // Needs to be checked AFTER isSolid()
	    if (MaterialHandler.isCrop(b) && isLightingOk(b))
		return;

	    e.setCancelled(true);
	    DataHandler.breakBlock(b, null);
	    return;
	}

	/* Attachable */
	else if (MaterialHandler.getNeededFace(b) != null) {
	    Block bl = b.getRelative(MaterialHandler.getNeededFace(b));

	    // If the block (to which the first block is attached to) is solid
	    if (isSolid(bl))
		return;

	    e.setCancelled(true);
	    DataHandler.breakBlock(b, null);
	    return;
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

    private boolean willChorusDrop(Block b) {
	boolean isVerticalEmpty = b.getRelative(BlockFace.UP).getType() == Material.AIR
		|| b.getRelative(BlockFace.DOWN).getType() == Material.AIR;

	List<Block> horisontals = getValidHorisontalChoruses(b);

	if (!isBelowChorusOk(b) && horisontals.isEmpty())
	    return true;

	if (!horisontals.isEmpty() && !isVerticalEmpty)
	    return true;

	return false;
    }

    private boolean isBelowChorusOk(Block b) {
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

	    if (bl.getType() == Material.CHORUS_PLANT && isBelowChorusOk(bl)) {
		list.add(bl);
		break;
	    }
	}

	return list;
    }

    private boolean isLightingOk(Block b) {
	return b.getLightFromSky() >= 5 || b.getLightFromBlocks() >= 8;
    }

    private boolean isCactusOk(Block b) {
	boolean nothingAround = b.getRelative(BlockFace.EAST).isEmpty() && b.getRelative(BlockFace.WEST).isEmpty()
		&& b.getRelative(BlockFace.NORTH).isEmpty() && b.getRelative(BlockFace.SOUTH).isEmpty();
	boolean belowOk = b.getRelative(BlockFace.DOWN).getType() == Material.SAND
		|| b.getRelative(BlockFace.DOWN).getType() == Material.CACTUS;

	return nothingAround && belowOk;
    }

    private boolean isSugarCaneOk(Block b) {
	Block bl = b.getRelative(BlockFace.DOWN);

	if (bl.getType() == Material.SUGAR_CANE)
	    return true;

	return bl.getRelative(BlockFace.EAST).getType() == Material.WATER
		|| bl.getRelative(BlockFace.WEST).getType() == Material.WATER
		|| bl.getRelative(BlockFace.NORTH).getType() == Material.WATER
		|| bl.getRelative(BlockFace.SOUTH).getType() == Material.WATER;
    }
}
