package me.prunt.restrictedcreative.listeners;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.logging.AbstractLoggingExtent;

import me.prunt.restrictedcreative.Main;

public class WELogger extends AbstractLoggingExtent {
    private Player p;
    private Main main;

    WELogger(Main main, Player p, Extent extent) {
	super(extent);
	this.p = p;
	this.main = main;
    }

    @Override
    protected void onBlockChange(Vector position, BaseBlock newBlock) {
	Block b = p.getWorld().getBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ());

	// If the block is removed (0 == AIR)
	if (newBlock.getId() == 0) {
	    // If it's tracked
	    if (Main.isCreative(b))
		Main.remove(b);

	    // If the block is changed/placed
	} else {
	    // Bypass + config check
	    if (p.hasPermission("rc.bypass.track.save-blocks")
		    || (!main.we_extended && p.getGameMode() != GameMode.CREATIVE))
		return;

	    // If it's not tracked
	    if (!Main.isCreative(b))
		Main.add(b);
	}
    }
}
