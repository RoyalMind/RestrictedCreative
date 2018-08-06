package me.prunt.restrictedcreative.utils;

import java.util.Collection;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class PlayerInfo {
    List<ItemStack> storage;
    List<ItemStack> armor;
    List<ItemStack> extra;
    Collection<PotionEffect> effects;
    int xp;
    GameMode gm;

    PlayerInfo(List<ItemStack> storage, List<ItemStack> armor, List<ItemStack> extra, Collection<PotionEffect> effects,
	    int xp, GameMode gm) {
	this.storage = storage;
	this.armor = armor;
	this.extra = extra;
	this.effects = effects;
	this.xp = xp;
	this.gm = gm;
    }
}
