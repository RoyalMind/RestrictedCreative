package me.prunt.restrictedcreative.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.store.DataHandler;

public class BlockPlaceListener implements Listener {
    private Main main;

    public BlockPlaceListener(Main main) {
	this.main = main;
    }

    private Main getMain() {
	return this.main;
    }

    // HIGHEST required for WorldGuard and similar plugins
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
	Player p = e.getPlayer();
	Block b = e.getBlockPlaced();
	Material m = b.getType();

	// No need to track blocks in disabled and bypassed worlds
	if (getMain().isDisabledWorld(p.getWorld().getName()) || getMain().isBypassedWorld(p.getWorld().getName()))
	    return;

	// No need to track non-creative players
	if (p.getGameMode() != GameMode.CREATIVE)
	    return;

	// No need to track excluded blocks
	if (getMain().isExcluded(b.getType()))
	    return;

	// No need to track bypassed players
	if (p.hasPermission("rc.bypass.tracking.blocks") || p.hasPermission("rc.bypass.tracking.blocks." + m))
	    return;

	/* Disabled blocks */
	if (getMain().isDisabledPlacing(m) && !p.hasPermission("rc.bypass.disable.placing")
		&& !p.hasPermission("rc.bypass.disable.placing." + m)) {
	    main.sendMessage(p, true, "disabled.general");

	    e.setCancelled(true);
	    return;
	}

	DataHandler.addForTracking(b);

	/* Creature creation */
	Block head = b;

	// Wither + config check
	if (m == Material.SKULL && main.wither && main.couldWitherBeBuilt(head)) {
	    if (p.getGameMode() == GameMode.CREATIVE || !main.canSurvivalBuildWither(head)) {
		e.setCancelled(true);

		// Message none check
		if (!main.isNone(main.dis_creature))
		    p.sendMessage(main.prefix + main.dis_creature);

		return;
	    }

	    // Golem check
	} else if (m == Material.PUMPKIN || m == Material.JACK_O_LANTERN) {
	    // Config boolean check
	    if (main.snowgolem && main.couldSnowGolemBeBuilt(head)) {
		// Creative check
		if (p.getGameMode() == GameMode.CREATIVE || !main.canSurvivalBuildSnowGolem(head)) {
		    e.setCancelled(true);

		    // Message none check
		    if (!main.isNone(main.dis_creature))
			p.sendMessage(main.prefix + main.dis_creature);

		    return;
		}
		// Config boolean check
	    } else if (main.irongolem && main.couldIronGolemBeBuilt(head)) {
		// Creative check
		if (p.getGameMode() == GameMode.CREATIVE || !main.canSurvivalBuildIronGolem(head)) {
		    e.setCancelled(true);

		    // Message none check
		    if (!main.isNone(main.dis_creature))
			p.sendMessage(main.prefix + main.dis_creature);

		    return;
		}
	    }
	}
    }
}
