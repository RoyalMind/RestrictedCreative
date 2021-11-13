package solutions.nuhvel.spigot.rc.storage.config.config.system;

import de.exlll.configlib.annotation.ConfigurationElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ConfigurationElement
public final class SystemWorlds {
    public List<String> disabled = new ArrayList<>(Arrays.asList("pure-survival-world", "pure-creative-world"));
    public List<String> whitelisted = new ArrayList<>();
}
