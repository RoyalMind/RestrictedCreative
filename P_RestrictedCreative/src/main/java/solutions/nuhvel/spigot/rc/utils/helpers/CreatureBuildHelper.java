package solutions.nuhvel.spigot.rc.utils.helpers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.storage.handlers.TrackableHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreatureBuildHelper {
    private final RestrictedCreative plugin;

    public CreatureBuildHelper(RestrictedCreative plugin) {
        this.plugin = plugin;
    }

    public boolean canPlaceHead(Material type, Block head, boolean isPlayerCreative) {
        return switch (type) {
            case WITHER_SKELETON_SKULL, WITHER_SKELETON_WALL_SKULL -> canPlaceWitherSkull(head, isPlayerCreative);
            case PUMPKIN, CARVED_PUMPKIN, JACK_O_LANTERN -> canPlacePumpkin(head, isPlayerCreative);
            default -> true;
        };
    }

    private boolean canPlaceWitherSkull(Block head, boolean isPlayerCreative) {
        if (!plugin.config.limitations.creation.wither || !couldWitherBeBuilt(head))
            return true;

        return isPlayerCreative && canSurvivalBuildWither(head);
    }

    private boolean canPlacePumpkin(Block head, boolean isPlayerCreative) {
        if (plugin.config.limitations.creation.ironGolem && couldIronGolemBeBuilt(head)) {
            return !isPlayerCreative && canSurvivalBuildIronGolem(head);
        } else if (plugin.config.limitations.creation.snowGolem && couldSnowGolemBeBuilt(head)) {
            return !isPlayerCreative && canSurvivalBuildSnowGolem(head);
        }
        return true;
    }

    private boolean couldSnowGolemBeBuilt(Block head) {
        return isInRow(head, Material.SNOW_BLOCK) != null;
    }

    private boolean canSurvivalBuildSnowGolem(Block head) {
        BlockFace bodyDir = isInRow(head, Material.SNOW_BLOCK);
        Block middle = head.getRelative(bodyDir);
        Block bottom = middle.getRelative(bodyDir);

        return !TrackableHandler.isTracked(middle) && !TrackableHandler.isTracked(bottom);
    }

    private boolean couldIronGolemBeBuilt(Block head) {
        BlockFace middle = getMiddleBody(head, Material.IRON_BLOCK);
        if (middle == null)
            return false;

        Block bottom = head.getRelative(middle).getRelative(middle);

        return bottom.getType() == Material.IRON_BLOCK;
    }

    private boolean canSurvivalBuildIronGolem(Block head) {
        BlockFace bodyDir = getMiddleBody(head, Material.IRON_BLOCK);
        Block middle = head.getRelative(bodyDir);
        Block bottom = middle.getRelative(bodyDir);

        BlockFace middleDir = getRowDirection(middle, Material.IRON_BLOCK);
        Block middle1 = head.getRelative(middleDir);
        Block middle2 = head.getRelative(middleDir.getOppositeFace());

        boolean middleRow =
                TrackableHandler.isTracked(middle) || TrackableHandler.isTracked(middle1) || TrackableHandler.isTracked(middle2);
        boolean headBottom = TrackableHandler.isTracked(head) || TrackableHandler.isTracked(bottom);

        return !(headBottom || middleRow);
    }

    private boolean couldWitherBeBuilt(Block head) {
        head = getMiddleHead(head);
        if (head == null)
            return false;

        BlockFace middle = getMiddleBody(head, Material.SOUL_SAND, Material.SOUL_SOIL);
        if (middle == null)
            return false;

        Block bottom = head.getRelative(middle).getRelative(middle);

        return bottom.getType() == Material.SOUL_SAND || bottom.getType() == Material.SOUL_SOIL;
    }

    // Return whether the wither's body is built in survival
    // (whether a survival player should be allowed to create a wither from it)
    private boolean canSurvivalBuildWither(Block head) {
        head = getMiddleHead(head);
        BlockFace headDir = getRowDirection(head, Material.WITHER_SKELETON_SKULL, Material.WITHER_SKELETON_WALL_SKULL);
        Block head1 = head.getRelative(headDir);
        Block head2 = head.getRelative(headDir.getOppositeFace());

        BlockFace bodyDir = getMiddleBody(head, Material.SOUL_SAND, Material.SOUL_SOIL);
        Block middle = head.getRelative(bodyDir);
        Block bottom = middle.getRelative(bodyDir);
        Block middle1 = head.getRelative(headDir);
        Block middle2 = head.getRelative(headDir.getOppositeFace());

        boolean headRow =
                TrackableHandler.isTracked(head) || TrackableHandler.isTracked(head1) || TrackableHandler.isTracked(head2);
        boolean middleRow = TrackableHandler.isTracked(middle1) || TrackableHandler.isTracked(middle2);
        boolean middleBottom = TrackableHandler.isTracked(middle) || TrackableHandler.isTracked(bottom);

        return !(middleBottom || headRow || middleRow);
    }

    // Return the direction in which the row is situated or null if it's not a row
    BlockFace isInRow(Block head, Material type) {
        BlockFace bodyDir = null;
        boolean isbodyDir = false;

        for (int i = 0; !isbodyDir; i++) {
            switch (i) {
                case 0:
                    bodyDir = BlockFace.EAST;
                    break;
                case 1:
                    bodyDir = BlockFace.WEST;
                    break;
                case 2:
                    bodyDir = BlockFace.NORTH;
                    break;
                case 3:
                    bodyDir = BlockFace.SOUTH;
                    break;
                case 4:
                    bodyDir = BlockFace.UP;
                    break;
                case 5:
                    bodyDir = BlockFace.DOWN;
                    break;
                default:
                    return null;
            }

            Block middle = head.getRelative(bodyDir);

            if (middle.getType() == type)
                isbodyDir = middle.getRelative(bodyDir).getType() == type;
        }

        return bodyDir;
    }

    // Return the direction in which the row is located
    // or null if the given block isn't in the middle
    BlockFace getRowDirection(Block middle, Material... type) {
        List<Material> types = new ArrayList<>(List.of(type));

        Block east = middle.getRelative(BlockFace.EAST);
        Block west = middle.getRelative(BlockFace.WEST);
        Block north = middle.getRelative(BlockFace.NORTH);
        Block south = middle.getRelative(BlockFace.SOUTH);
        Block up = middle.getRelative(BlockFace.UP);
        Block down = middle.getRelative(BlockFace.DOWN);

        boolean eastwest = types.contains(east.getType()) && types.contains(west.getType());
        boolean northsouth = types.contains(north.getType()) && types.contains(south.getType());
        boolean updown = types.contains(up.getType()) && types.contains(down.getType());

        if (eastwest) {
            return BlockFace.EAST;
        } else if (northsouth) {
            return BlockFace.NORTH;
        } else if (updown) {
            return BlockFace.UP;
        } else {
            return null;
        }
    }

    // Return the wither's middle head as a block or null if there is no middle head
    Block getMiddleHead(Block head) {
        boolean isMiddleHead =
                getRowDirection(head, Material.WITHER_SKELETON_SKULL, Material.WITHER_SKELETON_WALL_SKULL) != null;

        if (isMiddleHead)
            return head;

        Block newHead = null;

        for (int i = 0; !isMiddleHead; i++) {
            BlockFace bf;

            switch (i) {
                case 0:
                    bf = BlockFace.EAST;
                    break;
                case 1:
                    bf = BlockFace.WEST;
                    break;
                case 2:
                    bf = BlockFace.NORTH;
                    break;
                case 3:
                    bf = BlockFace.SOUTH;
                    break;
                case 4:
                    bf = BlockFace.UP;
                    break;
                case 5:
                    bf = BlockFace.DOWN;
                    break;
                default:
                    return null;
            }

            newHead = head.getRelative(bf);

            if (newHead.getType() == Material.WITHER_SKELETON_SKULL ||
                    newHead.getType() == Material.WITHER_SKELETON_WALL_SKULL)
                isMiddleHead =
                        getRowDirection(newHead, Material.WITHER_SKELETON_SKULL, Material.WITHER_SKELETON_WALL_SKULL) !=
                                null;
        }

        return newHead;
    }

    // Return the direction in which the body is situated
    // or null if it's not a complete body
    BlockFace getMiddleBody(Block head, Material... types) {
        BlockFace body = null;
        boolean isMiddleBody = false;

        for (int i = 0; !isMiddleBody; i++) {
            switch (i) {
                case 0:
                    body = BlockFace.EAST;
                    break;
                case 1:
                    body = BlockFace.WEST;
                    break;
                case 2:
                    body = BlockFace.NORTH;
                    break;
                case 3:
                    body = BlockFace.SOUTH;
                    break;
                case 4:
                    body = BlockFace.UP;
                    break;
                case 5:
                    body = BlockFace.DOWN;
                    break;
                default:
                    return null;
            }

            Block middle = head.getRelative(body);
            if (Arrays.asList(types).contains(middle.getType())) {
                isMiddleBody = getRowDirection(middle, types) != null;
            }
        }

        return body;
    }
}
