package solutions.nuhvel.spigot.rc.storage.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

import org.bukkit.Bukkit;

import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.storage.handlers.BlockHandler;
import solutions.nuhvel.spigot.rc.utils.Utils;

public class DataSyncRunnable implements Runnable {
	private final RestrictedCreative plugin;
	private final Set<BlockModel> toAdd;
	private final Set<BlockModel> toRemove;
	private final boolean onDisable;

	public DataSyncRunnable(RestrictedCreative plugin, Set<BlockModel> fAdd, Set<BlockModel> fDel, boolean onDisable) {
		this.plugin = plugin;
		this.toAdd = fAdd;
		this.toRemove = fDel;
		this.onDisable = onDisable;
	}

	@Override
	public void run() {
		int addedCount = toAdd.size();
		int removedCount = toRemove.size();

		// If no changes should be made
		if (addedCount + removedCount == 0)
			return;

		long start = System.currentTimeMillis();
		String or = BlockHandler.isUsingSQLite() ? "OR " : "";

		plugin.getUtils().sendMessage(Bukkit.getConsoleSender(), true, "database.save");

		plugin.getDB().setAutoCommit(false);

		if (addedCount > 0)
			syncData(toAdd, "INSERT " + or + "IGNORE INTO " + plugin.getDB().getBlocksTable()
					+ " (block) VALUES (?)", "database.added");

		if (removedCount > 0)
			syncData(toRemove, "DELETE FROM " + plugin.getDB().getBlocksTable() + " WHERE block = ?",
					"database.removed");

		plugin.getDB().setAutoCommit(true);

		if (onDisable) {
			BlockHandler.addToDatabase.clear();
			BlockHandler.removeFromDatabase.clear();

			String took = String.valueOf(System.currentTimeMillis() - start);

			Utils.sendMessage(Bukkit.getConsoleSender(),
					plugin.getUtils().getFormattedMessage(true, "database.done").replaceAll("%mills%", took));
		} else {
			Bukkit.getScheduler().runTask(plugin, new Runnable() {
				@Override
				public void run() {
					BlockHandler.addToDatabase.removeAll(toAdd);
					BlockHandler.removeFromDatabase.removeAll(toRemove);

					String took = String.valueOf(System.currentTimeMillis() - start);

					Utils.sendMessage(Bukkit.getConsoleSender(), plugin.getUtils()
							.getFormattedMessage(true, "database.done").replaceAll("%mills%", took));
				}
			});
		}
	}

	private void syncData(Set<BlockModel> blocks, String statement, String message) {
		PreparedStatement ps = plugin.getDB().getStatement(statement);
		int count = 0;

		try {
			for (BlockModel block : blocks) {
				ps.setString(1, block);
				ps.addBatch();
				count++;

				if (count % 4096 == 0) {
					ps.executeBatch();
					ps.clearBatch();
				}
			}

			ps.executeBatch();
			ps.clearBatch();
			plugin.getDB().commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		Utils.sendMessage(Bukkit.getConsoleSender(), plugin.getUtils().getFormattedMessage(true, message)
				.replaceAll("%blocks%", String.valueOf(count)));
	}
}
