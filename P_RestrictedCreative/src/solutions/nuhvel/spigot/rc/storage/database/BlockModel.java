package solutions.nuhvel.spigot.rc.storage.database;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class BlockModel {
    public Block block;
    public Player owner;
    public Date created;

    public static BlockModel fromBlock(Block block) {
        return fromBlock(block, null);
    }

    public static BlockModel fromBlock(Block block, Player owner) {
        if (block == null)
            return null;

        var blockModel = new BlockModel();

        blockModel.block = block;
        blockModel.owner = owner;
        blockModel.created = new Date();

        return blockModel;
    }

    public static BlockModel fromData(int x, int y, int z, String worldName, String owner, long created) {
        var blockModel = new BlockModel();

        var world = Bukkit.getWorld(worldName);
        if (world == null)
            return null;

        blockModel.block = world.getBlockAt(x, y, z);
        blockModel.owner = owner.isEmpty() ? null : Bukkit.getPlayer(UUID.fromString(owner));
        blockModel.created = Date.from(Instant.ofEpochSecond(created));

        return blockModel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(block.getLocation().getBlockX(), block.getLocation().getBlockY(),
                block.getLocation().getBlockZ());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        BlockModel that = (BlockModel) o;
        return block.getLocation().getBlockX() == that.block.getLocation().getBlockX() &&
                block.getLocation().getBlockY() == that.block.getLocation().getBlockY() &&
                block.getLocation().getBlockZ() == that.block.getLocation().getBlockZ();
    }
}
