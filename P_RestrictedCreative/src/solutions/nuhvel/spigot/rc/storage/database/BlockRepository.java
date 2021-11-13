package solutions.nuhvel.spigot.rc.storage.database;

import ee.kurel.hans.rc.RestrictedCreative;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;

public class BlockRepository {
    private RestrictedCreative plugin;
    public Database database;

    private List<BlockModel> addToDatabase = new ArrayList<>();
    private List<BlockModel> removeFromDatabase = new ArrayList<>();

    public BlockRepository(RestrictedCreative plugin) {
        this.plugin = plugin;
    }

    public void addBlock(Block block, Player owner) {
        if (block == null) return;
        block.setMetadata("RC3", new FixedMetadataValue(plugin, "true"));
        addToDatabase.add(BlockModel.fromBlock(block, owner));
    }

    public void removeBlock(Block block) {
        block.removeMetadata("RC3", plugin);
        addToDatabase.con
    }
}
