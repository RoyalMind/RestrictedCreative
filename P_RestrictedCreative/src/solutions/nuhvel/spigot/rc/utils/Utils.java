package solutions.nuhvel.spigot.rc.utils;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.storage.config.config.miscellaneous.ArmorMaterial;
import solutions.nuhvel.spigot.rc.storage.handlers.BlockHandler;
import solutions.nuhvel.spigot.rc.storage.handlers.InventoryHandler;
import solutions.nuhvel.spigot.rc.storage.handlers.PermissionHandler;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;

public class Utils {
    /* --- Static methods --- */
    public static boolean isVersionOlderThanInclusive(MinecraftVersion version) {
        return getCurrentVersion().compareTo(version) <= 0;
    }

    public static boolean isVersionNewerThanInclusive(MinecraftVersion version) {
        return getCurrentVersion().compareTo(version) >= 0;
    }

    private static MinecraftVersion getCurrentVersion() {
        String version = Bukkit.getVersion();

        if (version.contains("1.19"))
            return MinecraftVersion.v1_19;
        if (version.contains("1.18"))
            return MinecraftVersion.v1_18;
        if (version.contains("1.17"))
            return MinecraftVersion.v1_17;
        if (version.contains("1.16"))
            return MinecraftVersion.v1_16;
        if (version.contains("1.15"))
            return MinecraftVersion.v1_15;
        if (version.contains("1.14"))
            return MinecraftVersion.v1_14;
        if (version.contains("1.13"))
            return MinecraftVersion.v1_13;

        return MinecraftVersion.UNKNOWN;
    }

    public static boolean isInstalled(String plugin) {
        return Bukkit.getPluginManager().getPlugin(plugin) != null;
    }

    // Return block position as a string
    public static String getBlockString(Block b) {
        return b.getWorld().getName() + ";" + b.getX() + ";" + b.getY() + ";" + b.getZ();
    }

    // Return chunk position as a string
    public static String getChunkString(Chunk c) {
        return c.getWorld().getName() + ";" + c.getX() + ";" + c.getZ();
    }

    // Return location coordinates as a string
    public static String getLocString(Location loc) {
        World w = loc.getWorld();

        if (w == null)
            return null;

        return w.getName() + ";" + loc.getBlockX() + ";" + loc.getBlockZ();
    }

    // Return block from a position string
    public static Block getBlock(String s) {
        // Get coordinates from given string
        String[] sl = s.split(";");
        String world = sl[0];
        int x = Integer.parseInt(sl[1]);
        int y = Integer.parseInt(sl[2]);
        int z = Integer.parseInt(sl[3]);

        World w = Bukkit.getServer().getWorld(world);

        // Return null if world doesn't exist
        if (w == null)
            return null;

        // Return block from given coordinates
        return w.getBlockAt(x, y, z);
    }

    public static String getBlockChunk(String block) {
        String[] blockParts = block.split(";");

        // Get chunk data from given block string
        String world = blockParts[0];
        int chunkX = Integer.parseInt(blockParts[1]) >> 4; // ">> 4" == "/ 16", but faster
        int chunkZ = Integer.parseInt(blockParts[3]) >> 4; // ">> 4" == "/ 16", but faster

        return world + ";" + chunkX + ";" + chunkZ;
    }

    // Print error to console
    public static void log(String msg) {
        Bukkit.getLogger().severe(msg);
    }

    public static void sendMessage(CommandSender sender, String msg) {
        if (!msg.equalsIgnoreCase(""))
            sender.sendMessage(msg);
    }

    public static boolean isForceGamemodeEnabled() {
        try {
            FileInputStream in = new FileInputStream(
                    new File(".").getAbsolutePath() + "/server.properties");
            Properties prop = new Properties();

            prop.load(in);
            boolean result = Boolean.parseBoolean(prop.getProperty("force-gamemode"));
            in.close();

            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* --- Non-static methods --- */
    private final RestrictedCreative plugin;

    public Utils(RestrictedCreative plugin) {
        this.plugin = plugin;
    }

    public boolean isTrackingDisabled() {
        return !plugin.config.tracking.blocks.enabled;
    }

    public boolean isExcludedFromTracking(Material m) {
        return plugin.config.tracking.blocks.excluded.contains(m)
                || isTrackingDisabled() || m == Material.AIR;
    }

    public boolean isExcludedFromConfiscating(Material m) {
        return plugin.config.confiscate.middleClick.excluded.contains(m) || m == Material.AIR;
    }

    private boolean isInvalid(Material m) {
        return plugin.config.confiscate.items.materials.contains(m);
    }

    public boolean isDisabledPlacing(Material m) {
        return plugin.config.disable.placing.contains(m);
    }

    public boolean isDisabledBreaking(Material m) {
        return plugin.config.disable.breaking.contains(m);
    }

    public void sendMessage(CommandSender sender, boolean includePrefix, String message) {
        if (sender != null)
            sendMessage(sender, getFormattedMessage(includePrefix, message));
    }

    public String getFormattedMessage(boolean prefix, String message) {
        if (message.isEmpty())
            return "";

        String msg = prefix ? plugin.messages.prefix : "";

        msg += plugin.getMessages().getMessage(path);

        return msg;
    }

    private boolean isInvalidNBT(ItemStack is) {
        // No need to control disabled features
        if (!plugin.config.confiscate.invalidItems)
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
        if (!plugin.config.confiscate.invalidItems)
            return false;

        if (is == null || is.getType() == Material.AIR)
            return false;

        ItemMeta im = is.getItemMeta();

        // Displayname length check
        if (im != null && im.getDisplayName().length() > 30)
            return true;

        // Enchantments check
        for (Map.Entry<Enchantment, Integer> ench : is.getEnchantments().entrySet())
            if (ench.getValue() < 1 || ench.getValue() > ench.getKey().getMaxLevel())
                return true;

        return false;
    }

    // Check if item name is bad
    private boolean isBadName(ItemStack is) {
        if (is == null || is.getItemMeta() == null)
            return false;

        for (String name : plugin.config.confiscate.items.names) {
            ItemMeta im = is.getItemMeta();
            if (im == null)
                return false;

            String dn = im.getDisplayName();
            if (dn.contains(name))
                return true;
        }

        return false;
    }

    // Check if item lore is bad
    private boolean isBadLore(ItemStack is) {
        for (String name : plugin.config.confiscate.items.lores) {
            ItemMeta im = is.getItemMeta();
            if (im == null)
                return false;

            List<String> lores = im.getLore();
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
        if (plugin.config.confiscate.invalidItems
                && !p.hasPermission("rc.bypass.confiscate.invalid-items")) {
            if ((Utils.isInstalled("ProtocolLib") && plugin.getUtils().isInvalidNBT(is))
                    || plugin.getUtils().isInvalid(is))
                return true;
        }

        if (!plugin.config.confiscate.items.enabled)
            return false;

        if (!p.hasPermission("rc.bypass.confiscate.items.material")
                && !p.hasPermission("rc.bypass.confiscate.items.material." + m))
            if (plugin.getUtils().isInvalid(m))
                return true;

        if (!p.hasPermission("rc.bypass.confiscate.items.name"))
            if (plugin.getUtils().isBadName(is))
                return true;

        if (!p.hasPermission("rc.bypass.confiscate.items.lore"))
            if (plugin.getUtils().isBadLore(is))
                return true;

        return false;
    }

    public void equipArmor(Player p) {
        ArmorMaterial type = plugin.config.miscellaneous.armor.type;
        List<ItemStack> armorList = MaterialHandler.getArmorList(type);

        if (type == ArmorMaterial.LEATHER) {
            Color c = Color.fromRGB(plugin.getSettings().getInt("creative.armor.color"));

            for (ItemStack is : armorList) {
                LeatherArmorMeta lam = (LeatherArmorMeta) is.getItemMeta();
                if (lam == null)
                    continue;

                lam.setColor(c);
                is.setItemMeta(lam);
            }
        }

        ItemStack[] list = new ItemStack[armorList.size()];
        list = armorList.toArray(list);

        p.getInventory().setArmorContents(list);
        p.updateInventory();
    }

    public void setCreative(Player p, boolean toCreative) {
        // Permissions
        if (plugin.getSettings().isEnabled("creative.permissions.enabled")
                && !p.hasPermission("rc.bypass.creative.permissions"))
            setPermissions(p, toCreative);

        // Groups
        if (Utils.isInstalled("Vault")
                && plugin.getSettings().isEnabled("creative.groups.enabled")
                && !p.hasPermission("rc.bypass.creative.groups"))
            setGroups(p, toCreative);

        // Inventory
        if (plugin.getSettings().isEnabled("tracking.inventory.enabled")
                && !p.hasPermission("rc.bypass.tracking.inventory"))
            separateInventory(p, toCreative);

        // Armor
        if (toCreative && plugin.getSettings().isEnabled("creative.armor.enabled")
                && !p.hasPermission("rc.bypass.creative.armor"))
            equipArmor(p);
    }

    private void separateInventory(Player p, boolean toCreative) {
        PlayerInfo pi = getPlayerInfo(p);

        if (RestrictedCreative.DEBUG) {
            System.out.println("separateInventory: " + toCreative + " " + pi.gm + " c?"
                    + (InventoryHandler.getCreativeInv(p) != null) + " s?"
                    + (InventoryHandler.getSurvivalInv(p) != null));
        }

        // Stores Player with PlayerInfo into HashMap
        if (toCreative) {
            InventoryHandler.saveSurvivalInv(p, pi);
            setInventory(p, InventoryHandler.getCreativeInv(p));
        } else {
            InventoryHandler.saveCreativeInv(p, pi);
            setInventory(p, InventoryHandler.getSurvivalInv(p));
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
                for (PotionEffect pe : p.getActivePotionEffects())
                    p.removePotionEffect(pe.getType());
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
            for (PotionEffect pe : p.getActivePotionEffects())
                p.removePotionEffect(pe.getType());
            p.addPotionEffects(pi.effects);
        }

        p.updateInventory();
    }

    private void setGroups(Player p, boolean toCreative) {
        RegisteredServiceProvider<Permission> provider =
                Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
        Permission vault = Objects.requireNonNull(provider).getProvider();

        if (toCreative) {
            for (String group : plugin.getSettings().getStringList("creative.groups.list")) {
                // Remove group
                if (group.startsWith("-")) {
                    // .substring(1) removes "-" from the front
                    group = group.substring(1);

                    if (!vault.playerInGroup(p, group))
                        return;

                    PermissionHandler.addVaultGroup(p, group);
                    vault.playerRemoveGroup(p, group);
                }

                // Add group
                else {
                    if (vault.playerInGroup(p, group))
                        return;

                    PermissionHandler.addVaultGroup(p, group);
                    vault.playerAddGroup(p, group);
                }
            }
        } else {
            if (PermissionHandler.getVaultGroups(p) == null)
                return;

            for (String group : PermissionHandler.getVaultGroups(p)) {
                if (vault.playerInGroup(p, group)) {
                    vault.playerRemoveGroup(p, group);
                } else {
                    vault.playerAddGroup(p, group);
                }
            }

            PermissionHandler.removeVaultGroup(p);
        }
    }

    private void setPermissions(Player p, boolean toCreative) {
        if (Utils.isInstalled("Vault")
                && plugin.getSettings().isEnabled("creative.permissions.use-vault")) {
            RegisteredServiceProvider<Permission> provider =
                    Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
            Permission vault = Objects.requireNonNull(provider).getProvider();

            if (toCreative) {
                for (String perm : plugin.getSettings()
                        .getStringList("creative.permissions.list")) {
                    // Remove permission
                    if (perm.startsWith("-")) {
                        // .substring(1) removes "-" from the front
                        perm = perm.substring(1);

                        if (!vault.has(p, perm))
                            return;

                        PermissionHandler.addVaultPerm(p, perm);
                        vault.playerRemove(p, perm);
                    }

                    // Add permission
                    else {
                        if (vault.has(p, perm))
                            return;

                        PermissionHandler.addVaultPerm(p, perm);
                        vault.playerAdd(p, perm);
                    }
                }
            } else {
                if (PermissionHandler.getVaultPerms(p) == null)
                    return;

                for (String perm : PermissionHandler.getVaultPerms(p)) {
                    if (vault.has(p, perm)) {
                        vault.playerRemove(p, perm);
                    } else {
                        vault.playerAdd(p, perm);
                    }
                }

                PermissionHandler.removeVaultPerm(p);
            }
        } else {
            if (toCreative) {
                PermissionAttachment attachment = p.addAttachment(plugin);

                for (String perm : plugin.getSettings()
                        .getStringList("creative.permissions.list")) {
                    if (perm.startsWith("-")) {
                        // .substring(1) removes "-" from the front
                        attachment.setPermission(perm.substring(1), false);
                    } else {
                        attachment.setPermission(perm, true);
                    }
                }

                PermissionHandler.setPerms(p, attachment);
            } else {
                if (PermissionHandler.getPerms(p) == null)
                    return;

                PermissionAttachment attachment = PermissionHandler.getPerms(p);
                if (attachment != null)
                    p.removeAttachment(attachment);

                PermissionHandler.removePerms(p);
            }
        }
    }

    public void saveInventory(Player p) {
        // No need to control disabled features
        if (!plugin.getSettings().isEnabled("general.saving.inventories.enabled")) {
            // Let the gamemode listener handle switching inventories
            if (p.getGameMode() == GameMode.CREATIVE)
                p.setGameMode(InventoryHandler.getPreviousGameMode(p));

            InventoryHandler.removeSurvivalInv(p);
            InventoryHandler.removeCreativeInv(p);

            plugin.getUtils().debug("saveInventory: inv saving disabled");

            return;
        }

        // No need to control bypassed players
        if (p.hasPermission("rc.bypass.tracking.inventory"))
            return;

        if (InventoryHandler.getSurvivalInv(p) == null
                && InventoryHandler.getCreativeInv(p) == null)
            return;

        if (RestrictedCreative.DEBUG)
            System.out.println("saveInventory: s?" + (InventoryHandler.getSurvivalInv(p) != null)
                    + " c?" + (InventoryHandler.getCreativeInv(p) != null));

        PlayerInfo pi;
        int type;
        if (p.getGameMode() == GameMode.CREATIVE) { // creative inv remains with player, survival
            // must be saved
            pi = InventoryHandler.getSurvivalInv(p);
            type = 0;

            plugin.getUtils().debug("saveInventory: survival " + (pi != null));
        } else { // survival inv remains with player, creative must be saved
            pi = InventoryHandler.getCreativeInv(p);
            type = 1;

            plugin.getUtils().debug("saveInventory: creative " + (pi != null));
        }

        if (pi != null) {
            // Only one inventory per player is saved to the database - the one that the
            // player doesn't carry at the moment
            if (BlockHandler.isUsingSQLite()) {
                // Inserts a new row if it doesn't exist already and updates it with new values
                plugin.getDB().executeUpdate("INSERT OR IGNORE INTO "
                        + plugin.getDB().getInventoryTable()
                        + " (player, type, storage, armor, extra, effects, xp, lastused) VALUES ('"
                        + p.getUniqueId() + "', " + type + ", '" + pi.getStorage()
                        + "', '" + pi.getArmor() + "', '" + pi.getExtra() + "', '" + pi.getEffects()
                        + "', " + p.getTotalExperience() + ", " + Instant.now().getEpochSecond()
                        + ")");
                plugin.getDB()
                        .executeUpdate("UPDATE " + plugin.getDB().getInventoryTable() + " SET type = "
                                + type + ", storage = '" + pi.getStorage() + "', armor = '"
                                + pi.getArmor() + "', extra = '" + pi.getExtra() + "', effects = '"
                                + pi.getEffects() + "', xp = " + p.getTotalExperience()
                                + ", lastused = " + Instant.now().getEpochSecond()
                                + " WHERE player = '" + p.getUniqueId() + "'");
            } else {
                // Inserts a new row or updates the old one if it already exists
                plugin.getDB().executeUpdate("INSERT INTO " + plugin.getDB().getInventoryTable()
                        + " (player, type, storage, armor, extra, effects, xp, lastused) VALUES ('"
                        + p.getUniqueId() + "', " + type + ", '" + pi.getStorage()
                        + "', '" + pi.getArmor() + "', '" + pi.getExtra() + "', '" + pi.getEffects()
                        + "', " + p.getTotalExperience() + ", " + Instant.now().getEpochSecond()
                        + ") ON DUPLICATE KEY UPDATE type = " + type + ", storage = '"
                        + pi.getStorage() + "', armor = '" + pi.getArmor() + "', extra = '"
                        + pi.getExtra() + "', effects = '" + pi.getEffects() + "', xp = "
                        + p.getTotalExperience() + ", lastused = "
                        + Instant.now().getEpochSecond());
            }
        }

        InventoryHandler.removeSurvivalInv(p);
        InventoryHandler.removeCreativeInv(p);
    }

    public void loadInventory(Player p) {
        // No need to control bypassed players
        if (p.hasPermission("rc.bypass.tracking.inventory"))
            return;

        ResultSet rs = plugin.getDB()
                .executeQuery("SELECT * FROM " + plugin.getDB().getInventoryTable()
                        + " WHERE player = '" + p.getUniqueId() + "'");

        try {
            while (rs.next()) {
                GameMode gm = (rs.getInt("type") == 0) ? Bukkit.getDefaultGameMode()
                        : GameMode.CREATIVE;
                PlayerInfo pi = new PlayerInfo(rs.getString("storage"), rs.getString("armor"),
                        rs.getString("extra"), rs.getString("effects"), rs.getInt("xp"), gm);

                if (rs.getInt("type") == 0) {
                    InventoryHandler.saveSurvivalInv(p, pi);

                    if (RestrictedCreative.DEBUG)
                        System.out.println("loadInventory: is run " + pi.gm);
                } else {
                    InventoryHandler.saveCreativeInv(p, pi);

                    if (RestrictedCreative.DEBUG)
                        System.out.println("loadInventory: never run " + pi.gm);
                }
            }
        } catch (SQLException | NullPointerException e) {
            Bukkit.getLogger().severe("Database error:");
            e.printStackTrace();
        }

        if (RestrictedCreative.DEBUG)
            System.out.println("loadInventory: c?" + (InventoryHandler.getCreativeInv(p) != null)
                    + " s?" + (InventoryHandler.getSurvivalInv(p) != null));
    }

    public void debug(String message) {
        if (!plugin.config.debug) return;

        plugin.getLogger().log(Level.INFO, message);
    }
}
