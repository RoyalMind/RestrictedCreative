package solutions.nuhvel.spigot.rc.storage.config.config.miscellaneous;

import de.exlll.configlib.annotation.ConfigurationElement;

import java.util.List;

@ConfigurationElement
public class MiscPermissions {
    public boolean enabled = false;
    public boolean useVault = true;
    public List<String> add = List.of("prefix.creative");
    public List<String> remove = List.of("prefix.survival");
}
