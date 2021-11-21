package solutions.nuhvel.spigot.rc.storage.handlers;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import solutions.nuhvel.spigot.rc.storage.database.PlayerInfo;

import java.util.HashMap;
import java.util.Map;

public class InventoryHandler {
    private static final Map<Player, PlayerInfo> survivalInvs = new HashMap<>();
    private static final Map<Player, PlayerInfo> creativeInvs = new HashMap<>();

    private static final Map<Player, GameMode> previousGameMode = new HashMap<>();
    private static boolean isForceGamemodeEnabled = false;

    public static GameMode getPreviousGameMode(Player p) {
        return previousGameMode.containsKey(p) ? previousGameMode.get(p) : Bukkit.getDefaultGameMode();
    }

    public static void setPreviousGameMode(Player p, GameMode gm) {
        previousGameMode.put(p, gm);
    }

    public static void removePreviousGameMode(Player p) {
        previousGameMode.remove(p);
    }

    public static void saveSurvivalInv(Player p, PlayerInfo pi) {
        survivalInvs.put(p, pi);
    }

    public static PlayerInfo getSurvivalInv(Player p) {
        return survivalInvs.getOrDefault(p, null);
    }

    public static void removeSurvivalInv(Player p) {
        survivalInvs.remove(p);
    }

    public static void saveCreativeInv(Player p, PlayerInfo pi) {
        creativeInvs.put(p, pi);
    }

    public static PlayerInfo getCreativeInv(Player p) {
        return creativeInvs.getOrDefault(p, null);
    }

    public static void removeCreativeInv(Player p) {
        creativeInvs.remove(p);
    }

    public static boolean isForceGamemodeEnabled() {
        return isForceGamemodeEnabled;
    }

    public static void setForceGamemodeEnabled(boolean isForceGamemodeEnabled) {
        InventoryHandler.isForceGamemodeEnabled = isForceGamemodeEnabled;
    }
}
