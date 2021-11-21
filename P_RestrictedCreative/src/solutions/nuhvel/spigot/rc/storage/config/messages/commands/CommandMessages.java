package solutions.nuhvel.spigot.rc.storage.config.messages.commands;

import de.exlll.configlib.annotation.ConfigurationElement;

import java.util.Arrays;
import java.util.List;

@ConfigurationElement
public final class CommandMessages {
    public Command rc = new Command("rc", "RestrictedCreative main command used for reloading, statistics and other",
            "&cCorrect usage: /<command> <reload|block|i-am-sure-i-want-to-delete-all-plugin-data-from-database>",
            List.of("restrictedcreative"), null);
    public Command survival = new Command("survival", "Enter survival mode.", "&cCorrect usage: /<command> [player]",
            List.of("gm0", "gms"),
            new SwitchMessages("&aSwitched to survival mode!", "&aSwitched %player% to survival mode!"));
    public Command creative = new Command("creative", "Enter creative mode.", "&cCorrect usage: /<command> [player]",
            List.of("gm1", "gmc"),
            new SwitchMessages("&aSwitched to creative mode!", "&aSwitched %player% to creative mode!"));
    public Command adventure = new Command("adventure", "Enter adventure mode.", "&cCorrect usage: /<command> [player]",
            List.of("gm2", "gma"),
            new SwitchMessages("&aSwitched to adventure mode!", "&aSwitched %player% to adventure mode!"));
    public Command spectator = new Command("spectator", "Enter spectator mode.", "&cCorrect usage: /<command> [player]",
            List.of("gm3", "gmsp"),
            new SwitchMessages("&aSwitched to spectator mode!", "&aSwitched %player% to spectator mode!"));

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
