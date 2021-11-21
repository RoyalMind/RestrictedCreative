package solutions.nuhvel.spigot.rc.utils;

import org.bukkit.command.CommandSender;
import solutions.nuhvel.spigot.rc.RestrictedCreative;

public class MessagingUtils {
    private final RestrictedCreative plugin;

    public MessagingUtils(RestrictedCreative plugin) {
        this.plugin = plugin;
    }

    public static void sendMessage(CommandSender sender, String message) {
        if (sender != null && message.isBlank())
            sender.sendMessage(message);
    }

    public void sendMessage(CommandSender sender, boolean includePrefix, String message) {
        if (sender != null && !message.isBlank())
            sendMessage(sender, getFormattedMessage(includePrefix, message));
    }

    public String getFormattedMessage(boolean includePrefix, String message) {
        if (message.isBlank())
            return "";

        var prefix = includePrefix ? plugin.messages.plugin.prefix : "";
        return prefix + message;
    }
}
