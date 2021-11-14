package solutions.nuhvel.spigot.rc.listeners;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.type.Piston;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.storage.handlers.BlockHandler;
import solutions.nuhvel.spigot.rc.utils.MaterialHandler;
import solutions.nuhvel.spigot.rc.utils.helpers.PreconditionChecker;

import java.util.ArrayList;
import java.util.List;

public class BlockUpdateListener implements Listener {
    private final RestrictedCreative plugin;

    private final BlockFace[] horisontal = {BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH};

    public BlockUpdateListener(RestrictedCreative plugin) {
        this.plugin = plugin;
    }

    /*
     * Thrown when a block physics check is called
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onBlockUpdate(BlockPhysicsEvent event) {
        var block = event.getBlock();
        var blockType = block.getType();
        var world = block.getWorld().getName();

        if (new PreconditionChecker(plugin).isTracked(block)
                                           .isAllowedWorld(world)
                                           .isNotExcludedFromTracking(blockType)
                                           .anyFailed())
            return;

        var blockBelow = block.getRelative(BlockFace.DOWN);
        var blockBelowType = blockBelow.getType();

        /* 1.-4. Rail */
        if (MaterialHandler.isRail(block)) {
            // If the block below the rail isn't solid or
            // if rail is on slope and there isn't a block to support it
            if (!isSolid(blockBelow) || !isRailSlopeOk(block)) {
                event.setCancelled(true);
                BlockHandler.breakBlock(block, null);
            }
            return;
        }

        /* 1.-4. Chorus */
        if (blockType == Material.CHORUS_PLANT) {
            if (!isChorusOk(block)) {
                event.setCancelled(true);
                BlockHandler.breakBlock(block, null);
            }
            return;
        }

        /* 1.-4. Scaffolding */
        if (blockType == Material.SCAFFOLDING) {
            if (!isScaffoldingOk(block)) {
                event.setCancelled(true);
                BlockHandler.breakBlock(block, null);
            }
            return;
        }

        /* 1.-4. Weeping & twisting vines */
        if (blockType == Material.WEEPING_VINES || blockType == Material.WEEPING_VINES_PLANT) {
            if (!isWeepingVinesOk(block)) {
                event.setCancelled(true);
                BlockHandler.breakBlock(block, null);
            }
            return;
        }
        if (blockType == Material.TWISTING_VINES || blockType == Material.TWISTING_VINES_PLANT) {
            if (!isTwistingVinesOk(block)) {
                event.setCancelled(true);
                BlockHandler.breakBlock(block, null);
            }
            return;
        }

        /* 5. Top blocks */
        if (MaterialHandler.needsBlockBelow(block)) {
            // Needs to be checked BEFORE isSolid()
            if (blockType == Material.BAMBOO || blockType == Material.BAMBOO_SAPLING) {
                if (!isBambooOk(block)) {
                    event.setCancelled(true);
                    BlockHandler.breakBlock(block, null);
                }
                return;
            }

            // Needs to be checked BEFORE isSolid()
            switch (blockType) {
                case LILY_PAD -> {
                    if (blockBelowType != Material.WATER) {
                        event.setCancelled(true);
                        BlockHandler.breakBlock(block, null);
                    }
                    return;
                }
                case KELP, KELP_PLANT -> {
                    if (!isKelpOk(block)) {
                        event.setCancelled(true);
                        BlockHandler.breakBlock(block, null);
                    }
                    return;
                }
                case CACTUS -> {
                    if (!isCactusOk(block)) {
                        event.setCancelled(true);
                        BlockHandler.breakBlock(block, null);
                    }
                    return;
                }
                case SUGAR_CANE -> {
                    if (!isSugarCaneOk(block)) {
                        event.setCancelled(true);
                        BlockHandler.breakBlock(block, null);
                    }
                    return;
                }
                default -> {
                }
            }

            // Needs to be checked BEFORE isSolid()
            if (MaterialHandler.isDoublePlant(block)) {
                if (!isDoublePlantOk(block)) {
                    event.setCancelled(true);
                    BlockHandler.breakBlock(block, null);
                }
                return;
            }

            // Needs to be checked BEFORE isSolid()
            if (MaterialHandler.isCarpet(block)) {
                if (isBelowEmpty(block)) {
                    event.setCancelled(true);
                    BlockHandler.breakBlock(block, null);
                }
                return;
            }

            // Needs to be checked BEFORE isSolid()
            if (MaterialHandler.isCrop(block)) {
                if (!isLightingOk(block) || !isSolid(blockBelow)) {
                    event.setCancelled(true);
                    BlockHandler.breakBlock(block, null);
                }
                return;
            }

            // Needs to be checked BEFORE isSolid()
            if (MaterialHandler.isDoor(block)) {
                if (!isDoorOk(block) || !isSolid(blockBelow)) {
                    event.setCancelled(true);
                    BlockHandler.breakBlock(block, null);
                }
                return;
            }

            if (!isSolid(blockBelow)) {
                event.setCancelled(true);
                BlockHandler.breakBlock(block, null);
            }
            return;
        }

        /* 5. Attachable */
        BlockFace bf = MaterialHandler.getNeededFace(block);
        if (bf != null) {
            blockBelow = block.getRelative(bf);

            // If the block (to which the first block is attached to) isn't solid
            if (!isSolid(blockBelow)) {
                event.setCancelled(true);
                BlockHandler.breakBlock(block, null);
            }
        }
    }

    private boolean isWeepingVinesOk(Block b) {
        Block bl = b.getRelative(BlockFace.UP);
        Material ma = bl.getType();

        return ma == Material.WEEPING_VINES || ma == Material.WEEPING_VINES_PLANT || isSolid(bl);
    }

    private boolean isTwistingVinesOk(Block b) {
        Block bl = b.getRelative(BlockFace.DOWN);
        Material ma = bl.getType();

        return ma == Material.TWISTING_VINES || ma == Material.TWISTING_VINES_PLANT || isSolid(bl);
    }

    private boolean isScaffoldingOk(Block scaffolding) {
        Block down = scaffolding.getRelative(BlockFace.DOWN);
        return down.getType().isSolid() || down.getType() == Material.SCAFFOLDING;
    }

    private boolean isRailSlopeOk(Block b) {
        return switch (((Rail) b.getBlockData()).getShape()) {
            case ASCENDING_EAST -> b.getRelative(BlockFace.EAST).getType().isSolid();
            case ASCENDING_NORTH -> b.getRelative(BlockFace.NORTH).getType().isSolid();
            case ASCENDING_SOUTH -> b.getRelative(BlockFace.SOUTH).getType().isSolid();
            case ASCENDING_WEST -> b.getRelative(BlockFace.WEST).getType().isSolid();
            default -> true;
        };
    }

    private boolean isChorusOk(Block b) {
        var horizontalChoruses = getHorizontalChoruses(b);

        // Chorus plant will break unless the block below is (chorus plant or end stone)
        // or any horizontally adjacent block is a chorus plant above (chorus plant or
        // end stone)
        if (!isBelowChorusOk(b) && !validHorizontalChorusExists(horizontalChoruses))
            return false;

        boolean isVerticalOk = b.getRelative(BlockFace.UP).getType() == Material.AIR ||
                b.getRelative(BlockFace.DOWN).getType() == Material.AIR;

        // Chorus plant with at least one other chorus plant horizontally adjacent will
        // break unless at least one of the vertically adjacent blocks is air
        return horizontalChoruses.isEmpty() || isVerticalOk;
    }

    private boolean isBelowChorusOk(Block b) {
        Material m = b.getRelative(BlockFace.DOWN).getType();
        return m == Material.END_STONE || m == Material.CHORUS_PLANT;
    }

    private boolean isSolid(Block b) {
        Material m = b.getType();
        BlockData bd = b.getBlockData();

        boolean isPistonOk = !(bd instanceof Piston) || !((Piston) bd).isExtended();
        boolean isTrapdoorOk = !(bd instanceof TrapDoor) || !((TrapDoor) bd).isOpen();

        return m.isSolid() && isPistonOk && isTrapdoorOk;
    }

    private List<Block> getHorizontalChoruses(Block b) {
        List<Block> choruses = new ArrayList<>();

        for (BlockFace bf : horisontal) {
            Block bl = b.getRelative(bf);

            if (bl.getType() == Material.CHORUS_PLANT)
                choruses.add(bl);
        }

        return choruses;
    }

    private boolean validHorizontalChorusExists(List<Block> blocks) {
        for (Block b : blocks) {
            if (isBelowChorusOk(b))
                return true;
        }

        return false;
    }

    private boolean isLightingOk(Block b) {
        return b.getLightFromSky() >= 5 || b.getLightFromBlocks() >= 8;
    }

    private boolean isDoorOk(Block b) {
        Block bl = b.getRelative(BlockFace.DOWN);
        BlockData bd = bl.getBlockData();

        if (bd instanceof TrapDoor && ((TrapDoor) bd).isOpen())
            return false;

        return isSolid(bl);
    }

    private boolean isCactusOk(Block b) {
        boolean nothingAround =
                isAroundCactusOk(b.getRelative(BlockFace.EAST)) && isAroundCactusOk(b.getRelative(BlockFace.WEST)) &&
                        isAroundCactusOk(b.getRelative(BlockFace.NORTH)) &&
                        isAroundCactusOk(b.getRelative(BlockFace.SOUTH));

        Material m = b.getRelative(BlockFace.DOWN).getType();
        boolean belowOk = m == Material.SAND || m == Material.RED_SAND || m == Material.CACTUS;

        boolean aboveOk = b.getRelative(BlockFace.UP).getType() != Material.WATER;

        return nothingAround && belowOk && aboveOk;
    }

    private boolean isAroundCactusOk(Block b) {
        Material m = b.getType();
        return m == Material.AIR || m == Material.WATER;
    }

    private boolean isSugarCaneOk(Block b) {
        Block bl = b.getRelative(BlockFace.DOWN);
        Material ma = bl.getType();

        if (ma == Material.SUGAR_CANE)
            return true;

        boolean soil =
                ma == Material.GRASS_BLOCK || ma == Material.DIRT || ma == Material.SAND || ma == Material.PODZOL ||
                        ma == Material.COARSE_DIRT || ma == Material.RED_SAND;

        Material east = bl.getRelative(BlockFace.EAST).getType();
        Material west = bl.getRelative(BlockFace.WEST).getType();
        Material north = bl.getRelative(BlockFace.NORTH).getType();
        Material south = bl.getRelative(BlockFace.SOUTH).getType();

        boolean water =
                east == Material.WATER || west == Material.WATER || north == Material.WATER || south == Material.WATER;
        boolean frosted_ice =
                east == Material.FROSTED_ICE || west == Material.FROSTED_ICE || north == Material.FROSTED_ICE ||
                        south == Material.FROSTED_ICE;

        return soil && (water || frosted_ice);
    }

    private boolean isBambooOk(Block b) {
        Block bl = b.getRelative(BlockFace.DOWN);
        Material ma = bl.getType();

        return Tag.BAMBOO_PLANTABLE_ON.isTagged(ma);
    }

    private boolean isKelpOk(Block b) {
        Block bl = b.getRelative(BlockFace.DOWN);
        Material m = bl.getType();

        return m == Material.KELP || m == Material.KELP_PLANT || isSolid(bl);
    }

    private boolean isDoublePlantOk(Block b) {
        // Both up and down must be OK
        if (isSolid(b.getRelative(BlockFace.DOWN)))
            return b.getRelative(BlockFace.UP).getType() == b.getType();

        // Below is not solid
        return b.getRelative(BlockFace.DOWN).getType() == b.getType();
    }

    private boolean isBelowEmpty(Block b) {
        return b.getRelative(BlockFace.DOWN).isEmpty();
    }
}
