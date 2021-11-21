package solutions.nuhvel.spigot.rc.storage.config.messages;

import de.exlll.configlib.annotation.ConfigurationElement;

@ConfigurationElement
public class ErrorMessages {
    public String noPermission = "&cYou don't have enough permissions to do that!";
    public String noConsole = "&cThis command can only be used in-game.";
    public String notFound = "&cPlayer %player% is not online!";
}
