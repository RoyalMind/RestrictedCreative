package me.prunt.restrictedcreative.commands;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.storage.handlers.BlockHandler;
import me.prunt.restrictedcreative.storage.handlers.CommandHandler;
import me.prunt.restrictedcreative.storage.handlers.EntityHandler;
import me.prunt.restrictedcreative.utils.Utils;

public class MainCommand implements CommandExecutor {
	private Main main;

	public MainCommand(Main plugin) {
		this.main = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0)
			return false;

		switch (args[0]) {
		case "reload":
			if (sender.hasPermission("rc.commands.reload")) {
				reload(sender);
				return true;
			}
			break;
		case "block":
			if (sender.hasPermission("rc.commands.block")) {
				block(sender, args);
				return true;
			}
			break;
		case "i-am-sure-i-want-to-delete-all-plugin-data-from-database":
			if (sender.hasPermission("rc.commands.delete")) {
				delete(sender);
				return true;
			}
			break;
		default:
			return false;
		}

		main.getUtils().sendMessage(sender, false, "no-permission");
		return true;
	}

	private void reload(CommandSender sender) {
		main.reloadConfigs();
		main.registerListeners();
		main.getUtils().sendMessage(sender, true, "reloaded");
	}

	private void delete(CommandSender sender) {
		main.getDB().executeUpdate("DELETE FROM " + main.getDB().getBlocksTable());

		// Loops through worlds
		for (World w : main.getServer().getWorlds()) {
			// Leaves out the disabled ones
			if (main.getUtils().isDisabledWorld(w.getName()))
				continue;

			// Loops through entities
			for (Entity e : w.getEntities()) {
				if (EntityHandler.isTracked(e))
					EntityHandler.removeTracking(e);
			}
		}

		main.getUtils().sendMessage(sender, true, "database.deleted");
	}

	private void block(CommandSender sender, String[] args) {
		if (args.length < 2) {
			main.getUtils().sendMessage(sender, false, "usage.block");
			return;
		}

		if (args[1].equalsIgnoreCase("stats")) {
			String msg = main.getUtils().getMessage(true, "block.stats").replaceAll("%total%",
					BlockHandler.getTotalCount());
			Utils.sendMessage(sender, msg);
			return;
		}

		if (!(sender instanceof Player)) {
			main.getUtils().sendMessage(sender, true, "no-console");
			return;
		}

		Player p = (Player) sender;

		switch (args[1]) {
		case "add":
			if (CommandHandler.getAddWithCommand().contains(p)) {
				CommandHandler.getAddWithCommand().remove(p);
				main.getUtils().sendMessage(sender, true, "block.cancel");
			} else {
				CommandHandler.getAddWithCommand().add(p);
				main.getUtils().sendMessage(sender, true, "block.add.add");
			}
			break;
		case "remove":
			if (CommandHandler.getRemoveWithCommand().contains(p)) {
				CommandHandler.getRemoveWithCommand().remove(p);
				main.getUtils().sendMessage(sender, true, "block.cancel");
			} else {
				CommandHandler.getRemoveWithCommand().add(p);
				main.getUtils().sendMessage(sender, true, "block.remove.remove");
			}
			break;
		case "info":
			if (CommandHandler.getInfoWithCommand().contains(p)) {
				CommandHandler.getInfoWithCommand().remove(p);
				main.getUtils().sendMessage(sender, true, "block.cancel");
			} else {
				CommandHandler.getInfoWithCommand().add(p);
				main.getUtils().sendMessage(sender, true, "block.info.info");
			}
			break;
		default:
			main.getUtils().sendMessage(sender, false, "usage.block");
		}
	}
}
