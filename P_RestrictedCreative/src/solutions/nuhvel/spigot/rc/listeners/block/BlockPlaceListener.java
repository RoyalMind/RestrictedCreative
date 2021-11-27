package solutions.nuhvel.spigot.rc.listeners.block;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.utils.helpers.CreatureBuildHelper;
import solutions.nuhvel.spigot.rc.utils.helpers.PreconditionChecker;

import java.util.List;
import java.util.stream.Collectors;

public class BlockPlaceListener implements Listener {
    private final RestrictedCreative plugin;

    public BlockPlaceListener(RestrictedCreative plugin) {
        this.plugin = plugin;
    }

    /*
     * Fired when a single block placement action of a player triggers the creation
     * of multiple blocks(e.g. placing a bed block). The block returned by
     * BlockPlaceEvent.getBlockPlaced() and its related methods is the block where
     * the placed block would exist if the placement only affected a single block.
     */
    // HIGHEST required for WorldGuard and similar plugins
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockMultiPlace(BlockMultiPlaceEvent event) {
        var blocks = event.getReplacedBlockStates().stream().map(BlockState::getBlock).collect(Collectors.toList());

        try {
            onBlockPlace(event.getBlockPlaced(), event.getPlayer(), blocks);
        } catch (Exception ex) {
            event.setCancelled(true);
        }
    }

    /*
     * Called when a block is placed by a player.
     *
     * If a Block Place event is cancelled, the block will not be placed.
     */
    // HIGHEST required for WorldGuard and similar plugins
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        var block = event.getBlockPlaced();

        try {
            onBlockPlace(block, event.getPlayer(), List.of(block));
        } catch (Exception ex) {
            event.setCancelled(true);
        }
    }

    /*
     * Called when a block is placed by a player.
     *
     * If a Block Place event is cancelled, the block will not be placed.
     */
    // HIGHEST required for WorldGuard and similar plugins
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onCreatureCreate(BlockPlaceEvent event) {
        var player = event.getPlayer();
        var world = player.getWorld().getName();
        var head = event.getBlockPlaced();
        var isPlayerCreative = player.getGameMode() == GameMode.CREATIVE;

        if (new PreconditionChecker(plugin, player).isWorldAllowed(world).anyFailed())
            return;

        var canPlaceHead = new CreatureBuildHelper(plugin).canPlaceHead(head.getType(), head, isPlayerCreative);
        if (!canPlaceHead) {
            plugin.messagingUtils.sendMessage(player, true, plugin.messages.disabled.creature);
            event.setCancelled(true);
        }
    }

    /*
     * Called when an item is dispensed from a block.
     *
     * If a Block Dispense event is cancelled, the block will not dispense the item.
     */
    // HIGHEST required for WorldGuard and similar plugins
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockDispense(BlockDispenseEvent event) {
        var block = event.getBlock();
        var world = block.getWorld().getName();
        var blockData = block.getBlockData();

        if (new PreconditionChecker(plugin).isWorldAllowed(world).anyFailed() || !(blockData instanceof Dispenser))
            return;

        var direction = ((Directional) blockData).getFacing();
        var head = block.getRelative(direction);

        var canPlaceHead = new CreatureBuildHelper(plugin).canPlaceHead(event.getItem().getType(), head, false);
        if (!canPlaceHead)
            event.setCancelled(true);
    }

    private void onBlockPlace(Block block, Player player, List<Block> blocks) throws Exception {
        var material = block.getType();
        var world = player.getWorld().getName();

        if (new PreconditionChecker(plugin, player).isWorldAllowed(world).isGameMode(GameMode.CREATIVE).anyFailed())
            return;

        // Must come before excluded check in order for it to work when tracking is disabled
        if (new PreconditionChecker(plugin, player).isPlacingForbidden(material).allSucceeded()) {
            plugin.messagingUtils.sendMessage(player, true, plugin.messages.disabled.placing);
            throw new Exception("Placing this block is disabled");
        }

        if (new PreconditionChecker(plugin, player).isTrackingAllowed(material).anyFailed())
            return;

        for (var bl : blocks)
            plugin.trackableHandler.setAsTracked(bl, player);
    }
}
