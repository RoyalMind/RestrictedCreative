package me.prunt.restrictedcreative.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Bed.Part;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.PistonHead;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.storage.DataHandler;
import me.prunt.restrictedcreative.storage.MaterialHandler;
import me.prunt.restrictedcreative.utils.Utils;

public class BlockBreakListener implements Listener {
    private Main main;

    public BlockBreakListener(Main main) {
	this.main = main;
    }

    private Main getMain() {
	return this.main;
    }

    /*
     * Called when a block is broken by a player.
     * 
     * Note: Plugins wanting to simulate a traditional block drop should set the
     * block to air and utilize their own methods for determining what the default
     * drop for the block being broken is and what to do about it, if anything.
     * 
     * If a Block Break event is cancelled, the block will not break and experience
     * will not drop.
     */
    // HIGHEST required for WorldGuard and similar plugins
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
	Player p = e.getPlayer();
	Block b = e.getBlock();
	BlockData bd = b.getBlockData();
	Material m = b.getType();

	// No need to control blocks in disabled worlds
	if (getMain().isDisabledWorld(p.getWorld().getName()))
	    return;

	// No need to control excluded blocks
	if (getMain().isExcluded(b.getType()))
	    return;

	// No need to control non-tracked blocks
	if (!DataHandler.isTracked(b))
	    return;

	/* Disabled blocks for creative players */
	if (p.getGameMode() == GameMode.CREATIVE && getMain().isDisabledBreaking(m)
		&& !p.hasPermission("rc.bypass.disable.breaking")
		&& !p.hasPermission("rc.bypass.disable.breaking." + m)) {
	    main.sendMessage(p, true, "disabled.general");
	    e.setCancelled(true);
	    return;
	}

	// Door
	if (bd instanceof Door) {
	    Door door = (Door) bd;
	    Block bl;

	    if (door.getHalf() == Half.TOP) {
		bl = b.getRelative(BlockFace.DOWN);
	    } else {
		bl = b.getRelative(BlockFace.UP);
	    }

	    remove(e, p, false, bl);
	}

	// Bed
	else if (bd instanceof Bed) {
	    Bed bed = (Bed) bd;
	    Block bl;

	    if (bed.getPart() == Part.HEAD) {
		bl = b.getRelative(bed.getFacing().getOppositeFace());
	    } else {
		bl = b.getRelative(bed.getFacing());
	    }

	    remove(e, p, false, bl);
	}

	// Piston head
	else if (bd instanceof PistonHead) {
	    PistonHead head = (PistonHead) bd;
	    Block piston = b.getRelative(head.getFacing().getOppositeFace());

	    remove(e, p, false, piston);
	}

	// Double plant
	else if (MaterialHandler.isDoublePlant(b)) {
	    Block bl = b.getRelative(BlockFace.DOWN);

	    // If block below isn't a double plant, the one above must be
	    if (!MaterialHandler.isDoublePlant(bl))
		bl = b.getRelative(BlockFace.UP);

	    remove(e, p, false, bl);
	}

	// Others
	remove(e, p, true, b);
    }

    private void remove(BlockBreakEvent e, Player p, boolean update, Block... blocks) {
	if (Utils.isInstalled("Jobs") || Utils.isInstalled("mcMMO")) {
	    for (Block b : blocks)
		DataHandler.breakBlock(b, p, update);

	    e.setCancelled(true);
	} else {
	    for (Block b : blocks)
		DataHandler.removeTracking(b);

	    e.setDropItems(false);
	    e.setExpToDrop(0);
	}
    }
}
