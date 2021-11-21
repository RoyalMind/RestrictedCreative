package solutions.nuhvel.spigot.rc.storage.config.messages.commands;

import de.exlll.configlib.annotation.ConfigurationElement;

import java.util.ArrayList;
import java.util.List;

@ConfigurationElement
public final class Command {
    public String name;
    public String description;
    public String usage;
    public List<String> aliases;
    public SwitchMessages Switch;

    private Command() {
        this("", "", "", new ArrayList<>(), null);
    }

    public Command(String name, String description, String usage, List<String> aliases, SwitchMessages switchMessages) {
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.aliases = aliases;
        this.Switch = switchMessages;
    }
}
