package solutions.nuhvel.spigot.rc.listeners;

import solutions.nuhvel.spigot.rc.utils.helpers.PreconditionChecker;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Bed.Part;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.PistonHead;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.storage.handlers.BlockHandler;
import solutions.nuhvel.spigot.rc.utils.MaterialHandler;
import solutions.nuhvel.spigot.rc.utils.Utils;

public class BlockBreakListener implements Listener {
	private final RestrictedCreative plugin;

	public BlockBreakListener(RestrictedCreative plugin) {
		this.plugin = plugin;
	}

	private RestrictedCreative getMain() {
		return this.plugin;
	}

	/*
	 * Called when a block is broken by a player.
	 *
	 * Note: Plugins wanting to simulate a traditional block drop should set the
	 * block to air and utilize their own methods for determining what the default
	 * drop for the block being broken is and what to do about it, if anything.
	 *
	 * If a Block Break event is cancelled, the block will not break and experience
	 * will not drop.
	 */
	// HIGHEST required for WorldGuard and similar plugins
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent e) {
		Player player = e.getPlayer();
		Block b = e.getBlock();
		BlockData bd = b.getBlockData();
		Material m = b.getType();

		if (new PreconditionChecker(plugin, player).isAllowedWorld(e.getBlock().getWorld().getName()).anyFailed())
			return;

		/*
		 * Disabled blocks for creative players
		 * 
		 * Must come before excluded check in order for it to work when tracking is disabled
		 */
		if (player.getGameMode() == GameMode.CREATIVE && getMain().getUtils().isDisabledBreaking(m)
				&& !player.hasPermission("rc.bypass.disable.breaking")
				&& !player.hasPermission("rc.bypass.disable.breaking." + m)) {
			getMain().getUtils().sendMessage(player, true, "disabled.general");
			e.setCancelled(true);
			return;
		}

		// No need to control excluded blocks
		if (getMain().getUtils().isExcludedFromTracking(b.getType()))
			return;

		// Piston head
		// Needs to be BEFORE isTracked() because PistonHead is not tracked
		if (bd instanceof PistonHead head) {
			Block piston = b.getRelative(head.getFacing().getOppositeFace());

			// No need to control non-tracked pistons
			if (!BlockHandler.isTracked(piston))
				return;

			plugin.getUtils().debug("PistonHead: " + piston.getType());

			remove(e, player, false, piston, b);
		}

		// No need to control non-tracked blocks
		if (!BlockHandler.isTracked(b))
			return;

		plugin.getUtils().debug("onBlockBreak: " + m);

		// Door
		if (bd instanceof Door door) {
			Block bl;

			if (door.getHalf() == Half.TOP) {
				bl = b.getRelative(BlockFace.DOWN);
			} else {
				bl = b.getRelative(BlockFace.UP);
			}

			remove(e, player, false, bl);
		}

		// Bed
		else if (bd instanceof Bed bed) {
			Block bl;

			if (bed.getPart() == Part.HEAD) {
				bl = b.getRelative(bed.getFacing().getOppositeFace());
			} else {
				bl = b.getRelative(bed.getFacing());
			}

			remove(e, player, false, bl);
		}

		// Double plant
		else if (MaterialHandler.isDoublePlant(b)) {
			Block bl = b.getRelative(BlockFace.DOWN);

			// If block below isn't a double plant, the one above must be
			if (!MaterialHandler.isDoublePlant(bl))
				bl = b.getRelative(BlockFace.UP);

			remove(e, player, false, bl);
		}

		// Others
		remove(e, player, true, b);
	}

	private void remove(BlockBreakEvent e, Player p, boolean update, Block... blocks) {
		if (Utils.isInstalled("Jobs") || Utils.isInstalled("mcMMO")) {
			for (Block b : blocks)
				BlockHandler.breakBlock(b, p, update);

			e.setCancelled(true);
		} else {
			for (Block b : blocks) {
				BlockHandler.removeTracking(b);
			}

			e.setDropItems(false);
			e.setExpToDrop(0);
		}

		// Notify the breaker why there's no drops
		if (p.getGameMode() != GameMode.CREATIVE && plugin.config.tracking.blocks.notify)
			getMain().getUtils().sendMessage(p, true, "disabled.drops");
	}
}
