package me.prunt.restrictedcreative.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.potion.PotionEffect;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.storage.DataHandler;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.milkbowl.vault.permission.Permission;

public class Utils {
    /* --- Static methods --- */
    public static boolean isInstalled(String plugin) {
	return Bukkit.getPluginManager().getPlugin(plugin) != null;
    }

    // Return block position as a string
    public static String getBlockString(Block b) {
	return b.getWorld().getName() + ";" + b.getX() + ";" + b.getY() + ";" + b.getZ();
    }

    // Return location coordinates as a string
    public static String getLocString(Location loc) {
	return loc.getWorld().getName() + ";" + loc.getBlockX() + ";" + loc.getBlockZ();
    }

    // Return block from a position string
    public static Block getBlock(String s) {
	// Get coordinates from given string
	String[] sl = s.split(";");
	String world = sl[0];
	int x = Integer.valueOf(sl[1]);
	int y = Integer.valueOf(sl[2]);
	int z = Integer.valueOf(sl[3]);

	// Return null if world doesn't exist
	if (Bukkit.getServer().getWorld(world) == null)
	    return null;

	// Return block from given coordinates
	return Bukkit.getServer().getWorld(world).getBlockAt(x, y, z);
    }

    // Print error to console
    public static void log(String msg) {
	Bukkit.getLogger().severe(msg);
    }

    public static void sendMessage(CommandSender sender, String msg) {
	if (!msg.equalsIgnoreCase(""))
	    sender.sendMessage(msg);
    }

    /* --- Non-static methods --- */
    private Main main;

    public Utils(Main main) {
	this.main = main;
    }

    private Main getMain() {
	return this.main;
    }

    /**
     * @param name
     *                 World name
     * @return Whether the plugin is disabled in the given world
     */
    public boolean isDisabledWorld(String name) {
	return getMain().getSettings().getStringList("general.disabled-worlds").contains(name);
    }

    /**
     * @param m
     *              Material type
     * @return Whether tracking is enabled in the config
     */
    public boolean isTrackingOn() {
	return getMain().getSettings().isEnabled("tracking.blocks.enabled");
    }

    /**
     * @param m
     *              Material type
     * @return Whether the given type should be excluded from tracking
     */
    public boolean isExcluded(Material m) {
	return getMain().getSettings().getMaterialList("tracking.blocks.exclude").contains(m) || !isTrackingOn();
    }

    private boolean isInvalid(Material m) {
	return getMain().getSettings().getMaterialList("confiscate.items.material").contains(m);
    }

    /**
     * @param m
     *              Material type
     * @return Whether the given type should be disabled from placing by creative
     *         players
     */
    public boolean isDisabledPlacing(Material m) {
	return getMain().getSettings().getMaterialList("disable.placing").contains(m);
    }

    /**
     * @param m
     *              Material type
     * @return Whether the given type should be disabled from breaking by creative
     *         players
     */
    public boolean isDisabledBreaking(Material m) {
	return getMain().getSettings().getMaterialList("disable.breaking").contains(m);
    }

    public boolean isWhitelistedRegion(String name) {
	return getMain().getSettings().getStringList("limit.regions.whitelist.list").contains(name);
    }

    /**
     * @param sender
     *                   Player to send the message to
     * @param prefix
     *                   Whether to include a prefix in the message
     * @param string
     *                   Paths of messages to send to the player
     */
    public void sendMessage(CommandSender sender, boolean prefix, String... paths) {
	sendMessage(sender, getMessage(prefix, paths));
    }

    /**
     * @param prefix
     *                   Whether to include a prefix in the message
     * @param string
     *                   Paths of messages to send to the player
     */
    public String getMessage(boolean prefix, String... paths) {
	if (getMain().getMessages().isNone(paths))
	    return "";

	String msg = prefix ? getMain().getMessages().getMessage("prefix") : "";
	for (String path : paths)
	    msg += getMain().getMessages().getMessage(path);

	return msg;
    }

    public boolean canBuildHere(Player p, Block b, ItemStack is) {
	// Gets the player or block location
	Location loc = p.getLocation();
	if (b != null)
	    loc = b.getLocation();

	// WorldGuard check
	if (Utils.isInstalled("WorldGuard")) {
	    WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
	    LocalPlayer lp = wg.wrapPlayer(p);

	    // Gets all regions covering the block or player location
	    ApplicableRegionSet set = wg.getRegionManager(p.getWorld()).getApplicableRegions(loc);

	    // Loops through applicable regions
	    for (ProtectedRegion rg : set) {
		// Whitelist check
		if (getMain().getSettings().isEnabled("limit.regions.whitelist.enabled")) {
		    // If it's whitelisted
		    if (isWhitelistedRegion(rg.getId()))
			return true;
		}

		// Owner check
		if (getMain().getSettings().isEnabled("limit.regions.owner-based.enabled")) {
		    if (rg.isOwner(lp))
			return true;

		    // Member check
		    if (getMain().getSettings().isEnabled("limit.regions.owner-based.allow-members")) {
			if (rg.isMember(lp))
			    return true;
		    }
		}
	    }
	}

	// GriefPrevention check
	if (Utils.isInstalled("GriefPrevention")) {
	    Claim claim = GriefPrevention.instance.dataStore.getClaimAt(b.getLocation(), false, null);

	    if (claim == null)
		return false;

	    // Owner check
	    if (getMain().getSettings().isEnabled("limit.regions.owner-based.enabled")) {
		if (claim.getOwnerName() == p.getName())
		    return true;

		// Member check
		if (getMain().getSettings().isEnabled("limit.regions.owner-based.allow-members")) {
		    if (claim.allowBuild(p, is.getType()) == null)
			return true;
		}
	    }
	}

	return false;
    }

    private boolean isInvalidNBT(ItemStack is) {
	// No need to control disabled features
	if (!getMain().getSettings().isEnabled("confiscate.invalid-items"))
	    return false;

	if (is == null || is.getType() == Material.AIR)
	    return false;

	try {
	    NbtCompound nc = (NbtCompound) NbtFactory.fromItemTag(is);

	    if (nc.containsKey("CustomPotionEffects") || nc.containsKey("StoredEnchantments")
		    || nc.containsKey("HideFlags") || nc.containsKey("Unbreakable")
		    || nc.containsKey("AttributeModifiers"))
		return true;
	} catch (Exception ex) {
	    return false;
	}

	return false;
    }

    private boolean isInvalid(ItemStack is) {
	// No need to control disabled features
	if (!getMain().getSettings().isEnabled("confiscate.invalid-items"))
	    return false;

	if (is == null || is.getType() == Material.AIR)
	    return false;

	ItemMeta im = is.getItemMeta();

	// Displayname length check
	if (im.getDisplayName() != null) {
	    if (im.getDisplayName().length() > 30)
		return true;
	}

	// Enchantments check
	for (Map.Entry<Enchantment, Integer> ench : is.getEnchantments().entrySet()) {
	    if (ench.getValue() < 1 || ench.getValue() > ench.getKey().getMaxLevel())
		return true;
	}

	return false;
    }

    // Check if item name is bad
    private boolean isBadName(ItemStack is) {
	for (String name : getMain().getSettings().getStringList("confiscate.items.name")) {
	    String dn = is.getItemMeta().getDisplayName();

	    if (dn != null && dn.contains(name))
		return true;
	}

	return false;
    }

    // Check if item lore is bad
    private boolean isBadLore(ItemStack is) {
	for (String name : getMain().getSettings().getStringList("confiscate.items.lore")) {
	    List<String> lores = is.getItemMeta().getLore();

	    if (lores == null)
		return false;

	    for (String lore : lores) {
		if (lore.contains(name))
		    return true;
	    }
	}

	return false;
    }

    public boolean shouldConfiscate(Player p, ItemStack is) {
	if (is == null)
	    return false;

	Material m = is.getType();

	// Invalid items
	if (getMain().getSettings().isEnabled("confiscate.invalid-items")
		&& !p.hasPermission("rc.bypass.confiscate.invalid-items")) {
	    if ((Utils.isInstalled("ProtocolLib") && getMain().getUtils().isInvalidNBT(is))
		    || getMain().getUtils().isInvalid(is))
		return true;
	}

	if (!getMain().getSettings().isEnabled("confiscate.items.enabled"))
	    return false;

	// Material
	if (!p.hasPermission("rc.bypass.confiscate.items.material")
		&& !p.hasPermission("rc.bypass.confiscate.items.material." + m)) {
	    if (getMain().getUtils().isInvalid(m))
		return true;
	}

	// Name
	if (!p.hasPermission("rc.bypass.confiscate.items.name")) {
	    if (getMain().getUtils().isBadName(is))
		return true;
	}

	// Lore
	if (!p.hasPermission("rc.bypass.confiscate.items.lore")) {
	    if (getMain().getUtils().isBadLore(is))
		return true;
	}

	return false;
    }

    public void equipArmor(Player p) {
	Color c = Color.fromRGB(getMain().getSettings().getInt("creative.armor.color"));
	List<ItemStack> armorList = MaterialHandler.getArmorList();

	for (ItemStack is : armorList) {
	    LeatherArmorMeta lam = (LeatherArmorMeta) is.getItemMeta();
	    lam.setColor(c);
	    is.setItemMeta(lam);
	}

	ItemStack[] list = new ItemStack[armorList.size()];
	list = armorList.toArray(list);

	p.getInventory().setArmorContents(list);
	p.updateInventory();
    }

    public boolean isHeightOk(Player p) {
	if (!getMain().getSettings().isEnabled("limit.moving.enabled"))
	    return true;

	if (p.hasPermission("rc.bypass.limit.moving"))
	    return true;

	double y = p.getLocation().getY();
	int above_y = getMain().getSettings().getInt("limit.moving.above-y");
	int below_y = getMain().getSettings().getInt("limit.moving.below-y");

	return (below_y > 0 && y < below_y) && (above_y > 0 && y > above_y);
    }

    public void setCreative(Player p, boolean toCreative) {
	// Permissions
	if (getMain().getSettings().isEnabled("creative.permissions.enabled")
		&& !p.hasPermission("rc.bypass.creative.permissions"))
	    setPermissions(p, toCreative);

	// Inventory
	if (getMain().getSettings().isEnabled("tracking.inventory.enabled")
		&& !p.hasPermission("rc.bypass.tracking.inventory"))
	    separateInventory(p, toCreative);

	// Armor
	if (toCreative && getMain().getSettings().isEnabled("creative.armor.enabled")
		&& !p.hasPermission("rc.bypass.creative.armor"))
	    equipArmor(p);
    }

    private void separateInventory(Player p, boolean toCreative) {
	PlayerInfo pi = getPlayerInfo(p);

	// Stores Player with PlayerInfo into HashMap
	if (toCreative) {
	    DataHandler.saveSurvivalInv(p, pi);
	    setInventory(p, DataHandler.getCreativeInv(p));
	} else {
	    DataHandler.saveCreativeInv(p, pi);
	    setInventory(p, DataHandler.getSurvivalInv(p));
	}
    }

    private static PlayerInfo getPlayerInfo(Player p) {
	// Gets the data to save
	List<ItemStack> storage = Arrays.asList(p.getInventory().getContents());
	List<ItemStack> armor = Arrays.asList(p.getInventory().getArmorContents());
	List<ItemStack> extra = Arrays.asList(p.getInventory().getExtraContents());

	Collection<PotionEffect> effects = p.getActivePotionEffects();
	int xp = p.getTotalExperience();
	GameMode gm = p.getGameMode();

	return new PlayerInfo(storage, armor, extra, effects, xp, gm);
    }

    private void setInventory(Player p, PlayerInfo pi) {
	// Clear the inventory
	if (pi == null) {
	    if (!p.hasPermission("rc.bypass.tracking.inventory.contents"))
		p.getInventory().clear();

	    if (!p.hasPermission("rc.bypass.tracking.inventory.xp"))
		p.setTotalExperience(0);

	    if (!p.hasPermission("rc.bypass.tracking.inventory.effects")) {
		for (PotionEffect pe : p.getActivePotionEffects()) {
		    p.removePotionEffect(pe.getType());
		}
	    }

	    p.updateInventory();
	    return;
	}

	if (!p.hasPermission("rc.bypass.tracking.inventory.contents")) {
	    ItemStack[] old_storage = new ItemStack[pi.storage.size()];
	    old_storage = pi.storage.toArray(old_storage);
	    p.getInventory().setContents(old_storage);

	    ItemStack[] old_armor = new ItemStack[pi.armor.size()];
	    old_armor = pi.armor.toArray(old_armor);
	    p.getInventory().setArmorContents(old_armor);

	    ItemStack[] old_extra = new ItemStack[pi.extra.size()];
	    old_extra = pi.extra.toArray(old_extra);
	    p.getInventory().setExtraContents(old_extra);
	}

	if (!p.hasPermission("rc.bypass.tracking.inventory.xp"))
	    p.setTotalExperience(pi.xp);

	if (!p.hasPermission("rc.bypass.tracking.inventory.effects")) {
	    for (PotionEffect pe : p.getActivePotionEffects()) {
		p.removePotionEffect(pe.getType());
	    }
	    p.addPotionEffects(pi.effects);
	}

	p.updateInventory();
    }

    private void setPermissions(Player p, boolean toCreative) {
	if (Utils.isInstalled("Vault")) {
	    Permission vault = Bukkit.getServer().getServicesManager().getRegistration(Permission.class).getProvider();

	    if (toCreative) {
		for (String perm : getMain().getSettings().getStringList("creative.permissions.list")) {
		    // Remove permission
		    if (perm.startsWith("-")) {
			// .substring(1) removes "-" from the front
			perm = perm.substring(1);

			if (!vault.has(p, perm))
			    return;

			DataHandler.addVaultPerm(p, perm);
			vault.playerRemove(p, perm);
		    }

		    // Add permission
		    else {
			if (vault.has(p, perm))
			    return;

			DataHandler.addVaultPerm(p, perm);
			vault.playerAdd(p, perm);
		    }
		}
	    } else {
		if (DataHandler.getVaultPerms(p) == null)
		    return;

		for (String perm : DataHandler.getVaultPerms(p)) {
		    if (vault.has(p, perm)) {
			vault.playerRemove(p, perm);
		    } else {
			vault.playerAdd(p, perm);
		    }
		}

		DataHandler.removeVaultPerm(p);
	    }
	} else {
	    if (toCreative) {
		PermissionAttachment attachment = p.addAttachment(getMain());

		for (String perm : getMain().getSettings().getStringList("creative.permissions.list")) {
		    if (perm.startsWith("-")) {
			// .substring(1) removes "-" from the front
			attachment.setPermission(perm.substring(1), false);
		    } else {
			attachment.setPermission(perm, true);
		    }
		}

		DataHandler.setPerms(p, attachment);
	    } else {
		if (DataHandler.getPerms(p) == null)
		    return;

		PermissionAttachment attachment = DataHandler.getPerms(p);
		p.removeAttachment(attachment);
		DataHandler.removePerms(p);
	    }
	}
    }

    public void saveInventory(Player p) {
	// No need to control disabled features
	if (!getMain().getSettings().isEnabled("saving.inventories.enabled")) {
	    // Let the gamemode listener handle switching inventories
	    if (p.getGameMode() == GameMode.CREATIVE)
		p.setGameMode(DataHandler.getPreviousGameMode(p));
	    return;
	}

	// No need to control bypassed players
	if (p.hasPermission("rc.bypass.tracking.inventory"))
	    return;

	if (DataHandler.getSurvivalInv(p) == null && DataHandler.getCreativeInv(p) == null)
	    return;

	PlayerInfo pi;
	int type;
	if (p.getGameMode() == GameMode.CREATIVE) {
	    pi = DataHandler.getSurvivalInv(p);
	    type = 1;
	} else {
	    pi = DataHandler.getCreativeInv(p);
	    type = 0;
	}

	getMain().getDB().executeUpdate("INSERT INTO " + getMain().getDB().getInvsTable()
		+ " (player, type, storage, armor, extra, effects, xp, lastused) VALUES (" + p.getUniqueId().toString()
		+ ", " + type + ", " + pi.getStorage() + ", " + pi.getArmor() + ", " + pi.getExtra() + ", "
		+ pi.getEffects() + ", " + p.getTotalExperience() + ", " + System.currentTimeMillis() / 1000 + ")");

	DataHandler.removeSurvivalInv(p);
	DataHandler.removeCreativeInv(p);
    }

    public void loadInventory(Player p) {
	// No need to control disabled features
	if (!getMain().getSettings().isEnabled("saving.inventories.enabled"))
	    return;

	// No need to control bypassed players
	if (p.hasPermission("rc.bypass.tracking.inventory"))
	    return;

	ResultSet rs = getMain().getDB().executeQuery("SELECT * FROM " + getMain().getDB().getInvsTable()
		+ " WHERE player = '" + p.getUniqueId().toString() + "'");

	try {
	    if (rs.next()) {
		GameMode gm;
		if (rs.getInt("type") == 0) {
		    gm = Bukkit.getDefaultGameMode();
		} else {
		    gm = GameMode.CREATIVE;
		}

		PlayerInfo pi = new PlayerInfo(rs.getString("storage"), rs.getString("armor"), rs.getString("extra"),
			rs.getString("effects"), rs.getInt("xp"), gm);

		if (rs.getInt("type") == 0) {
		    DataHandler.saveSurvivalInv(p, pi);
		} else {
		    DataHandler.saveCreativeInv(p, pi);
		}
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	}
    }
}
