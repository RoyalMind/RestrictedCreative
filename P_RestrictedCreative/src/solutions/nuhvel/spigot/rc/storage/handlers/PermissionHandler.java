package solutions.nuhvel.spigot.rc.storage.handlers;

import java.util.*;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

public class PermissionHandler {
	private static final Map<Player, Set<String>> vaultPerms = new HashMap<>();
	private static final Map<Player, Set<String>> vaultGroups = new HashMap<>();
	private static final Map<Player, PermissionAttachment> permissions = new HashMap<>();

	public static PermissionAttachment getPerms(Player p) {
		return permissions.getOrDefault(p, null);
	}

	public static void setPerms(Player p, PermissionAttachment attachment) {
		permissions.put(p, attachment);
	}

	public static void removePerms(Player p) {
		permissions.remove(p);
	}

	public static Set<String> getVaultPerms(Player p) {
		return vaultPerms.getOrDefault(p, null);
	}

	public static void addVaultPerm(Player p, String perm) {
		Set<String> prevPerms = getVaultPerms(p);

		if (prevPerms != null) {
			prevPerms.add(perm);
		} else {
			vaultPerms.put(p, new HashSet<>(Collections.singletonList(perm)));
		}
	}

	public static void removeVaultPerm(Player p) {
		vaultPerms.remove(p);
	}

	public static Set<String> getVaultGroups(Player p) {
		return vaultGroups.getOrDefault(p, null);
	}

	public static void addVaultGroup(Player p, String group) {
		Set<String> prevGroups = getVaultGroups(p);

		if (prevGroups != null) {
			vaultGroups.get(p).add(group);
		} else {
			vaultGroups.put(p, new HashSet<>(Collections.singletonList(group)));
		}
	}

	public static void removeVaultGroup(Player p) {
		vaultGroups.remove(p);
	}
}
