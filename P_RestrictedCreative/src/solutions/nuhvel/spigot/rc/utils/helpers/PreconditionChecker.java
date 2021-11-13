package solutions.nuhvel.spigot.rc.utils.helpers;

import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.utils.Utils;
import solutions.nuhvel.spigot.rc.utils.external.GriefPreventionUtils;
import solutions.nuhvel.spigot.rc.utils.external.TownyAdvancedUtils;
import solutions.nuhvel.spigot.rc.utils.external.WorldGuardUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

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

    public PreconditionChecker isAllowedWorld(String name) {
        if (this.checkHasFailed) return this;

        if (this.player != null && this.player.hasPermission("rc.bypass.general.disabled-worlds"))
            return this;

        List<String> whitelisted = plugin.config.system.worlds.whitelisted;
        List<String> disabled = plugin.config.system.worlds.disabled;

        updateCheckWithResult(!whitelisted.isEmpty()
                ? whitelisted.contains(name)
                : !disabled.contains(name));
        return this;
    }

    public PreconditionChecker isAllowedHeight() {
        if (this.checkHasFailed && this.player == null) return this;

        if (this.player.getGameMode() != GameMode.CREATIVE)
            return this;

        if (!plugin.config.limitations.moving.enabled)
            return this;

        if (this.player.hasPermission("rc.bypass.limit.moving"))
            return this;

        double current = this.player.getLocation().getY();
        int above = plugin.config.limitations.moving.above;
        int below = plugin.config.limitations.moving.below;

        updateCheckWithResult(below <= current && current <= above);
        return this;
    }

    public PreconditionChecker isAllowedRegion() {
        if (this.player == null) return this;
        return isAllowedRegion(this.player.getLocation().getBlock());
    }

    public PreconditionChecker isAllowedRegion(Block block) {
        return isAllowedRegion(block, Material.DIRT);
    }

    public PreconditionChecker isAllowedRegion(Block block, Material type) {
        if (this.checkHasFailed || this.player == null) return this;

        if (!plugin.config.limitations.regions.ownership.enabled
                && !plugin.config.limitations.regions.ownership.allowMembers)
            return this;

        if (this.player.hasPermission("rc.bypass.limit.regions"))
            return this;

        updateCheckWithResult(canBuildHere(player, block, type));
        return this;
    }

    public PreconditionChecker doesNotHavePermission(String permission) {
        if (this.checkHasFailed || this.player == null) return this;

        updateCheckWithResult(!this.player.hasPermission(permission));
        return this;
    }

    private boolean canBuildHere(Player player, Block block, Material material) {
        if (Utils.isInstalled("WorldGuard") && WorldGuardUtils.canBuildHere(plugin, player, block, material))
            return true;

        if (Utils.isInstalled("GriefPrevention")
                && GriefPreventionUtils.canBuildHere(plugin, player, block, material))
            return true;

        if (Utils.isInstalled("Towny") && TownyAdvancedUtils.canBuildHere(plugin, player, block, material))
            return true;

        return false;
    }

    private void updateCheckWithResult(boolean checkResult) {
        if (!this.checkHasFailed) this.checkHasFailed = !checkResult;
    }
}
