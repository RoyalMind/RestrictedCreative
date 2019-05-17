package me.prunt.restrictedcreative.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.type.Bell;
import org.bukkit.block.data.type.Cake;
import org.bukkit.block.data.type.Cocoa;
import org.bukkit.block.data.type.Comparator;
import org.bukkit.block.data.type.CoralWallFan;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Ladder;
import org.bukkit.block.data.type.RedstoneWallTorch;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.block.data.type.Repeater;
import org.bukkit.block.data.type.Sapling;
import org.bukkit.block.data.type.SeaPickle;
import org.bukkit.block.data.type.Sign;
import org.bukkit.block.data.type.Snow;
import org.bukkit.block.data.type.Switch;
import org.bukkit.block.data.type.TripwireHook;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Button;
import org.bukkit.material.MaterialData;
import org.bukkit.material.RedstoneTorch;
import org.bukkit.material.Torch;

import me.prunt.restrictedcreative.Main;

@SuppressWarnings("deprecation")
public class MaterialHandler {
    // Items that will break if block below is air
    private static HashSet<Material> top = new HashSet<>(Arrays.asList(Material.DEAD_BUSH, Material.DANDELION,
	    Material.ORANGE_TULIP, Material.PINK_TULIP, Material.RED_TULIP, Material.WHITE_TULIP, Material.BLUE_ORCHID,
	    Material.ALLIUM, Material.POPPY, Material.AZURE_BLUET, Material.OXEYE_DAISY, Material.RED_MUSHROOM,
	    Material.BROWN_MUSHROOM, Material.SUGAR_CANE, Material.MELON_STEM, Material.PUMPKIN_STEM,
	    Material.ATTACHED_MELON_STEM, Material.ATTACHED_PUMPKIN_STEM, Material.CACTUS, Material.LILY_PAD,
	    Material.KELP, Material.KELP_PLANT, Material.GRASS, Material.FERN, Material.TALL_SEAGRASS,
	    Material.STONE_PRESSURE_PLATE, Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
	    Material.LIGHT_WEIGHTED_PRESSURE_PLATE, Material.NETHER_WART));

    // Crops
    private static HashSet<Material> crops = new HashSet<>(
	    Arrays.asList(Material.WHEAT, Material.CARROTS, Material.POTATOES, Material.BEETROOTS));

    // Double plants
    private static HashSet<Material> doublePlants = new HashSet<>(Arrays.asList(Material.TALL_GRASS, Material.SUNFLOWER,
	    Material.LILAC, Material.ROSE_BUSH, Material.PEONY, Material.LARGE_FERN));

    // Placeable entities (but not hangables)
    private static HashSet<Material> entities = new HashSet<>(Arrays.asList(Material.END_CRYSTAL, Material.ARMOR_STAND,
	    Material.MINECART, Material.CHEST_MINECART, Material.COMMAND_BLOCK_MINECART, Material.FURNACE_MINECART,
	    Material.HOPPER_MINECART, Material.TNT_MINECART, Material.ACACIA_BOAT, Material.BIRCH_BOAT,
	    Material.DARK_OAK_BOAT, Material.JUNGLE_BOAT, Material.OAK_BOAT, Material.SPRUCE_BOAT));

    // List of colorable leather armor
    private static List<ItemStack> armorList = Arrays.asList(new ItemStack(Material.LEATHER_BOOTS),
	    new ItemStack(Material.LEATHER_LEGGINGS), new ItemStack(Material.LEATHER_CHESTPLATE),
	    new ItemStack(Material.LEATHER_HELMET));

    public static boolean needsBlockBelow(Block b) {
	BlockData bd = b.getBlockData();
	Material m = b.getType();

	if (Main.DEBUG && Main.EXTRADEBUG)
	    Bukkit.getLogger().log(Level.FINE, "needsBlockBelow: " + m);

	if (getTop().contains(m))
	    return true;

	if (bd instanceof Cake)
	    return true;
	if (bd instanceof Comparator)
	    return true;
	if (bd instanceof Door)
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

	// Standing banners are not directional
	if (Tag.BANNERS.isTagged(m) && !(bd instanceof Directional))
	    return true;
	if (Tag.CORALS.isTagged(m))
	    return true;
	if (Tag.FLOWER_POTS.isTagged(m))
	    return true;
	if (Tag.WOODEN_PRESSURE_PLATES.isTagged(m))
	    return true;

	if (isCrop(b))
	    return true;
	if (isRail(b))
	    return true;
	if (isDoublePlant(b))
	    return true;
	if (isCarpet(b))
	    return true;

	if (getNeededFace(b) == BlockFace.DOWN)
	    return true;

	return false;
    }

    public static BlockFace getNeededFace(Block b) {
	BlockData bd = b.getBlockData();
	MaterialData md = b.getState().getData();

	if (Main.DEBUG && Main.EXTRADEBUG)
	    Bukkit.getLogger().log(Level.FINE, "getNeededFace: " + b.getType());

	// Some of the MaterialData instances aren't directional
	if (md instanceof Button)
	    return ((Button) md).getAttachedFace();
	if (md instanceof Torch)
	    return ((Torch) md).getAttachedFace();
	if (md instanceof RedstoneTorch)
	    return ((RedstoneTorch) md).getAttachedFace();

	if (!(bd instanceof Directional))
	    return null;

	Directional d = (Directional) bd;

	// getOppositeFace() because getFacing() returns where the item is "looking",
	// opposite of where it is attached
	// NB: this doesn't seem to be always true
	if (bd instanceof Cocoa)
	    return d.getFacing(); // TESTED 1.13.2
	if (bd instanceof Ladder)
	    return d.getFacing().getOppositeFace(); // TESTED 1.13.2
	if (bd instanceof CoralWallFan)
	    return d.getFacing().getOppositeFace(); // TESTED 1.13.2
	if (bd instanceof RedstoneWallTorch) // currently covered by RedstoneTorch MaterialData above
	    return d.getFacing().getOppositeFace(); // TESTED 1.13.2
	if (bd instanceof TripwireHook)
	    return d.getFacing().getOppositeFace(); // TESTED 1.13.2
	if (bd instanceof WallSign)
	    return d.getFacing().getOppositeFace(); // TESTED 1.13.2
	if (bd instanceof Switch) {
	    switch (((Switch) bd).getFace()) {
	    case CEILING:
		return BlockFace.UP;
	    case FLOOR:
		return BlockFace.DOWN;
	    case WALL:
		return d.getFacing().getOppositeFace(); // TESTED 1.13.2
	    }
	}
	if (Bukkit.getVersion().contains("1.14") && bd instanceof Bell) {
	    switch (((Bell) bd).getAttachment()) {
	    case CEILING:
		return BlockFace.UP;
	    case FLOOR:
		return BlockFace.DOWN;
	    case SINGLE_WALL:
	    case DOUBLE_WALL:
		return d.getFacing(); // TESTED 1.14
	    }
	}

	Material m = b.getType();

	if (Tag.BANNERS.isTagged(m))
	    return d.getFacing().getOppositeFace(); // TESTED 1.13.2

	return null;
    }

    public static boolean isCrop(Block b) {
	return crops.contains(b.getType());
    }

    public static boolean isRail(Block b) {
	return b.getBlockData() instanceof Rail;
    }

    public static boolean isDoublePlant(Block b) {
	return doublePlants.contains(b.getType());
    }

    public static boolean isCarpet(Block b) {
	return Tag.CARPETS.isTagged(b.getType());
    }

    public static boolean isPlaceableEntity(Material m) {
	return entities.contains(m);
    }

    public static List<ItemStack> getArmorList() {
	return armorList;
    }

    private static HashSet<Material> getTop() {
	if (Bukkit.getVersion().contains("1.14")) {
	    top.add(Material.valueOf("BAMBOO"));
	    top.add(Material.valueOf("BAMBOO_SAPLING"));
	    top.add(Material.valueOf("CORNFLOWER"));
	    top.add(Material.valueOf("LILY_OF_THE_VALLEY"));
	    top.add(Material.valueOf("WITHER_ROSE"));
	    top.add(Material.valueOf("SWEET_BERRY_BUSH"));
	} else if (Bukkit.getVersion().contains("1.13")) {
	    top.add(Material.valueOf("DANDELION_YELLOW"));
	}
	return top;
    }
}
