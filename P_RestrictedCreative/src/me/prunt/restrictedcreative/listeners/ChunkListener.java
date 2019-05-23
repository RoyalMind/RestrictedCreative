package me.prunt.restrictedcreative.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import me.prunt.restrictedcreative.storage.DataHandler;

public class ChunkListener implements Listener {
    /*
     * Called when a chunk is loaded
     */
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
	DataHandler.loadBlocks(e.getChunk());
    }
}
