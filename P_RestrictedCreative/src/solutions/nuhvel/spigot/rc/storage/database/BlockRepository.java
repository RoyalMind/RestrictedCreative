package solutions.nuhvel.spigot.rc.storage.database;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.storage.config.config.database.DatabaseType;
import solutions.nuhvel.spigot.rc.storage.handlers.BlockHandler;
import solutions.nuhvel.spigot.rc.storage.handlers.InventoryHandler;
import solutions.nuhvel.spigot.rc.utils.Utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;

public class BlockRepository {
    private final FixedMetadataValue fixedMetadataValue;
    private final String metadataKey = "RestrictedCreative3";
    private final RestrictedCreative plugin;
    public Database database;

    private final List<BlockModel> addToDatabase = new ArrayList<>();
    private final List<BlockModel> removeFromDatabase = new ArrayList<>();

    public BlockRepository(RestrictedCreative plugin, Database database) {
        this.plugin = plugin;
        fixedMetadataValue = new FixedMetadataValue(plugin, "true");
        this.database = database;

        // TODO start sync
    }

    public void addBlock(Block block, Player owner) {
        if (block == null)
            return;

        block.setMetadata(metadataKey, fixedMetadataValue);
        var model = BlockModel.fromBlock(block, owner);

        removeFromDatabase.remove(model);
        addToDatabase.add(model);
    }

    public void removeBlock(Block block) {
        block.removeMetadata(metadataKey, plugin);
        var model = BlockModel.fromBlock(block);

        addToDatabase.remove(model);
        removeFromDatabase.add(model);
    }

    public static void loadChunkFromDatabase(Chunk chunk) {

    }

    public void saveAndClose() {
        // TODO save for the last time
        final Set<String> fAdd = new HashSet<>(BlockHandler.addToDatabase);
        final Set<String> fDel = new HashSet<>(BlockHandler.removeFromDatabase);
        new DataSyncRunnable(this, fAdd, fDel, true).run();
    }


    private void loadData() {
        setDB(new Database(this, null));

        BlockHandler.setUsingSQLite(config.database.type == DatabaseType.SQLITE);
        InventoryHandler.setForceGamemodeEnabled(Utils.isForceGamemodeEnabled());

        // Tracked blocks
        getDB().executeUpdate("CREATE TABLE IF NOT EXISTS " + getDB().getBlocksTable() +
                "`x` BIGINT NOT NULL, " +
                "`y` BIGINT NOT NULL, " +
                "`z` BIGINT NOT NULL, " +
                "`world` VARCHAR(100) NOT NULL, " +
                "`chunk_x` INT NOT NULL, " +
                "`chunk_z` INT NOT NULL, " +
                "`owner` VARCHAR(36) NOT NULL, " +
                "UNIQUE `x_y_z_world` (`x`, `y`, `z`, `world`))");

        // Tracked inventories
        if (BlockHandler.isUsingSQLite()) {
            String tableName = getDB().getInventoryTable();
            ResultSet rs = getDB()
                    .executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'");

            boolean tableExists = false;
            try {
                tableExists = rs.next();
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (tableExists)
                tableName += "_tmp";

            getDB().executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName
                    + " (player VARCHAR(36), type TINYINT(1), storage TEXT, armor TEXT, extra TEXT, effects TEXT, xp BIGINT, lastused BIGINT(11), UNIQUE (player))");

            if (tableExists) {
                // Retain only the latest inventory from each player
                try {
                    Statement stm = getDB().getConnection().createStatement();
                    stm.addBatch("INSERT INTO " + tableName
                            + " SELECT player, type, storage, armor, extra, effects, xp, MAX(lastused) as lastused FROM "
                            + getDB().getInventoryTable() + " GROUP BY player");
                    stm.addBatch("DROP TABLE " + getDB().getInventoryTable());
                    stm.addBatch(
                            "ALTER TABLE " + tableName + " RENAME TO " + getDB().getInventoryTable());
                    stm.executeBatch();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            getDB().executeUpdate("CREATE TABLE IF NOT EXISTS " + getDB().getInventoryTable()
                    + " (player VARCHAR(36), type TINYINT(1), storage TEXT, armor TEXT, extra TEXT, effects TEXT, xp BIGINT, lastused BIGINT(11), UNIQUE (player))");
        }

        // Purge old database entries
        if (config.tracking.inventories.purge.enabled) {
            long survival = Instant.now().getEpochSecond()
                    - 86400L * config.tracking.inventories.purge.survival;
            long creative = Instant.now().getEpochSecond()
                    - 86400L * config.tracking.inventories.purge.creative;

            getDB().executeUpdate(
                    "DELETE FROM " + getDB().getInventoryTable() + " WHERE type = 0 AND lastused < "
                            + survival + " OR type = 1 AND lastused < " + creative);
        }

        BlockHandler.usingAdvancedLoading = true;
        BlockHandler.loadFromDatabaseAdvanced(this);

        BlockHandler.startDataSync(this);
    }

    public static void loadFromDatabaseAdvanced(RestrictedCreative restrictedCreative) {
        restrictedCreative.getUtils().sendMessage(Bukkit.getConsoleSender(), true, "database.load");

        // Start async processing
        Bukkit.getScheduler().runTaskAsynchronously(restrictedCreative, () -> {
            long start = System.currentTimeMillis();

            // Gets all blocks from database
            ResultSet rs = restrictedCreative.getDB()
                                             .executeQuery("SELECT * FROM " + restrictedCreative.getDB().getBlocksTable());

            long current = System.currentTimeMillis();
            float average = current - start;
            long prevTime = current;

            if (RestrictedCreative.DEBUG)
                System.out
                        .println("loadFromDatabaseAdvanced: resultset took " + average + "ms");

            int count = 0;
            Map<String, Set<String>> blocksInChunk = new HashMap<>();

            try {
                while (rs.next()) {
                    String block = rs.getString("block");
                    String chunk = Utils.getBlockChunk(block);

                    // TODO: move somewhere else?
                    /*
                     * String world = blockParts[0]; if (main.getUtils().isDisabledWorld(world)
                     * || Bukkit.getWorld(world) == null) continue;
                     *
                     * addBlockToChunk(chunk, block);
                     */

                    Set<String> blocks = blocksInChunk.getOrDefault(chunk, new HashSet<>());
                    blocks.add(block);
                    blocksInChunk.put(chunk, blocks);

                    count++;
                    if (count % 5000 == 0 && RestrictedCreative.DEBUG) {
                        current = System.currentTimeMillis();
                        average = 5000f / (current - prevTime);
                        prevTime = current;
                        System.out.println("loadFromDatabaseAdvanced: " + count + " avg "
                                + average + " b/ms");
                    }
                }
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.WARNING,
                        "Data loading was interrupted! Restarting...");
                BlockHandler.loadFromDatabaseAdvanced(restrictedCreative);
            }

            final Map<String, Set<String>> fBlocksInChunk = blocksInChunk;
            final int fBlocksLoaded = count;
            final int fChunksLoaded = blocksInChunk.size();
            final long fTook = System.currentTimeMillis() - start;

            if (RestrictedCreative.DEBUG) {
                current = System.currentTimeMillis();
                average = (float) count / (current - start);
                System.out.println("loadFromDatabaseAdvanced: total: " + count + " blocks, "
                        + fChunksLoaded + " chunks, " + (current - start) + " ms (avg "
                        + average + " b/ms)");
            }

            Bukkit.getScheduler().runTask(restrictedCreative, () -> {
                long start1 = System.currentTimeMillis();
                BlockHandler.blocksInChunk = fBlocksInChunk;

                // Even if plugin.yml has "load: STARTUP" the blocks haven't loaded from the
                // database when chunk load events are fired
                // TODO: remember all the chunks that load before database is loaded
                //  and parse them afterwards
                int radius = 8;
                for (World world : Bukkit.getWorlds()) {
                    // Ignore disabled worlds
                    if (restrictedCreative.getUtils().isDisabledWorld(world.getName()))
                        continue;

                    Chunk center = world.getSpawnLocation().getChunk();

                    for (int x = center.getX() - radius; x < center.getX() + radius; x++) {
                        for (int z = center.getZ() - radius; z < center.getZ()
                                + radius; z++) {
                            Chunk c = world.getChunkAt(x, z);
                            loadBlocks(c);
                        }
                    }
                }

                long chunksTook = System.currentTimeMillis() - start1;
                if (RestrictedCreative.DEBUG)
                    System.out.println("loadFromDatabaseAdvanced: chunk loading took "
                            + chunksTook + " ms");

                setTotalCount(fBlocksLoaded);
                BlockHandler.isLoadingDone = true;

                Utils.sendMessage(Bukkit.getConsoleSender(),
                        restrictedCreative.getUtils().getFormattedMessage(true, "database.loaded")
                                          .replaceAll("%blocks%", getTotalCount())
                                          .replaceAll("%chunks%", String.valueOf(fChunksLoaded)));

                String took = String.valueOf(fTook + chunksTook);

                Utils.sendMessage(Bukkit.getConsoleSender(), restrictedCreative.getUtils()
                                                                               .getFormattedMessage(true, "database.done").replaceAll("%mills%", took));
            });
        });
    }

    public static void startDataSync(RestrictedCreative restrictedCreative) {
        int interval = restrictedCreative.getSettings().getInt("general.saving.interval");

        Bukkit.getServer().getScheduler().runTaskTimer(restrictedCreative, () -> {
            final HashSet<String> fAdd = new HashSet<>(addToDatabase);
            final HashSet<String> fDel = new HashSet<>(removeFromDatabase);

            Bukkit.getScheduler().runTaskAsynchronously(restrictedCreative,
                    new DataSyncRunnable(restrictedCreative, fAdd, fDel, false));
        }, interval, interval);
    }
}
