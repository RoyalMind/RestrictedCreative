package solutions.nuhvel.spigot.rc.listeners;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.storage.handlers.BlockHandler;
import solutions.nuhvel.spigot.rc.utils.helpers.PreconditionChecker;

import java.util.List;

public class BlockExplodeListener implements Listener {
    private final RestrictedCreative plugin;

    public BlockExplodeListener(RestrictedCreative plugin) {
        this.plugin = plugin;
    }

    private RestrictedCreative getMain() {
        return this.plugin;
    }

    /*
     * Called when a block explodes
     */
    // HIGHEST required for WorldGuard and similar plugins
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockExplode(BlockExplodeEvent e) {
        if (new PreconditionChecker(plugin).isAllowedWorld(e.getBlock().getWorld().getName()).anyFailed())
            return;

        // Loops through all broken blocks
        breakTrackedBlocks(e.blockList());
    }

    /*
     * Called when an entity explodes
     */
    // HIGHEST required for WorldGuard and similar plugins
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent e) {
        if (new PreconditionChecker(plugin).isAllowedWorld(e.getEntity().getWorld().getName()).anyFailed())
            return;

        // Loops through all broken blocks
        breakTrackedBlocks(e.blockList());
    }

	private void breakTrackedBlocks(List<Block> blocks) {
		for (Block b : blocks) {
			if (new PreconditionChecker(plugin).isExcludedFromTracking(b.getType()).isTracked(b).allSucceeded())
				continue;

			BlockHandler.breakBlock(b);
		}
	}
}
