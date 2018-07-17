package me.prunt.restrictedcreative.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;

import me.prunt.restrictedcreative.Main;

public class AliasManager {
    private Logger logger;
    private Server server;

    public AliasManager(Main main) {
	logger = main.getLogger();
	server = main.getServer();
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, Command> getCommandMap() throws NoSuchMethodException, SecurityException,
	    IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
	Method cmp = server.getClass().getMethod("getCommandMap");
	SimpleCommandMap scmp = (SimpleCommandMap) cmp.invoke(server);

	if (scmp == null)
	    return new HashMap<String, Command>();

	Field field = scmp.getClass().getDeclaredField("knownCommands");
	field.setAccessible(true);

	HashMap<String, Command> map = (HashMap<String, Command>) field.get(scmp);

	return map;
    }

    public boolean setAdditionalAliases(Command command, List<String> aliases) {
	Map<String, Command> omap = new HashMap<String, Command>();
	Map<String, Command> map = new HashMap<String, Command>();

	try {
	    map = getCommandMap();
	    omap.putAll(map);
	} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
		| InvocationTargetException | NoSuchFieldException e) {
	    logger.severe(e.getLocalizedMessage());
	}

	int added = 0;
	for (String alias : aliases) {
	    map.put(alias.toLowerCase(), command);

	    if (map.containsKey(alias.toLowerCase()))
		added++;
	}

	return added == aliases.size();
    }

}