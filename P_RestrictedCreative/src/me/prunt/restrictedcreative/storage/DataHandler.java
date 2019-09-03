package me.prunt.restrictedcreative.storage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.PermissionAttachment;

import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.utils.BlocksHub;
import me.prunt.restrictedcreative.utils.PlayerInfo;
import me.prunt.restrictedcreative.utils.Utils;

public class DataHandler {
    public static List<String> addToDatabase = new ArrayList<>();
    public static List<String> removeFromDatabase = new ArrayList<>();

    private static List<String> trackedLocs = new ArrayList<>();

    private static HashMap<Player, PlayerInfo> survivalInvs = new HashMap<>();
    private static HashMap<Player, PlayerInfo> creativeInvs = new HashMap<>();

    private static HashMap<Player, GameMode> previousGameMode = new HashMap<>();
    private static HashMap<Player, List<String>> vaultPerms = new HashMap<>();
    private static HashMap<Player, List<String>> vaultGroups = new HashMap<>();
    private static HashMap<Player, PermissionAttachment> permissions = new HashMap<>();

    private static HashMap<String, List<String>> blocksInChunk = new HashMap<>();

    private static boolean usingOldAliases = false;
    private static boolean usingSQLite = false;
    private static boolean isForceGamemodeEnabled = false;

    private static List<Player> addWithCommand = new ArrayList<>();
    private static List<Player> removeWithCommand = new ArrayList<>();
    private static List<Player> infoWithCommand = new ArrayList<>();

    private static int totalCount = -1;

    public static boolean isTracked(Block b) {
	if (b == null)
	    return false;

	for (MetadataValue mdv : b.getMetadata("GMC")) {
	    if (mdv.getOwningPlugin() == Main.getInstance()) {
		if (mdv.asBoolean())
		    return true;
	    }
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

    public static boolean isTracked(Entity e) {
	if (e == null)
	    return false;

	return e.getScoreboardTags().contains("GMC");
    }

    public static void setAsTracked(Entity e) {
	if (e == null)
	    return;

	e.addScoreboardTag("GMC");
    }

    public static void removeTracking(Entity e) {
	if (e == null)
	    return;

	e.removeScoreboardTag("GMC");
    }

    public static boolean hasTrackedItem(ItemFrame frame) {
	if (frame == null)
	    return false;

	return frame.getScoreboardTags().contains("GMC_IF");
    }

    public static void setAsTrackedItem(ItemFrame frame) {
	if (frame == null)
	    return;

	frame.addScoreboardTag("GMC_IF");
    }

    public static void removeItemTracking(ItemFrame frame) {
	if (frame == null)
	    return;

	frame.removeScoreboardTag("GMC_IF");
    }

    public static void removeItem(ItemFrame frame) {
	if (frame == null)
	    return;

	frame.setItem(new ItemStack(Material.AIR));
	removeItemTracking(frame);
    }

    public static boolean isTrackedSlot(ArmorStand stand, EquipmentSlot slot) {
	if (stand == null || slot == null)
	    return false;

	return stand.getScoreboardTags().contains("GMC_AS_" + slot);
    }

    public static void setAsTrackedSlot(ArmorStand stand, EquipmentSlot slot) {
	if (stand == null)
	    return;

	stand.addScoreboardTag("GMC_AS_" + slot);
    }

    public static void removeSlotTracking(ArmorStand stand, EquipmentSlot slot) {
	if (stand == null)
	    return;

	stand.removeScoreboardTag("GMC_AS_" + slot);
    }

    public static boolean isTrackedLoc(Location loc) {
	return trackedLocs.contains(Utils.getLocString(loc));
    }

    public static void addToTrackedLocs(Location loc) {
	trackedLocs.add(Utils.getLocString(loc));
    }

    public static void removeFromTrackedLocs(Location loc) {
	if (isTrackedLoc(loc))
	    trackedLocs.remove(Utils.getLocString(loc));
    }

    public static GameMode getPreviousGameMode(Player p) {
	return previousGameMode.containsKey(p) ? previousGameMode.get(p) : Bukkit.getDefaultGameMode();
    }

    public static void setPreviousGameMode(Player p, GameMode gm) {
	previousGameMode.put(p, gm);
    }

    public static void removePreviousGameMode(Player p) {
	if (previousGameMode.containsKey(p))
	    previousGameMode.remove(p);
    }

    public static void saveSurvivalInv(Player p, PlayerInfo pi) {
	survivalInvs.put(p, pi);
    }

    public static PlayerInfo getSurvivalInv(Player p) {
	return survivalInvs.containsKey(p) ? survivalInvs.get(p) : null;
    }

    public static void removeSurvivalInv(Player p) {
	if (survivalInvs.containsKey(p))
	    survivalInvs.remove(p);
    }

    public static void saveCreativeInv(Player p, PlayerInfo pi) {
	creativeInvs.put(p, pi);
    }

    public static PlayerInfo getCreativeInv(Player p) {
	return creativeInvs.containsKey(p) ? creativeInvs.get(p) : null;
    }

    public static void removeCreativeInv(Player p) {
	if (creativeInvs.containsKey(p))
	    creativeInvs.remove(p);
    }

    public static PermissionAttachment getPerms(Player p) {
	return permissions.containsKey(p) ? permissions.get(p) : null;
    }

    public static void setPerms(Player p, PermissionAttachment attachment) {
	permissions.put(p, attachment);
    }

    public static void removePerms(Player p) {
	if (permissions.containsKey(p))
	    permissions.remove(p);
    }

    public static List<String> getVaultPerms(Player p) {
	return vaultPerms.containsKey(p) ? vaultPerms.get(p) : null;
    }

    public static void addVaultPerm(Player p, String perm) {
	List<String> prevPerms = getVaultPerms(p);

	if (prevPerms != null) {
	    prevPerms.add(perm);
	} else {
	    vaultPerms.put(p, new ArrayList<>(Arrays.asList(perm)));
	}
    }

    public static void removeVaultPerm(Player p) {
	if (vaultPerms.containsKey(p))
	    vaultPerms.remove(p);
    }

    public static List<String> getVaultGroups(Player p) {
	return vaultGroups.containsKey(p) ? vaultGroups.get(p) : null;
    }

    public static void addVaultGroup(Player p, String group) {
	List<String> prevGroups = getVaultGroups(p);

	if (prevGroups != null) {
	    vaultGroups.get(p).add(group);
	} else {
	    vaultGroups.put(p, new ArrayList<>(Arrays.asList(group)));
	}
    }

    public static void removeVaultGroup(Player p) {
	if (vaultGroups.containsKey(p))
	    vaultGroups.remove(p);
    }

    private static void setTotalCount(int totalCount) {
	DataHandler.totalCount = totalCount;
    }

    public static String getTotalCount() {
	return String.valueOf(totalCount);
    }

    public static boolean isUsingOldAliases() {
	return usingOldAliases;
    }

    public static void setUsingOldAliases(boolean usingOldAliases) {
	DataHandler.usingOldAliases = usingOldAliases;
    }

    public static boolean isUsingSQLite() {
	return usingSQLite;
    }

    public static void setUsingSQLite(boolean usingSQLite) {
	DataHandler.usingSQLite = usingSQLite;
    }

    public static boolean isForceGamemodeEnabled() {
	return isForceGamemodeEnabled;
    }

    public static void setForceGamemodeEnabled(boolean isForceGamemodeEnabled) {
	DataHandler.isForceGamemodeEnabled = isForceGamemodeEnabled;
    }

    public static List<Player> getAddWithCommand() {
	return addWithCommand;
    }

    public static List<Player> getRemoveWithCommand() {
	return removeWithCommand;
    }

    public static List<Player> getInfoWithCommand() {
	return infoWithCommand;
    }

    public static boolean isAddWithCommand(Player p) {
	return getAddWithCommand().contains(p);
    }

    public static boolean isRemoveWithCommand(Player p) {
	return getRemoveWithCommand().contains(p);
    }

    public static boolean isInfoWithCommand(Player p) {
	return getInfoWithCommand().contains(p);
    }

    public static void removeAddWithCommand(Player p) {
	if (isAddWithCommand(p))
	    getAddWithCommand().remove(p);
    }

    public static void removeRemoveWithCommand(Player p) {
	if (isRemoveWithCommand(p))
	    getRemoveWithCommand().remove(p);
    }

    public static void removeInfoWithCommand(Player p) {
	if (isInfoWithCommand(p))
	    getInfoWithCommand().remove(p);
    }

    public static void setAddWithCommand(Player p) {
	getAddWithCommand().add(p);
    }

    public static void setRemoveWithCommand(Player p) {
	getRemoveWithCommand().add(p);
    }

    public static void setInfoWithCommand(Player p) {
	getInfoWithCommand().add(p);
    }

    public static boolean isTrackedChunk(Chunk c) {
	return !getBlocksInChunk(c).isEmpty();
    }

    public static List<String> getBlocksInChunk(Chunk c) {
	return getBlocksInChunk(Utils.getChunkString(c));
    }

    public static List<String> getBlocksInChunk(String c) {
	return blocksInChunk.containsKey(c) ? blocksInChunk.get(c) : new ArrayList<String>();
    }

    public static void addBlockToChunk(String c, String block) {
	List<String> prevBlocks = getBlocksInChunk(c);

	if (!prevBlocks.isEmpty()) {
	    blocksInChunk.get(c).add(block);
	} else {
	    blocksInChunk.put(c, new ArrayList<>(Arrays.asList(block)));
	}
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

		// Gets all blocks from database
		ResultSet rs = main.getDB().executeQuery("SELECT * FROM " + main.getDB().getBlocksTable());

		int count = 0;
		try {
		    while (rs.next()) {
			String block = rs.getString("block");
			String chunk = Utils.getBlockChunk(block);

			String world = block.split(";")[0];
			if (main.getUtils().isDisabledWorld(world) || Bukkit.getWorld(world) == null)
			    continue;

			addBlockToChunk(chunk, block);
			count++;
		    }
		} catch (SQLException e) {
		    Bukkit.getLogger().log(Level.WARNING, "Data loading was interrupted!");
		    e.printStackTrace();
		}

		int chunksLoaded = blocksInChunk.size();

		if (Main.DEBUG)
		    System.out.println("loadFromDatabase: " + chunksLoaded + " chunks");

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

		setTotalCount(count);

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
