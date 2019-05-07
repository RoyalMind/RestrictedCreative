package me.prunt.restrictedcreative.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onBlockUpdate(BlockPhysicsEvent e) {
	Block b = e.getBlock();

	// No need to control non-tracked blocks
	if (!DataHandler.isTracked(b))
	    return;

	// No need to control blocks in disabled worlds
	if (getMain().getUtils().isDisabledWorld(b.getWorld().getName()))
	    return;

	Material m = b.getType();

	// No need to control excluded blocks
	if (getMain().getUtils().isExcluded(m))
	    return;

	BlockData bd = b.getBlockData();
	Block bl = b.getRelative(BlockFace.DOWN);
	Material ma = bl.getType();

	if (Main.DEBUG && Main.EXTRADEBUG)
	    System.out.println("onBlockUpdate: " + m + " (" + ma + ")");

	/* 1.-2. Rail */
	if (bd instanceof Rail) {
	    // If the block below the rail is solid
	    // and if rail is on slope, there's a block to support on
	    if (isSolid(bl) && isSlopeOk(b)) {
		e.setCancelled(true);
		DataHandler.breakBlock(b, null);
	    }
	    return;
	}

	/* 1.-2. Chorus */
	if (m == Material.CHORUS_PLANT) {
	    if (!isChorusOk(b)) {
		e.setCancelled(true);
		DataHandler.breakBlock(b, null);

		if (Main.DEBUG)
		    System.out.println("isChorusOk: false");
	    }
	    return;
	}

	/* 3. Top blocks */
	if (MaterialHandler.needsBlockBelow(b)) {
	    if (Main.DEBUG)
		System.out.println("needsBlockBelow");

	    // Needs to be checked BEFORE isSolid()
	    if (Bukkit.getVersion().contains("1.14") && m == Material.valueOf("BAMBOO")) {
		if (!isBambooOk(b)) {
		    e.setCancelled(true);
		    DataHandler.breakBlock(b, null);
		}
		return;
	    }

	    // Needs to be checked BEFORE isSolid()
	    switch (m) {
	    case LILY_PAD:
		if (ma != Material.WATER) {
		    e.setCancelled(true);
		    DataHandler.breakBlock(b, null);
		}
		return;
	    case KELP:
	    case KELP_PLANT:
		if (!isKelpOk(b)) {
		    e.setCancelled(true);
		    DataHandler.breakBlock(b, null);
		}
		return;
	    case CACTUS:
		if (!isCactusOk(b)) {
		    e.setCancelled(true);
		    DataHandler.breakBlock(b, null);
		}
		return;
	    case SUGAR_CANE:
		if (!isSugarCaneOk(b)) {
		    e.setCancelled(true);
		    DataHandler.breakBlock(b, null);
		}
		return;
	    default:
		break;
	    }

	    // Needs to be checked BEFORE isSolid()
	    if (MaterialHandler.isDoublePlant(b)) {
		if (!isDoublePlantOk(b)) {
		    e.setCancelled(true);
		    DataHandler.breakBlock(b, null);
		}
		return;
	    }

	    // Needs to be checked BEFORE isSolid()
	    if (MaterialHandler.isCarpet(b)) {
		if (isBelowEmpty(b)) {
		    e.setCancelled(true);
		    DataHandler.breakBlock(b, null);
		}
		return;
	    }

	    // Needs to be checked BEFORE isSolid()
	    if (MaterialHandler.isCrop(b)) {
		if (!isLightingOk(b) || !isSolid(bl)) {
		    e.setCancelled(true);
		    DataHandler.breakBlock(b, null);
		}
		return;
	    }

	    if (!isSolid(bl)) {
		e.setCancelled(true);
		DataHandler.breakBlock(b, null);
	    }
	    return;
	}

	/* 4. Attachable */
	BlockFace bf = MaterialHandler.getNeededFace(b);
	if (bf != null) {
	    bl = b.getRelative(bf);

	    if (Main.DEBUG)
		System.out.println("getNeededFace: " + m + " " + b.getFace(bl));

	    // If the block (to which the first block is attached to) is solid
	    if (!isSolid(bl)) {
		e.setCancelled(true);
		DataHandler.breakBlock(b, null);
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

    private boolean isChorusOk(Block b) {
	List<Block> horisontalChoruses = horisontalChoruses(b);
	boolean validHorisontalChorusExists = validHorisontalChorusExists(horisontalChoruses);

	// Chorus plant will break unless the block below is chorus plant or end stone
	// or any horizontally adjacent block is a chorus plant above chorus plant or
	// end stone
	if (!isBelowChorusOk(b) && !validHorisontalChorusExists)
	    return false;

	boolean isVerticalOk = b.getRelative(BlockFace.UP).getType() == Material.AIR
		|| b.getRelative(BlockFace.DOWN).getType() == Material.AIR;

	// Chorus plant with at least one other chorus plant horizontally adjacent will
	// break unless at least one of the vertically adjacent blocks is air
	return horisontalChoruses.isEmpty() || isVerticalOk;
    }

    private boolean isBelowChorusOk(Block b) {
	Material m = b.getRelative(BlockFace.DOWN).getType();
	return m == Material.END_STONE || m == Material.CHORUS_PLANT;
    }

    private boolean isSolid(Block b) {
	Material m = b.getType();
	return m.isSolid() && m != Material.PISTON && m != Material.STICKY_PISTON;
    }

    private List<Block> horisontalChoruses(Block b) {
	List<Block> choruses = new ArrayList<>();

	for (BlockFace bf : horisontal) {
	    Block bl = b.getRelative(bf);

	    if (bl.getType() == Material.CHORUS_PLANT)
		choruses.add(bl);
	}

	return choruses;
    }

    private boolean validHorisontalChorusExists(List<Block> blocks) {
	for (Block b : blocks) {
	    if (isBelowChorusOk(b))
		return true;
	}

	return false;
    }

    private boolean isLightingOk(Block b) {
	return b.getLightFromSky() >= 5 || b.getLightFromBlocks() >= 8;
    }

    private boolean isCactusOk(Block b) {
	boolean nothingAround = isAroundCactusOk(b.getRelative(BlockFace.EAST))
		&& isAroundCactusOk(b.getRelative(BlockFace.WEST)) && isAroundCactusOk(b.getRelative(BlockFace.NORTH))
		&& isAroundCactusOk(b.getRelative(BlockFace.SOUTH));

	Material m = b.getRelative(BlockFace.DOWN).getType();
	boolean belowOk = m == Material.SAND || m == Material.CACTUS;

	return nothingAround && belowOk;
    }

    private boolean isAroundCactusOk(Block b) {
	Material m = b.getType();

	if (Main.DEBUG)
	    System.out.println("isAroundCactusOk: " + m);

	return m == Material.AIR || m == Material.WATER;
    }

    private boolean isSugarCaneOk(Block b) {
	Block bl = b.getRelative(BlockFace.DOWN);
	Material ma = bl.getType();

	if (ma == Material.SUGAR_CANE)
	    return true;

	boolean soil = ma == Material.GRASS || ma == Material.DIRT || ma == Material.SAND || ma == Material.PODZOL
		|| ma == Material.COARSE_DIRT || ma == Material.RED_SAND;

	Material east = bl.getRelative(BlockFace.EAST).getType();
	Material west = bl.getRelative(BlockFace.WEST).getType();
	Material north = bl.getRelative(BlockFace.NORTH).getType();
	Material south = bl.getRelative(BlockFace.SOUTH).getType();

	boolean water = east == Material.WATER || west == Material.WATER || north == Material.WATER
		|| south == Material.WATER;
	boolean frosted_ice = east == Material.FROSTED_ICE || west == Material.FROSTED_ICE
		|| north == Material.FROSTED_ICE || south == Material.FROSTED_ICE;

	return soil && (water || frosted_ice);
    }

    private boolean isBambooOk(Block b) {
	Block bl = b.getRelative(BlockFace.DOWN);
	Material ma = bl.getType();

	if (ma == Material.valueOf("BAMBOO"))
	    return true;

	boolean isSoilOk = ma == Material.GRASS || ma == Material.DIRT || ma == Material.SAND || ma == Material.PODZOL
		|| ma == Material.COARSE_DIRT || ma == Material.RED_SAND || ma == Material.GRAVEL
		|| ma == Material.MYCELIUM;

	return isSoilOk;
    }

    private boolean isKelpOk(Block b) {
	Block bl = b.getRelative(BlockFace.DOWN);
	Material m = bl.getType();

	return m == Material.KELP || m == Material.KELP_PLANT || isSolid(bl);
    }

    private boolean isDoublePlantOk(Block b) {
	// Both up and down must be OK
	if (isSolid(b.getRelative(BlockFace.DOWN)))
	    return b.getRelative(BlockFace.UP).getType() == b.getType();

	// Below is not solid
	return b.getRelative(BlockFace.DOWN).getType() == b.getType();
    }

    private boolean isBelowEmpty(Block b) {
	return b.getRelative(BlockFace.DOWN).isEmpty();
    }
}
