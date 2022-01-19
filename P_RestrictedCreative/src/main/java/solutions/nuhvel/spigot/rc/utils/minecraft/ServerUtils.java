package solutions.nuhvel.spigot.rc.utils.minecraft;

import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ServerUtils {
    public static boolean isInstalled(String plugin) {
        return Bukkit.getPluginManager().getPlugin(plugin) != null;
    }

    public static boolean isForceGamemodeEnabled() {
        try {
            FileInputStream in = new FileInputStream(new File(".").getAbsolutePath() + "/server.properties");
            Properties prop = new Properties();

            prop.load(in);
            boolean result = Boolean.parseBoolean(prop.getProperty("force-gamemode"));
            in.close();

            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
