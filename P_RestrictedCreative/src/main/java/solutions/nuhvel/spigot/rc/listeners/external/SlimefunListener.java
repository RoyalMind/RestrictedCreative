package solutions.nuhvel.spigot.rc.listeners.external;

import io.github.thebusybiscuit.slimefun4.api.events.AndroidMineEvent;
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.utils.helpers.PreconditionChecker;

public class SlimefunListener implements Listener {
    private final RestrictedCreative plugin;

    public SlimefunListener(RestrictedCreative plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onAndroidMine(AndroidMineEvent e) {
        Block b = e.getBlock();

        // No need to control disabled feature
        if (!plugin.config.limitations.interaction.slimefun)
            return;

        if (new PreconditionChecker(plugin)
                .isWorldAllowed(b.getWorld().getName())
                .isTrackingAllowed(b.getType())
                .isTracked(b)
                .anyFailed())
            return;

        e.setCancelled(true);
        plugin.trackableHandler.breakBlock(b);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerRightClick(PlayerRightClickEvent e) {
        Block b = e.getClickedBlock().orElse(null);
        SlimefunItem sfb = e.getSlimefunBlock().orElse(null);
        Player p = e.getPlayer();

        if (b == null || sfb == null)
            return;

        // No need to control disabled feature
        if (!plugin.config.limitations.interaction.slimefun || p.hasPermission("rc.bypass.limit.interact.slimefun"))
            return;

        if (new PreconditionChecker(plugin)
                .isWorldAllowed(b.getWorld().getName())
                .isGameMode(GameMode.CREATIVE)
                .anyFailed())
            return;

        e.setUseBlock(Result.DENY);
        e.setUseItem(Result.DENY);

        plugin.messagingUtils.sendMessage(e.getPlayer(), true, plugin.messages.disabled.general);
    }
}
