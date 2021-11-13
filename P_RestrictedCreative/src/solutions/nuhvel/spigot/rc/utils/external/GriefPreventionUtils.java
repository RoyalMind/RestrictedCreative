package solutions.nuhvel.spigot.rc.utils.external;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import solutions.nuhvel.spigot.rc.RestrictedCreative;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

public class GriefPreventionUtils {
	public static boolean canBuildHere(RestrictedCreative restrictedCreative, Player p, Block b, Material m) {
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(b.getLocation(), false, null);

		// Wilderness
		if (claim == null)
			return false;

		if (!restrictedCreative.getSettings().isEnabled("limit.regions.owner-based.enabled"))
			return false;

		// Owner check
		if (claim.getOwnerName().equalsIgnoreCase(p.getName()))
			return true;

		if (!restrictedCreative.getSettings().isEnabled("limit.regions.owner-based.allow-members"))
			return false;

		// Member check
		if (claim.allowBuild(p, m) == null)
			return true;

		return false;
	}
}
