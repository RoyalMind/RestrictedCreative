package me.prunt.restrictedcreative.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
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

import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.storage.DataHandler;
import me.prunt.restrictedcreative.utils.MaterialHandler;
import me.prunt.restrictedcreative.utils.Utils;

public class PlayerInteractListener implements Listener {
    private Main main;

    public PlayerInteractListener(Main main) {
	this.main = main;
    }

    private Main getMain() {
	return this.main;
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

	// No need to track entities in disabled worlds
	if (getMain().getUtils().isDisabledWorld(p.getWorld().getName()))
	    return;

	// No need to track non-creative players
	if (p.getGameMode() != GameMode.CREATIVE)
	    return;

	ItemStack is = e.getItem();

	// Region check
	if (getMain().getSettings().isEnabled("limit.regions.owner-based.enabled")
		|| getMain().getSettings().isEnabled("limit.regions.whitelist.enabled")) {
	    if (is != null && !getMain().getUtils().canBuildHere(p, e.getClickedBlock(), is)) {
		e.setCancelled(true);
		getMain().getUtils().sendMessage(p, true, "disabled.region");
		return;
	    }
	}

	// Confiscate
	if (getMain().getUtils().shouldConfiscate(p, is)) {
	    p.getInventory().remove(is);
	    e.setCancelled(true);

	    if (Main.DEBUG)
		System.out.println("shouldConfiscate: " + is.getType());

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

	// No need to control bypassed players
	if (p.hasPermission("rc.bypass.disable.interacting.on-ground")
		|| p.hasPermission("rc.bypass.disable.interacting.on-ground." + m))
	    return;

	// No need to control non-blocked items
	if (!getMain().getSettings().getMaterialList("disable.interacting.on-ground").contains(m))
	    return;

	e.setCancelled(true);

	// Prevent double message
	if (e.getHand() != EquipmentSlot.OFF_HAND)
	    getMain().getUtils().sendMessage(p, true, "disabled.general");
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

	// No need to track entities in disabled worlds
	if (getMain().getUtils().isDisabledWorld(p.getWorld().getName()))
	    return;

	// No need to track non-creative players
	if (p.getGameMode() != GameMode.CREATIVE)
	    return;

	ItemStack is = e.getItem();

	if (is == null || is.getType() == Material.AIR)
	    return;

	Material m = is.getType();

	// No need to track bypassed players
	if (p.hasPermission("rc.bypass.disable.interacting.in-hand")
		|| p.hasPermission("rc.bypass.disable.interacting.in-hand." + m))
	    return;

	// No need to control non-blocked items
	if (!getMain().getSettings().getMaterialList("disable.interacting.in-hand").contains(m))
	    return;

	e.setCancelled(true);

	// Prevent double message
	if (e.getHand() != EquipmentSlot.OFF_HAND)
	    getMain().getUtils().sendMessage(p, true, "disabled.general");
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

	// No need to track entities in disabled worlds
	if (getMain().getUtils().isDisabledWorld(p.getWorld().getName()))
	    return;

	// We only need to control right click interactions on blocks
	if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
	    return;

	Block b = e.getClickedBlock();

	if (b == null || b.getType() == Material.AIR)
	    return;

	/* Command /block */
	if (DataHandler.isInfoWithCommand(p)) {
	    if (DataHandler.isTracked(b)) {
		Utils.sendMessage(p, getMain().getUtils().getMessage(true, "block.info.true").replaceAll("%material%",
			b.getType().toString()));
	    } else {
		Utils.sendMessage(p, getMain().getUtils().getMessage(true, "block.info.false").replaceAll("%material%",
			b.getType().toString()));
	    }

	    DataHandler.removeInfoWithCommand(p);
	    e.setCancelled(true);
	} else if (DataHandler.isAddWithCommand(p)) {
	    DataHandler.setAsTracked(b);
	    DataHandler.removeAddWithCommand(p);
	    e.setCancelled(true);

	    Utils.sendMessage(p, getMain().getUtils().getMessage(true, "block.add.added").replaceAll("%material%",
		    b.getType().toString()));
	} else if (DataHandler.isRemoveWithCommand(p)) {
	    DataHandler.removeTracking(b);
	    DataHandler.removeRemoveWithCommand(p);
	    e.setCancelled(true);

	    Utils.sendMessage(p, getMain().getUtils().getMessage(true, "block.remove.removed").replaceAll("%material%",
		    b.getType().toString()));
	}

	// Creative placed cake shouldn't be edible
	if (DataHandler.isTracked(b) && b.getType() == Material.CAKE) {
	    getMain().getUtils().sendMessage(p, true, "disabled.interact");

	    e.setCancelled(true);
	    return;
	}

	if (e.getItem() == null)
	    return;

	Material m = e.getItem().getType();

	// Pumpkins can be carved with shears and they drop seeds
	if (DataHandler.isTracked(b) && m == Material.SHEARS && b.getType() == Material.PUMPKIN) {
	    getMain().getUtils().sendMessage(p, true, "disabled.interact");

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

	DataHandler.addToTrackedLocs(b.getLocation());
    }

    /*
     * Represents an event that is called when a player right clicks an entity.
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
	Player p = e.getPlayer();

	// No need to track entities in disabled worlds
	if (getMain().getUtils().isDisabledWorld(p.getWorld().getName()))
	    return;

	Entity en = e.getRightClicked();
	EntityType et = en.getType();

	/* Command /block */
	if (DataHandler.isInfoWithCommand(p)) {
	    if (DataHandler.isTracked(en)) {
		Utils.sendMessage(p, getMain().getUtils().getMessage(true, "block.info.true").replaceAll("%material%",
			et.toString()));
	    } else {
		Utils.sendMessage(p, getMain().getUtils().getMessage(true, "block.info.false").replaceAll("%material%",
			et.toString()));
	    }

	    DataHandler.removeInfoWithCommand(p);
	    e.setCancelled(true);
	} else if (DataHandler.isAddWithCommand(p)) {
	    DataHandler.setAsTracked(en);
	    DataHandler.removeAddWithCommand(p);
	    e.setCancelled(true);

	    Utils.sendMessage(p,
		    getMain().getUtils().getMessage(true, "block.add.added").replaceAll("%material%", et.toString()));
	} else if (DataHandler.isRemoveWithCommand(p)) {
	    DataHandler.removeTracking(en);
	    DataHandler.removeRemoveWithCommand(p);
	    e.setCancelled(true);

	    Utils.sendMessage(p, getMain().getUtils().getMessage(true, "block.remove.removed").replaceAll("%material%",
		    et.toString()));
	}

	// No need to track non-creative players
	if (p.getGameMode() != GameMode.CREATIVE)
	    return;

	// If creative player wants to put something in an empty item frame
	if (en instanceof ItemFrame && !p.hasPermission("rc.bypass.tracking.blocks")
		&& !p.hasPermission("rc.bypass.tracking.blocks." + et)) {
	    ItemStack is = e.getHand() == EquipmentSlot.HAND ? p.getInventory().getItemInMainHand()
		    : p.getInventory().getItemInOffHand();
	    ItemFrame frame = (ItemFrame) en;
	    ItemStack fis = frame.getItem();

	    if ((is != null && is.getType() != Material.AIR) && (fis == null || fis.getType() == Material.AIR)) {
		DataHandler.setAsTrackedItem(frame);
		return;
	    }
	}

	// No need to control disabled features
	if (!getMain().getSettings().isEnabled("limit.interact.entities"))
	    return;

	// No need to track bypassed players
	if (p.hasPermission("rc.bypass.limit.interact.entities")
		|| p.hasPermission("rc.bypass.limit.interact.entities." + et))
	    return;

	e.setCancelled(true);

	// Prevent double message
	if (e.getHand() != EquipmentSlot.OFF_HAND)
	    getMain().getUtils().sendMessage(p, true, "disabled.general");
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

	// No need to track entities in disabled worlds
	if (getMain().getUtils().isDisabledWorld(p.getWorld().getName()))
	    return;

	// No need to track bypassed players
	if (p.hasPermission("rc.bypass.limit.interact.entities")
		|| p.hasPermission("rc.bypass.limit.interact.entities." + a.getType()))
	    return;

	// No need to control disabled features
	if (p.getGameMode() == GameMode.CREATIVE && getMain().getSettings().isEnabled("limit.interact.entities")) {
	    getMain().getUtils().sendMessage(p, true, "disabled.general");

	    if (Main.DEBUG)
		System.out.println("cancelEvent: " + slot);

	    e.setCancelled(true);
	    return;
	}

	// Survival player is taking creative item from armor stand
	if (p.getGameMode() != GameMode.CREATIVE && e.getArmorStandItem().getType() != Material.AIR
		&& DataHandler.isTrackedSlot(a, slot)) {
	    e.setCancelled(true);

	    EntityEquipment inv = a.getEquipment();
	    ItemStack air = new ItemStack(Material.AIR);

	    switch (slot) {
	    case CHEST:
		a.setChestplate(air);
		break;
	    case FEET:
		a.setBoots(air);
		break;
	    case HEAD:
		a.setHelmet(air);
		break;
	    case LEGS:
		a.setLeggings(air);
		break;
	    case HAND:
		inv.setItemInMainHand(air);
		break;
	    case OFF_HAND:
		inv.setItemInOffHand(air);
		break;
	    default:
		break;
	    }

	    if (Main.DEBUG)
		System.out.println("removeSlotTracking: " + slot);

	    DataHandler.removeSlotTracking(a, slot);

	    // Prevent double message
	    if (e.getHand() != EquipmentSlot.OFF_HAND)
		getMain().getUtils().sendMessage(p, true, "disabled.interact");

	    return;
	}

	// Only creative players going forward
	if (p.getGameMode() != GameMode.CREATIVE)
	    return;

	if (Main.DEBUG)
	    System.out.println("onPlayerArmorStandManipulate: " + slot);

	// Creative player is taking a creative item from armor stand
	if (e.getArmorStandItem().getType() != Material.AIR && DataHandler.isTrackedSlot(a, slot))
	    DataHandler.removeSlotTracking(a, slot);

	// Creative player is putting an item on the armor stand
	if (e.getPlayerItem().getType() != Material.AIR)
	    DataHandler.setAsTrackedSlot(a, slot);
    }

    /*
     * Called when one Entity breeds with another Entity.
     */
    @EventHandler(ignoreCancelled = true)
    public void onEntityBreed(EntityBreedEvent e) {
	// Only players going forward
	if (!(e.getBreeder() instanceof Player))
	    return;

	Player p = (Player) e.getBreeder();

	// No need to track entities in disabled worlds
	if (getMain().getUtils().isDisabledWorld(p.getWorld().getName()))
	    return;

	// No need to track non-creative players
	if (p.getGameMode() != GameMode.CREATIVE)
	    return;

	// No need to control disabled features
	if (!getMain().getSettings().isEnabled("limit.interact.breeding"))
	    return;

	EntityType et = e.getEntityType();

	// No need to track bypassed players
	if (p.hasPermission("rc.bypass.limit.interact.breeding")
		|| p.hasPermission("rc.bypass.limit.interact.breeding." + et))
	    return;

	if (Main.DEBUG)
	    System.out.println("onEntityBreed: " + et);

	e.setCancelled(true);
    }
}
