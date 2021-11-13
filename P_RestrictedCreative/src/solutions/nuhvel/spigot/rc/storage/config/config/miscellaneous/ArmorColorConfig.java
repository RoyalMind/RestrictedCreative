package solutions.nuhvel.spigot.rc.storage.config.config.miscellaneous;

import de.exlll.configlib.annotation.ConfigurationElement;

@ConfigurationElement
public final class ArmorColorConfig {
    public boolean enabled = true;
    public ArmorMaterial type = ArmorMaterial.LEATHER;
    public String color = "#AA0000";
}
