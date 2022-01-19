package solutions.nuhvel.spigot.rc.storage.config.config.database;

import de.exlll.configlib.annotation.ConfigurationElement;

@ConfigurationElement
public final class DatabaseConnection {
    public DatabaseType type = DatabaseType.SQLITE;
    public String name = "rc3_database";
    public DatabaseTables tables = new DatabaseTables();
    public MySQLConnection mysql = new MySQLConnection();
}
