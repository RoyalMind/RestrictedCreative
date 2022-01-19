package solutions.nuhvel.spigot.rc.storage.config.messages.commands;

import de.exlll.configlib.annotation.ConfigurationElement;

@ConfigurationElement
public class GamemodeMessages {
    public String me;
    public String other;

    public GamemodeMessages() {
        this("", "");
    }

    public GamemodeMessages(String me, String other) {
        this.me = me;
        this.other = other;
    }
}
