package me.prunt.restrictedcreative.listeners;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.logging.AbstractLoggingExtent;

import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.store.DataHandler;

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

	if (!DataHandler.isCreative(b))
	    return;

	// If the block is removed (0 == AIR)
	if (newBlock.getId() == 0) {
	    DataHandler.removeFromTracking(b);
	    return;
	}

	// The block is changed/placed

	if (p.hasPermission("rc.bypass.tracking.worldedit")
		|| (!main.getSettings().isEnabled("tracking.wordedit.extended")
			&& p.getGameMode() != GameMode.CREATIVE))
	    return;

	DataHandler.addForTracking(b);
    }
}
