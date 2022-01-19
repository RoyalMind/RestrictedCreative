package solutions.nuhvel.spigot.rc.storage.config.messages;

import de.exlll.configlib.annotation.Comment;
import de.exlll.configlib.configs.yaml.BukkitYamlConfiguration;
import solutions.nuhvel.spigot.rc.storage.config.messages.block.BlockMessages;
import solutions.nuhvel.spigot.rc.storage.config.messages.commands.CommandMessages;

import java.nio.file.Path;

public final class RcMessages extends BukkitYamlConfiguration {
    @Comment("")
    public PluginMessages plugin = new PluginMessages();
    @Comment({""})
    public CommandMessages commands = new CommandMessages();
    @Comment("")
    public ErrorMessages errors = new ErrorMessages();
    @Comment("")
    public DisabledMessages disabled = new DisabledMessages();
    @Comment("")
    public DatabaseMessages database = new DatabaseMessages();
    @Comment("")
    public BlockMessages block = new BlockMessages();

    public RcMessages(Path path, BukkitYamlProperties properties) {
        super(path, properties);
    }
}
