package me.prunt.restrictedcreative.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class Utils {
    public static boolean isInstalled(String plugin) {
	return Bukkit.getPluginManager().getPlugin(plugin) != null;
    }

    // Return block position as a string
    public static String getBlockString(Block b) {
	return b.getWorld().getName() + ";" + b.getX() + ";" + b.getY() + ";" + b.getZ();
    }

    // Return location coordinates as a string
    public static String getLocString(Location loc) {
	return loc.getWorld().getName() + ";" + loc.getBlockX() + ";" + loc.getBlockZ();
    }

    // Return block from a position string
    public static Block getBlock(String s) {
	// Get coordinates from given string
	String[] sl = s.split(";");
	String world = sl[0];
	int x = Integer.valueOf(sl[1]);
	int y = Integer.valueOf(sl[2]);
	int z = Integer.valueOf(sl[3]);

	// Return null if world doesn't exist
	if (Bukkit.getServer().getWorld(world) == null)
	    return null;

	// Return block from given coordinates
	return Bukkit.getServer().getWorld(world).getBlockAt(x, y, z);
    }

    // Print error to console
    public static void log(String msg) {
	Bukkit.getLogger().severe(msg);
    }
}
