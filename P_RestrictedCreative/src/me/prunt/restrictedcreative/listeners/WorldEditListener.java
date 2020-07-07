package me.prunt.restrictedcreative.listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.EditSession.Stage;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.storage.handlers.BlockHandler;
import me.prunt.restrictedcreative.utils.Utils;

public class WorldEditListener {
	private Main main;

	public WorldEditListener(Main main) {
		this.main = main;

		if (Main.DEBUG)
			System.out.println("Loaded WorldEditListener");
	}

	@Subscribe
	public void wrapForLogging(EditSessionEvent e) {
		// Otherwise, this method is called 3 times for a single event
		if (e.getStage() != Stage.BEFORE_CHANGE)
			return;

		if (Main.DEBUG)
			System.out.println("wrapForLogging: " + e.getStage());

		Actor a = e.getActor();

		if (a == null || !a.isPlayer() || e.getWorld() == null)
			return;

		String world = e.getWorld().getName();

		if (main.getUtils().isDisabledWorld(world))
			return;

		Player p = Bukkit.getServer().getPlayer(a.getUniqueId());
		World w = main.getServer().getWorld(world);

		e.setExtent(new AbstractDelegateExtent(e.getExtent()) {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public boolean setBlock(BlockVector3 position, BlockStateHolder newBlock)
					throws WorldEditException {
				Block b = w.getBlockAt(position.getBlockX(), position.getBlockY(),
						position.getBlockZ());

				if (Main.EXTRADEBUG)
					System.out.println("setBlock: " + b.getType() + " " + Utils.getBlockString(b)
							+ ", " + newBlock.getAsString());

				// If a tracked block is removed
				if (newBlock.getBlockType().getMaterial().isAir()) {
					BlockHandler.removeTracking(b);
				}
				// The block is changed/placed
				else if (!p.hasPermission("rc.bypass.tracking.worldedit")
						&& (main.getSettings().isEnabled("tracking.worldedit.extended")
								|| p.getGameMode() == GameMode.CREATIVE)) {
					BlockHandler.setAsTracked(b);
				}

				return getExtent().setBlock(position, newBlock);
			}
		});
	}
}
