package solutions.nuhvel.spigot.rc.storage.database;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.Objects;

public class BlockModel {
    public Location location;
    public Player owner;
    public Date created;

    public static BlockModel fromBlock(Block block) {
        return fromBlock(block, null);
    }

    public static BlockModel fromBlock(Block block, Player owner) {
        BlockModel blockModel = new BlockModel();

        blockModel.location = block.getLocation();
        blockModel.owner = owner;
        blockModel.created = new Date();

        return blockModel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        BlockModel that = (BlockModel) o;
        return location.getBlockX() == that.location.getBlockX() && location.getBlockY() == that.location.getBlockY() &&
                location.getBlockZ() == that.location.getBlockZ();
    }
}
