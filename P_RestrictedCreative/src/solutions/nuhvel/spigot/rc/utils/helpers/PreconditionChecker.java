package solutions.nuhvel.spigot.rc.utils.helpers;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.utils.Utils;
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

    public PreconditionChecker isAllowedWorld(String name) {
        if (checkHasFailed) return this;

        if (player != null && player.hasPermission("rc.bypass.general.disabled-worlds"))
            return this;

        List<String> whitelisted = plugin.config.system.worlds.whitelisted;
        List<String> disabled = plugin.config.system.worlds.disabled;

        return updateCheckWithResult(!whitelisted.isEmpty() ? whitelisted.contains(name) : !disabled.contains(name));
    }

    public PreconditionChecker isAllowedHeight() {
        if (checkHasFailed && player == null) return this;

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

    public PreconditionChecker isAllowedRegion() {
        if (player == null) return this;
        return isAllowedRegion(player.getLocation().getBlock());
    }

    public PreconditionChecker isAllowedRegion(Block block) {
        return isAllowedRegion(block, Material.DIRT);
    }

    public PreconditionChecker isAllowedRegion(Block block, Material type) {
        if (checkHasFailed || player == null) return this;

        if (!plugin.config.limitations.regions.ownership.enabled
                && !plugin.config.limitations.regions.ownership.allowMembers)
            return this;

        if (player.hasPermission("rc.bypass.limit.regions"))
            return this;

        return updateCheckWithResult(canBuildHere(player, block, type));
    }

    public PreconditionChecker doesNotHavePermission(String permission) {
        if (checkHasFailed || player == null) return this;

        return updateCheckWithResult(!player.hasPermission(permission));
    }

    public PreconditionChecker isNotExcludedFromTracking(Material m, Material... exceptions) {
        if (checkHasFailed) return this;

        if (Arrays.asList(exceptions).contains(m))
            return this;

        if (player != null && (player.hasPermission("rc.bypass.tracking.blocks")
                || player.hasPermission("rc.bypass.tracking.blocks." + m)))
            return this;

        var isExcluded = plugin.config.tracking.blocks.excluded.contains(m)
                || isTrackingDisabled() || m == Material.AIR;
        return updateCheckWithResult(!isExcluded);
    }

    public PreconditionChecker isTracked(Block b) {
        if (checkHasFailed) return this;
        return updateCheckWithResult(isTrackedBlock(b));
    }

    public PreconditionChecker isTracked(Entity e) {
        if (checkHasFailed) return this;
        return updateCheckWithResult(e != null && e.getScoreboardTags().contains("GMC"));
    }

    public PreconditionChecker isDisabledBreaking(Material m) {
        if (checkHasFailed) return this;

        if (player != null && (player.hasPermission("rc.bypass.disable.breaking")
                || player.hasPermission("rc.bypass.disable.breaking." + m)))
            return this;

        return updateCheckWithResult(plugin.config.disable.breaking.contains(m));
    }


    public PreconditionChecker isDisabledPlacing(Material type) {
        if (checkHasFailed) return this;

        if (player != null && (player.hasPermission("rc.bypass.disable.placing")
                || player.hasPermission("rc.bypass.disable.placing." + type)))
            return this;

        return updateCheckWithResult(plugin.config.disable.placing.contains(type));
    }

    private boolean isTrackedBlock(Block b) {
        if (b == null) return false;

        for (MetadataValue mdv : b.getMetadata("RC3"))
            if (mdv.asBoolean())
                return true;

        return false;
    }

    private boolean isTrackingDisabled() {
        return !plugin.config.tracking.blocks.enabled;
    }

    private boolean canBuildHere(Player player, Block block, Material material) {
        if (Utils.isInstalled("WorldGuard") && WorldGuardUtils.canBuildHere(plugin, player, block, material))
            return true;

        if (Utils.isInstalled("GriefPrevention")
                && GriefPreventionUtils.canBuildHere(plugin, player, block, material))
            return true;

        return Utils.isInstalled("Towny") && TownyAdvancedUtils.canBuildHere(plugin, player, block, material);
    }

    private PreconditionChecker updateCheckWithResult(boolean checkResult) {
        if (!checkHasFailed) checkHasFailed = !checkResult;
        return this;
    }
}
