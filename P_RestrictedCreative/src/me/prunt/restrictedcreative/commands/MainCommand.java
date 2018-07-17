package me.prunt.restrictedcreative.commands;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.prunt.restrictedcreative.Main;
import me.prunt.restrictedcreative.store.Database;

public class MainCommand implements CommandExecutor {
    private Main main;

    public MainCommand(Main plugin) {
	this.main = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	if (args.length == 0) {
	    main.sendMessage(sender, true, "incorrect.main");
	    return true;
	}
	
	switch (args[0]) {
	case 
	}

	// If there's at least 1 argument
	if (args.length > 0) {
	    // If it's the reload command
	    if (args[0].equalsIgnoreCase("reload")) {
		// If the sender has enough permissions
		if (sender.hasPermission("rc.commands.reload")) {
		    // Reloads from config file
		    main.loadConfig();

		    main.sendMessage(sender, true, "reloaded");
		} else {
		    main.sendMessage(sender, false, "no-permission");
		}

		return true;

		// If it's the delete command
	    } else if (args[0].equalsIgnoreCase("i-am-sure-i-want-to-delete-all-plugin-data-from-database")) {
		// If the sender has enough permissions
		if (sender.hasPermission("rc.commands.delete")) {
		    try {
			main.getDB().getStatement("DELETE FROM " + main.getDB().getTableName()).executeUpdate();

			// Loops through worlds
			for (World w : main.getServer().getWorlds()) {
			    // Leaves out the disabled ones
			    if (main.isDisabledWorld(w.getName())) {
				continue;
			    }
			    // Loops through entities
			    for (Entity e : w.getEntities()) {
				// If it's tracked
				if (Main.isCreative(e)) {
				    // Removes tracking
				    Main.remove(e);
				}
			    }
			}

			// Message none check
			if (!main.isNone(main.db_deleted))
			    sender.sendMessage(main.prefix + main.db_deleted);
		    } catch (SQLException e) {
			e.printStackTrace();
		    }
		} else {
		    // Message none check
		    if (!main.isNone(main.no_perm))
			sender.sendMessage(main.prefix + main.no_perm);
		}

		return true;

		// If it's the database fix command
	    } else if (args[0].equalsIgnoreCase("fix-database")) {
		if (!(sender instanceof Player)) {
		    sender.sendMessage(
			    main.prefix + ChatColor.YELLOW + "Starting database update... Don't close the server!");

		    main.getServer().getScheduler().runTaskAsynchronously(main, new Runnable() {
			@Override
			public void run() {
			    try {
				// Create new table, but with unique index
				if (main.db_type.equalsIgnoreCase("mysql")) {
				    main.getDB().getStatement("CREATE TABLE IF NOT EXISTS "
					    + main.getDB().getTableName() + "_new (block VARCHAR(255), UNIQUE (block))")
					    .executeUpdate();
				} else if (main.db_type.equalsIgnoreCase("sqlite")) {
				    main.getDB().getStatement("CREATE TABLE IF NOT EXISTS "
					    + main.getDB().getTableName() + "_new (block VARCHAR(255) UNIQUE)")
					    .executeUpdate();
				}

				// Copy data from old table to new table
				main.getDB()
					.getStatement("INSERT " + main.or + "IGNORE INTO " + main.getDB().getTableName()
						+ "_new SELECT * FROM " + main.getDB().getTableName())
					.executeUpdate();

				// Drop old table
				main.getDB().getStatement("DROP TABLE " + main.getDB().getTableName()).executeUpdate();

				// Rename new table to old name
				if (main.db_type.equalsIgnoreCase("mysql")) {
				    main.getDB().getStatement("RENAME TABLE " + main.getDB().getTableName() + "_new TO "
					    + main.getDB().getTableName()).executeUpdate();
				} else if (main.db_type.equalsIgnoreCase("sqlite")) {
				    main.getDB().getStatement("ALTER TABLE " + main.getDB().getTableName()
					    + "_new RENAME TO " + main.getDB().getTableName()).executeUpdate();
				}
			    } catch (SQLException e) {
				e.printStackTrace();
			    }

			    sender.sendMessage(main.prefix + ChatColor.GREEN + "Database updated!");
			}
		    });

		    return true;
		}

		// If it's the convert command
	    } else if (args[0].equalsIgnoreCase("convert")) {
		// creativecontrol:
		// blocks ( x , y , z , world , material )
		// vehicles ( world , x , y , z , uuid , timestamp )
		// hangings ( world , x , y , z , uuid , timestamp )
		//
		// gamemode inventories:
		// blocks (worldchunk varchar(128), location text)
		// stands (uuid varchar(48))
		//
		// sharecontrol:
		// blocks (x int(11), y int(11), z int(11), world INTEGER)
		// int w = Bukkit.getWorlds().indexOf(b.getWorld())

		if (sender.hasPermission("rc.commands.convert")) {
		    // If there's at least 2 arguments
		    if (args.length > 1) {
			if (args[1].equalsIgnoreCase("sc")) {
			    // Message none check
			    if (!main.isNone(main.db_load))
				sender.sendMessage(main.prefix + main.db_load);

			    loadAsyncSC(new DBCallback() {
				@Override
				public void onQueryDone(ResultSet rs, ResultSet rs2, ResultSet rs3, long start,
					List<String> added, List<String> removed, List<UUID> fenadd, List<UUID> fendel,
					List<UUID> fifadd, List<UUID> fifdel) {
				    int addcount = 0;

				    try {
					while (rs.next()) {
					    int x = rs.getInt("x");
					    int y = rs.getInt("y");
					    int z = rs.getInt("z");
					    World world = Bukkit.getWorlds().get(rs.getInt("world"));

					    Main.add(world.getBlockAt(x, y, z));
					    addcount++;
					}
				    } catch (SQLException e) {
					sender.sendMessage("Error finding table named '" + main.sc_table
						+ "'. Could not import it.");
				    }

				    // Message none check
				    if (!main.isNone(main.db_added))
					sender.sendMessage(
						main.prefix + main.db_added.replaceAll("%blocks%", "" + addcount));

				    long took = System.currentTimeMillis() - start;

				    // Message none check
				    if (!main.isNone(main.db_done))
					sender.sendMessage(main.prefix + main.db_done.replaceAll("%mills%", "" + took));

				}
			    });

			    return true;
			} else if (args[1].equalsIgnoreCase("cc")) {
			    // Message none check
			    if (!main.isNone(main.db_load))
				sender.sendMessage(main.prefix + main.db_load);

			    loadAsyncCC(new DBCallback() {
				@Override
				public void onQueryDone(ResultSet rs, ResultSet rs2, ResultSet rs3, long start,
					List<String> added, List<String> removed, List<UUID> fenadd, List<UUID> fendel,
					List<UUID> fifadd, List<UUID> fifdel) {
				    int addcount = 0;

				    try {
					while (rs.next()) {
					    int x = rs.getInt("x");
					    int y = rs.getInt("y");
					    int z = rs.getInt("z");
					    String world = rs.getString("world");

					    Main.add(main.getServer().getWorld(world).getBlockAt(x, y, z));
					    addcount++;
					}
				    } catch (SQLException e) {
					sender.sendMessage("Error finding table named '" + main.cc_prefix
						+ "blocks'. Could not import it.");
				    }
				    try {
					while (rs2.next()) {
					    String uuid = rs2.getString("uuid");

					    try {
						if (main.getServer().getVersion().contains("1.10")
							|| main.getServer().getVersion().contains("1.9")
							|| main.getServer().getVersion().contains("1.8")) {
						    for (World w : main.getServer().getWorlds()) {
							if (!main.isDisabledWorld(w.getName())) {
							    for (Entity e : w.getEntities()) {
								if (e.getUniqueId().toString().equalsIgnoreCase(uuid)) {
								    Main.add(e);
								}
							    }
							}
						    }
						} else {
						    Main.add(main.getServer().getEntity(UUID.fromString(uuid)));
						}

						addcount++;
					    } catch (IllegalArgumentException e) {
						sender.sendMessage("Error finding entity with UUID '" + uuid
							+ "'. Could not import it.");
					    }
					}
				    } catch (SQLException e) {
					sender.sendMessage("Error finding table named '" + main.cc_prefix
						+ "vehicles'. Could not import it.");
				    }
				    try {
					while (rs3.next()) {
					    String uuid = rs3.getString("uuid");

					    try {
						if (main.getServer().getVersion().contains("1.10")
							|| main.getServer().getVersion().contains("1.9")
							|| main.getServer().getVersion().contains("1.8")) {
						    for (World w : main.getServer().getWorlds()) {
							if (!main.isDisabledWorld(w.getName())) {
							    for (Entity e : w.getEntities()) {
								if (e.getUniqueId().toString().equalsIgnoreCase(uuid)) {
								    Main.add(e);
								}
							    }
							}
						    }
						} else {
						    Main.add(main.getServer().getEntity(UUID.fromString(uuid)));
						}

						addcount++;
					    } catch (IllegalArgumentException e) {
						sender.sendMessage("Error finding entity with UUID '" + uuid
							+ "'. Could not import it.");
					    }
					}
				    } catch (SQLException e) {
					sender.sendMessage("Error finding table named '" + main.cc_prefix
						+ "hanging'. Could not import it.");
				    }

				    // Message none check
				    if (!main.isNone(main.db_added))
					sender.sendMessage(
						main.prefix + main.db_added.replaceAll("%blocks%", "" + addcount));

				    long took = System.currentTimeMillis() - start;

				    // Message none check
				    if (!main.isNone(main.db_done))
					sender.sendMessage(main.prefix + main.db_done.replaceAll("%mills%", "" + took));

				}
			    });

			    return true;
			} else if (args[1].equalsIgnoreCase("gmi")) {
			    // Message none check
			    if (!main.isNone(main.db_load))
				sender.sendMessage(main.prefix + main.db_load);

			    loadAsyncGMI(new DBCallback() {
				@Override
				public void onQueryDone(ResultSet rs, ResultSet rs2, ResultSet rs3, long start,
					List<String> added, List<String> removed, List<UUID> fenadd, List<UUID> fendel,
					List<UUID> fifadd, List<UUID> fifdel) {
				    int addcount = 0;

				    try {
					while (rs.next()) {
					    String loc = rs.getString("location");
					    int x = getInt(loc, ".*x=-?([0-9]+).*");
					    int y = getInt(loc, ".*y=-?([0-9]+).*");
					    int z = getInt(loc, ".*z=-?([0-9]+).*");
					    String world = Pattern.compile(".*{name=(.+)}.*").matcher(loc).group(1);

					    Main.add(main.getServer().getWorld(world).getBlockAt(x, y, z));
					    addcount++;
					}
				    } catch (SQLException e) {
					sender.sendMessage("Error finding table named 'blocks'. Could not import it.");
				    }
				    try {
					while (rs2.next()) {
					    String uuid = rs2.getString("uuid");

					    try {
						if (main.getServer().getVersion().contains("1.10")
							|| main.getServer().getVersion().contains("1.9")
							|| main.getServer().getVersion().contains("1.8")) {
						    for (World w : main.getServer().getWorlds()) {
							if (!main.isDisabledWorld(w.getName())) {
							    for (Entity e : w.getEntities()) {
								if (e.getUniqueId().toString().equalsIgnoreCase(uuid)) {
								    Main.add(e);
								}
							    }
							}
						    }
						} else {
						    Main.add(main.getServer().getEntity(UUID.fromString(uuid)));
						}

						addcount++;
					    } catch (IllegalArgumentException e) {
						sender.sendMessage("Error finding entity with UUID '" + uuid
							+ "'. Could not import it.");
					    }
					}
				    } catch (SQLException e) {
					sender.sendMessage("Error finding table named 'stands'. Could not import it.");
				    }

				    // Message none check
				    if (!main.isNone(main.db_added))
					sender.sendMessage(
						main.prefix + main.db_added.replaceAll("%blocks%", "" + addcount));

				    long took = System.currentTimeMillis() - start;

				    // Message none check
				    if (!main.isNone(main.db_done))
					sender.sendMessage(main.prefix + main.db_done.replaceAll("%mills%", "" + took));
				}
			    });

			    return true;
			} else if (args[1].equalsIgnoreCase("sql")) {
			    // Initializes new database instance for the other
			    // SQL database type
			    Database db;

			    // Checks which database is chosen
			    String type = "MySQL";
			    if (main.db_type.equalsIgnoreCase("mysql")) {
				type = "SQLite";
			    }

			    // Creates database connection of another type
			    db = new Database(main, type, false);

			    // Checks connection
			    if (!db.isValidConnection()) {
				sender.sendMessage("Error connecting with " + type + " database. Could not import it.");
				return true;
			    }

			    // Message none check
			    if (!main.isNone(main.db_load))
				sender.sendMessage(main.prefix + main.db_load);

			    loadAsyncSQL(new DBCallback() {
				@Override
				public void onQueryDone(ResultSet rs, ResultSet rs2, ResultSet rs3, long start,
					List<String> added, List<String> removed, List<UUID> fenadd, List<UUID> fendel,
					List<UUID> fifadd, List<UUID> fifdel) {
				    int addcount = 0;

				    try {
					while (rs.next()) {
					    Main.add(main.getBlock(rs.getString("block")));
					    addcount++;
					}
				    } catch (SQLException e) {
					sender.sendMessage("Error finding table named '" + db.getTableName()
						+ "'. Could not import it.");
				    }

				    // Message none check
				    if (!main.isNone(main.db_added))
					sender.sendMessage(
						main.prefix + main.db_added.replaceAll("%blocks%", "" + addcount));

				    long took = System.currentTimeMillis() - start;

				    // Message none check
				    if (!main.isNone(main.db_done))
					sender.sendMessage(main.prefix + main.db_done.replaceAll("%mills%", "" + took));

				}
			    });

			    return true;
			}
		    }

		    // Message none check
		    if (!main.isNone(main.wrong_convert))
			sender.sendMessage(main.wrong_convert);

		    return true;
		} else {
		    // Message none check
		    if (!main.isNone(main.no_perm))
			sender.sendMessage(main.prefix + main.no_perm);
		}
	    } else if (args[0].equalsIgnoreCase("block")) {
		// If command sender is not a player
		if (!(sender instanceof Player)) {
		    // Message none check
		    if (!main.isNone(main.no_console))
			sender.sendMessage(main.prefix + main.no_console);
		    return true;
		}

		Player p = (Player) sender;

		if (sender.hasPermission("rc.commands.block")) {
		    // If there's at least 2 arguments
		    if (args.length > 1) {
			if (args[1].equalsIgnoreCase("add")) {
			    if (main.addList.contains(p)) {
				main.addList.remove(p);

				// Message none check
				if (!main.isNone(main.cancel))
				    sender.sendMessage(main.cancel);
			    } else {
				main.addList.add(p);

				// Message none check
				if (!main.isNone(main.block_add))
				    sender.sendMessage(main.block_add);
			    }

			    return true;
			} else if (args[1].equalsIgnoreCase("remove")) {
			    if (main.remList.contains(p)) {
				main.remList.remove(p);

				// Message none check
				if (!main.isNone(main.cancel))
				    sender.sendMessage(main.cancel);
			    } else {
				main.remList.add(p);

				// Message none check
				if (!main.isNone(main.block_add))
				    sender.sendMessage(main.block_add);
			    }

			    return true;
			} else if (args[1].equalsIgnoreCase("info")) {
			    if (main.infoList.contains(p)) {
				main.infoList.remove(p);

				// Message none check
				if (!main.isNone(main.cancel))
				    sender.sendMessage(main.cancel);
			    } else {
				main.infoList.add(p);

				// Message none check
				if (!main.isNone(main.block_info))
				    sender.sendMessage(main.block_info);
			    }

			    return true;
			} else if (args[1].equalsIgnoreCase("stats")) {
			    // Message none check
			    if (!main.isNone(main.stats))
				sender.sendMessage(main.stats.replaceAll("%total%", "" + Main.getTotalCount()));

			    return true;
			}
		    }

		    // Message none check
		    if (!main.isNone(main.wrong_block))
			sender.sendMessage(main.wrong_block);

		    return true;
		} else {
		    // Message none check
		    if (!main.isNone(main.no_perm))
			sender.sendMessage(main.prefix + main.no_perm);
		}
	    }

	    sender.sendMessage(ChatColor.RED + "/" + label
		    + " <reload|convert|block|i-am-sure-i-want-to-delete-all-plugin-data-from-database>");

	    return false;
	}

	// If command sender is not a player
	if (!(sender instanceof Player)) {
	    // Message none check
	    if (!main.isNone(main.no_console))
		sender.sendMessage(main.prefix + main.no_console);
	    return true;
	}

	Player p = (Player) sender;

	// World check
	if (main.isDisabledWorld(p.getWorld().getName())) {
	    // Message none check
	    if (!main.isNone(main.no_perm))
		sender.sendMessage(main.prefix + main.no_perm);
	    return true;
	}

	// Region check
	if (main.region && !p.hasPermission("rc.bypass.disabled-regions")) {
	    // Gets the player or block location
	    Location loc = p.getLocation();

	    // Gets all regions covering the player location
	    ApplicableRegionSet set = WGBukkit.getPlugin().getRegionManager(p.getWorld()).getApplicableRegions(loc);

	    // Whether it's happening in the allowed region
	    boolean allowed = false;

	    // Loops through applicable regions
	    for (ProtectedRegion rg : set) {
		// If it's whitelisted
		if (main.regions.contains(rg.getId())) {
		    allowed = true;
		    break;
		}
	    }

	    if (!allowed) {
		// Message none check
		if (!main.isNone(main.disabled_region))
		    p.sendMessage(main.prefix + main.disabled_region);

		return true;
	    }
	}

	// Player height check
	if ((main.below_y > 0 && p.getLocation().getY() < main.below_y)
		|| (main.above_y > 0 && p.getLocation().getY() > main.above_y)) {
	    // Message none check
	    if (!main.isNone(main.disabled_region))
		p.sendMessage(main.prefix + main.disabled_region);

	    return true;
	}

	// If player is already in creative mode
	if (p.getGameMode() == GameMode.CREATIVE) {
	    p.setGameMode(main.getPreviousGameMode(p));

	    // Message none check
	    if (!main.isNone(main.creative_off))
		p.sendMessage(main.prefix + main.creative_off);
	} else {
	    p.setGameMode(GameMode.CREATIVE);

	    // Message none check
	    if (!main.isNone(main.creative_on))
		p.sendMessage(main.prefix + main.creative_on);
	}

	return true;
    }

    private int getInt(String text, String regex) {
	return Integer.valueOf(Pattern.compile(regex).matcher(text).group(1));
    }

    private void loadAsyncSC(DBCallback callback) {
	// Start async processing
	Bukkit.getScheduler().runTaskAsynchronously(main, new Runnable() {
	    @Override
	    public void run() {
		try {
		    long start = System.currentTimeMillis();

		    // Gets data from database
		    ResultSet rs = main.getDB().getStatement("SELECT * FROM " + main.sc_table).executeQuery();

		    // Back to sync processing
		    Bukkit.getScheduler().runTask(main, new Runnable() {
			@Override
			public void run() {
			    callback.onQueryDone(rs, null, null, start, null, null, null, null, null, null);
			}
		    });
		} catch (SQLException e) {
		    e.printStackTrace();
		}
	    }
	});
    }

    private void loadAsyncCC(DBCallback callback) {
	// Start async processing
	Bukkit.getScheduler().runTaskAsynchronously(main, new Runnable() {
	    @Override
	    public void run() {
		try {
		    long start = System.currentTimeMillis();

		    // Gets data from database
		    ResultSet rs = main.getDB().getStatement("SELECT * FROM " + main.cc_prefix + "blocks")
			    .executeQuery();
		    ResultSet rs2 = main.getDB().getStatement("SELECT * FROM " + main.cc_prefix + "vehicles")
			    .executeQuery();
		    ResultSet rs3 = main.getDB().getStatement("SELECT * FROM " + main.cc_prefix + "hangings")
			    .executeQuery();

		    // Back to sync processing
		    Bukkit.getScheduler().runTask(main, new Runnable() {
			@Override
			public void run() {
			    callback.onQueryDone(rs, rs2, rs3, start, null, null, null, null, null, null);
			}
		    });
		} catch (SQLException e) {
		    e.printStackTrace();
		}
	    }
	});
    }

    private void loadAsyncGMI(DBCallback callback) {
	// Start async processing
	Bukkit.getScheduler().runTaskAsynchronously(main, new Runnable() {
	    @Override
	    public void run() {
		try {
		    long start = System.currentTimeMillis();

		    // Gets data from database
		    ResultSet rs = main.getDB().getStatement("SELECT * FROM blocks").executeQuery();
		    ResultSet rs2 = main.getDB().getStatement("SELECT * FROM stands").executeQuery();

		    // Back to sync processing
		    Bukkit.getScheduler().runTask(main, new Runnable() {
			@Override
			public void run() {
			    callback.onQueryDone(rs, rs2, null, start, null, null, null, null, null, null);
			}
		    });
		} catch (SQLException e) {
		    e.printStackTrace();
		}
	    }
	});
    }

    private void loadAsyncSQL(DBCallback callback) {
	// Start async processing
	Bukkit.getScheduler().runTaskAsynchronously(main, new Runnable() {
	    @Override
	    public void run() {
		try {
		    long start = System.currentTimeMillis();

		    // Gets data from database
		    ResultSet rs = main.getDB().getStatement("SELECT * FROM " + main.getDB().getTableName())
			    .executeQuery();

		    // Back to sync processing
		    Bukkit.getScheduler().runTask(main, new Runnable() {
			@Override
			public void run() {
			    callback.onQueryDone(rs, null, null, start, null, null, null, null, null, null);
			}
		    });
		} catch (SQLException e) {
		    e.printStackTrace();
		}
	    }
	});
    }
}
