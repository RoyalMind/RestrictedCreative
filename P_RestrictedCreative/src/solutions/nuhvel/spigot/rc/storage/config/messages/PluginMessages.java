package solutions.nuhvel.spigot.rc.storage.config.messages;

import de.exlll.configlib.annotation.Comment;
import de.exlll.configlib.configs.yaml.BukkitYamlConfiguration;

import java.nio.file.Path;

public final class PluginMessages extends BukkitYamlConfiguration {
    @Comment("")
    public String prefix = "&2&lCreative > ";
    @Comment("")
    public ErrorMessages errors = new ErrorMessages();
    @Comment("")
    public DisabledMessages disabled = new DisabledMessages();

    public PluginMessages(Path path, BukkitYamlProperties properties) {
        super(path, properties);
    }
}
