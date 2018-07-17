package me.prunt.restrictedcreative.store;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.Bukkit;

import me.prunt.restrictedcreative.Main;

public class Database {
    private Main main;

    private String host, name, user, pass, type, table;
    private int port;
    private Connection connection;

    public Database(Main main, String type) {
	this.main = main;

	this.type = type.toLowerCase();
	this.host = main.getConfig().getString("database.host");
	this.name = main.getConfig().getString("database.database");
	this.user = main.getConfig().getString("database.username");
	this.pass = main.getConfig().getString("database.password");
	this.port = main.getConfig().getInt("database.port");
	this.table = main.getConfig().getString("database.table");

	openConnection();
    }

    /**
     * Return table name
     */
    public String getTableName() {
	return table;
    }

    /**
     * Return current connection
     */
    public Connection getConnection() {
	if (connection == null) {
	    openConnection();
	    return getConnection();
	}

	if (isValidConnection()) {
	    return connection;
	} else {
	    try {
		connection.close();
	    } catch (SQLException e) {
		e.printStackTrace();
	    }

	    openConnection();
	    return getConnection();
	}
    }

    /**
     * Check if connection is valid
     */
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

    /**
     * Open a connection of provided type (MySQL or SQLite)
     */
    private void openConnection() {
	switch (type) {
	case ("mysql"):
	    // Creates MySQL connection
	    openMySQLConnection();
	    break;
	case ("sqlite"):
	    // Creates MySQL connection
	    openSQLiteConnection();
	    break;
	default:
	    log("Incompatible database type provided: '" + type + "'. Compatible are: MySQL and SQLite.");
	    break;
	}
    }

    /**
     * Open MySQL connection
     */
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
		connection = DriverManager.getConnection(
			"jdbc:mysql://" + this.host + ":" + this.port + "/" + this.name + "?autoReconnect=true",
			this.user, this.pass);
	    }
	} catch (SQLException e) {
	    log("Could not connect to database, check config:");
	    e.printStackTrace();
	} catch (ClassNotFoundException e) {
	    log("com.mysql.jdbc.Driver is not installed.");
	}
    }

    /**
     * Open SQLite connection
     */
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
			.getConnection("jdbc:sqlite:" + main.getDataFolder().getPath() + "/" + this.name + ".db");
	    }
	} catch (SQLException e) {
	    log("Could not connect to database:");
	    e.printStackTrace();
	} catch (ClassNotFoundException e) {
	    log("org.sqlite.JDBC is not installed.");
	}
    }

    /**
     * Close current connection
     */
    public void closeConnection() {
	try {
	    connection.close();
	} catch (SQLException e) {
	    log("Could not close connection:");
	    e.printStackTrace();
	}
    }

    /**
     * Return PreparedStatement
     */
    public PreparedStatement getStatement(String sql) {
	try {
	    return connection.prepareStatement(sql);
	} catch (SQLException e) {
	    log("Could not prepare SQL statement:");
	    e.printStackTrace();

	    return null;
	}
    }

    /**
     * Print error to console
     */
    private void log(String msg) {
	Bukkit.getLogger().severe(msg);
    }
}
