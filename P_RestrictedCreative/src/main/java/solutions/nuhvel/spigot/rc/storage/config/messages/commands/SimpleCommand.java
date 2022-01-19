package solutions.nuhvel.spigot.rc.storage.config.messages.commands;

import de.exlll.configlib.annotation.ConfigurationElement;

import java.util.ArrayList;
import java.util.List;

@ConfigurationElement
public class SimpleCommand implements ISimpleCommand {
    public String name;
    public String description;
    public String usage;
    public List<String> aliases;

    private SimpleCommand() {
        this("", "", "", new ArrayList<>());
    }

    public SimpleCommand(String name, String description, String usage, List<String> aliases) {
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.aliases = aliases;
    }

    @Override
    public List<String> getAliases() {
        return aliases;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getUsage() {
        return usage;
    }
}
