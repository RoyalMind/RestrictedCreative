package me.prunt.restrictedcreative;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;

import me.prunt.restrictedcreative.storage.DataHandler;

public class RestrictedCreativeAPI {
    public RestrictedCreativeAPI() {
    }

    // Returns the total count of creative placed blocks and entities
    public static int getTotal() {
	return Integer.valueOf(DataHandler.getTotalCount());
    }

    // Returns whether the block was placed in creative or not
    public static boolean isCreative(Block b) {
	return DataHandler.isTracked(b);
    }

    // Returns whether the entity was created in creative or not
    public static boolean isCreative(Entity en) {
	return DataHandler.isTracked(en);
    }

    // Returns whether the item frame contains creative items or not
    public static boolean isCreativeItemFrame(ItemFrame itf) {
	return DataHandler.hasTrackedItem(itf);
    }

    // Adds given block to creative blocks list
    public static void add(Block b) {
	DataHandler.setAsTracked(b);
    }

    // Adds given block to creative blocks list
    public static void remove(Block b) {
	DataHandler.removeTracking(b);
    }

    // Adds given block to creative blocks list
    public static void add(Entity en) {
	DataHandler.setAsTracked(en);
    }

    // Adds given block to creative blocks list
    public static void remove(Entity en) {
	DataHandler.removeTracking(en);
    }
}
