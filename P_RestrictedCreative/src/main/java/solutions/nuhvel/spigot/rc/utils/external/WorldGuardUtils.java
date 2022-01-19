package solutions.nuhvel.spigot.rc.utils.external;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import solutions.nuhvel.spigot.rc.RestrictedCreative;

public class WorldGuardUtils {
    public static boolean canBuildHere(RestrictedCreative plugin, Player p, Block b) {
        WorldGuardPlugin worldGuard = (WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuard == null)
            return true;

        Location location = b != null ? b.getLocation() : p.getLocation();
        LocalPlayer localPlayer = worldGuard.wrapPlayer(p);

        // Gets all regions covering the block or player location
        RegionQuery rq = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        ApplicableRegionSet set = rq.getApplicableRegions(BukkitAdapter.adapt(location));

        // Loops through applicable regions
        for (ProtectedRegion rg : set) {
            // Whitelist check
            if (plugin.config.limitations.regions.whitelisted.contains(rg.getId()))
                return true;

            // Owner check
            if (plugin.config.limitations.regions.ownership.enabled) {
                if (rg.isOwner(localPlayer))
                    return true;

                // Member check
                if (plugin.config.limitations.regions.ownership.allowMembers)
                    if (rg.isMember(localPlayer))
                        return true;
            }
        }

        return false;
    }
}
