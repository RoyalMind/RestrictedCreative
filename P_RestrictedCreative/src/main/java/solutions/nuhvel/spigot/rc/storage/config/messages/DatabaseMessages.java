package solutions.nuhvel.spigot.rc.storage.config.messages;

import de.exlll.configlib.annotation.ConfigurationElement;

@ConfigurationElement
public class DatabaseMessages {
    public String loadSpawns = "&ePlease wait! Loading data from database...";
    public String saving = "&eSaving data to database...";
    public String added = "&eAdded %blocks% new blocks to database.";
    public String removed = "&eDeleted %blocks% old blocks from database.";
    public String done = "&aDone! Took %mills%ms";
    public String deleted = "&eDeleted database. Full restart is required for changes to take effect.";
}
