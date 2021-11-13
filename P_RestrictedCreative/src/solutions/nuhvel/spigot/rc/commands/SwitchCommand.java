package solutions.nuhvel.spigot.rc.commands;

import solutions.nuhvel.spigot.rc.utils.helpers.PreconditionChecker;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.utils.Utils;

public class SwitchCommand implements CommandExecutor {
	private final RestrictedCreative plugin;
	private final GameMode gameMode;

	public SwitchCommand(RestrictedCreative plugin, GameMode gm) {
		this.plugin = plugin;
		this.gameMode = gm;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		switch (args.length) {
		case 0:
			if (!(sender instanceof Player player))
				return false;

			if (new PreconditionChecker(plugin, player)
					.isAllowedWorld(player.getWorld().getName())
					.isAllowedHeight()
					.isAllowedRegion()
					.anyFailed()) {
				plugin.getUtils().sendMessage(sender, true, "disabled.region");
				return true;
			}

			player.setGameMode(this.gameMode);
			plugin.getUtils().sendMessage(player, true, "gamemodes." + getName() + ".me");
			return true;
		case 1:
			if (sender.hasPermission("rc.commands." + getName() + ".others")) {
				Player player = Bukkit.getPlayer(args[0]);

				if (player == null || !player.isOnline()) {
					Utils.sendMessage(sender, plugin.getUtils().getFormattedMessage(true, "not-found")
							.replaceAll("%player%", args[0]));
					return true;
				}

				if (new PreconditionChecker(plugin, player).isAllowedWorld(player.getWorld().getName()).anyFailed()) {
					plugin.getUtils().sendMessage(sender, true, "disabled.region");
					return true;
				}

				player.setGameMode(this.gameMode);
				plugin.getUtils().sendMessage(player, true, "gamemodes." + getName() + ".me");
				Utils.sendMessage(sender,
						plugin.getUtils().getFormattedMessage(true, "gamemodes." + getName() + ".other")
								.replaceAll("%player%", player.getName()));
				return true;
			}

			break;
		default:
			return false;
		}

		plugin.getUtils().sendMessage(sender, false, "no-permission");
		return true;
	}

	private String getName() {
		return this.gameMode.toString().toLowerCase();
	}
}
