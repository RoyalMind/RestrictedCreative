package solutions.nuhvel.spigot.rc.storage.config.messages.block;

import de.exlll.configlib.annotation.ConfigurationElement;

@ConfigurationElement
public class SubCommandMessages {
    public String info;
    public String done;

    public SubCommandMessages() {
        this("", "");
    }

    public SubCommandMessages(String info, String done) {
        this.info = info;
        this.done = done;
    }
}
