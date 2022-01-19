package solutions.nuhvel.spigot.rc.commands;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.utils.MessagingUtils;
import solutions.nuhvel.spigot.rc.utils.helpers.PreconditionChecker;

public class GameModeCommand implements CommandExecutor {
    private final RestrictedCreative plugin;
    private final GameMode gameMode;

    public GameModeCommand(RestrictedCreative plugin, GameMode gm) {
        this.plugin = plugin;
        this.gameMode = gm;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        switch (args.length) {
            case 0:
                if (!(sender instanceof Player player))
                    return false;

                if (new PreconditionChecker(plugin, player).isWorldAllowed(player.getWorld().getName()).anyFailed()) {
                    plugin.messagingUtils.sendMessage(sender, true, plugin.messages.disabled.world);
                    return true;
                }

                if (new PreconditionChecker(plugin, player).isRegionAllowed().anyFailed()) {
                    plugin.messagingUtils.sendMessage(sender, true, plugin.messages.disabled.region);
                    return true;
                }

                if (new PreconditionChecker(plugin, player).isHeightAllowed().anyFailed()) {
                    plugin.messagingUtils.sendMessage(sender, true, plugin.messages.disabled.height);
                    return true;
                }

                player.setGameMode(this.gameMode);
                plugin.messagingUtils.sendMessage(player, true,
                        plugin.messages.commands.getCommandByName(getName()).gamemode.me);
                return true;
            case 1:
                if (sender.hasPermission("rc.commands." + getName() + ".others")) {
                    Player player = Bukkit.getPlayer(args[0]);

                    if (player == null || !player.isOnline()) {
                        MessagingUtils.sendMessage(sender, plugin.messagingUtils
                                .getFormattedMessage(true, plugin.messages.errors.notFound)
                                .replaceAll("%player%", args[0]));
                        return true;
                    }

                    if (new PreconditionChecker(plugin, player)
                            .isWorldAllowed(player.getWorld().getName())
                            .anyFailed()) {
                        plugin.messagingUtils.sendMessage(sender, true, plugin.messages.disabled.world);
                        return true;
                    }

                    player.setGameMode(this.gameMode);
                    plugin.messagingUtils.sendMessage(player, true,
                            plugin.messages.commands.getCommandByName(getName()).gamemode.me);
                    MessagingUtils.sendMessage(sender, plugin.messagingUtils
                            .getFormattedMessage(true, plugin.messages.commands.getCommandByName(getName()).gamemode.other)
                            .replaceAll("%player%", player.getName()));
                    return true;
                }

                break;
            default:
                return false;
        }

        plugin.messagingUtils.sendMessage(sender, false, plugin.messages.errors.noPermission);
        return true;
    }

    private String getName() {
        return this.gameMode.toString().toLowerCase();
    }
}
