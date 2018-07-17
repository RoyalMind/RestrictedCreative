package me.prunt.restrictedcreative.listeners;

import org.bukkit.entity.Player;

import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.eventbus.Subscribe;

import me.prunt.restrictedcreative.Main;

public class WEListener {
    private Main main;

    public WEListener(Main main) {
	this.main = main;
    }

    @Subscribe
    public void wrapForLogging(EditSessionEvent event) {
	Actor actor = event.getActor();

	if (actor != null && actor.isPlayer()) {
	    Player p = main.getServer().getPlayer(actor.getUniqueId());

	    // World check
	    if (main.isDisabledWorld(p.getWorld().getName()))
		return;

	    event.setExtent(new WELogger(main, p, event.getExtent()));
	}
    }
}
