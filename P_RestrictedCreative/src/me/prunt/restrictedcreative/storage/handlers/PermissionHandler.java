package me.prunt.restrictedcreative.storage.handlers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

public class PermissionHandler {
	private static Map<Player, Set<String>> vaultPerms = new HashMap<>();
	private static Map<Player, Set<String>> vaultGroups = new HashMap<>();
	private static Map<Player, PermissionAttachment> permissions = new HashMap<>();

	private static boolean usingOldAliases = false;

	public static PermissionAttachment getPerms(Player p) {
		return permissions.containsKey(p) ? permissions.get(p) : null;
	}

	public static void setPerms(Player p, PermissionAttachment attachment) {
		permissions.put(p, attachment);
	}

	public static void removePerms(Player p) {
		if (permissions.containsKey(p))
			permissions.remove(p);
	}

	public static Set<String> getVaultPerms(Player p) {
		return vaultPerms.containsKey(p) ? vaultPerms.get(p) : null;
	}

	public static void addVaultPerm(Player p, String perm) {
		Set<String> prevPerms = getVaultPerms(p);

		if (prevPerms != null) {
			prevPerms.add(perm);
		} else {
			vaultPerms.put(p, new HashSet<>(Arrays.asList(perm)));
		}
	}

	public static void removeVaultPerm(Player p) {
		if (vaultPerms.containsKey(p))
			vaultPerms.remove(p);
	}

	public static Set<String> getVaultGroups(Player p) {
		return vaultGroups.containsKey(p) ? vaultGroups.get(p) : null;
	}

	public static void addVaultGroup(Player p, String group) {
		Set<String> prevGroups = getVaultGroups(p);

		if (prevGroups != null) {
			vaultGroups.get(p).add(group);
		} else {
			vaultGroups.put(p, new HashSet<>(Arrays.asList(group)));
		}
	}

	public static void removeVaultGroup(Player p) {
		if (vaultGroups.containsKey(p))
			vaultGroups.remove(p);
	}

	public static boolean isUsingOldAliases() {
		return usingOldAliases;
	}

	public static void setUsingOldAliases(boolean usingOldAliases) {
		PermissionHandler.usingOldAliases = usingOldAliases;
	}
}
