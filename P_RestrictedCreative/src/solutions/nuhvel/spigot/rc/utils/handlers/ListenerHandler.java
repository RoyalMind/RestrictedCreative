package solutions.nuhvel.spigot.rc.utils.handlers;

import com.sk89q.worldedit.WorldEdit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.listeners.*;
import solutions.nuhvel.spigot.rc.listeners.block.*;
import solutions.nuhvel.spigot.rc.listeners.entity.EntityCreateListener;
import solutions.nuhvel.spigot.rc.listeners.entity.EntityDamageListener;
import solutions.nuhvel.spigot.rc.listeners.external.SlimefunListener;
import solutions.nuhvel.spigot.rc.listeners.external.WorldEditListener;
import solutions.nuhvel.spigot.rc.listeners.player.PlayerInteractListener;
import solutions.nuhvel.spigot.rc.listeners.player.PlayerInventoryListener;
import solutions.nuhvel.spigot.rc.listeners.player.PlayerItemListener;
import solutions.nuhvel.spigot.rc.listeners.player.PlayerMiscListener;
import solutions.nuhvel.spigot.rc.utils.minecraft.ServerUtils;

public class ListenerHandler {
    private final RestrictedCreative plugin;

    private WorldEditListener worldEditListener;

    public ListenerHandler(RestrictedCreative plugin) {
        this.plugin = plugin;

        registerListeners();
    }

    public void registerListeners() {
        // In case of plugin reload
        HandlerList.unregisterAll(plugin);

        PluginManager manager = plugin.getServer().getPluginManager();

        manager.registerEvents(new BlockPlaceListener(plugin), plugin);
        manager.registerEvents(new BlockBreakListener(plugin), plugin);
        manager.registerEvents(new BlockUpdateListener(plugin), plugin);
        manager.registerEvents(new BlockChangeListener(plugin), plugin);
        manager.registerEvents(new BlockExplodeListener(plugin), plugin);
        manager.registerEvents(new BlockPistonListener(plugin), plugin);

        manager.registerEvents(new ChunkListener(plugin), plugin);

        manager.registerEvents(new EntityDamageListener(plugin), plugin);
        manager.registerEvents(new EntityCreateListener(plugin), plugin);

        manager.registerEvents(new PlayerInteractListener(plugin), plugin);
        manager.registerEvents(new PlayerInventoryListener(plugin), plugin);
        manager.registerEvents(new PlayerItemListener(plugin), plugin);
        manager.registerEvents(new PlayerMiscListener(plugin), plugin);

        registerSlimefunEvents(manager);
        registerWorldEditEvents();
    }

    private void registerWorldEditEvents() {
        if (!ServerUtils.isInstalled("WorldEdit"))
            return;

        if (this.worldEditListener != null) {
            WorldEdit.getInstance().getEventBus().unregister(this.worldEditListener);
            this.worldEditListener = null;
        }

        if (plugin.config.tracking.worldedit.enabled) {
            this.worldEditListener = new WorldEditListener(plugin);
            WorldEdit.getInstance().getEventBus().register(this.worldEditListener);
        }
    }

    private void registerSlimefunEvents(PluginManager manager) {
        if (!ServerUtils.isInstalled("Slimefun"))
            return;

        manager.registerEvents(new SlimefunListener(plugin), plugin);
    }
}
