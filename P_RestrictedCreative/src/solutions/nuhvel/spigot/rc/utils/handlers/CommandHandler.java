package solutions.nuhvel.spigot.rc.utils.handlers;

import org.bukkit.GameMode;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.commands.GameModeCommand;
import solutions.nuhvel.spigot.rc.commands.MainCommand;
import solutions.nuhvel.spigot.rc.storage.config.messages.commands.Command;

import java.util.Map;

public class CommandHandler {
    private final RestrictedCreative plugin;

    public CommandHandler(RestrictedCreative plugin) {
        this.plugin = plugin;

        registerCommands();
    }

    private void registerCommands() {
        // Register commands
        for (Map.Entry<String, Map<String, Object>> entry : plugin.getDescription().getCommands().entrySet()) {
            String name = entry.getKey();
            PluginCommand cmd = plugin.getCommand(name);
            Command command = plugin.messages.commands.getByName(name);

            if (cmd == null || command == null)
                continue;

            cmd.setExecutor(getExecutor(name));
            cmd.setPermissionMessage(plugin.messages.errors.noPermission);
            cmd.setDescription(command.description);
            cmd.setUsage(command.usage);
        }
    }

    private CommandExecutor getExecutor(String name) {
        return switch (name) {
            case "rc" -> new MainCommand(plugin);
            case "creative" -> new GameModeCommand(plugin, GameMode.CREATIVE);
            case "survival" -> new GameModeCommand(plugin, GameMode.SURVIVAL);
            case "adventure" -> new GameModeCommand(plugin, GameMode.ADVENTURE);
            case "spectator" -> new GameModeCommand(plugin, GameMode.SPECTATOR);
            default -> null;
        };
    }
}
