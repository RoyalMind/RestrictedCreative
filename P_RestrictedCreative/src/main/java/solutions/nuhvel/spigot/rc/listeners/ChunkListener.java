package solutions.nuhvel.spigot.rc.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.utils.helpers.PreconditionChecker;

public class ChunkListener implements Listener {
    private final RestrictedCreative plugin;

    public ChunkListener(RestrictedCreative plugin) {
        this.plugin = plugin;
    }

    /*
     * Called when a chunk is loaded
     */
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        // New chunks can't contain creative blocks
        if (new PreconditionChecker(plugin).isWorldAllowed(e.getWorld().getName()).anyFailed() || e.isNewChunk())
            return;

        plugin.blockRepository.loadChunkFromDatabase(e.getChunk());
    }
}
