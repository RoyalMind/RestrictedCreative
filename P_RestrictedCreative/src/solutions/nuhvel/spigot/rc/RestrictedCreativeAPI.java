package solutions.nuhvel.spigot.rc;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import solutions.nuhvel.spigot.rc.storage.handlers.TrackableHandler;

public class RestrictedCreativeAPI {
    public RestrictedCreativeAPI() {
    }

    // Returns the total count of creative placed blocks and entities
    public static int getTotal() {
        return Integer.parseInt(TrackableHandler.getTotalCount());
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
    public static void add(Block b) {
        TrackableHandler.setAsTracked(b);
    }

    // Adds given block to creative blocks list
    public static void add(Entity en) {
        TrackableHandler.setAsTracked(en);
    }

    // Adds given block to creative blocks list
    public static void remove(Block b) {
        TrackableHandler.removeTracking(b);
    }

    // Adds given block to creative blocks list
    public static void remove(Entity en) {
        TrackableHandler.removeTracking(en);
    }
}
