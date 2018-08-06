package me.prunt.restrictedcreative.storage;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;

import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.utils.Utils;

public class SyncData implements Runnable {
    private Main main;
    private List<String> toAdd;
    private List<String> toRemove;
    private List<UUID> toAddInv;
    private List<UUID> toRemoveInv;

    public SyncData(Main main, List<String> fAdd, List<String> fDel, List<UUID> fAddInv, List<UUID> fDelInv) {
	this.main = main;
	this.toAdd = fAdd;
	this.toRemove = fDel;
	this.toAddInv = fAddInv;
	this.toRemoveInv = fDelInv;
    }

    @Override
    public void run() {
	int addedCount = toAdd.size();
	int removedCount = toRemove.size();

	// If no changes should be made
	if (addedCount + removedCount == 0)
	    return;

	long start = System.currentTimeMillis();
	String or = DataHandler.isUsingSQLite() ? "or" : "";

	main.getUtils().sendMessage(Bukkit.getConsoleSender(), true, "database.save");

	main.getDB().setAutoCommit(false);

	if (addedCount > 0) {
	    for (String str : toAdd) {
		main.getDB().executeUpdate("INSERT " + or + "IGNORE INTO " + main.getDB().getBlocksTable()
			+ " (block) VALUES ('" + str + "')");
	    }
	    Utils.sendMessage(Bukkit.getConsoleSender(), main.getUtils().getMessage(true, "database.added")
		    .replaceAll("%blocks%", String.valueOf(addedCount)));
	}
	if (removedCount > 0) {
	    for (String str : toRemove) {
		main.getDB()
			.executeUpdate("DELETE FROM " + main.getDB().getBlocksTable() + " WHERE block = '" + str + "'");
	    }
	    Utils.sendMessage(Bukkit.getConsoleSender(), main.getUtils().getMessage(true, "database.removed")
		    .replaceAll("%blocks%", String.valueOf(removedCount)));
	}

	main.getDB().commit();
	main.getDB().setAutoCommit(true);

	Bukkit.getScheduler().runTask(main, new Runnable() {
	    @Override
	    public void run() {
		DataHandler.addToDatabase.removeAll(toAdd);
		DataHandler.removeFromDatabase.removeAll(toRemove);

		String took = String.valueOf(System.currentTimeMillis() - start);

		Utils.sendMessage(Bukkit.getConsoleSender(),
			main.getUtils().getMessage(true, "database.done").replaceAll("%mills%", took));
	    }
	});
    }
}
