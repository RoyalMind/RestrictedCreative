package solutions.nuhvel.spigot.rc.listeners.entity;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.storage.handlers.TrackableHandler;
import solutions.nuhvel.spigot.rc.utils.helpers.PreconditionChecker;

public class EntityCreateListener implements Listener {
    private final RestrictedCreative plugin;

    public EntityCreateListener(RestrictedCreative plugin) {
        this.plugin = plugin;
    }

    /*
     * Called when a creature is spawned into a world.
     *
     * If a Creature Spawn event is cancelled, the creature will not spawn.
     */
    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        onEntityCreation(e.getEntity(), e.getLocation());
    }

    /*
     * Raised when a vehicle is created.
     */
    @EventHandler(ignoreCancelled = true)
    public void onVehicleCreate(VehicleCreateEvent e) {
        Entity entity = e.getVehicle();
        Location location = entity.getLocation();

        onEntityCreation(entity, location);
    }

    /*
     * Triggered when a hanging entity is created in the world
     */
    // HIGHEST required for WorldGuard and similar plugins
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onHangingPlace(HangingPlaceEvent e) {
        Player player = e.getPlayer();
        Entity entity = e.getEntity();

        if (new PreconditionChecker(plugin, player)
                .isWorldAllowed(entity.getWorld().getName())
                .isTrackingAllowed(entity.getType())
                .isGameMode(GameMode.CREATIVE)
                .anyFailed())
            return;

        TrackableHandler.setAsTracked(entity);
    }

    /*
     * Called when a projectile is launched.
     */
    @EventHandler(ignoreCancelled = true)
    public void onShoot(ProjectileLaunchEvent e) {
        Projectile entity = e.getEntity();
        EntityType type = entity.getType();

        if (!(entity.getShooter() instanceof Player p))
            return;

        // No need to control entities in disabled worlds
        if (new PreconditionChecker(plugin, p)
                .isWorldAllowed(entity.getWorld().getName())
                .isThrowingForbidden(type)
                .isGameMode(GameMode.CREATIVE)
                .anyFailed())
            return;

        e.setCancelled(true);
        plugin.messagingUtils.sendMessage(p, true, plugin.messages.disabled.throwing);
    }

    private void onEntityCreation(Entity entity, Location location) {
        if (new PreconditionChecker(plugin).isWorldAllowed(entity.getWorld().getName()).isTracked(location).anyFailed())
            return;

        TrackableHandler.removeTracking(location);
        TrackableHandler.setAsTracked(entity);
    }
}
