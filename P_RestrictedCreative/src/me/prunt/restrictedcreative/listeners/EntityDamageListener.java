package me.prunt.restrictedcreative.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.storage.DataHandler;

public class EntityDamageListener implements Listener {
	private Main main;

	public EntityDamageListener(Main main) {
		this.main = main;
	}

	private Main getMain() {
		return this.main;
	}

	/*
	 * Called when an entity is damaged by an entity
	 */
	// HIGHEST required for WorldGuard and similar plugins
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onEntityDamage(EntityDamageByEntityEvent e) {
		Entity en = e.getEntity();

		// No need to control entities in disabled worlds
		if (getMain().getUtils().isDisabledWorld(en.getWorld().getName()))
			return;

		// Item frame
		if (en instanceof ItemFrame) {
			ItemFrame frame = (ItemFrame) en;
			ItemStack is = frame.getItem();

			// The item isn't going to pop off
			if (is == null || is.getType() == Material.AIR)
				return;

			// Item frame doesn't contain creative items
			if (!DataHandler.hasTrackedItem(frame))
				return;

			if (Main.DEBUG)
				System.out.println("removeItem: " + is.getType());

			DataHandler.removeItem(frame);
		}

		// Armor stand
		if (en instanceof ArmorStand) {
			ArmorStand a = (ArmorStand) en;

			for (EquipmentSlot slot : EquipmentSlot.values()) {
				if (!DataHandler.isTrackedSlot(a, slot))
					continue;

				EntityEquipment inv = a.getEquipment();
				ItemStack air = new ItemStack(Material.AIR);

				switch (slot) {
				case CHEST:
					inv.setChestplate(air);
					break;
				case FEET:
					inv.setBoots(air);
					break;
				case HEAD:
					inv.setHelmet(air);
					break;
				case LEGS:
					inv.setLeggings(air);
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
			}
		}

		// PVP and PVE
		if (e.getDamager() instanceof Player) {
			Player p = (Player) e.getDamager();

			// No need to control non-creative players
			if (p.getGameMode() != GameMode.CREATIVE)
				return;

			// PVE
			if (en instanceof LivingEntity) {
				// No need to control disabled features
				if (!getMain().getSettings().isEnabled("limit.combat.pve"))
					return;

				// No need to control bypassed players
				if (p.hasPermission("rc.bypass.limit.combat.pve")
						|| p.hasPermission("rc.bypass.limit.combat.pve." + en.getType()))
					return;

				if (Main.DEBUG)
					System.out.println("PVE: " + en.getType());

				e.setCancelled(true);
				getMain().getUtils().sendMessage(p, true, "disabled.general");
				return;
			}

			// PVP
			else if (en instanceof Player) {
				// No need to control disabled features
				if (!getMain().getSettings().isEnabled("limit.combat.pvp"))
					return;

				// No need to control bypassed players
				if (p.hasPermission("rc.bypass.limit.combat.pvp"))
					return;

				if (Main.DEBUG)
					System.out.println("PVP: " + en.getName());

				e.setCancelled(true);
				getMain().getUtils().sendMessage(p, true, "disabled.general");
				return;
			}
		}

		// No need to control non-tracked entities
		if (!DataHandler.isTracked(en))
			return;

		if (Main.DEBUG)
			System.out.println("onEntityDamage: " + en.getType());

		// Remove armor stands etc.
		en.remove();
		e.setCancelled(true);
	}

	/*
	 * Raised when a vehicle is destroyed, which could be caused by either a player
	 * or the environment. This is not raised if the boat is simply 'removed' due to
	 * other means.
	 */
	@EventHandler(ignoreCancelled = true)
	public void onVehicleDestory(VehicleDestroyEvent e) {
		Entity en = e.getVehicle();

		// No need to control entities in disabled worlds
		if (getMain().getUtils().isDisabledWorld(en.getWorld().getName()))
			return;

		// No need to control non-tracked entities
		if (!DataHandler.isTracked(en))
			return;

		if (Main.DEBUG)
			System.out.println("onVehicleDestroy: " + en.getType());

		// Remove boats, carts etc.
		en.remove();
		e.setCancelled(true);
	}

	/*
	 * Triggered when a hanging entity is removed
	 */
	// HIGHEST required for WorldGuard and similar plugins
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onHangingBreak(HangingBreakEvent e) {
		Entity en = e.getEntity();

		// No need to control entities in disabled worlds
		if (getMain().getUtils().isDisabledWorld(en.getWorld().getName()))
			return;

		if (!DataHandler.isTracked(en)) {
			if (en instanceof ItemFrame) {
				ItemFrame frame = (ItemFrame) en;

				if (DataHandler.hasTrackedItem(frame)) {
					en.remove();

					if (Main.DEBUG)
						System.out.println("removeTrackedItem: " + frame.getItem().getType());
				}
			}

			return;
		}

		if (Main.DEBUG)
			System.out.println("onHangingBreak: " + en.getType());

		en.remove();
		e.setCancelled(true);
	}
}
