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

    PlayerInfo(String storage, String armor, String extra, String effects, int xp, GameMode gm) {
	this.storage = getStorage(storage);
	this.armor = getArmor(armor);
	this.extra = getExtra(extra);
	this.effects = getEffects(effects);
	this.xp = xp;
	this.gm = gm;
    }

    public String getStorage() {
	return InventoryUtils.toBase64(this.storage);
    }

    public String getArmor() {
	return InventoryUtils.toBase64(this.armor);
    }

    public String getExtra() {
	return InventoryUtils.toBase64(this.extra);
    }

    public String getEffects() {
	return InventoryUtils.toBase64(this.effects);
    }

    public List<ItemStack> getStorage(String contents) {
	return InventoryUtils.stacksFromData(contents);
    }

    public List<ItemStack> getArmor(String contents) {
	return InventoryUtils.stacksFromData(contents);
    }

    public List<ItemStack> getExtra(String contents) {
	return InventoryUtils.stacksFromData(contents);
    }

    public Collection<PotionEffect> getEffects(String contents) {
	return InventoryUtils.effectsFromData(contents);
    }
}
