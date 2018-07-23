package me.prunt.restrictedcreative.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Comparator;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.type.Cake;
import org.bukkit.block.data.type.Cocoa;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Ladder;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.block.data.type.Repeater;
import org.bukkit.block.data.type.Sapling;
import org.bukkit.block.data.type.SeaPickle;
import org.bukkit.block.data.type.Sign;
import org.bukkit.block.data.type.Snow;
import org.bukkit.block.data.type.Switch;
import org.bukkit.material.Banner;
import org.bukkit.material.Button;
import org.bukkit.material.RedstoneTorch;
import org.bukkit.material.Torch;

public class MaterialHandler {
    // Items that will break if block below is air
    private static List<Material> top = new ArrayList<>(Arrays.asList(Material.DEAD_BUSH, Material.DANDELION,
	    Material.DANDELION_YELLOW, Material.ORANGE_TULIP, Material.PINK_TULIP, Material.RED_TULIP,
	    Material.WHITE_TULIP, Material.BLUE_ORCHID, Material.ALLIUM, Material.POPPY, Material.RED_MUSHROOM,
	    Material.BROWN_MUSHROOM, Material.SUGAR_CANE, Material.MELON_STEM, Material.PUMPKIN_STEM,
	    Material.ATTACHED_MELON_STEM, Material.ATTACHED_PUMPKIN_STEM, Material.CACTUS, Material.LILY_PAD,
	    Material.TALL_GRASS, Material.SUNFLOWER, Material.LILAC, Material.ROSE_BUSH, Material.PEONY,
	    Material.LARGE_FERN, Material.KELP_PLANT, Material.GRASS, Material.FERN, Material.TALL_SEAGRASS,
	    Material.STONE_PRESSURE_PLATE, Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
	    Material.LIGHT_WEIGHTED_PRESSURE_PLATE, Material.WHEAT, Material.CARROTS, Material.POTATOES,
	    Material.BEETROOTS, Material.NETHER_WART));

    public static boolean needsBlockBelow(Block b) {
	BlockData bd = b.getBlockData();
	Material m = b.getType();

	if (Tag.CARPETS.isTagged(m))
	    return true;
	if (Tag.CORALS.isTagged(m))
	    return true;
	if (Tag.FLOWER_POTS.isTagged(m))
	    return true;
	if (Tag.WOODEN_PRESSURE_PLATES.isTagged(m))
	    return true;

	if (bd instanceof Cake)
	    return true;
	if (bd instanceof Comparator)
	    return true;
	if (bd instanceof Door)
	    return true;
	if (bd instanceof Rail)
	    return true;
	if (bd instanceof RedstoneWire)
	    return true;
	if (bd instanceof Repeater)
	    return true;
	if (bd instanceof Sapling)
	    return true;
	if (bd instanceof SeaPickle)
	    return true;
	if (bd instanceof Sign)
	    return true;
	if (bd instanceof Snow)
	    return true;

	if (top.contains(m))
	    return true;

	return false;
    }

    public static BlockFace getNeededSide(Block b) {
	BlockData bd = b.getBlockData();

	if (!(bd instanceof Directional))
	    return null;

	Directional d = (Directional) bd;

	if (bd instanceof Cocoa)
	    return d.getFacing();
	if (bd instanceof Ladder)
	    return d.getFacing();
	if (bd instanceof Switch)
	    return d.getFacing();
	// if (bd instanceof CoralWallFan)
	// return d.getFacing();

	if (bd instanceof Banner)
	    return d.getFacing();
	if (bd instanceof Button)
	    return d.getFacing();
	if (bd instanceof RedstoneTorch)
	    return d.getFacing();
	if (bd instanceof Torch)
	    return d.getFacing();

	return null;
    }
}
