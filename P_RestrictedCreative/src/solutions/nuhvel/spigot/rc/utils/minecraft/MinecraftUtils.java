package solutions.nuhvel.spigot.rc.utils.minecraft;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class MinecraftUtils {
    // Return location coordinates as a string
    public static String getAsString(Location loc) {
        World w = loc.getWorld();

        if (w == null)
            return null;

        return w.getName() + ";" + loc.getBlockX() + ";" + loc.getBlockZ();
    }

    public static boolean isVersionOlderThanInclusive(MinecraftVersion version) {
        return getCurrentVersion().compareTo(version) <= 0;
    }

    public static boolean isVersionNewerThanInclusive(MinecraftVersion version) {
        return getCurrentVersion().compareTo(version) >= 0;
    }

    private static MinecraftVersion getCurrentVersion() {
        String version = Bukkit.getVersion();

        if (version.contains("1.20"))
            return MinecraftVersion.v1_20;
        if (version.contains("1.19"))
            return MinecraftVersion.v1_19;
        if (version.contains("1.18"))
            return MinecraftVersion.v1_18;
        if (version.contains("1.17"))
            return MinecraftVersion.v1_17;

        return MinecraftVersion.UNKNOWN;
    }
}
