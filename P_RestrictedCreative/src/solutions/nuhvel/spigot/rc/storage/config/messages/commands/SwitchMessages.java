package solutions.nuhvel.spigot.rc.storage.config.messages.commands;

import de.exlll.configlib.annotation.ConfigurationElement;

@ConfigurationElement
public class SwitchMessages {
    public String me;
    public String other;

    public SwitchMessages() {
        this("", "");
    }

    public SwitchMessages(String me, String other) {
        this.me = me;
        this.other = other;
    }
}
