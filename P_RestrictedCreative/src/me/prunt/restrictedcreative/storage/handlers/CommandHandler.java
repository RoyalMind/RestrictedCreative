package me.prunt.restrictedcreative.storage.handlers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

public class CommandHandler {
	private static List<Player> addWithCommand = new ArrayList<>();
	private static List<Player> removeWithCommand = new ArrayList<>();
	private static List<Player> infoWithCommand = new ArrayList<>();

	public static List<Player> getAddWithCommand() {
		return addWithCommand;
	}

	public static List<Player> getRemoveWithCommand() {
		return removeWithCommand;
	}

	public static List<Player> getInfoWithCommand() {
		return infoWithCommand;
	}

	public static boolean isAddWithCommand(Player p) {
		return getAddWithCommand().contains(p);
	}

	public static boolean isRemoveWithCommand(Player p) {
		return getRemoveWithCommand().contains(p);
	}

	public static boolean isInfoWithCommand(Player p) {
		return getInfoWithCommand().contains(p);
	}

	public static void removeAddWithCommand(Player p) {
		if (isAddWithCommand(p))
			getAddWithCommand().remove(p);
	}

	public static void removeRemoveWithCommand(Player p) {
		if (isRemoveWithCommand(p))
			getRemoveWithCommand().remove(p);
	}

	public static void removeInfoWithCommand(Player p) {
		if (isInfoWithCommand(p))
			getInfoWithCommand().remove(p);
	}

	public static void setAddWithCommand(Player p) {
		getAddWithCommand().add(p);
	}

	public static void setRemoveWithCommand(Player p) {
		getRemoveWithCommand().add(p);
	}

	public static void setInfoWithCommand(Player p) {
		getInfoWithCommand().add(p);
	}
}
