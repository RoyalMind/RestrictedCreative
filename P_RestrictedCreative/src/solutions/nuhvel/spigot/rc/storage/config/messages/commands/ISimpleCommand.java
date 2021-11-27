package solutions.nuhvel.spigot.rc.storage.config.messages.commands;

import java.util.List;

public interface ISimpleCommand {
    List<String> getAliases();
    String getName();
    String getDescription();
    String getUsage();
}
