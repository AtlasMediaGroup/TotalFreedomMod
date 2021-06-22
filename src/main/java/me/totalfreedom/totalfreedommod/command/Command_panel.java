package me.totalfreedom.totalfreedommod.command;

import com.google.common.base.Strings;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import me.totalfreedom.totalfreedommod.admin.Admin;
import me.totalfreedom.totalfreedommod.player.PlayerData;
import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = Rank.SENIOR_ADMIN, source = SourceType.ONLY_IN_GAME)
@CommandParameters(description = "Manage your Pterodactyl panel account", usage = "/<command> <create | delete>")
public class Command_panel extends FreedomCommand
{
    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (!plugin.ptero.isEnabled())
        {
            msg("Pterodactyl integration is currently disabled.", ChatColor.RED);
            return true;
        }

        PlayerData playerData = getData(playerSender);

        if (playerData.getDiscordID() == null)
        {
            msg("You must have a linked Discord account.", ChatColor.RED);
            return true;
        }

        if (args.length == 0)
        {
            return false;
        }

        if (args[0].equalsIgnoreCase("create"))
        {
            msg("Creating your Pterodactyl account...", ChatColor.GREEN);
            Admin admin = getAdmin(playerSender);

            if (admin.getPteroID() != null)
            {
                msg("You already have a Pterodactyl account.", ChatColor.RED);
                return true;
            }

            String username = sender.getName();
            String password = FUtil.randomString(30);

            String id = plugin.ptero.createAccount(username, password);
            if (Strings.isNullOrEmpty(id))
            {
                msg("Failed to create your Pterodactyl account.", ChatColor.RED);
                return true;
            }

            plugin.ptero.addAccountToServer(id);
            admin.setPteroID(id);
            plugin.al.save(admin);
            plugin.al.updateTables();
            plugin.dc.sendPteroInfo(playerData, username, password);
            msg("Successfully created your Pterodactyl account. Check your DMs from " + plugin.dc.formatBotTag() + " on Discord to get your credentials.", ChatColor.GREEN);
            return true;
        }
        else if (args[0].equalsIgnoreCase("delete"))
        {
            msg("Deleting your Pterodactyl account...", ChatColor.GREEN);
            Admin admin = getAdmin(playerSender);

            if (admin.getPteroID() == null)
            {
                msg("You do not have a Pterodactyl account.", ChatColor.RED);
                return true;
            }

            plugin.ptero.deleteAccount(admin.getPteroID());
            admin.setPteroID(null);
            plugin.al.save(admin);
            plugin.al.updateTables();
            msg("Successfully deleted your Pterodactyl account.", ChatColor.GREEN);
            return true;
        }
        return false;
    }

    @Override
    public List<String> getTabCompleteOptions(CommandSender sender, Command command, String alias, String[] args)
    {
        if (args.length == 1 && plugin.al.isSeniorAdmin(sender))
        {
            return Arrays.asList("create", "delete");
        }

        return Collections.emptyList();
    }
}