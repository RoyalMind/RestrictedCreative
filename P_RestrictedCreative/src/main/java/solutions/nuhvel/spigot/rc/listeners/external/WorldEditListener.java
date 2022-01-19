package solutions.nuhvel.spigot.rc.listeners.external;

import com.sk89q.worldedit.EditSession.Stage;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.utils.Utils;
import solutions.nuhvel.spigot.rc.utils.helpers.PreconditionChecker;

public class WorldEditListener {
    private final RestrictedCreative plugin;

    public WorldEditListener(RestrictedCreative plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onEditSession(EditSessionEvent e) {
        // Without this check this method is called 3 times for a single event
        if (e.getStage() != Stage.BEFORE_CHANGE)
            return;

        if (e.getActor() == null || !e.getActor().isPlayer() || e.getWorld() == null)
            return;

        Player player = plugin.getServer().getPlayer(e.getActor().getUniqueId());
        String world = e.getWorld().getName();

        if (player == null)
            return;

        if (new PreconditionChecker(plugin, player)
                .isWorldAllowed(world)
                .doesNotHavePermission("rc.bypass.tracking.worldedit")
                .anyFailed())
            return;

        if (!plugin.config.tracking.worldedit.extended && player.getGameMode() != GameMode.CREATIVE)
            return;

        plugin.getUtils().debug("onEditSession: " + player.getGameMode());

        e.setExtent(createNewExtent(e.getExtent(), world, player));
    }

    private Extent createNewExtent(Extent currentExtent, String worldName, Player player) {
        return new AbstractDelegateExtent(currentExtent) {
            @SuppressWarnings({"unchecked"})
            @Override
            public boolean setBlock(BlockVector3 position, BlockStateHolder newBlock) throws WorldEditException {
                World world = plugin.getServer().getWorld(worldName);
                if (world == null)
                    return getExtent().setBlock(position, newBlock);

                Block block = world.getBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ());

                plugin.getUtils().debug("setBlock: " + block.getType() + " " + newBlock.getAsString());

                if (blockIsRemoved(newBlock))
                    plugin.trackableHandler.removeTracking(block);
                else
                    plugin.trackableHandler.setAsTracked(block, player);

                return getExtent().setBlock(position, newBlock);
            }
        };
    }

    @SuppressWarnings("rawtypes")
    private boolean blockIsRemoved(BlockStateHolder block) {
        return block.getBlockType().getMaterial().isAir();
    }
}
