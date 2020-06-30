package me.prunt.restrictedcreative.storage;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.bukkit.Bukkit;

import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.storage.handlers.BlockHandler;
import me.prunt.restrictedcreative.utils.Utils;

public class SyncData implements Runnable {
	private Main main;
	private List<String> toAdd;
	private List<String> toRemove;
	private boolean onDisable;

	public SyncData(Main main, List<String> fAdd, List<String> fDel, boolean onDisable) {
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

	private void syncData(List<String> blocks, String statement, String message) {
		PreparedStatement ps = main.getDB().getStatement(statement);
		int count = 0;

		try {
			for (String block : blocks) {
				ps.setString(1, block);
				ps.executeUpdate();
				count++;

				if (count % 5000 == 0)
					main.getDB().commit();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		main.getDB().commit();

		Utils.sendMessage(Bukkit.getConsoleSender(), main.getUtils().getMessage(true, message)
				.replaceAll("%blocks%", String.valueOf(count)));
	}

	/*private void addData() {
		List<String> blocks = toAdd;
		String or = BlockHandler.isUsingSQLite() ? "OR " : "";

		String statement = "INSERT " + or + "IGNORE INTO " + main.getDB().getBlocksTable()
				+ " (block) VALUES ";

		for (int i = 0; i < blocks.size(); i++) {
			statement += "('" + blocks.get(i) + "'),";
		}
		statement.substring(0, statement.length() - 1); // remove last comma

		main.getDB().executeUpdate(statement);

		Utils.sendMessage(Bukkit.getConsoleSender(),
				main.getUtils().getMessage(true, "database.added").replaceAll("%blocks%",
						String.valueOf(blocks.size())));
	}

	private void removeData() {
		List<String> blocks = toRemove;

		String statement = "DELETE FROM " + main.getDB().getBlocksTable() + " WHERE ";

		for (int i = 0; i < blocks.size(); i++) {
			statement += "block = '" + blocks.get(i) + "' OR ";
		}
		statement.substring(0, statement.length() - 4); // remove last OR

		main.getDB().executeUpdate(statement);

		Utils.sendMessage(Bukkit.getConsoleSender(),
				main.getUtils().getMessage(true, "database.removed").replaceAll("%blocks%",
						String.valueOf(blocks.size())));
	}*/
}
