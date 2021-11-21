package solutions.nuhvel.spigot.rc.utils.external;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import solutions.nuhvel.spigot.rc.RestrictedCreative;

public class TownyAdvancedUtils {
    // Only allow players to build in their own Town
    public static boolean canBuildHere(RestrictedCreative plugin, Player p, Block b) {
        if (!plugin.config.limitations.regions.ownership.enabled)
            return false;

        var loc = b != null ? b.getLocation() : p.getLocation();

        try {
            var townBlock = TownyAPI.getInstance().getTownBlock(loc);
            if (townBlock == null)
                return false;

            var resident = TownyUniverse.getInstance().getResident(p.getUniqueId());
            if (resident == null)
                return false;

            if (resident.getTown().equals(townBlock.getTown()))
                return true;
        } catch (NotRegisteredException | NullPointerException e) {
            return false;
        }

        return false;
    }
}
