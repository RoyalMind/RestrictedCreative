package solutions.nuhvel.spigot.rc.storage.config.messages.commands;

import de.exlll.configlib.annotation.ConfigurationElement;

import java.util.ArrayList;
import java.util.List;

@ConfigurationElement
public final class SwitchCommand implements ISimpleCommand {
    public String name;
    public String description;
    public String usage;
    public List<String> aliases;
    public GamemodeMessages gamemode;

    private SwitchCommand() {
        this("", "", "", new ArrayList<>(), new GamemodeMessages());
    }

    public SwitchCommand(String name, String description, String usage, List<String> aliases,
            GamemodeMessages gamemodeMessages) {
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.aliases = aliases;
        this.gamemode = gamemodeMessages;
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
