package solutions.nuhvel.spigot.rc.utils.external;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import solutions.nuhvel.spigot.rc.RestrictedCreative;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

public class GriefPreventionUtils {
	public static boolean canBuildHere(RestrictedCreative plugin, Player p, Block b, Material m) {
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(b.getLocation(), false, null);

		// Wilderness
		if (claim == null)
			return false;

		if (!plugin.config.limitations.regions.ownership.enabled)
			return false;

		// Owner check
		if (claim.getOwnerName().equalsIgnoreCase(p.getName()))
			return true;

		if (!plugin.config.limitations.regions.ownership.allowMembers)
			return false;

		// Member check
		if (claim.allowBuild(p, m) == null)
			return true;

		return false;
	}
}
