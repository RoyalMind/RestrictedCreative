package solutions.nuhvel.spigot.rc.storage.config.messages.block;

import de.exlll.configlib.annotation.ConfigurationElement;

@ConfigurationElement
public class BlockMessages {
    public String usage = "&cCorrect usage: /rc block <add|remove|info|stats>";
    public String stats = "&eTotal creative placed blocks: %total%";
    public String cancel = "&cCancelled!";

    public SubCommandMessages add = new SubCommandMessages(
            "&aRight-click a block/entity you want to add to the database. Type \"/rc block add\" again to cancel.",
            "&aAdded %type% to the database!");
    public SubCommandMessages remove = new SubCommandMessages(
            "&aRight-click a block/entity you want to remove from the database. Type \"/rc block remove\" again to cancel.",
            "&aRemoved %type% from the database!");

    public InfoMessages info = new InfoMessages();
}
