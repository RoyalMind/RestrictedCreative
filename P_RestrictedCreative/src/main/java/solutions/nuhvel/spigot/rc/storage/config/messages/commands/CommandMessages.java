package solutions.nuhvel.spigot.rc.storage.config.messages.commands;

import de.exlll.configlib.annotation.ConfigurationElement;

import java.util.Arrays;
import java.util.List;

@ConfigurationElement
public final class CommandMessages {
    public SimpleCommand rc =
            new SimpleCommand("rc", "RestrictedCreative main command used for reloading, statistics and other",
                    "&cCorrect usage: /<command> <reload|block|i-am-sure-i-want-to-delete-all-plugin-data-from-database>",
                    List.of("restrictedcreative"));

    public SwitchCommand survival =
            new SwitchCommand("survival", "Enter survival mode.", "&cCorrect usage: /<command> [player]",
                    List.of("gm0", "gms"),
                    new GamemodeMessages("&aSwitched to survival mode!", "&aSwitched %player% to survival mode!"));
    public SwitchCommand creative =
            new SwitchCommand("creative", "Enter creative mode.", "&cCorrect usage: /<command> [player]",
                    List.of("gm1", "gmc"),
                    new GamemodeMessages("&aSwitched to creative mode!", "&aSwitched %player% to creative mode!"));
    public SwitchCommand adventure =
            new SwitchCommand("adventure", "Enter adventure mode.", "&cCorrect usage: /<command> [player]",
                    List.of("gm2", "gma"),
                    new GamemodeMessages("&aSwitched to adventure mode!", "&aSwitched %player% to adventure mode!"));
    public SwitchCommand spectator =
            new SwitchCommand("spectator", "Enter spectator mode.", "&cCorrect usage: /<command> [player]",
                    List.of("gm3", "gmsp"),
                    new GamemodeMessages("&aSwitched to spectator mode!", "&aSwitched %player% to spectator mode!"));

    public SwitchCommand getCommandByName(String name) {
        return switch (name) {
            case "survival" -> survival;
            case "creative" -> creative;
            case "adventure" -> adventure;
            case "spectator" -> spectator;
            default -> null;
        };
    }

    public ISimpleCommand getByName(String name) {
        return name.equals("rc") ? rc : getCommandByName(name);
    }

    public List<ISimpleCommand> asList() {
        return Arrays.asList(rc, survival, creative, adventure, spectator);
    }
}
