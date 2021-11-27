package solutions.nuhvel.spigot.rc.storage.config.messages.block;

import de.exlll.configlib.annotation.ConfigurationElement;

@ConfigurationElement
public class InfoMessages {
    public String info = "&aRight-click a block/entity you want to know more about. Type \"/rc block info\" again to cancel.";
    public String yes = "&aBlock/entity %type% was placed in creative mode!";
    public String no = "&aBlock/entity %type% was not placed in creative mode.";
}
