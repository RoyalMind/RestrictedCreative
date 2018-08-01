package me.prunt.restrictedcreative.listeners;

import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.storage.DataHandler;

public class BlockPlaceListener implements Listener {
    private Main main;

    public BlockPlaceListener(Main main) {
	this.main = main;
    }

    private Main getMain() {
	return this.main;
    }

    /*
     * Fired when a single block placement action of a player triggers the creation
     * of multiple blocks(e.g. placing a bed block). The block returned by
     * BlockPlaceEvent.getBlockPlaced() and its related methods is the block where
     * the placed block would exist if the placement only affected a single block.
     */
    // HIGHEST required for WorldGuard and similar plugins
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockMultiPlace(BlockMultiPlaceEvent e) {
	Player p = e.getPlayer();
	Block b = e.getBlockPlaced();
	List<BlockState> states = e.getReplacedBlockStates();
	Material m = b.getType();

	// No need to track blocks in disabled worlds
	if (getMain().isDisabledWorld(p.getWorld().getName()))
	    return;

	// No need to track non-creative players
	if (p.getGameMode() != GameMode.CREATIVE)
	    return;

	// No need to track excluded blocks
	if (getMain().isExcluded(b.getType()))
	    return;

	// No need to track bypassed players
	if (p.hasPermission("rc.bypass.tracking.blocks") || p.hasPermission("rc.bypass.tracking.blocks." + m))
	    return;

	/* Disabled blocks */
	if (getMain().isDisabledPlacing(m) && !p.hasPermission("rc.bypass.disable.placing")
		&& !p.hasPermission("rc.bypass.disable.placing." + m)) {
	    main.sendMessage(p, true, "disabled.general");
	    e.setCancelled(true);
	    return;
	}

	for (BlockState bs : states)
	    DataHandler.setAsTracked(bs.getBlock());
    }

    /*
     * Called when a block is placed by a player.
     * 
     * If a Block Place event is cancelled, the block will not be placed.
     */
    // HIGHEST required for WorldGuard and similar plugins
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
	Player p = e.getPlayer();
	Block b = e.getBlockPlaced();
	Material m = b.getType();

	// No need to track blocks in disabled worlds
	if (getMain().isDisabledWorld(p.getWorld().getName()))
	    return;

	// No need to track non-creative players
	if (p.getGameMode() != GameMode.CREATIVE)
	    return;

	// No need to track excluded blocks
	if (getMain().isExcluded(b.getType()))
	    return;

	// No need to track bypassed players
	if (p.hasPermission("rc.bypass.tracking.blocks") || p.hasPermission("rc.bypass.tracking.blocks." + m))
	    return;

	/* Disabled blocks */
	if (getMain().isDisabledPlacing(m) && !p.hasPermission("rc.bypass.disable.placing")
		&& !p.hasPermission("rc.bypass.disable.placing." + m)) {
	    main.sendMessage(p, true, "disabled.general");
	    e.setCancelled(true);
	    return;
	}

	DataHandler.setAsTracked(b);
    }

    /*
     * Called when a block is placed by a player.
     * 
     * If a Block Place event is cancelled, the block will not be placed.
     */
    // HIGHEST required for WorldGuard and similar plugins
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onCreatureCreate(BlockPlaceEvent e) {
	Player p = e.getPlayer();
	Block head = e.getBlockPlaced();

	// No need to check creations in disabled worlds
	if (getMain().isDisabledWorld(p.getWorld().getName()))
	    return;

	switch (head.getType()) {
	// Wither
	case WITHER_SKELETON_SKULL:
	    if (!main.getSettings().isEnabled("limit.creation.wither"))
		return;

	    if (!couldWitherBeBuilt(head))
		return;

	    // Wither was built in survival mode
	    if (p.getGameMode() != GameMode.CREATIVE && canSurvivalBuildWither(head))
		return;

	    main.sendMessage(p, true, "disabled.creature");
	    e.setCancelled(true);
	    return;
	// Golem
	case PUMPKIN:
	case JACK_O_LANTERN:
	    // Iron golem
	    if (main.getSettings().isEnabled("limit.creation.iron-golem") && couldIronGolemBeBuilt(head)) {
		// Golem was built in survival mode
		if (p.getGameMode() != GameMode.CREATIVE && canSurvivalBuildIronGolem(head))
		    return;

		main.sendMessage(p, true, "disabled.creature");
		e.setCancelled(true);
		return;
	    }
	    // Snow golem
	    else if (main.getSettings().isEnabled("limit.creation.snow-golem") && couldSnowGolemBeBuilt(head)) {
		// Golem was built in survival mode
		if (p.getGameMode() != GameMode.CREATIVE && canSurvivalBuildSnowGolem(head))
		    return;

		main.sendMessage(p, true, "disabled.creature");
		e.setCancelled(true);
		return;
	    }

	    return;
	default:
	    return;
	}
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

    // Return whether the snow golem would spawn if the head was placed
    public boolean couldSnowGolemBeBuilt(Block head) {
	return isInRow(head, Material.SNOW_BLOCK) != null;
    }

    // Return whether the snow golem's body is built in survival
    // (whether a survival player should be allowed to create a golem from it)
    public boolean canSurvivalBuildSnowGolem(Block head) {
	BlockFace bodyDir = isInRow(head, Material.SNOW_BLOCK);
	Block middle = head.getRelative(bodyDir);
	Block bottom = middle.getRelative(bodyDir);

	return !(DataHandler.isCreative(middle) || DataHandler.isCreative(bottom));
    }

    // Return whether the iron golem would spawn if the head was placed
    public boolean couldIronGolemBeBuilt(Block head) {
	BlockFace middle = getMiddleBody(head, Material.IRON_BLOCK);
	if (middle == null)
	    return false;

	Block bottom = head.getRelative(middle).getRelative(middle);

	return bottom.getType() == Material.IRON_BLOCK;
    }

    // Return whether the iron golem's body is built in survival
    // (whether a survival player should be allowed to create a golem from it)
    public boolean canSurvivalBuildIronGolem(Block head) {
	BlockFace bodyDir = getMiddleBody(head, Material.IRON_BLOCK);
	Block middle = head.getRelative(bodyDir);
	Block bottom = middle.getRelative(bodyDir);

	BlockFace middleDir = getRowDirection(middle, Material.IRON_BLOCK);
	Block middle1 = head.getRelative(middleDir);
	Block middle2 = head.getRelative(middleDir.getOppositeFace());

	boolean middleRow = DataHandler.isCreative(middle) || DataHandler.isCreative(middle1)
		|| DataHandler.isCreative(middle2);
	boolean headBottom = DataHandler.isCreative(head) || DataHandler.isCreative(bottom);

	return !(headBottom || middleRow);
    }

    // Return the direction in which the row is located
    // or null if the given block isn't in the middle
    BlockFace getRowDirection(Block middle, Material type) {
	Block east = middle.getRelative(BlockFace.EAST);
	Block west = middle.getRelative(BlockFace.WEST);
	Block north = middle.getRelative(BlockFace.NORTH);
	Block south = middle.getRelative(BlockFace.SOUTH);
	Block up = middle.getRelative(BlockFace.UP);
	Block down = middle.getRelative(BlockFace.DOWN);

	boolean eastwest = east.getType() == type && west.getType() == type;
	boolean northsouth = north.getType() == type && south.getType() == type;
	boolean updown = up.getType() == type && down.getType() == type;

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
	boolean isMiddleHead = getRowDirection(head, Material.WITHER_SKELETON_SKULL) != null;

	if (isMiddleHead)
	    return head;

	Block newHead = null;

	for (int i = 0; !isMiddleHead; i++) {
	    BlockFace bf = null;

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

	    if (newHead.getType() == Material.WITHER_SKELETON_SKULL)
		isMiddleHead = getRowDirection(newHead, Material.WITHER_SKELETON_SKULL) != null;
	}

	return newHead;
    }

    // Return the direction in which the body is situated
    // or null if it's not a complete body
    BlockFace getMiddleBody(Block head, Material type) {
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
	    if (middle.getType() == type) {
		isMiddleBody = getRowDirection(middle, type) != null;
	    }
	}

	return body;
    }

    // Return whether the wither could be built if the head was placed
    public boolean couldWitherBeBuilt(Block head) {
	head = getMiddleHead(head);
	if (head == null)
	    return false;

	BlockFace middle = getMiddleBody(head, Material.SOUL_SAND);
	if (middle == null)
	    return false;

	Block bottom = head.getRelative(middle).getRelative(middle);

	return bottom.getType() == Material.SOUL_SAND;
    }

    // Return whether the wither's body is built in survival
    // (whether a survival player should be allowed to create a wither from it)
    public boolean canSurvivalBuildWither(Block head) {
	head = getMiddleHead(head);
	BlockFace headDir = getRowDirection(head, Material.WITHER_SKELETON_SKULL);
	Block head1 = head.getRelative(headDir);
	Block head2 = head.getRelative(headDir.getOppositeFace());

	BlockFace bodyDir = getMiddleBody(head, Material.SOUL_SAND);
	Block middle = head.getRelative(bodyDir);
	Block bottom = middle.getRelative(bodyDir);
	Block middle1 = head.getRelative(headDir);
	Block middle2 = head.getRelative(headDir.getOppositeFace());

	boolean headRow = DataHandler.isCreative(head) || DataHandler.isCreative(head1)
		|| DataHandler.isCreative(head2);
	boolean middleRow = DataHandler.isCreative(middle1) || DataHandler.isCreative(middle2);
	boolean middleBottom = DataHandler.isCreative(middle) || DataHandler.isCreative(bottom);

	return !(middleBottom || headRow || middleRow);
    }

}
