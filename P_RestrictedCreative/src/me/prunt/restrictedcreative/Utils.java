package me.prunt.restrictedcreative;

import org.bukkit.Bukkit;

public class Utils {
    public static boolean isInstalled(String plugin) {
	return Bukkit.getPluginManager().getPlugin(plugin) != null;
    }
}
