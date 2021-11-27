package solutions.nuhvel.spigot.rc.listeners.block;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Bed.Part;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.PistonHead;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.utils.minecraft.MaterialHandler;
import solutions.nuhvel.spigot.rc.utils.minecraft.ServerUtils;
import solutions.nuhvel.spigot.rc.utils.helpers.PreconditionChecker;

public class BlockBreakListener implements Listener {
    private final RestrictedCreative plugin;

    public BlockBreakListener(RestrictedCreative plugin) {
        this.plugin = plugin;
    }

    /*
     * Called when a block is broken by a player.
     *
     * Note: Plugins wanting to simulate a traditional block drop should set the
     * block to air and utilize their own methods for determining what the default
     * drop for the block being broken is and what to do about it, if anything.
     *
     * If a Block Break event is cancelled, the block will not break and experience
     * will not drop.
     */
    // HIGHEST required for WorldGuard and similar plugins
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block b = e.getBlock();
        BlockData bd = b.getBlockData();
        Material m = b.getType();

        if (new PreconditionChecker(plugin, player).isWorldAllowed(b.getWorld().getName()).anyFailed())
            return;

        /*
         * Disabled blocks for creative players
         *
         * Must come before excluded check in order for it to work when tracking is disabled
         */
        if (new PreconditionChecker(plugin, player)
                .isBreakingForbidden(m)
                .isGameMode(GameMode.CREATIVE)
                .allSucceeded()) {
            plugin.messagingUtils.sendMessage(player, true, plugin.messages.disabled.breaking);
            e.setCancelled(true);
            return;
        }

        if (new PreconditionChecker(plugin, player).isTrackingAllowed(b.getType()).anyFailed())
            return;

        // Piston head
        // Needs to be BEFORE isTracked() because PistonHead is not tracked
        if (bd instanceof PistonHead head) {
            Block piston = b.getRelative(head.getFacing().getOppositeFace());

            if (new PreconditionChecker(plugin, player).isTracked(piston).anyFailed())
                return;

            remove(e, player, false, piston, b);
        }

        if (new PreconditionChecker(plugin, player).isTracked(b).anyFailed())
            return;

        // Door
        if (bd instanceof Door door) {
            Block bl;

            if (door.getHalf() == Half.TOP) {
                bl = b.getRelative(BlockFace.DOWN);
            } else {
                bl = b.getRelative(BlockFace.UP);
            }

            remove(e, player, false, bl);
        }

        // Bed
        else if (bd instanceof Bed bed) {
            Block bl;

            if (bed.getPart() == Part.HEAD) {
                bl = b.getRelative(bed.getFacing().getOppositeFace());
            } else {
                bl = b.getRelative(bed.getFacing());
            }

            remove(e, player, false, bl);
        }

        // Double plant
        else if (MaterialHandler.isDoublePlant(b)) {
            Block bl = b.getRelative(BlockFace.DOWN);

            // If block below isn't a double plant, the one above must be
            if (!MaterialHandler.isDoublePlant(bl))
                bl = b.getRelative(BlockFace.UP);

            remove(e, player, false, bl);
        }

        // Others
        remove(e, player, true, b);
    }

    private void remove(BlockBreakEvent e, Player p, boolean update, Block... blocks) {
        if (ServerUtils.isInstalled("Jobs") || ServerUtils.isInstalled("mcMMO")) {
            for (Block b : blocks)
                plugin.trackableHandler.breakBlock(b, p, update);

            e.setCancelled(true);
        } else {
            for (Block b : blocks)
                plugin.trackableHandler.removeTracking(b);

            e.setDropItems(false);
            e.setExpToDrop(0);
        }

        // Notify the breaker why there's no drops
        if (p.getGameMode() != GameMode.CREATIVE && plugin.config.tracking.blocks.notify)
            plugin.messagingUtils.sendMessage(p, true, plugin.messages.disabled.drops);
    }
}
