package me.prunt.restrictedcreative.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.primesoft.blockshub.IBlocksHubApi;
import org.primesoft.blockshub.IBlocksHubApiProvider;
import org.primesoft.blockshub.api.IPlayer;
import org.primesoft.blockshub.api.IWorld;
import org.primesoft.blockshub.api.platform.BukkitBlockData;

public class BlocksHub {
    public BlocksHub(Block b, Player p, boolean update) {
	IBlocksHubApi blockshub = ((IBlocksHubApiProvider) Bukkit.getServer().getPluginManager().getPlugin("BlocksHub"))
		.getApi();

	IPlayer player = p == null ? blockshub.getPlayer("RestrictedCreative") : blockshub.getPlayer(p.getUniqueId());
	IWorld world = blockshub.getWorld(b.getWorld().getUID());
	BukkitBlockData oldBlock = new BukkitBlockData(b.getBlockData());

	b.setType(Material.AIR, update);

	BukkitBlockData newBlock = new BukkitBlockData(b.getBlockData());

	blockshub.logBlock(player, world, b.getX(), b.getY(), b.getZ(), oldBlock, newBlock);
    }
}
