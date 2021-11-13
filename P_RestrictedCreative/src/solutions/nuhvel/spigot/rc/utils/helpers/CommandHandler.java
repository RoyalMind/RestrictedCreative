package solutions.nuhvel.spigot.rc.utils.helpers;

import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.commands.MainCommand;
import solutions.nuhvel.spigot.rc.storage.config.config.commands.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;

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
            Command command = plugin.config.commands.getByName(name);

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
            case "rc" -> new MainCommand(this);
            case "creative" -> new GameModeCommand(this, GameMode.CREATIVE);
            case "survival" -> new GameModeCommand(this, GameMode.SURVIVAL);
            case "adventure" -> new GameModeCommand(this, GameMode.ADVENTURE);
            case "spectator" -> new GameModeCommand(this, GameMode.SPECTATOR);
            default -> null;
        };
    }
}
