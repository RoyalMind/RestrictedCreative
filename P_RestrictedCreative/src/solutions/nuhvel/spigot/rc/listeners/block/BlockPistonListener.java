package solutions.nuhvel.spigot.rc.listeners.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Bed.Part;
import org.bukkit.block.data.type.Door;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.storage.handlers.TrackableHandler;
import solutions.nuhvel.spigot.rc.utils.MaterialHandler;
import solutions.nuhvel.spigot.rc.utils.helpers.PreconditionChecker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlockPistonListener implements Listener {
    private final RestrictedCreative plugin;

    public BlockPistonListener(RestrictedCreative plugin) {
        this.plugin = plugin;
    }

    /*
     * Called when a piston retracts
     */
    // HIGHEST required for WorldGuard and similar plugins
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockPull(BlockPistonRetractEvent e) {
        if (new PreconditionChecker(plugin).isWorldAllowed(e.getBlock().getWorld().getName()).anyFailed())
            return;

        // Non-sticky piston doesn't pull anything
        if (!e.isSticky())
            return;

        try {
            updateNewBlockLocations(e.getBlocks(), e.getDirection());
        } catch (Exception ex) {
            e.setCancelled(true);
        }
    }

    /*
     * Called when a piston extends
     */
    // HIGHEST required for WorldGuard and similar plugins
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockPush(BlockPistonExtendEvent e) {
        if (new PreconditionChecker(plugin).isWorldAllowed(e.getBlock().getWorld().getName()).anyFailed())
            return;

        try {
            updateNewBlockLocations(e.getBlocks(), e.getDirection());
        } catch (Exception ex) {
            e.setCancelled(true);
        }
    }

    private RestrictedCreative getMain() {
        return this.plugin;
    }

    private void updateNewBlockLocations(List<Block> blocks, BlockFace direction) throws Exception {
        List<Block> newBlocks = new ArrayList<>();
        List<Block> oldBlocks = new ArrayList<>();
        List<BlockFace> blockfaces = getSurroundingBlockFaces(direction);

        // Loop through pushed blocks
        for (Block b : blocks) {
            checkSurroundingBlocks(b, blockfaces);

            if (!TrackableHandler.isTracked(b))
                continue;

            BlockData bd = b.getBlockData();

            /* Bed */
            if (bd instanceof Bed bed) {
                Block block = bed.getPart() == Part.FOOT ? b.getRelative(bed.getFacing())
                        : b.getRelative(bed.getFacing().getOppositeFace());

                plugin.trackableHandler.breakBlock(b, null, false);
                plugin.trackableHandler.breakBlock(block, null, false);
                continue;
            }

            /* Door */
            if (bd instanceof Door door) {
                breakDoor(door, b);
                continue;
            }

            if (b.getPistonMoveReaction() == PistonMoveReaction.BREAK)
                plugin.trackableHandler.breakBlock(b, null);

            // If it's on top of another moving block and need a block below
            if (blocks.contains(b.getRelative(BlockFace.DOWN)) && MaterialHandler.needsBlockBelow(b))
                plugin.trackableHandler.breakBlock(b, null);

            // If it's attached to another moving block
            BlockFace neededFace = MaterialHandler.getNeededFace(b);
            if (neededFace != null && blocks.contains(b.getRelative(neededFace)))
                plugin.trackableHandler.breakBlock(b, null);

            oldBlocks.add(b);
            newBlocks.add(b.getRelative(direction));
        }

        // Remove old blocks first
        for (Block b : oldBlocks) {
            plugin.trackableHandler.removeTracking(b);
        }
        // Add new blocks afterwards
        for (Block b : newBlocks) {
            plugin.trackableHandler.setAsTracked(b);
        }
    }

    private void breakDoor(Door door, Block b) {
        Block block = door.getHalf() == Half.BOTTOM ? b.getRelative(BlockFace.UP) : b.getRelative(BlockFace.DOWN);

        plugin.trackableHandler.breakBlock(b, null, false);
        plugin.trackableHandler.breakBlock(block, null, false);
    }

    private List<BlockFace> getSurroundingBlockFaces(BlockFace direction) {
        // Gets the non-pushed/-pulled faces
        BlockFace bf1 = null, bf2 = null, bf3 = null, bf4 = null, bf5 = direction.getOppositeFace();

        switch (direction.name()) {
            case ("UP"), ("DOWN") -> {
                bf1 = BlockFace.WEST;
                bf2 = BlockFace.EAST;
                bf3 = BlockFace.NORTH;
                bf4 = BlockFace.SOUTH;
            }
            case ("NORTH"), ("SOUTH") -> {
                bf1 = BlockFace.WEST;
                bf2 = BlockFace.EAST;
                bf3 = BlockFace.UP;
                bf4 = BlockFace.DOWN;
            }
            case ("EAST"), ("WEST") -> {
                bf1 = BlockFace.NORTH;
                bf2 = BlockFace.SOUTH;
                bf3 = BlockFace.UP;
                bf4 = BlockFace.DOWN;
            }
        }

        return Arrays.asList(bf1, bf2, bf3, bf4, bf5);
    }

    private void checkSurroundingBlocks(Block b, List<BlockFace> sides) throws Exception {
        for (BlockFace bf : sides) {
            Block bl = b.getRelative(bf);

            // Checks if the surrounding block is placed in creative
            if (!TrackableHandler.isTracked(bl))
                continue;

            // If it's attached to the moving block
            if (bl.getFace(b) == BlockFace.DOWN && MaterialHandler.needsBlockBelow(bl)) {
                if (MaterialHandler.isRail(bl) && b.getType() == Material.SLIME_BLOCK)
                    throw new Exception("Rails don't cooperate with slime blocks");

                BlockData bd = b.getBlockData();

                /* Door */
                if (bd instanceof Door door) {
                    breakDoor(door, b);
                    continue;
                }

                plugin.trackableHandler.breakBlock(bl);
                continue;
            }

            if (bl.getFace(b) == MaterialHandler.getNeededFace(bl))
                plugin.trackableHandler.breakBlock(bl);
        }
    }
}
