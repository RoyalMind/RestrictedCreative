package solutions.nuhvel.spigot.rc.storage.database;

import org.bukkit.Bukkit;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.storage.config.config.database.DatabaseType;
import solutions.nuhvel.spigot.rc.utils.MessagingUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

public class DataSyncRunnable implements Runnable {
    private final RestrictedCreative plugin;
    private final Set<BlockModel> toAdd;
    private final Set<BlockModel> toRemove;
    private final boolean onDisable;

    public DataSyncRunnable(RestrictedCreative plugin, Set<BlockModel> toAdd, Set<BlockModel> toRemove,
            boolean onDisable) {
        this.plugin = plugin;
        this.toAdd = toAdd;
        this.toRemove = toRemove;
        this.onDisable = onDisable;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();

        saveData(toAdd, toRemove);

        String took = String.valueOf(System.currentTimeMillis() - start);

        if (onDisable) {
            plugin.blockRepository.addToDatabase.clear();
            plugin.blockRepository.removeFromDatabase.clear();

            MessagingUtils.sendMessage(Bukkit.getConsoleSender(), plugin.messagingUtils
                    .getFormattedMessage(true, plugin.messages.database.done)
                    .replaceAll("%mills%", took));
        } else {
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.blockRepository.addToDatabase.removeAll(toAdd);
                plugin.blockRepository.removeFromDatabase.removeAll(toRemove);

                MessagingUtils.sendMessage(Bukkit.getConsoleSender(), plugin.messagingUtils
                        .getFormattedMessage(true, plugin.messages.database.done)
                        .replaceAll("%mills%", took));
            });
        }
    }

    private void saveData(Set<BlockModel> toAdd, Set<BlockModel> toRemove) {
        int addedCount = toAdd.size();
        int removedCount = toRemove.size();

        // If no changes should be made
        if (addedCount + removedCount == 0)
            return;

        plugin.messagingUtils.sendMessage(Bukkit.getConsoleSender(), true, plugin.messages.database.saving);

        plugin.database.setAutoCommit(false);

        if (addedCount > 0)
            syncData(toAdd, getAddStatement(), plugin.messages.database.added, false);
        if (removedCount > 0)
            syncData(toRemove, getRemoveStatement(), plugin.messages.database.removed, true);

        plugin.database.setAutoCommit(true);
    }

    private String getAddStatement() {
        String or = plugin.config.database.type == DatabaseType.SQLITE ? "OR " : "";
        return "INSERT " + or + "IGNORE INTO " + plugin.database.getBlocksTable() +
                " (x, y, z, world, chunk_x, chunk_z, owner, created) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    }

    private String getRemoveStatement() {
        return "DELETE FROM " + plugin.database.getBlocksTable() + " WHERE x = ? AND y = ? AND z = ? AND world = ?";
    }

    private void syncData(Set<BlockModel> blocks, String statement, String message, boolean onlyBasicData) {
        PreparedStatement ps = plugin.database.getStatement(statement);
        int count = 0;

        try {
            for (BlockModel model : blocks) {
                var location = model.block.getLocation();
                if (location.getWorld() == null)
                    continue;

                ps.setInt(1, location.getBlockX());
                ps.setInt(2, location.getBlockY());
                ps.setInt(3, location.getBlockZ());
                ps.setString(4, location.getWorld().getName());
                if (!onlyBasicData) {
                    ps.setInt(5, location.getChunk().getX());
                    ps.setInt(6, location.getChunk().getZ());
                    ps.setString(7, model.owner.getUniqueId().toString());
                    ps.setLong(8, model.created.getTime() / 1000);
                }

                ps.addBatch();
                count++;

                if (count % 4096 == 0) {
                    ps.executeBatch();
                    ps.clearBatch();
                }
            }

            ps.executeBatch();
            ps.clearBatch();
            plugin.database.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        MessagingUtils.sendMessage(Bukkit.getConsoleSender(),
                plugin.messagingUtils.getFormattedMessage(true, message).replaceAll("%blocks%", String.valueOf(count)));
    }
}
