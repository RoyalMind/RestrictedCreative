package me.prunt.restrictedcreative.utils;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.world.block.BaseBlock;

import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.storage.DataHandler;

public class WorldEditLogger extends AbstractDelegateExtent {
    private Player p;
    private Main main;

    public WorldEditLogger(Main main, Player p, Extent extent) {
	super(extent);
	this.p = p;
	this.main = main;
    }

    protected void onBlockChange(Vector position, BaseBlock newBlock) {
	Block b = p.getWorld().getBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ());

	if (!DataHandler.isTracked(b))
	    return;

	// If the block is removed
	if (newBlock.getBlockType().getMaterial().isAir()) {
	    DataHandler.removeTracking(b);
	    return;
	}
	// The block is changed/placed

	if (p.hasPermission("rc.bypass.tracking.worldedit")
		|| (!main.getSettings().isEnabled("tracking.wordedit.extended")
			&& p.getGameMode() != GameMode.CREATIVE))
	    return;

	DataHandler.setAsTracked(b);
    }
}
