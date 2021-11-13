package solutions.nuhvel.spigot.rc.storage.config.config.database;

import de.exlll.configlib.annotation.Comment;
import de.exlll.configlib.annotation.ConfigurationElement;

@ConfigurationElement
public class MySQLConnection {
    @Comment("MySQL connection username")
    public String username = "rc";
    @Comment("MySQL connection password")
    public String password = "";
    @Comment("MySQL host (domain or IP, without port)")
    public String host = "";
    @Comment("MySQL host port")
    public int port = 3306;
    @Comment("Whether to use a secure connection to the database")
    public boolean ssl = true;
}
