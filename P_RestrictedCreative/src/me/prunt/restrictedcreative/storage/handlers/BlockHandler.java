package me.prunt.restrictedcreative.storage.handlers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitTask;

import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.storage.SyncData;
import me.prunt.restrictedcreative.utils.BlocksHub;
import me.prunt.restrictedcreative.utils.Utils;

public class BlockHandler {
	public static List<String> addToDatabase = new ArrayList<>();
	public static List<String> removeFromDatabase = new ArrayList<>();
	public static boolean isLoadingDone = false;

	private static HashMap<String, char[][]> blocksInChunk = new HashMap<>();

	private static boolean usingSQLite = false;

	private static int totalCount = -1;
	private static int taskCount = 0;

	public static boolean isTracked(Block b) {
		if (b == null)
			return false;

		for (MetadataValue mdv : b.getMetadata("GMC")) {
			if (mdv.getOwningPlugin() == Main.getInstance() && mdv.asBoolean())
				return true;
		}

		return false;
	}

	public static void setAsTracked(Block b) {
		if (b == null || isTracked(b))
			return;

		b.setMetadata("GMC", Main.getFMV());
		addToDatabase.add(Utils.getBlockString(b));
		removeFromDatabase.remove(Utils.getBlockString(b));

		if (Main.EXTRADEBUG)
			System.out.println("setAsTracked: " + b.getType() + " " + Utils.getChunkString(b.getChunk()) + " vs "
					+ Utils.getBlockString(b));
	}

	public static void removeTracking(Block b) {
		if (b == null || !isTracked(b))
			return;

		b.removeMetadata("GMC", Main.getInstance());

		removeTracking(Utils.getBlockString(b));

		if (Main.EXTRADEBUG)
			System.out.println("removeTracking: " + b);
	}

	public static void removeTracking(String b) {
		addToDatabase.remove(b);
		removeFromDatabase.add(b);
	}

	public static void breakBlock(Block b, Player p) {
		breakBlock(b, p, true);
	}

	public static void breakBlock(Block b, Player p, boolean update) {
		// BlocksHub
		if (Utils.isInstalled("BlocksHub")) {
			new BlocksHub(b, p, update);
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

	public static boolean isUsingSQLite() {
		return usingSQLite;
	}

	public static void setUsingSQLite(boolean usingSQLite) {
		BlockHandler.usingSQLite = usingSQLite;
	}

	public static boolean isTrackedChunk(Chunk c) {
		return !getBlocksInChunk(c).isEmpty();
	}

	public static List<String> getBlocksInChunk(Chunk c) {
		return getBlocksInChunk(Utils.getChunkString(c));
	}

	public static List<String> getBlocksInChunk(String c) {
		return getListFromArrays(blocksInChunk.get(c));
	}

	public static void addBlockToChunk(String c, String block) {
		List<String> blocks = getBlocksInChunk(c);

		blocks.add(block);

		blocksInChunk.put(c, getArraysFromList(blocks));
	}

	private static List<String> getListFromArrays(char[][] arrays) {
		List<String> result = new ArrayList<String>();

		if (arrays == null)
			return result;

		for (int i = 0; i < arrays.length; i++)
			result.add(String.valueOf(arrays[i]));

		return result;
	}

	private static char[][] getArraysFromList(List<String> list) {
		char[][] result = new char[list.size()][0];

		for (int i = 0; i < list.size(); i++)
			result[i] = list.get(i).toCharArray();

		return result;
	}

	public static void removeBlocksInChunk(Chunk c) {
		removeBlocksInChunk(Utils.getChunkString(c));
	}

	public static void removeBlocksInChunk(String c) {
		if (blocksInChunk.containsKey(c))
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
				b.setMetadata("GMC", Main.getFMV());
			}
		}

		removeBlocksInChunk(c);

		if (Main.DEBUG)
			System.out.println("loadFromDatabase: " + c + " took " + (System.currentTimeMillis() - start) + "ms");
	}

	public static void loadFromDatabaseOld(Main main) {
		Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				long start = System.currentTimeMillis();

				main.getUtils().sendMessage(Bukkit.getConsoleSender(), true, "database.load");

				// Gets all blocks from database
				ResultSet rs = main.getDB().executeQuery("SELECT * FROM " + main.getDB().getBlocksTable());

				int count = 0;
				try {
					while (rs.next()) {
						String block = rs.getString("block");
						Block b = Utils.getBlock(block);

						if (b == null || b.isEmpty()) {
							removeFromDatabase.add(block);
						} else {
							count++;
							b.setMetadata("GMC", Main.getFMV());
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}

				setTotalCount(count);

				Utils.sendMessage(Bukkit.getConsoleSender(),
						main.getUtils().getMessage(true, "database.loaded").replaceAll("%blocks%", getTotalCount()));

				String took = String.valueOf(System.currentTimeMillis() - start);

				Utils.sendMessage(Bukkit.getConsoleSender(),
						main.getUtils().getMessage(true, "database.done").replaceAll("%mills%", took));
			}
		});
	}

	public static void loadFromDatabaseNew(Main main) {
		// Start async processing
		Bukkit.getScheduler().runTaskAsynchronously(main, new Runnable() {
			@Override
			public void run() {
				long start = System.currentTimeMillis();

				main.getUtils().sendMessage(Bukkit.getConsoleSender(), true, "database.load");

				// Gets amount of blocks from database
				ResultSet countRs = main.getDB().executeQuery("SELECT COUNT(*) FROM " + main.getDB().getBlocksTable());

				int count = 0;
				try {
					if (countRs.next())
						count = countRs.getInt(1);
				} catch (SQLException e) {
					Bukkit.getLogger().log(Level.WARNING, "Block counting was interrupted!");
					e.printStackTrace();
				}

				int limit = 10000;
				for (int i = 0; i <= count / limit; i++) {
					taskCount++;
					loadSectionFromDatabase(main, start, limit, limit * i);
				}

				if (Main.DEBUG)
					System.out.println("loadFromDatabaseNew: " + taskCount);
			}
		});
	}

	private static BukkitTask loadSectionFromDatabase(Main main, long start, int limit, int offset) {
		// Start async processing
		return Bukkit.getScheduler().runTaskAsynchronously(main, new Runnable() {
			@Override
			public void run() {
				int i = 0;
				try {
					// Gets a sections of blocks from database
					PreparedStatement statement = main.getDB()
							.getStatement("SELECT * FROM " + main.getDB().getBlocksTable() + " LIMIT ? OFFSET ?");
					statement.setInt(1, limit);
					statement.setInt(2, offset);
					ResultSet rs = statement.executeQuery();

					while (rs.next()) {
						String block = rs.getString("block");
						String chunk = Utils.getBlockChunk(block);

						String world = block.split(";")[0];
						if (main.getUtils().isDisabledWorld(world) || Bukkit.getWorld(world) == null)
							continue;

						addBlockToChunk(chunk, block);
						i++;
					}
				} catch (SQLException e) {
					Bukkit.getLogger().log(Level.WARNING, "Data loading was interrupted!");
					e.printStackTrace();
				}

				setTotalCount(totalCount + i);

				if (Main.DEBUG)
					System.out.println("loadSectionFromDatabase: " + getTotalCount());

				blockLoadingDone(main, start);
			}
		});
	}

	private static void blockLoadingDone(Main main, long start) {
		if (taskCount-- != 0)
			return;

		Bukkit.getScheduler().runTask(main, new Runnable() {
			@Override
			public void run() {
				int chunksLoaded = blocksInChunk.size();

				if (Main.DEBUG)
					System.out.println("blockLoadingDone: " + chunksLoaded + " chunks");

				int radius = 8;
				for (World world : Bukkit.getWorlds()) {
					// Ignore disabled worlds
					if (main.getUtils().isDisabledWorld(world.getName()))
						continue;

					Chunk center = world.getSpawnLocation().getChunk();

					for (int x = center.getX() - radius; x < center.getX() + radius; x++) {
						for (int z = center.getZ() - radius; z < center.getZ() + radius; z++) {
							Chunk c = world.getChunkAt(x, z);
							loadBlocks(c);
						}
					}
				}

				isLoadingDone = true;

				Utils.sendMessage(Bukkit.getConsoleSender(), main.getUtils().getMessage(true, "database.loaded")
						.replaceAll("%blocks%", getTotalCount()).replaceAll("%chunks%", String.valueOf(chunksLoaded)));

				String took = String.valueOf(System.currentTimeMillis() - start);

				Utils.sendMessage(Bukkit.getConsoleSender(),
						main.getUtils().getMessage(true, "database.done").replaceAll("%mills%", took));
			}
		});
	}

	public static void startDataSync(Main main) {
		int interval = main.getSettings().getInt("general.saving.interval");

		Bukkit.getServer().getScheduler().runTaskTimer(main, new Runnable() {
			@Override
			public void run() {
				final List<String> fAdd = new ArrayList<>(addToDatabase);
				final List<String> fDel = new ArrayList<>(removeFromDatabase);

				Bukkit.getScheduler().runTaskAsynchronously(main, new SyncData(main, fAdd, fDel, false));
			}
		}, interval, interval);
	}
}
