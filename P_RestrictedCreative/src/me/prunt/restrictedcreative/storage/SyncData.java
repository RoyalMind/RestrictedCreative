package me.prunt.restrictedcreative.storage;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

import org.bukkit.Bukkit;

import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.storage.handlers.BlockHandler;
import me.prunt.restrictedcreative.utils.Utils;

public class SyncData implements Runnable {
	private Main main;
	private Set<String> toAdd;
	private Set<String> toRemove;
	private boolean onDisable;

	public SyncData(Main main, Set<String> fAdd, Set<String> fDel, boolean onDisable) {
		this.main = main;
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

		main.getUtils().sendMessage(Bukkit.getConsoleSender(), true, "database.save");

		main.getDB().setAutoCommit(false);

		if (addedCount > 0)
			syncData(toAdd, "INSERT " + or + "IGNORE INTO " + main.getDB().getBlocksTable()
					+ " (block) VALUES (?)", "database.added");

		if (removedCount > 0)
			syncData(toRemove, "DELETE FROM " + main.getDB().getBlocksTable() + " WHERE block = ?",
					"database.removed");

		main.getDB().setAutoCommit(true);

		if (onDisable) {
			BlockHandler.addToDatabase.clear();
			BlockHandler.removeFromDatabase.clear();

			String took = String.valueOf(System.currentTimeMillis() - start);

			Utils.sendMessage(Bukkit.getConsoleSender(),
					main.getUtils().getMessage(true, "database.done").replaceAll("%mills%", took));
		} else {
			Bukkit.getScheduler().runTask(main, new Runnable() {
				@Override
				public void run() {
					BlockHandler.addToDatabase.removeAll(toAdd);
					BlockHandler.removeFromDatabase.removeAll(toRemove);

					String took = String.valueOf(System.currentTimeMillis() - start);

					Utils.sendMessage(Bukkit.getConsoleSender(), main.getUtils()
							.getMessage(true, "database.done").replaceAll("%mills%", took));
				}
			});
		}
	}

	private void syncData(Set<String> blocks, String statement, String message) {
		PreparedStatement ps = main.getDB().getStatement(statement);
		int count = 0;

		if (Main.DEBUG)
			System.out.println("syncData: starting");

		try {
			for (String block : blocks) {
				ps.setString(1, block);
				ps.addBatch();
				count++;

				if (count % 4096 == 0) {
					if (Main.DEBUG)
						System.out.println("executeBatch: " + count);

					ps.executeBatch();
					ps.clearBatch();
				}
			}

			if (Main.DEBUG)
				System.out.println("executeBatch: " + count);

			ps.executeBatch();
			ps.clearBatch();
			main.getDB().commit();

			if (Main.DEBUG)
				System.out.println("commited");

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				ps.close();

				if (Main.DEBUG)
					System.out.println("closed");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		Utils.sendMessage(Bukkit.getConsoleSender(), main.getUtils().getMessage(true, message)
				.replaceAll("%blocks%", String.valueOf(count)));
	}
}
