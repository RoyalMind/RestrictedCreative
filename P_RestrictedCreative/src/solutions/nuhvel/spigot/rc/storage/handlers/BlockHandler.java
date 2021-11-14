package solutions.nuhvel.spigot.rc.storage.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import solutions.nuhvel.spigot.rc.utils.external.CoreProtectUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.storage.database.DataSyncRunnable;
import solutions.nuhvel.spigot.rc.utils.external.BlocksHubUtils;
import solutions.nuhvel.spigot.rc.utils.Utils;

public class BlockHandler {
	public static boolean isLoadingDone = false;
	public static boolean usingAdvancedLoading = false;

	private static Map<String, Set<String>> blocksInChunk = new HashMap<>();
	private static int totalCount = -1;

	public static boolean isTracked(Block b) {
		if (b == null)
			return false;

		for (MetadataValue mdv : b.getMetadata("RC3")) {
			if (mdv.asBoolean())
				return true;
		}

		return false;
	}

	public static void breakBlock(Block b) {
		breakBlock(b, null);
	}

	public static void breakBlock(Block b, Player p) {
		breakBlock(b, p, true);
	}

	public static void breakBlock(Block b, Player p, boolean update) {
		if (Utils.isInstalled("CoreProtect")) {
			new CoreProtectUtils(b, p, update);
		} else if (Utils.isInstalled("BlocksHub")) {
			new BlocksHubUtils(b, p, update);
		} else {
			b.setType(Material.AIR, update);
		}

		removeTracking(b);
	}

	public static void setTotalCount(int totalCount) {
		BlockHandler.totalCount = totalCount;
	}

	public static String getTotalCount() {
		return String.valueOf(totalCount);
	}

	public static boolean isTrackedChunk(Chunk c) {
		return !getBlocksInChunk(c).isEmpty();
	}

	public static Set<String> getBlocksInChunk(Chunk c) {
		return getBlocksInChunk(Utils.getChunkString(c));
	}

	public static Set<String> getBlocksInChunk(String c) {
		return blocksInChunk.getOrDefault(c, new HashSet<>());
	}

	public static void removeBlocksInChunk(Chunk c) {
		removeBlocksInChunk(Utils.getChunkString(c));
	}

	public static void removeBlocksInChunk(String c) {
		blocksInChunk.remove(c);
	}

	public static void loadBlocks(Chunk c) {
		long start = System.currentTimeMillis();

		// No need to handle untracked chunks
		if (!isTrackedChunk(c))
			return;

		for (String block : getBlocksInChunk(c)) {
			Block b = Utils.getBlock(block);

			if (b == null || b.isEmpty()) {
				removeTracking(block);
			} else {
				b.setMetadata("GMC", RestrictedCreative.getFMV());
			}
		}

		removeBlocksInChunk(c);
	}

}
