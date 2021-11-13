package solutions.nuhvel.spigot.rc.storage.config.config.commands;

import de.exlll.configlib.annotation.ConfigurationElement;

import java.util.ArrayList;
import java.util.List;

@ConfigurationElement
public final class Command {
    public String name;
    public String description;
    public String usage;
    public List<String> aliases;

    private Command() {
        this("", "", "", new ArrayList<>());
    }

    public Command(String name, String description, String usage, List<String> aliases) {
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.aliases = aliases;
    }
}
