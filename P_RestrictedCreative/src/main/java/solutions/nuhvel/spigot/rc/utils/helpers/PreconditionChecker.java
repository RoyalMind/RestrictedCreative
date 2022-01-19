package solutions.nuhvel.spigot.rc.utils.helpers;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.metadata.Metadatable;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.storage.handlers.TrackableHandler;
import solutions.nuhvel.spigot.rc.utils.minecraft.ServerUtils;
import solutions.nuhvel.spigot.rc.utils.external.GriefPreventionUtils;
import solutions.nuhvel.spigot.rc.utils.external.TownyAdvancedUtils;
import solutions.nuhvel.spigot.rc.utils.external.WorldGuardUtils;

import java.util.Arrays;
import java.util.List;

public class PreconditionChecker {
    private final RestrictedCreative plugin;
    private Player player;
    private boolean checkHasFailed = false;

    public PreconditionChecker(RestrictedCreative plugin) {
        this.plugin = plugin;
    }

    public PreconditionChecker(RestrictedCreative plugin, Player player) {
        this(plugin);
        this.player = player;
    }

    public boolean anyFailed() {
        return checkHasFailed;
    }

    public boolean allSucceeded() {
        return !checkHasFailed;
    }

    public PreconditionChecker isWorldAllowed(String name) {
        if (checkHasFailed)
            return this;

        if (player != null)
            if (player.hasPermission("rc.bypass.general.disabled-worlds"))
                return updateCheckWithResult(false);

        List<String> whitelisted = plugin.config.system.worlds.whitelisted;
        List<String> disabled = plugin.config.system.worlds.disabled;

        return updateCheckWithResult(!whitelisted.isEmpty() ? whitelisted.contains(name) : !disabled.contains(name));
    }

    public PreconditionChecker isHeightAllowed() {
        if (checkHasFailed && player == null)
            return this;

        if (player.getGameMode() != GameMode.CREATIVE)
            return this;

        if (!plugin.config.limitations.moving.enabled)
            return this;

        if (player.hasPermission("rc.bypass.limit.moving"))
            return this;

        double current = player.getLocation().getY();
        int above = plugin.config.limitations.moving.above;
        int below = plugin.config.limitations.moving.below;

        return updateCheckWithResult(below <= current && current <= above);
    }

    public PreconditionChecker isRegionAllowed() {
        if (player == null)
            return this;
        return isRegionAllowed(player.getLocation().getBlock());
    }

    public PreconditionChecker isRegionAllowed(Block block) {
        return isRegionAllowed(block, Material.DIRT);
    }

    public PreconditionChecker isRegionAllowed(Block block, Material type) {
        if (checkHasFailed || player == null)
            return this;

        if (!plugin.config.limitations.regions.ownership.enabled &&
                !plugin.config.limitations.regions.ownership.allowMembers)
            return this;

        if (player.hasPermission("rc.bypass.limit.regions"))
            return this;

        return updateCheckWithResult(canBuildHere(player, block, type));
    }

    public PreconditionChecker doesNotHavePermission(String permission) {
        if (checkHasFailed || player == null)
            return this;
        return updateCheckWithResult(!player.hasPermission(permission));
    }

    public PreconditionChecker isTrackingAllowed(Material m, Material... exceptions) {
        if (checkHasFailed)
            return this;

        if (Arrays.asList(exceptions).contains(m))
            return this;

        if (player != null && (player.hasPermission("rc.bypass.tracking.blocks") ||
                player.hasPermission("rc.bypass.tracking.blocks." + m)))
            return updateCheckWithResult(false);

        var isExcluded =
                plugin.config.tracking.blocks.excluded.contains(m) || isTrackingDisabled() || m == Material.AIR;
        return updateCheckWithResult(!isExcluded);
    }

    public PreconditionChecker isTrackingAllowed(EntityType type) {
        if (checkHasFailed)
            return this;

        if (player != null)
            if (player.hasPermission("rc.bypass.tracking.entities") ||
                    player.hasPermission("rc.bypass.tracking.entities." + type))
                return updateCheckWithResult(false);

        return updateCheckWithResult(!isTrackingDisabled());
    }

    public PreconditionChecker isTracked(Metadatable blockOrEntity) {
        if (checkHasFailed)
            return this;
        return updateCheckWithResult(TrackableHandler.isTracked(blockOrEntity));
    }

    public PreconditionChecker isTracked(Location location) {
        if (checkHasFailed)
            return this;
        return updateCheckWithResult(TrackableHandler.isTracked(location));
    }

    public PreconditionChecker isBreakingForbidden(Material m) {
        if (checkHasFailed)
            return this;

        if (player != null && (player.hasPermission("rc.bypass.disable.breaking") ||
                player.hasPermission("rc.bypass.disable.breaking." + m)))
            return this;

        return updateCheckWithResult(plugin.config.disable.breaking.contains(m));
    }

    public PreconditionChecker isPlacingForbidden(Material type) {
        if (checkHasFailed)
            return this;

        if (player != null && (player.hasPermission("rc.bypass.disable.placing") ||
                player.hasPermission("rc.bypass.disable.placing." + type)))
            return this;

        return updateCheckWithResult(plugin.config.disable.placing.contains(type));
    }

    public PreconditionChecker isThrowingForbidden(EntityType type) {
        if (checkHasFailed)
            return this;

        if (player != null && (player.hasPermission("rc.bypass.limit.item.throw") ||
                player.hasPermission("rc.bypass.limit.item.throw." + type)))
            return updateCheckWithResult(false);

        return updateCheckWithResult(plugin.config.limitations.items.throwing);
    }

    public PreconditionChecker isDroppingForbidden() {
        return isDroppingForbidden(null);
    }

    public PreconditionChecker isDroppingForbidden(Material type) {
        if (checkHasFailed)
            return this;

        if (player != null && (player.hasPermission("rc.bypass.limit.item.drop") ||
                player.hasPermission("rc.bypass.limit.item.drop." + type)))
            return updateCheckWithResult(false);

        return updateCheckWithResult(plugin.config.limitations.items.dropping);
    }

    public PreconditionChecker isPickupForbidden(Material type) {
        if (checkHasFailed)
            return this;

        if (player != null && (player.hasPermission("rc.bypass.limit.item.pickup") ||
                player.hasPermission("rc.bypass.limit.item.pickup." + type)))
            return updateCheckWithResult(false);

        return updateCheckWithResult(plugin.config.limitations.items.pickingUp);
    }

    public PreconditionChecker isInvulnerable() {
        if (checkHasFailed || player == null)
            return this;

        if (player.hasPermission("rc.bypass.limit.damage"))
            return this;

        return updateCheckWithResult(plugin.config.limitations.receivingDamage);
    }

    public PreconditionChecker isCommandNotBypassed(String command) {
        if (checkHasFailed || player == null)
            return this;

        var hasBypassPermissions = player.hasPermission("rc.bypass.limit.commands") ||
                player.hasPermission("rc.bypass.limit.commands." + command);
        return updateCheckWithResult(hasBypassPermissions);
    }

    public PreconditionChecker isGameMode(GameMode gameMode) {
        if (checkHasFailed || player == null)
            return this;
        return updateCheckWithResult(player.getGameMode() == gameMode);
    }

    public PreconditionChecker isPlayerVersusEntityAllowed(EntityType type) {
        if (checkHasFailed || player == null)
            return this;

        if (player.hasPermission("rc.bypass.limit.combat.pve") ||
                player.hasPermission("rc.bypass.limit.combat.pve." + type))
            return this;

        return updateCheckWithResult(!plugin.config.limitations.combat.pve);
    }

    public PreconditionChecker isPlayerVersusPlayerAllowed() {
        if (checkHasFailed || player == null)
            return this;

        if (player.hasPermission("rc.bypass.limit.combat.pvp"))
            return this;

        return updateCheckWithResult(!plugin.config.limitations.combat.pvp);
    }

    public PreconditionChecker isInteractingOnGroundForbidden(Material m) {
        if (checkHasFailed)
            return this;

        if (player != null && (player.hasPermission("rc.bypass.disable.interacting.on-ground") ||
                player.hasPermission("rc.bypass.disable.interacting.on-ground." + m)))
            return updateCheckWithResult(false);

        return updateCheckWithResult(plugin.config.disable.interacting.onGround.contains(m));
    }

    public PreconditionChecker isInteractingInHandForbidden(Material m) {
        if (checkHasFailed)
            return this;

        if (player != null && (player.hasPermission("rc.bypass.disable.interacting.in-hand") ||
                player.hasPermission("rc.bypass.disable.interacting.in-hand." + m)))
            return updateCheckWithResult(false);

        return updateCheckWithResult(plugin.config.disable.interacting.inHand.contains(m));
    }

    private boolean isTrackingDisabled() {
        return !plugin.config.tracking.blocks.enabled;
    }

    private boolean canBuildHere(Player player, Block block, Material material) {
        if (ServerUtils.isInstalled("WorldGuard") && !WorldGuardUtils.canBuildHere(plugin, player, block))
            return false;

        if (ServerUtils.isInstalled("GriefPrevention") && !GriefPreventionUtils.canBuildHere(plugin, player, block, material))
            return false;

        //noinspection RedundantIfStatement
        if (ServerUtils.isInstalled("Towny") && !TownyAdvancedUtils.canBuildHere(plugin, player, block))
            return false;

        return true;
    }

    private PreconditionChecker updateCheckWithResult(boolean checkResult) {
        if (!checkHasFailed)
            checkHasFailed = !checkResult;
        return this;
    }
}
