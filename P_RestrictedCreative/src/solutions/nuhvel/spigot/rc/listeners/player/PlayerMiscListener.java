package solutions.nuhvel.spigot.rc.listeners.player;

import org.bukkit.GameMode;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.storage.config.messages.commands.ISimpleCommand;
import solutions.nuhvel.spigot.rc.storage.database.BlockRepository;
import solutions.nuhvel.spigot.rc.storage.handlers.CommandHandler;
import solutions.nuhvel.spigot.rc.storage.handlers.InventoryHandler;
import solutions.nuhvel.spigot.rc.utils.helpers.InventoryHelper;
import solutions.nuhvel.spigot.rc.utils.helpers.PreconditionChecker;
import solutions.nuhvel.spigot.rc.utils.helpers.SwitchingHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerMiscListener implements Listener {
    private final RestrictedCreative plugin;
    private final SwitchingHelper switchingHelper;

    public PlayerMiscListener(RestrictedCreative plugin) {
        this.plugin = plugin;
        switchingHelper = new SwitchingHelper(plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent e) {
        Player player = e.getPlayer();

        if (new PreconditionChecker(plugin, player).isWorldAllowed(player.getWorld().getName()).anyFailed())
            return;

        if (e.getNewGameMode() == player.getGameMode())
            return;

        plugin.getUtils().debug("onPlayerGameModeChange: " + player.getGameMode() + " -> " + e.getNewGameMode());

        // Player wants to switch into creative mode
        if (e.getNewGameMode() == GameMode.CREATIVE) {
            // Player height check
            if (new PreconditionChecker(plugin, player).isHeightAllowed().anyFailed()) {
                plugin.messagingUtils.sendMessage(player, true, plugin.messages.disabled.height);
                e.setCancelled(true);
                return;
            }

            // Prevents opening a container, switching to creative mode, and dumping items
            if (illegalContainerOpened(player)) {
                plugin.messagingUtils.sendMessage(player, true, plugin.messages.disabled.container);
                e.setCancelled(true);
                return;
            }

            // Switch inventories, permissions etc
            switchingHelper.setCreative(player, true);
        }

        // Player want's to switch out of creative
        else if (player.getGameMode() == GameMode.CREATIVE) {
            // Switch inventories, permissions etc
            switchingHelper.setCreative(player, false);
        } else {
            return;
        }

        InventoryHandler.setPreviousGameMode(player, player.getGameMode());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onCommandAliases(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        String fullCommand = e.getMessage();

        for (ISimpleCommand command : plugin.messages.commands.asList()) {
            for (String alias : command.getAliases()) {
                // + space to not catch other commands
                if (!fullCommand.startsWith("/" + alias + " ") && !fullCommand.equalsIgnoreCase("/" + alias))
                    continue;

                // + 1 to account for "/" maybe?
                String[] arguments = fullCommand.substring(alias.length() + 1).split(" ");
                List<String> argList = new ArrayList<>(Arrays.asList(arguments));

                // Remove empty strings caused by double spaces and such
                argList.removeAll(Arrays.asList("", null));
                arguments = argList.toArray(new String[0]);

                PluginCommand pc = plugin.getCommand(command.getName());
                if (pc == null)
                    continue;

                pc.execute(player, alias, arguments);
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        String command = e.getMessage();

        // No need to control commands in disabled worlds
        if (new PreconditionChecker(plugin, player)
                .isWorldAllowed(player.getWorld().getName())
                .isCommandNotBypassed(command)
                .isGameMode(GameMode.CREATIVE)
                .anyFailed())
            return;

        // Loops through all disabled commands
        for (String regex : plugin.config.limitations.commands) {
            // .substring(1) removes "/" from the command
            if (command.substring(1).toLowerCase().matches(regex)) {
                e.setCancelled(true);
                plugin.messagingUtils.sendMessage(player, true, plugin.messages.disabled.command);
                return;
            }
        }
    }

    /*
     * Called when a player switches to another world.
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();

        // No need to control world changing when both worlds are enabled or disabled
        if (new PreconditionChecker(plugin).isWorldAllowed(p.getWorld().getName()).allSucceeded() ==
                new PreconditionChecker(plugin).isWorldAllowed(e.getFrom().getName()).allSucceeded())
            return;

        // No need to control non-creative players
        if (new PreconditionChecker(plugin, p).isGameMode(GameMode.CREATIVE).anyFailed())
            return;

        plugin.getUtils().debug("onPlayerChangedWorld");

        // Removes creative mode
        p.setGameMode(InventoryHandler.getPreviousGameMode(p));

        // Switch inventories, permissions etc
        switchingHelper.setCreative(p, false);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        if (!new PreconditionChecker(plugin, player).isHeightAllowed().anyFailed())
            return;

        player.setGameMode(InventoryHandler.getPreviousGameMode(player));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent e) {
        // Ignore other than players
        if (!(e.getEntity() instanceof Player p))
            return;

        if (new PreconditionChecker(plugin, p)
                .isWorldAllowed(p.getWorld().getName())
                .isGameMode(GameMode.CREATIVE)
                .isInvulnerable()
                .anyFailed())
            return;

        plugin.getUtils().debug("onPlayerDamage");

        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();

        if (new PreconditionChecker(plugin, p)
                .isWorldAllowed(p.getWorld().getName())
                .isGameMode(GameMode.CREATIVE)
                .isDroppingForbidden()
                .anyFailed())
            return;

        plugin.getUtils().debug("onPlayerDeath");

        // Removes all drops
        e.getDrops().clear();
        e.setDroppedExp(0);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerLogin(PlayerLoginEvent e) {
        // We don't care if blocks have already been loaded
        if (BlockRepository.isLoadingDone)
            return;

        // No need to control disabled features
        if (!plugin.config.system.delayLogin)
            return;

        e.disallow(Result.KICK_OTHER,
                plugin.messagingUtils.getFormattedMessage(false, plugin.messages.database.loadSpawns));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        new InventoryHelper(plugin).loadInventory(p);

        // When force-gamemode is enabled, PlayerGamemodeChangeEvent isn't fired onjoin
        if (!InventoryHandler.isForceGamemodeEnabled())
            return;

        // If player was switched to creative by default and it was previously survival
        if (p.getGameMode() == GameMode.CREATIVE && InventoryHandler.getCreativeInv(p) != null) {
            // Switch inventories, permissions etc
            switchingHelper.setCreative(p, true);
            InventoryHandler.setPreviousGameMode(p, GameMode.SURVIVAL);
            return;
        }

        // If player was switched to survival by default and it was previously creative
        if (p.getGameMode() != GameMode.CREATIVE && InventoryHandler.getSurvivalInv(p) != null) {
            // Switch inventories, permissions etc
            switchingHelper.setCreative(p, false);
            InventoryHandler.setPreviousGameMode(p, GameMode.CREATIVE);

            //noinspection UnnecessaryReturnStatement
            return;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent e) {
        onPlayerLogout(e);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerKick(PlayerKickEvent e) {
        onPlayerLogout(e);
    }

    private void onPlayerLogout(PlayerEvent e) {
        Player p = e.getPlayer();

        CommandHandler.removeInfoWithCommand(p);
        CommandHandler.removeAddWithCommand(p);
        CommandHandler.removeRemoveWithCommand(p);

        new InventoryHelper(plugin).saveInventory(p);
    }

    private boolean illegalContainerOpened(Player p) {
        InventoryType it = p.getOpenInventory().getType();

        return it != InventoryType.PLAYER && it != InventoryType.CRAFTING && it != InventoryType.CREATIVE &&
                !p.hasPermission("rc.bypass.tracking.inventory") && plugin.config.limitations.items.dropping;
    }
}
