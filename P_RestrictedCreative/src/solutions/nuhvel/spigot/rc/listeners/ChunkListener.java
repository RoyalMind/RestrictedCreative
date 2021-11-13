package solutions.nuhvel.spigot.rc.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.storage.handlers.BlockHandler;
import solutions.nuhvel.spigot.rc.utils.Utils;

public class ChunkListener implements Listener {
	/*
	 * Called when a chunk is loaded
	 */
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent e) {
		// New chunks can't contain creative blocks
		if (e.isNewChunk())
			return;

		if (RestrictedCreative.EXTRADEBUG)
			System.out.println("onChunkLoad: " + Utils.getChunkString(e.getChunk()));

		// BlockHandler.loadChunkFromDatabase(e.getChunk());
		BlockHandler.loadBlocks(e.getChunk());
	}
}
