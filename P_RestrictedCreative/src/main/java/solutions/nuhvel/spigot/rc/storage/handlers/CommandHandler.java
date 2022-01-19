package solutions.nuhvel.spigot.rc.storage.handlers;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.metadata.Metadatable;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.utils.MessagingUtils;
import solutions.nuhvel.spigot.rc.utils.helpers.PreconditionChecker;

public class CommandHandler {
	private static final Set<Player> addWithCommand = new HashSet<>();
	private static final Set<Player> removeWithCommand = new HashSet<>();
	private static final Set<Player> infoWithCommand = new HashSet<>();

	public static Set<Player> getAddWithCommand() {
		return addWithCommand;
	}

	public static Set<Player> getRemoveWithCommand() {
		return removeWithCommand;
	}

	public static Set<Player> getInfoWithCommand() {
		return infoWithCommand;
	}

	public static boolean isAddWithCommand(Player p) {
		return getAddWithCommand().contains(p);
	}

	public static boolean isRemoveWithCommand(Player p) {
		return getRemoveWithCommand().contains(p);
	}

	public static boolean isInfoWithCommand(Player p) {
		return getInfoWithCommand().contains(p);
	}

	public static void removeAddWithCommand(Player p) {
		if (isAddWithCommand(p))
			getAddWithCommand().remove(p);
	}

	public static void removeRemoveWithCommand(Player p) {
		if (isRemoveWithCommand(p))
			getRemoveWithCommand().remove(p);
	}

	public static void removeInfoWithCommand(Player p) {
		if (isInfoWithCommand(p))
			getInfoWithCommand().remove(p);
	}

	public static void setAddWithCommand(Player p) {
		getAddWithCommand().add(p);
	}

	public static void setRemoveWithCommand(Player p) {
		getRemoveWithCommand().add(p);
	}

	public static void setInfoWithCommand(Player p) {
		getInfoWithCommand().add(p);
	}

	public static void checkCommands(RestrictedCreative plugin, Cancellable e, Player p, Metadatable blockOrEntity) {
		/* Command /block */
		if (CommandHandler.isInfoWithCommand(p)) {
			var message = new PreconditionChecker(plugin).isTracked(blockOrEntity).allSucceeded()
					? plugin.messages.block.info.yes : plugin.messages.block.info.no;
			MessagingUtils.sendMessage(p, plugin.messagingUtils
					.getFormattedMessage(true, message)
					.replaceAll("%type%", getType(blockOrEntity)));

			CommandHandler.removeInfoWithCommand(p);
			e.setCancelled(true);
		} else if (CommandHandler.isAddWithCommand(p)) {
			plugin.trackableHandler.setAsTracked(blockOrEntity, p);
			CommandHandler.removeAddWithCommand(p);
			e.setCancelled(true);

			MessagingUtils.sendMessage(p, plugin.messagingUtils
					.getFormattedMessage(true, plugin.messages.block.add.done)
					.replaceAll("%type%", getType(blockOrEntity)));
		} else if (CommandHandler.isRemoveWithCommand(p)) {
			plugin.trackableHandler.removeTracking(blockOrEntity);
			CommandHandler.removeRemoveWithCommand(p);
			e.setCancelled(true);

			MessagingUtils.sendMessage(p, plugin.messagingUtils
					.getFormattedMessage(true, plugin.messages.block.remove.done)
					.replaceAll("%type%", getType(blockOrEntity)));
		}
	}

	private static String getType(Metadatable blockOrEntity) {
		if (blockOrEntity instanceof Block block)
			return block.getType().toString();
		else if (blockOrEntity instanceof Entity entity)
			return entity.getType().toString();
		return "?";
	}
}
