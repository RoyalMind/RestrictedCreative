package solutions.nuhvel.spigot.rc;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import solutions.nuhvel.spigot.rc.storage.handlers.TrackableHandler;

public class RestrictedCreativeAPI {
    public RestrictedCreativeAPI() {
    }

    // Returns whether the block was placed in creative or not
    public static boolean isCreative(Block b) {
        return TrackableHandler.isTracked(b);
    }

    // Returns whether the entity was created in creative or not
    public static boolean isCreative(Entity en) {
        return TrackableHandler.isTracked(en);
    }

    // Returns whether the item frame contains creative items or not
    public static boolean isCreativeItemFrame(ItemFrame itf) {
        return TrackableHandler.hasTrackedItem(itf);
    }

    // Adds given block to creative blocks list
    public static void add(Block b, Player p) {
        ((RestrictedCreative) Bukkit.getPluginManager().getPlugin("RestrictedCreative")).trackableHandler.setAsTracked(b, p);
    }

    // Adds given block to creative blocks list
    public static void add(Entity en) {
        TrackableHandler.setAsTracked(en);
    }

    // Adds given block to creative blocks list
    public static void remove(Block b) {
        ((RestrictedCreative) Bukkit.getPluginManager().getPlugin("RestrictedCreative")).trackableHandler.removeTracking(b);
    }

    // Adds given block to creative blocks list
    public static void remove(Entity en) {
        TrackableHandler.removeTracking(en);
    }
}
