package me.prunt.restrictedcreative.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

import me.prunt.restrictedcreative.Main;

public class TownyAdvancedUtils {
	public static boolean canBuildHere(Main main, Player p, Block b, Material m) {
		if (!main.getSettings().isEnabled("limit.regions.owner-based.enabled"))
			return false;

		// Gets the player or block location
		Location loc = (b != null) ? b.getLocation() : p.getLocation();

		// Owner check
		try {
			TownyAPI towny = TownyAPI.getInstance();

			Resident resident = towny.getDataSource().getResident(p.getName());
			Town town = towny.getTownBlock(loc).getTown();

			if (resident.getTown().equals(town))
				return true;
		} catch (NotRegisteredException | NullPointerException e) {
			return false;
		}

		return false;
	}
}
