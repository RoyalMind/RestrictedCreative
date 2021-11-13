package solutions.nuhvel.spigot.rc.utils.helpers;

import com.sk89q.worldedit.WorldEdit;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import me.prunt.restrictedcreative.listeners.*;
import solutions.nuhvel.spigot.rc.listeners.*;
import solutions.nuhvel.spigot.rc.utils.Utils;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;

public class ListenerHandler {
    private final RestrictedCreative plugin;

    private WorldEditListener worldEditListener;

    public ListenerHandler(RestrictedCreative plugin) {
        this.plugin = plugin;

        registerListeners();
    }

    public void registerListeners() {
        if (Utils.isInstalled("WorldEdit")) {
            if (this.worldEditListener != null) {
                WorldEdit.getInstance().getEventBus().unregister(this.worldEditListener);
                this.worldEditListener = null;
            }

            if (plugin.config.tracking.worldedit.enabled) {
                this.worldEditListener = new WorldEditListener(plugin);
                WorldEdit.getInstance().getEventBus().register(this.worldEditListener);
            }
        }

        // In case of plugin reload
        HandlerList.unregisterAll(plugin);

        PluginManager manager = plugin.getServer().getPluginManager();

        manager.registerEvents(new BlockPlaceListener(plugin), plugin);
        manager.registerEvents(new BlockBreakListener(plugin), plugin);
        manager.registerEvents(new BlockUpdateListener(plugin), plugin);
        manager.registerEvents(new BlockChangeListener(plugin), plugin);
        manager.registerEvents(new BlockExplodeListener(plugin), plugin);
        manager.registerEvents(new BlockPistonListener(plugin), plugin);

        manager.registerEvents(new ChunkListener(), plugin);

        manager.registerEvents(new EntityDamageListener(plugin), plugin);
        manager.registerEvents(new EntityCreateListener(plugin), plugin);

        manager.registerEvents(new PlayerInteractListener(plugin), plugin);
        manager.registerEvents(new PlayerInventoryListener(plugin), plugin);
        manager.registerEvents(new PlayerItemListener(plugin), plugin);
        manager.registerEvents(new PlayerMiscListener(plugin), plugin);

        if (Utils.isInstalled("Slimefun"))
            manager.registerEvents(new SlimefunListener(plugin), plugin);
    }
}
