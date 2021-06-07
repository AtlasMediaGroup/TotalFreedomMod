package me.totalfreedom.totalfreedommod.command;

import me.totalfreedom.totalfreedommod.admin.Admin;
import me.totalfreedom.totalfreedommod.admin.AdminList;
import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.rank.Displayable;
import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FUtil;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@CommandPermissions(level = Rank.IMPOSTOR, source = SourceType.BOTH)
@CommandParameters(description = "Lists the real names of all online players.", usage = "/<command> [-s | -i | -f | -v]", aliases = "who,lsit")
public class Command_list extends FreedomCommand
{

    public boolean run(final CommandSender sender, final Player playerSender, final Command cmd, final String commandLabel, final String[] args, final boolean senderIsConsole)
    {
        if (args.length > 1)
        {
            return false;
        }
        if (FUtil.isFromHostConsole(sender.getName()))
        {
            List<String> names = new ArrayList<>();
            for (Player player : server.getOnlinePlayers())
            {
                if (!plugin.al.isVanished(player.getName()))
                {
                    names.add(player.getName());
                }
            }
            msg("There are " + names.size() + "/" + server.getMaxPlayers() + " players online:\n" + StringUtils.join(names, ", "), ChatColor.WHITE);
            return true;
        }
        ListFilter listFilter;
        if (args.length == 1)
        {
            String s = args[0];
            switch (s)
            {
                case "-s":
                case "-a":
                {
                    listFilter = ListFilter.ADMINS;
                    break;
                }
                case "-v":
                {
                    checkRank(Rank.ADMIN);
                    listFilter = ListFilter.VANISHED_ADMINS;
                    break;
                }
                case "-t":
                {
                    checkRank(Rank.ADMIN);
                    listFilter = ListFilter.TELNET_SESSIONS;
                    break;
                }
                case "-i":
                {
                    listFilter = ListFilter.IMPOSTORS;
                    break;
                }
                case "-f":
                {
                    listFilter = ListFilter.FAMOUS_PLAYERS;
                    break;
                }
                default:
                {
                    return false;
                }
            }
        }
        else
        {
            listFilter = ListFilter.PLAYERS;
        }
        StringBuilder onlineStats = new StringBuilder();
        StringBuilder onlineUsers = new StringBuilder();

        List<String> n = new ArrayList<>();

        if (listFilter == ListFilter.TELNET_SESSIONS && plugin.al.isAdmin(sender) && plugin.al.getAdmin(playerSender).getRank().isAtLeast(Rank.ADMIN))
        {
            List<Admin> connectedAdmins = plugin.btb.getConnectedAdmins();
            onlineStats.append(ChatColor.BLUE).append("There are ").append(ChatColor.RED).append(connectedAdmins.size())
                    .append(ChatColor.BLUE)
                    .append(" admins connected to telnet.");
            for (Admin admin : connectedAdmins)
            {
                n.add(admin.getName());
            }
        }
        else
        {
            int count = server.getOnlinePlayers().size() - AdminList.vanished.size();
            onlineStats.append(ChatColor.BLUE).append("There are ").append(ChatColor.RED).append(count < 0 ? 0 : count)
                    .append(ChatColor.BLUE)
                    .append(" out of a maximum ")
                    .append(ChatColor.RED)
                    .append(server.getMaxPlayers())
                    .append(ChatColor.BLUE)
                    .append(" players online.");
            for (Player p : server.getOnlinePlayers())
            {
                if (listFilter == ListFilter.ADMINS && !plugin.al.isAdmin(p))
                {
                    continue;
                }
                if (listFilter == ListFilter.ADMINS && plugin.al.isVanished(p.getName()))
                {
                    continue;
                }
                if (listFilter == ListFilter.VANISHED_ADMINS && !plugin.al.isVanished(p.getName()))
                {
                    continue;
                }
                if (listFilter == ListFilter.IMPOSTORS && !plugin.al.isAdminImpostor(p))
                {
                    continue;
                }
                if (listFilter == ListFilter.FAMOUS_PLAYERS && !ConfigEntry.FAMOUS_PLAYERS.getList().contains(p.getName().toLowerCase()))
                {
                    continue;
                }
                if (listFilter == ListFilter.PLAYERS && plugin.al.isVanished(p.getName()))
                {
                    continue;
                }

                final Displayable display = plugin.rm.getDisplay(p);
                n.add(display.getColoredTag() + p.getName());
            }
        }
        String playerType = listFilter.toString().toLowerCase().replace('_', ' ');
        onlineUsers.append("Connected ")
                .append(playerType)
                .append(": ")
                .append(StringUtils.join(n, ChatColor.WHITE + ", "));
        if (senderIsConsole)
        {
            msg(ChatColor.stripColor(onlineStats.toString()));
            msg(ChatColor.stripColor(onlineUsers.toString()));
        }
        else
        {
            msg(onlineStats.toString());
            msg(onlineUsers.toString());
        }
        n.clear();
        return true;
    }

    private enum ListFilter
    {
        PLAYERS,
        ADMINS,
        VANISHED_ADMINS,
        TELNET_SESSIONS,
        FAMOUS_PLAYERS,
        IMPOSTORS
    }
}