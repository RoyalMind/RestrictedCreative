package me.prunt.restrictedcreative.store;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.utils.Utils;

public class DataHandler {
    private static List<String> addToDatabase = new ArrayList<>();
    private static List<String> removeFromDatabase = new ArrayList<>();

    private static boolean usingOldAliases = false;

    private static List<Player> addWithCommand = new ArrayList<>();
    private static List<Player> removeWithCommand = new ArrayList<>();
    private static List<Player> infoWithCommand = new ArrayList<>();

    public static boolean isCreative(Block b) {
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
	if (b == null || isCreative(b))
	    return;

	b.setMetadata("GMC", Main.getFMV());
	addToDatabase.add(Utils.getBlockString(b));
	removeFromDatabase.remove(Utils.getBlockString(b));
    }

    public static void removeTracking(Block b) {
	if (b == null || !isCreative(b))
	    return;

	b.removeMetadata("GMC", Main.getInstance());
	addToDatabase.remove(Utils.getBlockString(b));
	removeFromDatabase.add(Utils.getBlockString(b));
    }

    public static boolean isCreative(Entity e) {
	if (e == null)
	    return false;

	return e.getScoreboardTags().contains("GMC");
    }

    public static boolean isCreativeItem(ItemFrame frame) {
	if (frame == null)
	    return false;

	return frame.getScoreboardTags().contains("GMC_IF");
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

    public static boolean isUsingOldAliases() {
	return usingOldAliases;
    }

    public static void setUsingOldAliases(boolean usingOldAliases) {
	DataHandler.usingOldAliases = usingOldAliases;
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
}
