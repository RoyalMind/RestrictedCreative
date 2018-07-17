package me.prunt.restrictedcreative.commands;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.store.DataHandler;

public class MainCommand implements CommandExecutor {
    private Main main;

    public MainCommand(Main plugin) {
	this.main = plugin;
    }

    private void reload(CommandSender sender) {
	main.loadConfig();
	main.sendMessage(sender, true, "reloaded");
    }

    private void delete(CommandSender sender) {
	main.getDB().executeUpdate("DELETE FROM " + main.getDB().getTableName());

	// Loops through worlds
	for (World w : main.getServer().getWorlds()) {
	    // Leaves out the disabled ones
	    if (main.isDisabledWorld(w.getName()))
		continue;

	    // Loops through entities
	    for (Entity e : w.getEntities()) {
		if (DataHandler.isCreative(e))
		    DataHandler.removeTracking(e);
	    }
	}

	main.sendMessage(sender, true, "database.deleted");
    }

    private void block(CommandSender sender, String[] args) {
	if (args[1] == "stats") {
	    String msg = main.getMessage(true, "block.stats").replaceAll("%total%", DataHandler.getTotalCount());
	    main.sendMessage(sender, msg);
	    return;
	}

	if (!(sender instanceof Player)) {
	    main.sendMessage(sender, true, "no-console");
	    return;
	}

	Player p = (Player) sender;

	switch (args[1]) {
	case "add":
	    if (DataHandler.getAddWithCommand().contains(p)) {
		DataHandler.getAddWithCommand().remove(p);
		main.sendMessage(sender, true, "block.cancel");
	    } else {
		DataHandler.getAddWithCommand().add(p);
		main.sendMessage(sender, true, "block.add.add");
	    }
	    break;
	case "remove":
	    if (DataHandler.getRemoveWithCommand().contains(p)) {
		DataHandler.getRemoveWithCommand().remove(p);
		main.sendMessage(sender, true, "block.cancel");
	    } else {
		DataHandler.getRemoveWithCommand().add(p);
		main.sendMessage(sender, true, "block.remove.remove");
	    }
	    break;
	case "info":
	    if (DataHandler.getInfoWithCommand().contains(p)) {
		DataHandler.getInfoWithCommand().remove(p);
		main.sendMessage(sender, true, "block.cancel");
	    } else {
		DataHandler.getInfoWithCommand().add(p);
		main.sendMessage(sender, true, "block.info.info");
	    }
	    break;
	default:
	    main.sendMessage(sender, false, "incorrect.block");
	}
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	if (args.length == 0) {
	    main.sendMessage(sender, false, "incorrect.main");
	    return true;
	}

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
	    main.sendMessage(sender, true, "incorrect.main");
	    return true;
	}

	main.sendMessage(sender, false, "no-permission");
	return true;
    }
}
