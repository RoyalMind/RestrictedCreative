package solutions.nuhvel.spigot.rc.utils;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.potion.PotionEffect;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.storage.config.config.miscellaneous.ArmorMaterial;
import solutions.nuhvel.spigot.rc.storage.database.PlayerInfo;
import solutions.nuhvel.spigot.rc.storage.handlers.InventoryHandler;
import solutions.nuhvel.spigot.rc.storage.handlers.PermissionHandler;
import solutions.nuhvel.spigot.rc.storage.handlers.TrackableHandler;
import solutions.nuhvel.spigot.rc.utils.helpers.ArmorHelper;
import solutions.nuhvel.spigot.rc.utils.minecraft.ServerUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class Utils {
    private final RestrictedCreative plugin;

    public Utils(RestrictedCreative plugin) {
        this.plugin = plugin;
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

        if (InventoryHandler.getSurvivalInv(p) == null && InventoryHandler.getCreativeInv(p) == null)
            return;

        if (RestrictedCreative.DEBUG)
            System.out.println("saveInventory: s?" + (InventoryHandler.getSurvivalInv(p) != null) + " c?" +
                    (InventoryHandler.getCreativeInv(p) != null));

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
            if (TrackableHandler.isUsingSQLite()) {
                // Inserts a new row if it doesn't exist already and updates it with new values
                plugin
                        .getDB()
                        .executeUpdate("INSERT OR IGNORE INTO " + plugin.getDB().getInventoryTable() +
                                " (player, type, storage, armor, extra, effects, xp, lastused) VALUES ('" +
                                p.getUniqueId() + "', " + type + ", '" + pi.getStorage() + "', '" + pi.getArmor() +
                                "', '" + pi.getExtra() + "', '" + pi.getEffects() + "', " + p.getTotalExperience() +
                                ", " + Instant.now().getEpochSecond() + ")");
                plugin
                        .getDB()
                        .executeUpdate("UPDATE " + plugin.getDB().getInventoryTable() + " SET type = " + type +
                                ", storage = '" + pi.getStorage() + "', armor = '" + pi.getArmor() + "', extra = '" +
                                pi.getExtra() + "', effects = '" + pi.getEffects() + "', xp = " +
                                p.getTotalExperience() + ", lastused = " + Instant.now().getEpochSecond() +
                                " WHERE player = '" + p.getUniqueId() + "'");
            } else {
                // Inserts a new row or updates the old one if it already exists
                plugin
                        .getDB()
                        .executeUpdate("INSERT INTO " + plugin.getDB().getInventoryTable() +
                                " (player, type, storage, armor, extra, effects, xp, lastused) VALUES ('" +
                                p.getUniqueId() + "', " + type + ", '" + pi.getStorage() + "', '" + pi.getArmor() +
                                "', '" + pi.getExtra() + "', '" + pi.getEffects() + "', " + p.getTotalExperience() +
                                ", " + Instant.now().getEpochSecond() + ") ON DUPLICATE KEY UPDATE type = " + type +
                                ", storage = '" + pi.getStorage() + "', armor = '" + pi.getArmor() + "', extra = '" +
                                pi.getExtra() + "', effects = '" + pi.getEffects() + "', xp = " +
                                p.getTotalExperience() + ", lastused = " + Instant.now().getEpochSecond());
            }
        }

        InventoryHandler.removeSurvivalInv(p);
        InventoryHandler.removeCreativeInv(p);
    }

    public void loadInventory(Player p) {
        // No need to control bypassed players
        if (p.hasPermission("rc.bypass.tracking.inventory"))
            return;

        ResultSet rs = plugin
                .getDB()
                .executeQuery(
                        "SELECT * FROM " + plugin.getDB().getInventoryTable() + " WHERE player = '" + p.getUniqueId() +
                                "'");

        try {
            while (rs.next()) {
                GameMode gm = (rs.getInt("type") == 0) ? Bukkit.getDefaultGameMode() : GameMode.CREATIVE;
                PlayerInfo pi = new PlayerInfo(rs.getString("storage"), rs.getString("armor"), rs.getString("extra"),
                        rs.getString("effects"), rs.getInt("xp"), gm);

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
            System.out.println("loadInventory: c?" + (InventoryHandler.getCreativeInv(p) != null) + " s?" +
                    (InventoryHandler.getSurvivalInv(p) != null));
    }

    public void debug(String message) {
        if (!plugin.config.debug)
            return;

        plugin.getLogger().log(Level.INFO, message);
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

    private void separateInventory(Player p, boolean toCreative) {
        PlayerInfo pi = getPlayerInfo(p);

        // Stores Player with PlayerInfo into HashMap
        if (toCreative) {
            InventoryHandler.saveSurvivalInv(p, pi);
            setInventory(p, InventoryHandler.getCreativeInv(p));
        } else {
            InventoryHandler.saveCreativeInv(p, pi);
            setInventory(p, InventoryHandler.getSurvivalInv(p));
        }
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
        if (ServerUtils.isInstalled("Vault") && plugin.getSettings().isEnabled("creative.permissions.use-vault")) {
            RegisteredServiceProvider<Permission> provider =
                    Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
            Permission vault = Objects.requireNonNull(provider).getProvider();

            if (toCreative) {
                for (String perm : plugin.getSettings().getStringList("creative.permissions.list")) {
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

                for (String perm : plugin.getSettings().getStringList("creative.permissions.list")) {
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
}
