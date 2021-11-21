package solutions.nuhvel.spigot.rc.storage.handlers;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.storage.database.BlockModel;
import solutions.nuhvel.spigot.rc.storage.database.BlockRepository;
import solutions.nuhvel.spigot.rc.utils.minecraft.ServerUtils;
import solutions.nuhvel.spigot.rc.utils.external.BlocksHubUtils;
import solutions.nuhvel.spigot.rc.utils.external.CoreProtectUtils;
import solutions.nuhvel.spigot.rc.utils.minecraft.MinecraftUtils;

import java.util.HashSet;
import java.util.Set;

public class TrackableHandler {
    public static final String metadataKey = "GMC";
    private static final String itemFrameMetadataKey = metadataKey + "_IF";
    private static final String armorStandMetadataKey = metadataKey + "_AS_";
    private static final Set<String> trackedLocs = new HashSet<>();
    private static int totalCount = -1;
    private final BlockRepository blockRepository;
    private final RestrictedCreative plugin;

    public TrackableHandler(RestrictedCreative plugin, BlockRepository blockRepository) {
        this.plugin = plugin;
        this.blockRepository = blockRepository;
    }

    public static String getTotalCount() {
        return String.valueOf(totalCount);
    }

    public static void setTotalCount(int totalCount) {
        TrackableHandler.totalCount = totalCount;
    }

    public static boolean isTracked(Metadatable blockOrEntity) {
        if (blockOrEntity == null)
            return false;

        if (blockOrEntity instanceof Block block) {
            for (MetadataValue mdv : block.getMetadata(metadataKey))
                if (mdv.asBoolean())
                    return true;
        } else if (blockOrEntity instanceof Entity entity)
            return entity.getScoreboardTags().contains(metadataKey);

        return false;
    }

    public void setAsTracked(Metadatable blockOrEntity) {
        if (blockOrEntity == null)
            return;

        if (blockOrEntity instanceof Block block)
            setAsTracked(block);
        else if (blockOrEntity instanceof Entity entity)
            setAsTracked(entity);
    }

    public void removeTracking(Metadatable blockOrEntity) {
        if (blockOrEntity == null)
            return;

        if (blockOrEntity instanceof Block block)
            removeTracking(block);
        else if (blockOrEntity instanceof Entity entity)
            removeTracking(entity);
    }

    public void setAsTracked(Block block) {
        setAsTracked(block, null);
    }

    public void setAsTracked(Block block, Player owner) {
        if (block == null)
            return;

        markAsCreative(block);
        blockRepository.addBlock(BlockModel.fromBlock(block, owner));
    }

    public void markAsCreative(Block block) {
        block.setMetadata(metadataKey, new FixedMetadataValue(plugin, "true"));
    }

    public void removeTracking(Block block) {
        if (block == null)
            return;

        block.removeMetadata(metadataKey, plugin);
        blockRepository.removeBlock(BlockModel.fromBlock(block));
    }

    public void breakBlock(Block b) {
        breakBlock(b, null);
    }

    public void breakBlock(Block b, Player p) {
        breakBlock(b, p, true);
    }

    public void breakBlock(Block b, Player p, boolean update) {
        if (ServerUtils.isInstalled("CoreProtect")) {
            new CoreProtectUtils(b, p, update);
        } else if (ServerUtils.isInstalled("BlocksHub")) {
            new BlocksHubUtils(b, p, update);
        } else {
            b.setType(Material.AIR, update);
        }

        removeTracking(b);
    }

    public static void setAsTracked(Entity e) {
        if (e == null)
            return;

        e.addScoreboardTag(metadataKey);
    }

    public static void removeTracking(Entity e) {
        if (e == null)
            return;

        e.removeScoreboardTag(metadataKey);
    }

    public static boolean hasTrackedItem(ItemFrame frame) {
        if (frame == null)
            return false;

        return frame.getScoreboardTags().contains(itemFrameMetadataKey);
    }

    public static void setItemAsTracked(ItemFrame frame) {
        if (frame == null)
            return;

        frame.addScoreboardTag(itemFrameMetadataKey);
    }

    public static void removeItem(ItemFrame frame) {
        if (frame == null)
            return;

        frame.setItem(new ItemStack(Material.AIR));
        removeItemTracking(frame);
    }

    public static void removeItemTracking(ItemFrame frame) {
        if (frame == null)
            return;

        frame.removeScoreboardTag(itemFrameMetadataKey);
    }

    public static boolean isTracked(ArmorStand stand, EquipmentSlot slot) {
        if (stand == null || slot == null)
            return false;

        return stand.getScoreboardTags().contains(armorStandMetadataKey + slot);
    }

    public static void setAsTracked(ArmorStand stand, EquipmentSlot slot) {
        if (stand == null || slot == null)
            return;

        stand.addScoreboardTag(armorStandMetadataKey + slot);
    }

    public static void removeTracking(ArmorStand stand, EquipmentSlot slot) {
        if (stand == null || slot == null)
            return;

        stand.removeScoreboardTag(armorStandMetadataKey + slot);
    }

    public static boolean isTracked(Location location) {
        return trackedLocs.contains(MinecraftUtils.getAsString(location));
    }

    public static void setAsTracked(Location location) {
        trackedLocs.add(MinecraftUtils.getAsString(location));
    }

    public static void removeTracking(Location location) {
        trackedLocs.remove(MinecraftUtils.getAsString(location));
    }
}
