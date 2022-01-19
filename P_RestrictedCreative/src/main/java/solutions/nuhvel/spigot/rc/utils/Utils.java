package solutions.nuhvel.spigot.rc.utils;

import solutions.nuhvel.spigot.rc.RestrictedCreative;

import java.util.logging.Level;

public class Utils {
    private final RestrictedCreative plugin;

    public Utils(RestrictedCreative plugin) {
        this.plugin = plugin;
    }

    public void debug(String message) {
        if (!plugin.config.debug)
            return;

        plugin.getLogger().log(Level.INFO, message);
    }
}
