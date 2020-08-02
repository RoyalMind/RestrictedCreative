package me.prunt.restrictedcreative.listeners;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import io.github.thebusybiscuit.slimefun4.api.events.AndroidMineEvent;
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.storage.handlers.BlockHandler;

public class SlimefunListener implements Listener {
	private Main main;

	public SlimefunListener(Main main) {
		this.main = main;

		if (Main.DEBUG)
			System.out.println("Loaded SlimefunListener");
	}

	private Main getMain() {
		return this.main;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onAndroidMine(AndroidMineEvent e) {
		Block b = e.getBlock();

		// No need to control blocks in disabled worlds
		if (getMain().getUtils().isDisabledWorld(b.getWorld().getName()))
			return;

		// No need to control excluded blocks
		if (getMain().getUtils().isExcludedFromTracking(b.getType()))
			return;

		// No need to control non-tracked blocks
		if (!BlockHandler.isTracked(b))
			return;

		if (Main.DEBUG)
			System.out.println("onAndroidMine: " + b.getType());

		e.setCancelled(true);
		BlockHandler.breakBlock(b, null);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerRightClick(PlayerRightClickEvent e) {
		Block b = e.getClickedBlock().orElse(null);
		SlimefunItem sfb = e.getSlimefunBlock().orElse(null);
		Player p = e.getPlayer();

		if (b == null || sfb == null)
			return;

		// No need to control blocks in disabled worlds
		if (getMain().getUtils().isDisabledWorld(b.getWorld().getName()))
			return;

		// No need to track non-creative players
		if (p.getGameMode() != GameMode.CREATIVE)
			return;

		if (Main.DEBUG)
			System.out.println("onPlayerRightClick: " + sfb.getItemName());

		e.setUseBlock(Result.DENY);
		e.setUseItem(Result.DENY);

		getMain().getUtils().sendMessage(e.getPlayer(), true, "disabled.general");
	}
}
