package solutions.nuhvel.spigot.rc.storage.database;

import org.bukkit.Bukkit;
import solutions.nuhvel.spigot.rc.RestrictedCreative;

import java.sql.*;

public class Database {
    private final RestrictedCreative plugin;

    private final String host;
    private final String name;
    private final String user;
    private final String pass;
    private final String type;
    private final String table_blocks;
    private final String table_inventories;
    private final int port;
    private final boolean ssl;
    private Connection connection;

    public Database(RestrictedCreative plugin, String type) {
        this.plugin = plugin;

        this.type = type != null ? type : plugin.config.database.type.toString().toLowerCase();
        this.host = plugin.config.database.mysql.host;
        this.name = plugin.config.database.name;
        this.user = plugin.config.database.mysql.username;
        this.pass = plugin.config.database.mysql.password;
        this.port = plugin.config.database.mysql.port;
        this.ssl = plugin.config.database.mysql.ssl;
        this.table_blocks = plugin.config.database.tables.block;
        this.table_inventories = plugin.config.database.tables.inventory;

        openConnection();
    }

    public String getBlocksTable() {
        return table_blocks;
    }

    public String getInventoryTable() {
        return table_inventories;
    }

    public Connection getConnection() {
        if (connection == null) {
            openConnection();
            return getConnection();
        }

        if (isValidConnection()) {
            return connection;
        }

        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        openConnection();
        return getConnection();
    }

    public boolean isValidConnection() {
        try {
            switch (type) {
                case ("mysql"):
                    return connection.isValid(1);
                case ("sqlite"):
                    return !connection.isClosed();
                default:
                    log("Incompatible database type provided: '" + type + "'. Compatible are: MySQL and SQLite.");
                    return false;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private void openConnection() {
        switch (type) {
            case ("mysql") -> openMySQLConnection();
            case ("sqlite") -> openSQLiteConnection();
            default -> log("Incompatible database type provided: '" + type + "'. Compatible are: MySQL and SQLite.");
        }
    }

    private void openMySQLConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }

            synchronized (this) {
                if (connection != null && !connection.isClosed()) {
                    return;
                }
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.name
                        + "?autoReconnect=true&useSSL=" + this.ssl, this.user, this.pass);
            }
        } catch (SQLException e) {
            log("Could not connect to database, check config:");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            log("com.mysql.jdbc.Driver is not installed.");
        }
    }

    private void openSQLiteConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }

            synchronized (this) {
                if (connection != null && !connection.isClosed()) {
                    return;
                }
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager
                        .getConnection("jdbc:sqlite:" + plugin.getDataFolder().getPath() + "/" + this.name + ".db");
            }
        } catch (SQLException e) {
            log("Could not connect to database:");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            log("org.sqlite.JDBC is not installed.");
        }
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            log("Could not close connection:");
            e.printStackTrace();
        }
    }

    public PreparedStatement getStatement(String sql) {
        try {
            return connection.prepareStatement(sql);
        } catch (SQLException e) {
            log("Could not prepare SQL statement:");
            e.printStackTrace();

            return null;
        }
    }

    public void executeUpdate(String sql) {
        try {
            getStatement(sql).executeUpdate();
        } catch (SQLException e) {
            log("Could not execute SQL statement:");
            e.printStackTrace();
        }
    }

    public ResultSet executeQuery(String sql) {
        try {
            return getStatement(sql).executeQuery();
        } catch (SQLException e) {
            log("Could not execute SQL statement:");
            e.printStackTrace();
        }
        return null;
    }

    public void setAutoCommit(boolean status) {
        try {
            getConnection().setAutoCommit(status);
        } catch (SQLException e) {
            log("Could not change autocommit status:");
            e.printStackTrace();
        }
    }

    public void commit() {
        try {
            getConnection().commit();
        } catch (SQLException e) {
            log("Could not commit:");
            e.printStackTrace();
        }
    }

    private void log(String msg) {
        Bukkit.getLogger().severe(msg);
    }
}
