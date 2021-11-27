package solutions.nuhvel.spigot.rc.utils.helpers;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.storage.config.config.database.DatabaseType;
import solutions.nuhvel.spigot.rc.storage.database.PlayerInfo;
import solutions.nuhvel.spigot.rc.storage.handlers.InventoryHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class InventoryHelper {
    private final RestrictedCreative plugin;

    public InventoryHelper(RestrictedCreative plugin) {
        this.plugin = plugin;
    }

    public void saveInventory(Player p) {
        // No need to control disabled features
        if (!plugin.config.tracking.inventories.saving) {
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
            if (plugin.database.type == DatabaseType.SQLITE) {
                // Inserts a new row if it doesn't exist already and updates it with new values
                plugin.database.executeUpdate("INSERT OR IGNORE INTO " + plugin.database.getInventoryTable() +
                        " (player, type, storage, armor, extra, effects, xp, last_used) VALUES ('" + p.getUniqueId() +
                        "', " + type + ", '" + pi.getStorage() + "', '" + pi.getArmor() + "', '" + pi.getExtra() +
                        "', '" + pi.getEffects() + "', " + p.getTotalExperience() + ", " +
                        Instant.now().getEpochSecond() + ")");
                plugin.database.executeUpdate(
                        "UPDATE " + plugin.database.getInventoryTable() + " SET type = " + type + ", storage = '" +
                                pi.getStorage() + "', armor = '" + pi.getArmor() + "', extra = '" + pi.getExtra() +
                                "', effects = '" + pi.getEffects() + "', xp = " + p.getTotalExperience() +
                                ", last_used = " + Instant.now().getEpochSecond() + " WHERE player = '" +
                                p.getUniqueId() + "'");
            } else {
                // Inserts a new row or updates the old one if it already exists
                plugin.database.executeUpdate("INSERT INTO " + plugin.database.getInventoryTable() +
                        " (player, type, storage, armor, extra, effects, xp, last_used) VALUES ('" + p.getUniqueId() +
                        "', " + type + ", '" + pi.getStorage() + "', '" + pi.getArmor() + "', '" + pi.getExtra() +
                        "', '" + pi.getEffects() + "', " + p.getTotalExperience() + ", " +
                        Instant.now().getEpochSecond() + ") ON DUPLICATE KEY UPDATE type = " + type + ", storage = '" +
                        pi.getStorage() + "', armor = '" + pi.getArmor() + "', extra = '" + pi.getExtra() +
                        "', effects = '" + pi.getEffects() + "', xp = " + p.getTotalExperience() + ", last_used = " +
                        Instant.now().getEpochSecond());
            }
        }

        InventoryHandler.removeSurvivalInv(p);
        InventoryHandler.removeCreativeInv(p);
    }

    public void loadInventory(Player p) {
        // No need to control bypassed players
        if (p.hasPermission("rc.bypass.tracking.inventory"))
            return;

        ResultSet rs = plugin.database.executeQuery(
                "SELECT * FROM " + plugin.database.getInventoryTable() + " WHERE player = '" + p.getUniqueId() + "'");

        try {
            while (rs.next()) {
                GameMode gm = (rs.getInt("type") == 0) ? Bukkit.getDefaultGameMode() : GameMode.CREATIVE;
                PlayerInfo pi = new PlayerInfo(rs.getString("storage"), rs.getString("armor"), rs.getString("extra"),
                        rs.getString("effects"), rs.getInt("xp"), gm);

                if (rs.getInt("type") == 0) {
                    InventoryHandler.saveSurvivalInv(p, pi);
                } else {
                    InventoryHandler.saveCreativeInv(p, pi);
                }
            }
        } catch (SQLException | NullPointerException e) {
            Bukkit.getLogger().severe("Database error:");
            e.printStackTrace();
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

    void separateInventory(Player p, boolean toCreative) {
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
}
