package solutions.nuhvel.spigot.rc.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

// https://gist.github.com/JustRayz/cacdb2995df4cd54531f
public class InventoryUtils {
    public static String toBase64(List<ItemStack> contents) {
        return toBase64(contents.toArray(new ItemStack[0]));
    }

    public static List<ItemStack> stacksFromData(String data) {
        try {
            return Arrays.asList(stacksFromBase64(data));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String toBase64(ItemStack[] contents) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(contents.length);

            for (ItemStack stack : contents) {
                dataOutput.writeObject(stack);
            }
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    public static ItemStack[] stacksFromBase64(String data) throws IOException {
        try {
            if (data == null)
                return new ItemStack[]{};

            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] stacks = new ItemStack[dataInput.readInt()];

            for (int i = 0; i < stacks.length; i++)
                stacks[i] = (ItemStack) dataInput.readObject();

            dataInput.close();
            return stacks;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }

    public static String toBase64(Collection<PotionEffect> effects) {
        return toBase64(effects.toArray(new PotionEffect[0]));
    }

    public static Collection<PotionEffect> effectsFromData(String data) {
        try {
            return Arrays.asList(effectsFromBase64(data));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String toBase64(PotionEffect[] effects) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(effects.length);

            for (PotionEffect effect : effects) {
                dataOutput.writeObject(effect);
            }
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save potion effects.", e);
        }
    }

    public static PotionEffect[] effectsFromBase64(String data) throws IOException {
        try {
            if (data == null)
                return new PotionEffect[]{};

            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            PotionEffect[] effects = new PotionEffect[dataInput.readInt()];

            for (int i = 0; i < effects.length; i++)
                effects[i] = (PotionEffect) dataInput.readObject();

            dataInput.close();
            return effects;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }
}