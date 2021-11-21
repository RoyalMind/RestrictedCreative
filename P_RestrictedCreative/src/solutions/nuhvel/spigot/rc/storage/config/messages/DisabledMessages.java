package solutions.nuhvel.spigot.rc.storage.config.messages;

import de.exlll.configlib.annotation.ConfigurationElement;

@ConfigurationElement
public class DisabledMessages {
    public String general = "&cYou can''t do that in creative mode.";
    public String creature = "&cYou can't do that, because a part of the creature was placed in creative mode.";
    public String height = "&cYou can't do that outside the allowed height limits.";
    public String container = "&cYou can't open a container while in creative mode.";
    public String command = "&cYou can't use that command while in creative mode.";
    public String region = "&cYou can't do that outside the allowed region.";
    public String world = "&cYou can't do that outside the allowed worlds.";
    public String breaking = "&cYou can't break that block in creative mode.";
    public String placing = "&cYou can't place that block in creative mode.";
    public String throwing = "&cYou can't throw/shoot that block in creative mode.";
    public String dropping = "&cYou can't drop that in creative mode.";
    public String drops = "&cThis block didn't drop anything, because it was placed in creative mode.";
    public String pvp = "&cYou can't hit other players in creative mode.";
    public String pve = "&cYou can't hit other entities in creative mode.";
    public String rightClicking = "&cYou can't right-click that in creative mode.";
    public String interacting = "&cYou can't do that, because it was placed in creative mode.";
    public String item = "&cYou can't do that, because this item was created in creative mode.";
    public String armor = "&cYou can't do that, because wearing armor is mandatory in creative mode.";
}
