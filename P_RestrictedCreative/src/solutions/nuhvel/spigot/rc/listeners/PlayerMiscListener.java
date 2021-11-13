package solutions.nuhvel.spigot.rc.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import solutions.nuhvel.spigot.rc.storage.config.config.commands.Command;
import solutions.nuhvel.spigot.rc.utils.helpers.PreconditionChecker;
import org.bukkit.GameMode;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.storage.handlers.BlockHandler;
import solutions.nuhvel.spigot.rc.storage.handlers.CommandHandler;
import solutions.nuhvel.spigot.rc.storage.handlers.InventoryHandler;

public class PlayerMiscListener implements Listener {
	private final RestrictedCreative plugin;

	public PlayerMiscListener(RestrictedCreative plugin) {
		this.plugin = plugin;
	}

	private RestrictedCreative getMain() {
		return this.plugin;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onPlayerGameModeChange(PlayerGameModeChangeEvent e) {
		Player player = e.getPlayer();

		if (new PreconditionChecker(plugin, player).isAllowedWorld(player.getWorld().getName()).anyFailed())
			return;

		if (e.getNewGameMode() == player.getGameMode())
			return;

		plugin.getUtils().debug("onPlayerGameModeChange: " + player.getGameMode() + " -> " + e.getNewGameMode());

		// Player wants to switch into creative mode
		if (e.getNewGameMode() == GameMode.CREATIVE) {
			// Player height check
			if (new PreconditionChecker(plugin).isAllowedHeight(player).anyFailed()) {
				getMain().getUtils().sendMessage(player, true, "disabled.region");
				e.setCancelled(true);
				return;
			}

			// Prevents opening a container, switching to creative mode, and dumping items
			if (illegalContainerOpened(player)) {
				getMain().getUtils().sendMessage(player, true, "disabled.general");
				e.setCancelled(true);
				return;
			}

			// Switch inventories, permissions etc
			getMain().getUtils().setCreative(player, true);
		}

		// Player want's to switch out of creative
		else if (player.getGameMode() == GameMode.CREATIVE) {
			// Switch inventories, permissions etc
			getMain().getUtils().setCreative(player, false);
		} else {
			return;
		}

		InventoryHandler.setPreviousGameMode(player, player.getGameMode());
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onCommandAliases(PlayerCommandPreprocessEvent e) {
		Player player = e.getPlayer();
		String fullCommand = e.getMessage();

		for (Command command : plugin.config.commands.asList()) {
			for (String alias : command.aliases) {
				// + space to not catch other commands
				if (!fullCommand.startsWith("/" + alias + " ")
						&& !fullCommand.equalsIgnoreCase("/" + alias))
					continue;

				// + 1 to account for "/" maybe?
				String[] arguments = fullCommand.substring(alias.length() + 1).split(" ");
				List<String> argList = new ArrayList<>(Arrays.asList(arguments));

				// Remove empty strings caused by double spaces and such
				argList.removeAll(Arrays.asList("", null));
				arguments = argList.toArray(new String[0]);

				PluginCommand pc = plugin.getCommand(command.name);
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
				.isAllowedWorld(player.getWorld().getName())
				.doesNotHavePermission("rc.bypass.limit.commands")
				.doesNotHavePermission("rc.bypass.limit.commands." + command.split(" ")[0])
				.anyFailed())
			return;

		// No need to control non-creative players
		if (player.getGameMode() != GameMode.CREATIVE)
			return;

		// Loops through all disabled commands
		for (String regex : plugin.config.limitations.commands) {
			// .substring(1) removes "/" from the command
			if (command.substring(1).toLowerCase().matches(regex)) {
				e.setCancelled(true);
				getMain().getUtils().sendMessage(player, true, "disabled.general");
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
		if (getMain().getUtils().isDisabledWorld(p.getWorld().getName()) == getMain().getUtils()
				.isDisabledWorld(e.getFrom().getName()))
			return;

		// No need to control non-creative players
		if (p.getGameMode() != GameMode.CREATIVE)
			return;

		plugin.getUtils().debug("onPlayerChangedWorld");

		// Removes creative mode
		p.setGameMode(InventoryHandler.getPreviousGameMode(p));

		// Switch inventories, permissions etc
		getMain().getUtils().setCreative(p, false);
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent e) {
		Player player = e.getPlayer();

		if (!new PreconditionChecker(plugin).isAllowedHeight(player).anyFailed())
			return;

		player.setGameMode(InventoryHandler.getPreviousGameMode(player));
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerDamage(EntityDamageEvent e) {
		// Ignore other than players
		if (!(e.getEntity() instanceof Player p))
			return;

		// No need to control damage in disabled worlds
		if (getMain().getUtils().isDisabledWorld(p.getWorld().getName()))
			return;

		// No need to control non-creative players
		if (p.getGameMode() != GameMode.CREATIVE)
			return;

		// No need to control disabled features
		if (!plugin.config.limitations.receivingDamage)
			return;

		// No need to control bypassed players
		if (p.hasPermission("rc.bypass.limit.damage"))
			return;

		plugin.getUtils().debug("onPlayerDamage");

		e.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();

		// No need to control moving in disabled worlds
		if (getMain().getUtils().isDisabledWorld(p.getWorld().getName()))
			return;

		// No need to control non-creative players
		if (p.getGameMode() != GameMode.CREATIVE)
			return;

		// No need to control disabled features
		if (!plugin.config.limitations.items.dropping)
			return;

		// No need to control bypassed players
		if (p.hasPermission("rc.bypass.limit.item.drop"))
			return;

		plugin.getUtils().debug("onPlayerDeath");

		// Removes all drops
		e.getDrops().clear();
		e.setDroppedExp(0);
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerLogin(PlayerLoginEvent e) {
		// We don't care if blocks have already been loaded
		if (BlockHandler.isLoadingDone)
			return;

		// No need to control disabled features
		if (!plugin.config.system.delayLogin)
			return;

		e.disallow(Result.KICK_OTHER, getMain().getUtils().getFormattedMessage(false, "database.load"));
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		getMain().getUtils().loadInventory(p);

		// When force-gamemode is enabled, PlayerGamemodeChangeEvent isn't fired onjoin
		if (!InventoryHandler.isForceGamemodeEnabled())
			return;

		// If player was switched to creative by default and it was previously survival
		if (p.getGameMode() == GameMode.CREATIVE && InventoryHandler.getCreativeInv(p) != null) {
			// Switch inventories, permissions etc
			getMain().getUtils().setCreative(p, true);
			InventoryHandler.setPreviousGameMode(p, GameMode.SURVIVAL);
			return;
		}

		// If player was switched to ~survival by default and it was previously creative
		if (p.getGameMode() != GameMode.CREATIVE && InventoryHandler.getSurvivalInv(p) != null) {
			// Switch inventories, permissions etc
			getMain().getUtils().setCreative(p, false);
			InventoryHandler.setPreviousGameMode(p, GameMode.CREATIVE);

			//noinspection UnnecessaryReturnStatement
			return;
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();

		CommandHandler.removeInfoWithCommand(p);
		CommandHandler.removeAddWithCommand(p);
		CommandHandler.removeRemoveWithCommand(p);

		getMain().getUtils().saveInventory(p);
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerKick(PlayerKickEvent e) {
		Player p = e.getPlayer();

		CommandHandler.removeInfoWithCommand(p);
		CommandHandler.removeAddWithCommand(p);
		CommandHandler.removeRemoveWithCommand(p);

		getMain().getUtils().saveInventory(p);
	}

	private boolean illegalContainerOpened(Player p) {
		InventoryType it = p.getOpenInventory().getType();

		return it != InventoryType.PLAYER && it != InventoryType.CRAFTING
				&& it != InventoryType.CREATIVE && !p.hasPermission("rc.bypass.tracking.inventory")
				&& plugin.config.limitations.items.dropping;
	}
}
