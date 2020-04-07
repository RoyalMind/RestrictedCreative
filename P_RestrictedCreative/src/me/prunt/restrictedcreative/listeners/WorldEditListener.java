package me.prunt.restrictedcreative.listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.storage.DataHandler;

public class WorldEditListener {
	private Main main;

	public WorldEditListener(Main main) {
		this.main = main;

		if (Main.DEBUG)
			System.out.println("Loaded WorldEditListener");
	}

	@Subscribe
	public void wrapForLogging(EditSessionEvent e) {
		if (Main.DEBUG)
			System.out.println("wrapForLogging");

		Actor a = e.getActor();

		if (a == null || !a.isPlayer() || e.getWorld() == null)
			return;

		Player p = Bukkit.getServer().getPlayer(a.getUniqueId());
		String world = e.getWorld().getName();
		World w = main.getServer().getWorld(world);

		if (main.getUtils().isDisabledWorld(world))
			return;

		e.setExtent(new AbstractDelegateExtent(e.getExtent()) {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public boolean setBlock(BlockVector3 position, BlockStateHolder newBlock) throws WorldEditException {
				Block b = w.getBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ());

				// If a tracked block is removed
				if (newBlock.getBlockType().getMaterial().isAir()) {
					DataHandler.removeTracking(b);
				}
				// The block is changed/placed
				else if (!p.hasPermission("rc.bypass.tracking.worldedit")
						&& (main.getSettings().isEnabled("tracking.worldedit.extended")
								|| p.getGameMode() == GameMode.CREATIVE)) {
					DataHandler.setAsTracked(b);
				}

				return getExtent().setBlock(position, newBlock);
			}
		});
	}
}
