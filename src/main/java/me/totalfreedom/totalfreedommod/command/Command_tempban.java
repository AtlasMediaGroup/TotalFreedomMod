package me.totalfreedom.totalfreedommod.command;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import me.totalfreedom.totalfreedommod.banning.Ban;
import me.totalfreedom.totalfreedommod.player.PlayerData;
import me.totalfreedom.totalfreedommod.punishments.Punishment;
import me.totalfreedom.totalfreedommod.punishments.PunishmentType;
import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = Rank.ADMIN, source = SourceType.BOTH)
@CommandParameters(description = "Temporarily ban someone.", usage = "/<command> [-q] <username> [duration] [reason]")
public class Command_tempban extends FreedomCommand
{

    private static final SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");

    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (args.length < 1)
        {
            return false;
        }

        boolean quiet = args[0].equalsIgnoreCase("-q");
        if (quiet)
        {
            args = org.apache.commons.lang3.ArrayUtils.subarray(args, 1, args.length);

            if (args.length < 1)
            {
                return false;
            }
        }

        final String username;
        final List<String> ips = new ArrayList<>();

        final Player player = getPlayer(args[0]);
        final PlayerData entry;
        if (player == null)
        {
            entry = plugin.pl.getData(args[0]);

            if (entry == null)
            {
                msg("Can't find that user. If target is not logged in, make sure that you spelled the name exactly.");
                return true;
            }

            username = entry.getName();
            ips.addAll(entry.getIps());
        }
        else
        {
            entry = plugin.pl.getData(player);
            username = player.getName();
            ips.add(FUtil.getIp(player));
        }

        final StringBuilder message = new StringBuilder("Temporarily banned " + username);

        Date expires = FUtil.parseDateOffset("30m");
        message.append(" until ").append(date_format.format(expires));

        String reason = null;
        if (args.length >= 2)
        {
            Date parsed_offset = FUtil.parseDateOffset(args[1]);
            reason = StringUtils.join(ArrayUtils.subarray(args, parsed_offset == null ? 1 : 2, args.length), " ") + " (" + sender.getName() + ")";
            if (parsed_offset != null)
            {
                expires = parsed_offset;
            }
            message.append(", Reason: \"").append(reason).append("\"");
        }

        Ban ban;

        if (player != null)
        {
            ban = Ban.forPlayer(player, sender, expires, reason);
        }
        else
        {
            ban = Ban.forPlayerName(username, sender, expires, reason);
        }

        for (String ip : ips)
        {
            ban.addIp(ip);
        }
        plugin.bm.addBan(ban);

        if (!quiet)
        {
            if (player != null)
            {
                // Strike with lightning
                final Location targetPos = player.getLocation();
                for (int x = -1; x <= 1; x++)
                {
                    for (int z = -1; z <= 1; z++)
                    {
                        final Location strike_pos = new Location(targetPos.getWorld(), targetPos.getBlockX() + x, targetPos.getBlockY(), targetPos.getBlockZ() + z);
                        Objects.requireNonNull(targetPos.getWorld()).strikeLightningEffect(strike_pos);
                    }
                }
            }

            FUtil.adminAction(sender.getName(), message.toString(), true);
        }
        else
        {
            msg("Quietly temporarily banned " + username + ".");
        }

        if (player != null)
        {
            player.kickPlayer(ban.bakeKickMessage());
            for (Player p : Bukkit.getOnlinePlayers())
            {
                if (FUtil.getIp(p).equals(FUtil.getIp(player)))
                {
                    p.kickPlayer(ChatColor.RED + "You've been kicked because someone on your IP has been banned.");
                }
            }
        }

        plugin.pul.logPunishment(new Punishment(username, ips.get(0), sender.getName(), PunishmentType.TEMPBAN, reason));
        return true;
    }
}