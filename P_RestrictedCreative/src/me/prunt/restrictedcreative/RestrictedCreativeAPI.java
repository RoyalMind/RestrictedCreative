package me.prunt.restrictedcreative;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;

import me.prunt.restrictedcreative.storage.handlers.BlockHandler;
import me.prunt.restrictedcreative.storage.handlers.EntityHandler;

public class RestrictedCreativeAPI {
	public RestrictedCreativeAPI() {
	}

	// Returns the total count of creative placed blocks and entities
	public static int getTotal() {
		return Integer.valueOf(BlockHandler.getTotalCount());
	}

	// Returns whether the block was placed in creative or not
	public static boolean isCreative(Block b) {
		return BlockHandler.isTracked(b);
	}

	// Returns whether the entity was created in creative or not
	public static boolean isCreative(Entity en) {
		return EntityHandler.isTracked(en);
	}

	// Returns whether the item frame contains creative items or not
	public static boolean isCreativeItemFrame(ItemFrame itf) {
		return EntityHandler.hasTrackedItem(itf);
	}

	// Adds given block to creative blocks list
	public static void add(Block b) {
		BlockHandler.setAsTracked(b);
	}

	// Adds given block to creative blocks list
	public static void remove(Block b) {
		BlockHandler.removeTracking(b);
	}

	// Adds given block to creative blocks list
	public static void add(Entity en) {
		EntityHandler.setAsTracked(en);
	}

	// Adds given block to creative blocks list
	public static void remove(Entity en) {
		EntityHandler.removeTracking(en);
	}
}
