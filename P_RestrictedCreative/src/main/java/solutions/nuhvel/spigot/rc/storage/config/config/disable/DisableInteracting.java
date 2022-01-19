package solutions.nuhvel.spigot.rc.storage.config.config.disable;

import de.exlll.configlib.annotation.ConfigurationElement;
import de.exlll.configlib.annotation.ElementType;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

@ConfigurationElement
public final class DisableInteracting {
    @ElementType(Material.class)
    public List<Material> inHand = List.of(Material.SPLASH_POTION, Material.LINGERING_POTION, Material.FLINT_AND_STEEL, Material.FIRE_CHARGE, Material.EXPERIENCE_BOTTLE, Material.ENDER_EYE, Material.EGG, Material.BONE_MEAL, Material.LAVA_BUCKET, Material.SHEARS, Material.BAT_SPAWN_EGG, Material.BLAZE_SPAWN_EGG, Material.CAVE_SPIDER_SPAWN_EGG, Material.CHICKEN_SPAWN_EGG, Material.COD_SPAWN_EGG, Material.COW_SPAWN_EGG, Material.CREEPER_SPAWN_EGG, Material.DOLPHIN_SPAWN_EGG, Material.DONKEY_SPAWN_EGG, Material.DROWNED_SPAWN_EGG, Material.ELDER_GUARDIAN_SPAWN_EGG, Material.ENDERMAN_SPAWN_EGG, Material.ENDERMITE_SPAWN_EGG, Material.EVOKER_SPAWN_EGG, Material.GHAST_SPAWN_EGG, Material.GUARDIAN_SPAWN_EGG, Material.HORSE_SPAWN_EGG, Material.HUSK_SPAWN_EGG, Material.LLAMA_SPAWN_EGG, Material.MAGMA_CUBE_SPAWN_EGG, Material.MOOSHROOM_SPAWN_EGG, Material.MULE_SPAWN_EGG, Material.PARROT_SPAWN_EGG, Material.PHANTOM_SPAWN_EGG, Material.PIG_SPAWN_EGG, Material.POLAR_BEAR_SPAWN_EGG, Material.PUFFERFISH_SPAWN_EGG, Material.RABBIT_SPAWN_EGG, Material.SALMON_SPAWN_EGG, Material.SHEEP_SPAWN_EGG, Material.SHULKER_SPAWN_EGG, Material.SILVERFISH_SPAWN_EGG, Material.SKELETON_HORSE_SPAWN_EGG, Material.SKELETON_SPAWN_EGG, Material.SLIME_SPAWN_EGG, Material.SPIDER_SPAWN_EGG, Material.SQUID_SPAWN_EGG, Material.STRAY_SPAWN_EGG, Material.TROPICAL_FISH_SPAWN_EGG, Material.TURTLE_SPAWN_EGG, Material.VEX_SPAWN_EGG, Material.VILLAGER_SPAWN_EGG, Material.VINDICATOR_SPAWN_EGG, Material.WITCH_SPAWN_EGG, Material.WITHER_SKELETON_SPAWN_EGG, Material.WOLF_SPAWN_EGG, Material.ZOMBIE_HORSE_SPAWN_EGG, Material.ZOMBIE_SPAWN_EGG, Material.ZOMBIE_VILLAGER_SPAWN_EGG, Material.ACACIA_SAPLING, Material.BIRCH_SAPLING, Material.DARK_OAK_SAPLING, Material.JUNGLE_SAPLING, Material.OAK_SAPLING, Material.SPRUCE_SAPLING, Material.CAT_SPAWN_EGG, Material.FOX_SPAWN_EGG, Material.PANDA_SPAWN_EGG, Material.PILLAGER_SPAWN_EGG, Material.RAVAGER_SPAWN_EGG, Material.TRADER_LLAMA_SPAWN_EGG, Material.WANDERING_TRADER_SPAWN_EGG, Material.BEE_SPAWN_EGG, Material.ZOMBIFIED_PIGLIN_SPAWN_EGG, Material.PIGLIN_SPAWN_EGG, Material.HOGLIN_SPAWN_EGG, Material.ZOGLIN_SPAWN_EGG, Material.STRIDER_SPAWN_EGG);
    @ElementType(Material.class)
    public List<Material> onGround = List.of(Material.JUKEBOX, Material.FLOWER_POT, Material.ACACIA_SIGN, Material.BIRCH_SIGN, Material.DARK_OAK_SIGN, Material.JUNGLE_SIGN, Material.OAK_SIGN, Material.SPRUCE_SIGN, Material.ACACIA_WALL_SIGN, Material.BIRCH_WALL_SIGN, Material.DARK_OAK_WALL_SIGN, Material.JUNGLE_WALL_SIGN, Material.OAK_WALL_SIGN, Material.SPRUCE_WALL_SIGN, Material.CAMPFIRE, Material.COMPOSTER, Material.CRIMSON_SIGN, Material.WARPED_SIGN, Material.CRIMSON_WALL_SIGN, Material.WARPED_WALL_SIGN, Material.SOUL_CAMPFIRE);
}
