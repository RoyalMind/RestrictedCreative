package solutions.nuhvel.spigot.rc.storage.database;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Date;

public class BlockModel {
    public Location location;
    public Player owner;
    public Date created;

    public static BlockModel fromBlock(Block block, Player owner) {
        BlockModel blockModel = new BlockModel();

        blockModel.location = block.getLocation();
        blockModel.owner = owner;
        blockModel.created = new Date();

        return blockModel;
    }
}
