package me.prunt.restrictedcreative.utils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import me.prunt.restrictedcreative.Main;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

public class GriefPreventionUtils {
	public static boolean canBuildHere(Main main, Player p, Block b, Material m) {
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(b.getLocation(), false, null);

		// Wilderness
		if (claim == null)
			return false;

		if (!main.getSettings().isEnabled("limit.regions.owner-based.enabled"))
			return false;

		// Owner check
		if (claim.getOwnerName().equalsIgnoreCase(p.getName()))
			return true;

		if (!main.getSettings().isEnabled("limit.regions.owner-based.allow-members"))
			return false;

		// Member check
		if (claim.allowBuild(p, m) == null)
			return true;

		return false;
	}
}
