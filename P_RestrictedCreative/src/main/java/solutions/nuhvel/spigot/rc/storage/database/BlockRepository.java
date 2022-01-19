package solutions.nuhvel.spigot.rc.storage.database;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.utils.helpers.PreconditionChecker;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntConsumer;
import java.util.logging.Level;

public class BlockRepository {
    public static boolean isLoadingDone = true; // TODO fix this
    protected final List<BlockModel> addToDatabase = new ArrayList<>();
    protected final List<BlockModel> removeFromDatabase = new ArrayList<>();
    private final RestrictedCreative plugin;
    public Database database;

    public BlockRepository(RestrictedCreative plugin, Database database) {
        this.plugin = plugin;
        this.database = database;

        createTables();
        purgeOldData();

        loadSpawnChunksFromDatabase();
        startDataSync();
    }

    public void addBlock(BlockModel model) {
        if (model == null)
            return;

        removeFromDatabase.remove(model);
        addToDatabase.add(model);
    }

    public void removeBlock(BlockModel model) {
        if (model == null)
            return;

        addToDatabase.remove(model);
        removeFromDatabase.add(model);
    }

    public void saveAndClose() {
        final Set<BlockModel> fAdd = new HashSet<>(addToDatabase);
        final Set<BlockModel> fDel = new HashSet<>(removeFromDatabase);
        new DataSyncRunnable(plugin, fAdd, fDel, true).run();
    }

    public void loadChunkFromDatabase(Chunk chunk) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            var blocks = getBlocksFromDatabase(chunk);

            if (!blocks.isEmpty())
                Bukkit.getScheduler().runTask(plugin, () -> loadBlocks(blocks));
        });
    }

    public void getTotalCount(IntConsumer callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                var result = plugin.database.executeQuery("SELECT COUNT(*) FROM " + plugin.database.getBlocksTable());
                result.next();

                var count = result.getInt(1);
                count -= removeFromDatabase.size();
                count += addToDatabase.size();

                plugin.getUtils().debug("to remove: " + removeFromDatabase.size() + "; to add: " + addToDatabase.size());

                callback.accept(count);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void loadBlocks(Set<BlockModel> blocks) {
        plugin.getUtils().debug("loadBlocks: " + blocks.size());

        for (BlockModel model : blocks) {
            Block block = model.block;

            if (block == null || block.isEmpty()) {
                removeBlock(model);
            } else {
                plugin.trackableHandler.markAsCreative(block);
            }
        }
    }

    private void createTables() {
        plugin.database.executeUpdate(
                "CREATE TABLE IF NOT EXISTS " + plugin.database.getBlocksTable() + " (" + "`x` INT NOT NULL, " +
                        "`y` INT NOT NULL, " + "`z` INT NOT NULL, " + "`world` VARCHAR NOT NULL, " +
                        "`chunk_x` INT NOT NULL, " + "`chunk_z` INT NOT NULL, " + "`owner` CHAR(36) NOT NULL, " +
                        "`created` INT UNSIGNED NOT NULL, " + "UNIQUE (`x`, `y`, `z`, `world`))");

        plugin.database.executeUpdate("CREATE TABLE IF NOT EXISTS " + plugin.database.getInventoryTable() + " (" +
                "`player` CHAR(36) NOT NULL, " + "`type` TINYINT(1), " + "`storage` VARCHAR, " + "`armor` VARCHAR, " +
                "`extra` VARCHAR, " + "`effects` VARCHAR, " + "`xp` BIGINT, " + "`last_used` BIGINT(11), " +
                "UNIQUE (player))");
    }

    private void purgeOldData() {
        if (plugin.config.tracking.inventories.purge.enabled) {
            long survival = Instant.now().getEpochSecond() - 86400L * plugin.config.tracking.inventories.purge.survival;
            long creative = Instant.now().getEpochSecond() - 86400L * plugin.config.tracking.inventories.purge.creative;

            plugin.database.executeUpdate(
                    "DELETE FROM " + plugin.database.getInventoryTable() + " WHERE type = 0 AND last_used < " +
                            survival + " OR type = 1 AND last_used < " + creative);
        }
    }

    private void startDataSync() {
        // `syncInterval` is in minutes, `interval` is in ticks
        int interval = plugin.config.tracking.blocks.syncInterval * 60 * 20;

        Bukkit.getServer().getScheduler().runTaskTimer(plugin, () -> {
            final HashSet<BlockModel> fAdd = new HashSet<>(addToDatabase);
            final HashSet<BlockModel> fDel = new HashSet<>(removeFromDatabase);

            Bukkit.getScheduler().runTaskAsynchronously(plugin, new DataSyncRunnable(plugin, fAdd, fDel, false));
        }, interval, interval);
    }

    // Even if plugin.yml has "load: STARTUP" the blocks haven't loaded from the
    // database when chunk load events are fired
    private void loadSpawnChunksFromDatabase() {
        plugin.messagingUtils.sendMessage(Bukkit.getConsoleSender(), true, plugin.messages.database.loadSpawns);

        int radius = 8;
        for (World world : Bukkit.getWorlds()) {
            if (new PreconditionChecker(plugin).isWorldAllowed(world.getName()).anyFailed())
                continue;

            Chunk center = world.getSpawnLocation().getChunk();

            for (int x = center.getX() - radius; x < center.getX() + radius; x++)
                for (int z = center.getZ() - radius; z < center.getZ() + radius; z++)
                    loadChunkFromDatabase(world.getChunkAt(x, z));
        }
    }

    private HashSet<BlockModel> getBlocksFromDatabase(Chunk chunk) {
        var blocks = new HashSet<BlockModel>();
        var world = chunk.getWorld().getName();

        try {
            var statement = plugin.database.getStatement(
                    "SELECT x, y, z, owner, created FROM " + plugin.database.getBlocksTable() +
                            " WHERE chunk_x = ? AND chunk_z = ? AND world = ?");

            statement.setInt(1, chunk.getX());
            statement.setInt(2, chunk.getZ());
            statement.setString(3, world);

            var result = statement.executeQuery();
            while (result.next()) {
                var x = result.getInt("x");
                var y = result.getInt("y");
                var z = result.getInt("z");
                var owner = result.getString("owner");
                var created = result.getLong("created");

                blocks.add(BlockModel.fromData(x, y, z, world, owner, created));
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Data loading was interrupted! Restarting...");
            loadChunkFromDatabase(chunk);
        }

        return blocks;
    }
}
