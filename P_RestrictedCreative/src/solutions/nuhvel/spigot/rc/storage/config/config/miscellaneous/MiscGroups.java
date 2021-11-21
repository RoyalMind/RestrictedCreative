package solutions.nuhvel.spigot.rc.storage.config.config.miscellaneous;

import de.exlll.configlib.annotation.ConfigurationElement;

import java.util.List;

@ConfigurationElement
public class MiscGroups {
    public boolean enabled = false;
    public List<String> add = List.of("creative-group");
    public List<String> remove = List.of("survival-group");
}
