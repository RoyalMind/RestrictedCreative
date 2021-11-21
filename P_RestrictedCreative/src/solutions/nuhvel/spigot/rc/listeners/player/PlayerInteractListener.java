package solutions.nuhvel.spigot.rc.listeners.player;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Door.Hinge;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.Metadatable;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.storage.handlers.CommandHandler;
import solutions.nuhvel.spigot.rc.storage.handlers.TrackableHandler;
import solutions.nuhvel.spigot.rc.utils.MaterialHandler;
import solutions.nuhvel.spigot.rc.utils.MessagingUtils;
import solutions.nuhvel.spigot.rc.utils.helpers.ArmorStandHelper;
import solutions.nuhvel.spigot.rc.utils.helpers.ConfiscationHelper;
import solutions.nuhvel.spigot.rc.utils.helpers.PreconditionChecker;

import java.util.Arrays;
import java.util.List;

public class PlayerInteractListener implements Listener {
    private static final List<BlockFace> ALL_SIDES =
            Arrays.asList(BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST,
                    BlockFace.WEST);
    private final RestrictedCreative plugin;

    public PlayerInteractListener(RestrictedCreative plugin) {
        this.plugin = plugin;
    }

    /*
     * Represents an event that is called when a player interacts with an object or
     * air, potentially fired once for each hand. The hand can be determined using
     * getHand().
     *
     * This event will fire as cancelled if the vanilla behavior is to do nothing
     * (e.g interacting with air)
     */
    // LOWEST required for signshops and similar plugins
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerInteractLowest(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack is = e.getItem();
        Material ma = is == null ? Material.AIR : is.getType();

        if (new PreconditionChecker(plugin, p)
                .isWorldAllowed(p.getWorld().getName())
                .isGameMode(GameMode.CREATIVE)
                .anyFailed())
            return;

        // Region check
        if (new PreconditionChecker(plugin, p).isRegionAllowed(e.getClickedBlock(), ma).anyFailed()) {
            e.setCancelled(true);
            plugin.messagingUtils.sendMessage(p, true, plugin.messages.disabled.region);
            return;
        }

        // Confiscate
        if (is != null && new ConfiscationHelper(plugin).shouldConfiscate(p, is)) {
            p.getInventory().remove(is);
            e.setCancelled(true);
            return;
        }

        // We only need to control right click interactions on blocks
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block b = e.getClickedBlock();

        // If block doesn't exist
        if (b == null || b.getType() == Material.AIR)
            return;

        Material m = b.getType();

        if (new PreconditionChecker(plugin, p).isInteractingOnGroundForbidden(m).anyFailed())
            return;

        e.setCancelled(true);

        // Prevent double message
        if (e.getHand() != EquipmentSlot.OFF_HAND)
            plugin.messagingUtils.sendMessage(p, true, plugin.messages.disabled.rightClicking);
    }

    /*
     * Represents an event that is called when a player interacts with an object or
     * air, potentially fired once for each hand. The hand can be determined using
     * getHand().
     *
     * This event will fire as cancelled if the vanilla behavior is to do nothing
     * (e.g interacting with air)
     */
    // "ignoreCancelled = true" skipped EYE_OF_ENDER for the latter reason
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if (new PreconditionChecker(plugin)
                .isWorldAllowed(p.getWorld().getName())
                .isGameMode(GameMode.CREATIVE)
                .anyFailed())
            return;

        ItemStack is = e.getItem();

        if (is == null || is.getType() == Material.AIR)
            return;

        Material m = is.getType();

        if (new PreconditionChecker(plugin, p).isInteractingInHandForbidden(m).anyFailed())
            return;

        e.setCancelled(true);

        // Prevent double message
        if (e.getHand() != EquipmentSlot.OFF_HAND)
            plugin.messagingUtils.sendMessage(p, true, plugin.messages.disabled.rightClicking);
    }

    /*
     * Represents an event that is called when a player interacts with an object or
     * air, potentially fired once for each hand. The hand can be determined using
     * getHand().
     *
     * This event will fire as cancelled if the vanilla behavior is to do nothing
     * (e.g interacting with air)
     */
    // HIGHEST required for WorldGuard and similar plugins
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerInteractHighest(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if (new PreconditionChecker(plugin).isWorldAllowed(p.getWorld().getName()).anyFailed())
            return;

        // We only need to control right click interactions on blocks
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block b = e.getClickedBlock();

        if (b == null || b.getType() == Material.AIR)
            return;

        checkCommands(e, p, b);

        // Creative placed cake shouldn't be edible
        if (new PreconditionChecker(plugin).isTracked(b).allSucceeded() && b.getType() == Material.CAKE) {
            plugin.messagingUtils.sendMessage(p, true, plugin.messages.disabled.interacting);

            e.setCancelled(true);
            return;
        }

        BlockData bd = b.getBlockData();

        // Door and trapdoor attachment check
        if (bd instanceof Door || bd instanceof TrapDoor) {
            checkForAttachments(p, b);
        }

        if (e.getItem() == null)
            return;

        Material m = e.getItem().getType();

        // Pumpkins can be carved with shears and they drop seeds
        if (new PreconditionChecker(plugin).isTracked(b).allSucceeded() && m == Material.SHEARS &&
                b.getType() == Material.PUMPKIN) {
            plugin.messagingUtils.sendMessage(p, true, plugin.messages.disabled.interacting);

            e.setCancelled(true);
            return;
        }

        // No need to track non-creative players
        if (p.getGameMode() != GameMode.CREATIVE)
            return;

        // No need to track bypassed players
        if (p.hasPermission("rc.bypass.tracking.blocks") || p.hasPermission("rc.bypass.tracking.blocks." + m))
            return;

        // No need to track non-entity materials
        if (!MaterialHandler.isPlaceableEntity(m))
            return;

        TrackableHandler.setAsTracked(b.getLocation());
    }

    /*
     * Represents an event that is called when a player right clicks an entity.
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();

        if (new PreconditionChecker(plugin).isWorldAllowed(p.getWorld().getName()).anyFailed())
            return;

        Entity en = e.getRightClicked();
        EntityType et = en.getType();

        checkCommands(e, p, en);

        // No need to track non-creative players
        if (p.getGameMode() != GameMode.CREATIVE)
            return;

        // If creative player wants to put something in an empty item frame
        if (en instanceof ItemFrame frame && !p.hasPermission("rc.bypass.tracking.blocks") &&
                !p.hasPermission("rc.bypass.tracking.blocks." + et)) {
            ItemStack is = e.getHand() == EquipmentSlot.HAND ? p.getInventory().getItemInMainHand()
                    : p.getInventory().getItemInOffHand();
            ItemStack fis = frame.getItem();

            if (!is.getType().isAir() && !fis.getType().isAir()) {
                TrackableHandler.setItemAsTracked(frame);
                return;
            }
        }

        // No need to control disabled features
        if (!plugin.config.limitations.interaction.entities)
            return;

        // No need to track bypassed players
        if (p.hasPermission("rc.bypass.limit.interact.entities") ||
                p.hasPermission("rc.bypass.limit.interact.entities." + et))
            return;

        e.setCancelled(true);

        // Prevent double message
        if (e.getHand() != EquipmentSlot.OFF_HAND)
            plugin.messagingUtils.sendMessage(p, true, plugin.messages.disabled.rightClicking);
    }

    /*
     * Called when a player interacts with an armor stand and will either swap,
     * retrieve or place an item.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
        Player p = e.getPlayer();
        ArmorStand a = e.getRightClicked();
        EquipmentSlot slot = e.getSlot();

        if (new PreconditionChecker(plugin).isWorldAllowed(p.getWorld().getName()).anyFailed())
            return;

        // No need to track bypassed players
        if (p.hasPermission("rc.bypass.limit.interact.entities") ||
                p.hasPermission("rc.bypass.limit.interact.entities." + a.getType()))
            return;

        // No need to control disabled features
        if (p.getGameMode() == GameMode.CREATIVE && plugin.config.limitations.interaction.entities) {
            plugin.messagingUtils.sendMessage(p, true, plugin.messages.disabled.interacting);
            e.setCancelled(true);
            return;
        }

        if (e.getArmorStandItem().getType().isAir())
            return;

        // Survival player is taking creative item from armor stand
        if (p.getGameMode() != GameMode.CREATIVE && !e.getArmorStandItem().getType().isAir() &&
                TrackableHandler.isTracked(a, slot)) {
            e.setCancelled(true);

            EntityEquipment inv = a.getEquipment();
            ItemStack air = new ItemStack(Material.AIR);

            if (inv == null)
                return;

            ArmorStandHelper.removeItemFromArmorStand(a, slot, inv, air);

            // Prevent double message
            if (e.getHand() != EquipmentSlot.OFF_HAND)
                plugin.messagingUtils.sendMessage(p, true, plugin.messages.disabled.item);

            return;
        }

        // Only creative players going forward
        if (p.getGameMode() != GameMode.CREATIVE)
            return;

        // Creative player is taking a creative item from armor stand
        if (e.getArmorStandItem().getType() != Material.AIR && TrackableHandler.isTracked(a, slot))
            TrackableHandler.removeTracking(a, slot);

        // Creative player is putting an item on the armor stand
        if (e.getPlayerItem().getType() != Material.AIR)
            TrackableHandler.setAsTracked(a, slot);
    }

    /*
     * Called when one Entity breeds with another Entity.
     */
    @EventHandler(ignoreCancelled = true)
    public void onEntityBreed(EntityBreedEvent e) {
        // Only players going forward
        if (!(e.getBreeder() instanceof Player p))
            return;

        if (new PreconditionChecker(plugin, p)
                .isWorldAllowed(p.getWorld().getName())
                .isGameMode(GameMode.CREATIVE)
                .anyFailed())
            return;

        // No need to control disabled features
        if (!plugin.config.limitations.interaction.breeding)
            return;

        EntityType et = e.getEntityType();

        // No need to track bypassed players
        if (p.hasPermission("rc.bypass.limit.interact.breeding") ||
                p.hasPermission("rc.bypass.limit.interact.breeding." + et))
            return;

        e.setCancelled(true);
    }

    private void checkCommands(Cancellable e, Player p, Metadatable blockOrEntity) {
        /* Command /block */
        if (CommandHandler.isInfoWithCommand(p)) {
            var message = new PreconditionChecker(plugin).isTracked(blockOrEntity).allSucceeded()
                    ? plugin.messages.block.info.yes : plugin.messages.block.info.no;
            MessagingUtils.sendMessage(p, plugin.messagingUtils
                    .getFormattedMessage(true, message)
                    .replaceAll("%material%", getType(blockOrEntity)));

            CommandHandler.removeInfoWithCommand(p);
            e.setCancelled(true);
        } else if (CommandHandler.isAddWithCommand(p)) {
            plugin.trackableHandler.setAsTracked(blockOrEntity);
            CommandHandler.removeAddWithCommand(p);
            e.setCancelled(true);

            MessagingUtils.sendMessage(p, plugin.messagingUtils
                    .getFormattedMessage(true, plugin.messages.block.add.done)
                    .replaceAll("%material%", getType(blockOrEntity)));
        } else if (CommandHandler.isRemoveWithCommand(p)) {
            plugin.trackableHandler.removeTracking(blockOrEntity);
            CommandHandler.removeRemoveWithCommand(p);
            e.setCancelled(true);

            MessagingUtils.sendMessage(p, plugin.messagingUtils
                    .getFormattedMessage(true, plugin.messages.block.remove.done)
                    .replaceAll("%material%", getType(blockOrEntity)));
        }
    }

    private String getType(Metadatable blockOrEntity) {
        if (blockOrEntity instanceof Block block)
            return block.getType().toString();
        else if (blockOrEntity instanceof Entity entity)
            return entity.getType().toString();
        return "?";
    }

    private void checkForAttachments(Player p, Block b) {
        checkSurroundingBlocks(p, b);
        BlockData bd = b.getBlockData();

        // Check other half of the door as well
        if (bd instanceof Door door) {
            Block bl = door.getHalf() == Half.TOP ? b.getRelative(BlockFace.DOWN) : b.getRelative(BlockFace.UP);
            checkSurroundingBlocks(p, bl);
        }
    }

    private void checkSurroundingBlocks(Player p, Block door) {
        for (BlockFace bf : ALL_SIDES) {
            Block attachable = door.getRelative(bf);

            // Checks if the surrounding block is placed in creative
            if (!TrackableHandler.isTracked(attachable))
                continue;

            BlockFace dir = MaterialHandler.getNeededFace(attachable);

            // If it's attached to the original door
            if (attachable.getFace(door) == dir && isNeededDirectionOk(attachable, dir))
                plugin.trackableHandler.breakBlock(attachable, p);
        }
    }

    private boolean isNeededDirectionOk(Block bl, BlockFace dir) {
        BlockData bd = bl.getBlockData();

        // Signs can be attached to a door no matter the direction
        if (bd instanceof WallSign)
            return false;

        if (bd instanceof Door)
            // If they're not facing each other, they're in illegal position
            return getDoorFace((Door) bd) == dir.getOppositeFace();

        if (bd instanceof TrapDoor door) {
            BlockFace face = door.getFacing();
            boolean open = door.isOpen();

            // Trapdoors always face down when they're closed
            if (!open && dir != BlockFace.UP)
                return false;

            // If trapdoors are open, they have the same face as the blocks attached to them
            return !open || dir == face;
        }

        return true;
    }

    private BlockFace getDoorFace(Door door) {
        Hinge hinge = door.getHinge();
        BlockFace face = door.getFacing();

        // If a door is closed, it's actually facing the opposite of placement face
        if (!door.isOpen())
            return face.getOppositeFace();

        var hingeFace = getHingeFace(face, hinge);
        return hingeFace == null ? null : hingeFace.getOppositeFace();
    }

    private BlockFace getHingeFace(BlockFace face, Hinge hinge) {
        return switch (face) {
            case NORTH -> hinge == Hinge.LEFT ? BlockFace.EAST : BlockFace.WEST;
            case EAST -> hinge == Hinge.LEFT ? BlockFace.SOUTH : BlockFace.NORTH;
            case SOUTH -> hinge == Hinge.LEFT ? BlockFace.WEST : BlockFace.EAST;
            case WEST -> hinge == Hinge.LEFT ? BlockFace.NORTH : BlockFace.SOUTH;
            default -> null;
        };
    }
}
