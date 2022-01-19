package solutions.nuhvel.spigot.rc.storage.config.config.limitations;

import de.exlll.configlib.annotation.ConfigurationElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ConfigurationElement
public final class MovingLimitation {
    public boolean enabled = false;
    public int above = 256;
    public int below = 0;
}
