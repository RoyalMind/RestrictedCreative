package solutions.nuhvel.spigot.rc.storage.config.config.commands;

import de.exlll.configlib.annotation.ConfigurationElement;

import java.util.Arrays;
import java.util.List;

@ConfigurationElement
public final class PluginCommands {
    public Command rc = new Command("rc", "RestrictedCreative main command used for reloading, statistics and other",
            "&cCorrect usage: /<command> <reload|block|i-am-sure-i-want-to-delete-all-plugin-data-from-database>",
            Arrays.asList("restrictedcreative"));
    public Command survival = new Command("survival", "Enter survival mode.",
            "&cCorrect usage: /<command> [player]",
            Arrays.asList("gm0", "gms"));
    public Command creative = new Command("creative", "Enter creative mode.",
            "&cCorrect usage: /<command> [player]",
            Arrays.asList("gm1", "gmc"));
    public Command adventure = new Command("adventure", "Enter adventure mode.",
            "&cCorrect usage: /<command> [player]",
            Arrays.asList("gm2", "gma"));
    public Command spectator = new Command("spectator", "Enter spectator mode.",
            "&cCorrect usage: /<command> [player]",
            Arrays.asList("gm3", "gmsp"));

    public Command getByName(String name) {
        return switch (name) {
            case "rc" -> rc;
            case "survival" -> survival;
            case "creative" -> creative;
            case "adventure" -> adventure;
            case "spectator" -> spectator;
            default -> null;
        };
    }

    public List<Command> asList() {
        return Arrays.asList(rc, survival, creative, adventure, spectator);
    }
}
