package solutions.nuhvel.spigot.rc.commands;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.storage.handlers.CommandHandler;
import solutions.nuhvel.spigot.rc.storage.handlers.TrackableHandler;
import solutions.nuhvel.spigot.rc.utils.MessagingUtils;
import solutions.nuhvel.spigot.rc.utils.helpers.PreconditionChecker;

public class MainCommand implements CommandExecutor {
    private final RestrictedCreative plugin;

    public MainCommand(RestrictedCreative plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0)
            return false;

        switch (args[0].toLowerCase()) {
            case "reload":
                if (sender.hasPermission("rc.commands.reload")) {
                    reload(sender);
                    return true;
                }
                break;
            case "block":
                if (sender.hasPermission("rc.commands.block")) {
                    block(sender, args);
                    return true;
                }
                break;
            /*
             * case "convert": if (sender.hasPermission("rc.commands.convert")) {
             * convert(sender, args); return true; } break;
             */
            case "i-am-sure-i-want-to-delete-all-plugin-data-from-database":
                if (sender.hasPermission("rc.commands.delete")) {
                    delete(sender);
                    return true;
                }
                break;
            default:
                return false;
        }

        plugin.messagingUtils.sendMessage(sender, false, plugin.messages.errors.noPermission);
        return true;
    }

    private void reload(CommandSender sender) {
        plugin.reload();
        plugin.messagingUtils.sendMessage(sender, true, plugin.messages.plugin.reloaded);
    }

    private void delete(CommandSender sender) {
        plugin.database.executeUpdate("DELETE FROM " + plugin.database.getBlocksTable());

        // Loops through worlds
        for (World w : plugin.getServer().getWorlds()) {
            // Leaves out the disabled ones
            if (new PreconditionChecker(plugin).isWorldAllowed(w.getName()).anyFailed())
                continue;

            // Loops through entities
            for (Entity e : w.getEntities())
                if (TrackableHandler.isTracked(e))
                    TrackableHandler.removeTracking(e);
        }

        plugin.messagingUtils.sendMessage(sender, true, plugin.messages.database.deleted);
    }

    /*
     * private void convert(CommandSender sender, String[] args) { String type =
     * main.getSettings().getString("database.type").toLowerCase();
     *
     * if (!type.equalsIgnoreCase("mysql") && !type.equalsIgnoreCase("sqlite")) {
     * main.getUtils().sendMessage(sender, true, "database.incorrect-type"); return;
     * }
     *
     * Database oldDb = new Database(main, type == "mysql" ? "sqlite" : "mysql");
     *
     * Bukkit.getScheduler().runTaskAsynchronously(main, new Runnable() {
     *
     * @Override public void run() { long start = System.currentTimeMillis();
     *
     * main.getUtils().sendMessage(sender, true, "database.load");
     *
     * // Gets all old blocks from database ResultSet rs =
     * oldDb.executeQuery("SELECT * FROM " + oldDb.getBlocksTable());
     *
     * int count = 0; try { while (rs.next()) { String block =
     * rs.getString("block"); String chunk = Utils.getBlockChunk(block);
     *
     * String world = block.split(";")[0]; if
     * (main.getUtils().isDisabledWorld(world) || Bukkit.getWorld(world) == null)
     * continue;
     *
     * BlockHandler.addBlockToChunk(chunk, block); count++; } } catch (SQLException
     * e) { Bukkit.getLogger().log(Level.WARNING, "Data loading was interrupted!");
     * e.printStackTrace(); }
     *
     * int chunksLoaded = blocksInChunk.size();
     *
     * if (Main.DEBUG) System.out.println("loadFromDatabase: " + chunksLoaded +
     * " chunks");
     *
     * int radius = 8; for (World world : Bukkit.getWorlds()) { // Ignore disabled
     * worlds if (main.getUtils().isDisabledWorld(world.getName())) continue;
     *
     * Chunk center = world.getSpawnLocation().getChunk();
     *
     * for (int x = center.getX() - radius; x < center.getX() + radius; x++) { for
     * (int z = center.getZ() - radius; z < center.getZ() + radius; z++) { Chunk c =
     * world.getChunkAt(x, z); loadBlocks(c); } } }
     *
     * setTotalCount(count);
     *
     * Utils.sendMessage(Bukkit.getConsoleSender(), main.getUtils().getMessage(true,
     * "database.loaded") .replaceAll("%blocks%",
     * getTotalCount()).replaceAll("%chunks%", String.valueOf(chunksLoaded)));
     *
     * String took = String.valueOf(System.currentTimeMillis() - start);
     *
     * Utils.sendMessage(Bukkit.getConsoleSender(), main.getUtils().getMessage(true,
     * "database.done").replaceAll("%mills%", took)); } }); }
     */

    private void block(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.messagingUtils.sendMessage(sender, false, plugin.messages.block.usage);
            return;
        }

        if (args[1].equalsIgnoreCase("stats")) {
            plugin.blockRepository.getTotalCount((count) -> {
                String message = plugin.messagingUtils
                        .getFormattedMessage(true, plugin.messages.block.stats)
                        .replaceAll("%total%", String.valueOf(count));
                MessagingUtils.sendMessage(sender, message);
            });
            return;
        }

        if (!(sender instanceof Player p)) {
            plugin.messagingUtils.sendMessage(sender, true, plugin.messages.errors.noConsole);
            return;
        }

        switch (args[1].toLowerCase()) {
            case "add":
                if (CommandHandler.getAddWithCommand().contains(p)) {
                    CommandHandler.getAddWithCommand().remove(p);
                    plugin.messagingUtils.sendMessage(sender, true, plugin.messages.block.cancel);
                } else {
                    CommandHandler.getAddWithCommand().add(p);
                    plugin.messagingUtils.sendMessage(sender, true, plugin.messages.block.add.info);
                }
                break;
            case "remove":
                if (CommandHandler.getRemoveWithCommand().contains(p)) {
                    CommandHandler.getRemoveWithCommand().remove(p);
                    plugin.messagingUtils.sendMessage(sender, true, plugin.messages.block.cancel);
                } else {
                    CommandHandler.getRemoveWithCommand().add(p);
                    plugin.messagingUtils.sendMessage(sender, true, plugin.messages.block.remove.info);
                }
                break;
            case "info":
                if (CommandHandler.getInfoWithCommand().contains(p)) {
                    CommandHandler.getInfoWithCommand().remove(p);
                    plugin.messagingUtils.sendMessage(sender, true, plugin.messages.block.cancel);
                } else {
                    CommandHandler.getInfoWithCommand().add(p);
                    plugin.messagingUtils.sendMessage(sender, true, plugin.messages.block.info.info);
                }
                break;
            default:
                plugin.messagingUtils.sendMessage(sender, false, plugin.messages.block.usage);
        }
    }
}
