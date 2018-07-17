package me.prunt.restrictedcreative.store;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.metadata.MetadataValue;

import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.utils.Utils;

public class DataHandler {
    private static List<String> addToDatabase;
    private static List<String> removeFromDatabase;

    private static boolean usingOldAliases = false;

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

    public static void addForTracking(Block b) {
	if (b == null || isCreative(b))
	    return;

	b.setMetadata("GMC", Main.getFMV());
	addToDatabase.add(Utils.getBlockString(b));
	removeFromDatabase.remove(Utils.getBlockString(b));
    }

    public static void removeFromTracking(Block b) {
	if (b == null || !isCreative(b))
	    return;

	b.removeMetadata("GMC", Main.getInstance());
	addToDatabase.remove(Utils.getBlockString(b));
	removeFromDatabase.add(Utils.getBlockString(b));
    }

    public static boolean isUsingOldAliases() {
	return usingOldAliases;
    }

    public static void setUsingOldAliases(boolean usingOldAliases) {
	DataHandler.usingOldAliases = usingOldAliases;
    }
}
