package solutions.nuhvel.spigot.rc.listeners;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.storage.handlers.BlockHandler;

public class BlockExplodeListener implements Listener {
	private RestrictedCreative restrictedCreative;

	public BlockExplodeListener(RestrictedCreative restrictedCreative) {
		this.restrictedCreative = restrictedCreative;
	}

	private RestrictedCreative getMain() {
		return this.restrictedCreative;
	}

	/*
	 * Called when a block explodes
	 */
	// HIGHEST required for WorldGuard and similar plugins
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onBlockExplode(BlockExplodeEvent e) {
		// No need to control blocks in disabled worlds
		if (getMain().getUtils().isDisabledWorld(e.getBlock().getWorld().getName()))
			return;

		// Loops through all broken blocks
		for (Block b : e.blockList()) {
			if (getMain().getUtils().isExcludedFromTracking(b.getType()))
				continue;

			if (!BlockHandler.isTracked(b))
				continue;

			if (RestrictedCreative.DEBUG)
				System.out.println("onBlockExplode: " + b.getType());

			BlockHandler.breakBlock(b, null);
		}
	}

	/*
	 * Called when an entity explodes
	 */
	// HIGHEST required for WorldGuard and similar plugins
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onEntityExplode(EntityExplodeEvent e) {
		// No need to control blocks in disabled worlds
		if (getMain().getUtils().isDisabledWorld(e.getEntity().getWorld().getName()))
			return;

		// Loops through all broken blocks
		for (Block b : e.blockList()) {
			if (getMain().getUtils().isExcludedFromTracking(b.getType()))
				continue;

			if (!BlockHandler.isTracked(b))
				continue;

			if (RestrictedCreative.DEBUG)
				System.out.println("onEntityExplode: " + b.getType());

			BlockHandler.breakBlock(b, null);
		}
	}
}
