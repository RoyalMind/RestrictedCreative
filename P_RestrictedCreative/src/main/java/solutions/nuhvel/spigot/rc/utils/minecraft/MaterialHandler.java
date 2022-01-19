package solutions.nuhvel.spigot.rc.utils.minecraft;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.FaceAttachable;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.type.*;
import org.bukkit.inventory.ItemStack;
import solutions.nuhvel.spigot.rc.storage.config.config.miscellaneous.ArmorMaterial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class MaterialHandler {
    // Items that will break if block below is air
    private static final HashSet<Material> top = new HashSet<>(
            Arrays.asList(Material.DEAD_BUSH, Material.DANDELION, Material.ORANGE_TULIP, Material.PINK_TULIP,
                    Material.RED_TULIP, Material.WHITE_TULIP, Material.BLUE_ORCHID, Material.ALLIUM, Material.POPPY,
                    Material.AZURE_BLUET, Material.OXEYE_DAISY, Material.RED_MUSHROOM, Material.BROWN_MUSHROOM,
                    Material.SUGAR_CANE, Material.MELON_STEM, Material.PUMPKIN_STEM, Material.ATTACHED_MELON_STEM,
                    Material.ATTACHED_PUMPKIN_STEM, Material.CACTUS, Material.LILY_PAD, Material.KELP,
                    Material.KELP_PLANT, Material.GRASS, Material.FERN, Material.TALL_SEAGRASS,
                    Material.STONE_PRESSURE_PLATE, Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
                    Material.LIGHT_WEIGHTED_PRESSURE_PLATE, Material.NETHER_WART, Material.BAMBOO,
                    Material.BAMBOO_SAPLING, Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY, Material.WITHER_ROSE,
                    Material.SWEET_BERRY_BUSH, Material.CRIMSON_FUNGUS, Material.WARPED_FUNGUS, Material.CRIMSON_ROOTS,
                    Material.WARPED_ROOTS, Material.NETHER_SPROUTS, Material.POLISHED_BLACKSTONE_PRESSURE_PLATE));

    // Crops
    private static final HashSet<Material> crops =
            new HashSet<>(Arrays.asList(Material.WHEAT, Material.CARROTS, Material.POTATOES, Material.BEETROOTS));

    // Double plants
    private static final HashSet<Material> doublePlants =
            new HashSet<>(Arrays.asList(Material.TALL_GRASS, Material.LARGE_FERN, Material.SUNFLOWER, Material.LILAC,
                    Material.ROSE_BUSH, Material.PEONY));

    // Placeable entities (but not hangables)
    private static final HashSet<Material> entities = new HashSet<>(
            Arrays.asList(Material.END_CRYSTAL, Material.ARMOR_STAND, Material.MINECART, Material.CHEST_MINECART,
                    Material.COMMAND_BLOCK_MINECART, Material.FURNACE_MINECART, Material.HOPPER_MINECART,
                    Material.TNT_MINECART, Material.ACACIA_BOAT, Material.BIRCH_BOAT, Material.DARK_OAK_BOAT,
                    Material.JUNGLE_BOAT, Material.OAK_BOAT, Material.SPRUCE_BOAT));

    public static boolean needsBlockBelow(Block b) {
        BlockData bd = b.getBlockData();
        Material m = b.getType();

        if (getTop().contains(m))
            return true;

        if (bd instanceof Cake)
            return true;
        if (bd instanceof Comparator)
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

        if (isDoor(b))
            return true;
        if (isCrop(b))
            return true;
        if (isRail(b))
            return true;
        if (isDoublePlant(b))
            return true;
        if (isCarpet(b))
            return true;

        return getNeededFace(b) == BlockFace.DOWN;
    }

    public static BlockFace getNeededFace(Block b) {
        BlockData bd = b.getBlockData();

        if (bd instanceof Lantern)
            return ((Lantern) bd).isHanging() ? BlockFace.UP : BlockFace.DOWN;
        if (b.getType() == Material.SOUL_TORCH)
            return BlockFace.DOWN;
        if (b.getType() == Material.TORCH)
            return BlockFace.DOWN;
        if (b.getType() == Material.REDSTONE_TORCH)
            return BlockFace.DOWN;

        if (!(bd instanceof Directional directional))
            return null;

        // getOppositeFace() because getFacing() returns where the item is "looking",
        // opposite of where it is attached. NB! this doesn't seem to be always true
        if (b.getType() == Material.valueOf("SOUL_WALL_TORCH"))
            return ((Directional) bd).getFacing().getOppositeFace();
        if (b.getType() == Material.WALL_TORCH)
            return ((Directional) bd).getFacing().getOppositeFace(); // TESTED 1.15.2
        if (bd instanceof RedstoneWallTorch)
            return directional.getFacing().getOppositeFace();
        if (bd instanceof Cocoa)
            return directional.getFacing(); // TESTED 1.13.2
        if (bd instanceof Ladder)
            return directional.getFacing().getOppositeFace(); // TESTED 1.13.2
        if (bd instanceof CoralWallFan)
            return directional.getFacing().getOppositeFace(); // TESTED 1.13.2
        if (bd instanceof TripwireHook)
            return directional.getFacing().getOppositeFace(); // TESTED 1.13.2
        if (bd instanceof WallSign)
            return directional.getFacing().getOppositeFace(); // TESTED 1.13.2
        if (bd instanceof FaceAttachable) {
            return switch (((FaceAttachable) bd).getAttachedFace()) {
                case CEILING -> BlockFace.UP;
                case FLOOR -> BlockFace.DOWN;
                case WALL -> directional.getFacing().getOppositeFace(); // TESTED 1.15.2
            };
        }
        if (bd instanceof Bell) {
            return switch (((Bell) bd).getAttachment()) {
                case CEILING -> BlockFace.UP;
                case FLOOR -> BlockFace.DOWN;
                case SINGLE_WALL, DOUBLE_WALL -> directional.getFacing(); // TESTED 1.14
            };
        }

        if (Tag.BANNERS.isTagged(b.getType()))
            return directional.getFacing().getOppositeFace(); // TESTED 1.13.2

        return null;
    }

    public static boolean isDoor(Block b) {
        return b.getBlockData() instanceof Door;
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

    public static List<ItemStack> getArmorList(ArmorMaterial type) {
        List<ItemStack> result = new ArrayList<>();

        for (String armorPiece : List.of("BOOTS", "LEGGINGS", "CHESTPLATE", "HELMET"))
            result.add(new ItemStack(Material.valueOf(type.toString() + "_" + armorPiece)));

        return result;
    }

    private static HashSet<Material> getTop() {
        return top;
    }
}
