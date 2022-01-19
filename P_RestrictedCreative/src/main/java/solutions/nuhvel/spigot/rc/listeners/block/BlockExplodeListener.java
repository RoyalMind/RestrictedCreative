package solutions.nuhvel.spigot.rc.listeners.block;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.utils.helpers.PreconditionChecker;

import java.util.List;

public class BlockExplodeListener implements Listener {
    private final RestrictedCreative plugin;

    public BlockExplodeListener(RestrictedCreative plugin) {
        this.plugin = plugin;
    }

    /*
     * Called when a block explodes
     */
    // HIGHEST required for WorldGuard and similar plugins
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockExplode(BlockExplodeEvent e) {
        onExplode(e.getBlock().getWorld(), e.blockList());
    }

    /*
     * Called when an entity explodes
     */
    // HIGHEST required for WorldGuard and similar plugins
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent e) {
        onExplode(e.getEntity().getWorld(), e.blockList());
    }

    private void onExplode(World world, List<Block> blocks) {
        if (new PreconditionChecker(plugin).isWorldAllowed(world.getName()).anyFailed())
            return;

        for (Block b : blocks) {
            if (new PreconditionChecker(plugin).isTrackingAllowed(b.getType()).isTracked(b).allSucceeded())
                continue;

            plugin.trackableHandler.breakBlock(b);
        }
    }
}
