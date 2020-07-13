package me.prunt.restrictedcreative.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.storage.handlers.BlockHandler;
import me.prunt.restrictedcreative.utils.Utils;

public class ChunkListener implements Listener {
	/*
	 * Called when a chunk is loaded
	 */
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent e) {
		// New chunks can't contain creative blocks
		if (e.isNewChunk())
			return;

		if (Main.EXTRADEBUG)
			System.out.println("onChunkLoad: " + Utils.getChunkString(e.getChunk()));

		// BlockHandler.loadChunkFromDatabase(e.getChunk());
		BlockHandler.loadBlocks(e.getChunk());
	}
}
