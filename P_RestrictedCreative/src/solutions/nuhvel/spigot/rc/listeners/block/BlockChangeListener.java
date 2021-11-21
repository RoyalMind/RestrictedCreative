package solutions.nuhvel.spigot.rc.listeners.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.SpongeAbsorbEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.storage.handlers.TrackableHandler;
import solutions.nuhvel.spigot.rc.utils.helpers.PreconditionChecker;

public class BlockChangeListener implements Listener {
    private final RestrictedCreative plugin;

    public BlockChangeListener(RestrictedCreative plugin) {
        this.plugin = plugin;
    }

    /*
     * Represents events with a source block and a destination block, currently only
     * applies to liquid (lava and water) and teleporting dragon eggs.
     *
     * If a Block From To event is cancelled, the block will not move (the liquid
     * will not flow).
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockChange(BlockFromToEvent e) {
        Block b = e.getToBlock();

        if (new PreconditionChecker(plugin)
                .isWorldAllowed(b.getWorld().getName())
                .isTrackingAllowed(b.getType())
                .isTracked(b)
                .anyFailed())
            return;

        // Waterloggable blocks won't be affected by liquids
        if (b.getBlockData() instanceof Waterlogged)
            return;

        // Removes, because otherwise a liquid would destroy it and drop the block
        e.setCancelled(true);
        plugin.trackableHandler.breakBlock(b);
    }

    /*
     * Called when any Entity, excluding players, changes a block.
     */
    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        Block b = e.getBlock();
        Material m = b.getType();
        Entity en = e.getEntity();

        if (new PreconditionChecker(plugin)
                .isWorldAllowed(b.getWorld().getName())
                .isTrackingAllowed(b.getType(), Material.AIR)
                .anyFailed())
            return;

        // Crops trampled by mobs
        Block bl = b.getRelative(BlockFace.UP);
        if (TrackableHandler.isTracked(bl) && m == Material.FARMLAND) {
            plugin.trackableHandler.breakBlock(bl);
            b.setType(Material.DIRT);
            e.setCancelled(true);
            return;
        }

        if (new PreconditionChecker(plugin).isTracked(b).isTracked(en).allSucceeded())
            return;

        // Lily pad broken by boat
        if (m == Material.LILY_PAD && new PreconditionChecker(plugin).isTracked(b).anyFailed()) {
            plugin.trackableHandler.breakBlock(b);
            return;
        }

        // Wither destroying blocks
        if (en.getType() == EntityType.WITHER && new PreconditionChecker(plugin).isTracked(b).anyFailed()) {
            plugin.trackableHandler.breakBlock(b);
            return;
        }

        // Falling block transforming into regular block and vice versa
        if (e.getEntityType() == EntityType.FALLING_BLOCK) {
            // Regular block starts to fall and becomes FALLING_BLOCK
            if (e.getTo() == Material.AIR) {
                plugin.trackableHandler.removeTracking(b);
                TrackableHandler.setAsTracked(en);

                ((FallingBlock) en).setDropItem(false);
            }

            // FALLING_BLOCK is transforming into regular block
            else {
                plugin.trackableHandler.setAsTracked(b);
            }
        }
    }

    /*
     * Called when a sponge absorbs water from the world. The world will be in its
     * previous state, and getBlocks() will represent the changes to be made to the
     * world, if the event is not cancelled. As this is a physics based event it may
     * be called multiple times for "the same" changes.
     */
    @EventHandler(ignoreCancelled = true)
    public void onSpongeAbsorb(SpongeAbsorbEvent e) {
        if (new PreconditionChecker(plugin).isWorldAllowed(e.getBlock().getWorld().getName()).anyFailed())
            return;

        for (BlockState bs : e.getBlocks()) {
            Block b = bs.getBlock();
            Material m = b.getType();

            if (new PreconditionChecker(plugin).isTracked(b).anyFailed())
                continue;

            if (m != Material.KELP && m != Material.KELP_PLANT)
                continue;

            plugin.trackableHandler.breakBlock(bs.getBlock(), null, false);
        }
    }
}
