package solutions.nuhvel.spigot.rc.utils.helpers;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.RegisteredServiceProvider;
import solutions.nuhvel.spigot.rc.RestrictedCreative;
import solutions.nuhvel.spigot.rc.storage.handlers.PermissionHandler;
import solutions.nuhvel.spigot.rc.utils.minecraft.ServerUtils;

import java.util.Objects;

public class PermissionsHelper {
    private final RestrictedCreative plugin;

    public PermissionsHelper(RestrictedCreative plugin) {
        this.plugin = plugin;
    }

    void setGroups(Player p, boolean toCreative) {
        RegisteredServiceProvider<Permission> provider =
                Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
        Permission vault = Objects.requireNonNull(provider).getProvider();

        if (toCreative) {
            for (String group : plugin.config.miscellaneous.groups.add) {
                if (vault.playerInGroup(p, group))
                    return;

                PermissionHandler.addVaultGroup(p, group);
                vault.playerAddGroup(p, group);
            }
            for (String group : plugin.config.miscellaneous.groups.remove) {
                if (!vault.playerInGroup(p, group))
                    return;

                PermissionHandler.addVaultGroup(p, group);
                vault.playerRemoveGroup(p, group);
            }
        } else {
            if (PermissionHandler.getVaultGroups(p) == null)
                return;

            for (String group : PermissionHandler.getVaultGroups(p)) {
                if (vault.playerInGroup(p, group)) {
                    vault.playerRemoveGroup(p, group);
                } else {
                    vault.playerAddGroup(p, group);
                }
            }

            PermissionHandler.removeVaultGroup(p);
        }
    }

    void setPermissions(Player p, boolean toCreative) {
        if (ServerUtils.isInstalled("Vault") && plugin.config.miscellaneous.permissions.useVault) {
            RegisteredServiceProvider<Permission> provider =
                    Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
            Permission vault = Objects.requireNonNull(provider).getProvider();

            if (toCreative) {
                for (String perm : plugin.config.miscellaneous.permissions.add) {
                    if (vault.has(p, perm))
                        return;

                    PermissionHandler.addVaultPerm(p, perm);
                    vault.playerAdd(p, perm);
                }

                for (String perm : plugin.config.miscellaneous.permissions.remove) {
                    if (!vault.has(p, perm))
                        return;

                    PermissionHandler.addVaultPerm(p, perm);
                    vault.playerRemove(p, perm);
                }
            } else {
                if (PermissionHandler.getVaultPerms(p) == null)
                    return;

                for (String perm : PermissionHandler.getVaultPerms(p)) {
                    if (vault.has(p, perm)) {
                        vault.playerRemove(p, perm);
                    } else {
                        vault.playerAdd(p, perm);
                    }
                }

                PermissionHandler.removeVaultPerm(p);
            }
        } else {
            if (toCreative) {
                PermissionAttachment attachment = p.addAttachment(plugin);

                for (String perm : plugin.config.miscellaneous.permissions.add)
                    attachment.setPermission(perm, true);

                for (String perm : plugin.config.miscellaneous.permissions.remove)
                    attachment.setPermission(perm, false);

                PermissionHandler.setPerms(p, attachment);
            } else {
                if (PermissionHandler.getPerms(p) == null)
                    return;

                PermissionAttachment attachment = PermissionHandler.getPerms(p);
                if (attachment != null)
                    p.removeAttachment(attachment);

                PermissionHandler.removePerms(p);
            }
        }
    }
}
