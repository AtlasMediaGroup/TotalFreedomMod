package me.totalfreedom.totalfreedommod.command;

import io.papermc.lib.PaperLib;
import java.util.HashMap;
import java.util.Map;
import me.totalfreedom.totalfreedommod.player.FPlayer;
import me.totalfreedom.totalfreedommod.player.PlayerData;
import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@CommandPermissions(level = Rank.OP, source = SourceType.ONLY_IN_GAME)
@CommandParameters(description = "Ride on the top of the specified player.", usage = "/<command> <playername | mode <normal | off | ask>>")
public class Command_ride extends FreedomCommand
{

    private final Map<Player, Player> RIDE_REQUESTS = new HashMap<>(); // requested, requester

    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        final FPlayer fPlayer = plugin.pl.getPlayer(playerSender);
        if (fPlayer.getCageData().isCaged())
        {
            msg("You cannot use this command while caged.");
            return true;
        }

        if (args.length < 1)
        {
            return false;
        }

        if (args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("yes"))
        {
            if (!RIDE_REQUESTS.containsKey(playerSender))
            {
                msg("You don't have any pending requests.");
                return true;
            }

            Player requester = RIDE_REQUESTS.get(playerSender);
            if (requester == null)
            {
                msg("The player who sent the request is no longer online.");
                RIDE_REQUESTS.remove(playerSender);
                return true;
            }

            msg("Request accepted.");
            msg(requester, "Your request has been accepted.");

            if (requester.getWorld() != playerSender.getWorld())
            {
                PaperLib.teleportAsync(requester, playerSender.getLocation());
            }

            RIDE_REQUESTS.remove(playerSender);
            playerSender.addPassenger(requester);
            return true;
        }

        if (args[0].equalsIgnoreCase("deny") || args[0].equalsIgnoreCase("no"))
        {
            if (!RIDE_REQUESTS.containsKey(playerSender))
            {
                msg("You don't have any pending requests.");
                return true;
            }
            Player requester = RIDE_REQUESTS.get(playerSender);
            if (requester == null)
            {
                msg("The player who sent the request is no longer online.");
                RIDE_REQUESTS.remove(playerSender);
                return true;
            }
            msg("Request denied.");
            RIDE_REQUESTS.remove(playerSender);
            msg(requester, "Your request has been denied.");
            return true;
        }

        if (args.length >= 2)
        {
            if (args[0].equalsIgnoreCase("mode"))
            {
                if (args[1].equalsIgnoreCase("normal") || args[1].equalsIgnoreCase("off") || args[1].equalsIgnoreCase("ask"))
                {
                    PlayerData playerDataSender = plugin.pl.getData(playerSender);
                    playerDataSender.setRideMode(args[1].toLowerCase());
                    plugin.pl.save(playerDataSender);
                    msg("Ride mode is now set to " + args[1].toLowerCase() + ".");
                    return true;
                }
            }
        }

        final Player player = getPlayer(args[0], true);
        if (player == null)
        {
            msg(PLAYER_NOT_FOUND);
            return true;
        }

        final PlayerData playerData = plugin.pl.getData(player);

        if (player == playerSender)
        {
            msg("You can't ride yourself. smh.", ChatColor.RED);
            return true;
        }

        if (playerData.getRideMode().equals("off") && !isAdmin(sender))
        {
            msg("That player cannot be ridden.", ChatColor.RED);
            return true;
        }

        if (playerData.getRideMode().equals("ask") && !FUtil.isExecutive(playerSender.getName()))
        {
            msg("Sent a request to the player.", ChatColor.GREEN);
            msg(player, sender.getName() + " has requested to ride you.", ChatColor.AQUA);
            msg(player, "Type " + ChatColor.GREEN + "/ride accept" + ChatColor.AQUA + " to allow the player to ride you.", ChatColor.AQUA);
            msg(player, "Type " + ChatColor.RED + "/ride deny" + ChatColor.AQUA + " to deny the player permission.", ChatColor.AQUA);
            msg(player, "Request will expire in 30 seconds.", ChatColor.AQUA);
            RIDE_REQUESTS.put(player, playerSender);

            new BukkitRunnable()
            {
                public void run()
                {
                    if (!RIDE_REQUESTS.containsKey(player))
                    {
                        return;
                    }

                    RIDE_REQUESTS.remove(player);
                    msg(playerSender, "It has been 30 seconds and " + player.getName() + " has not accepted your request.", ChatColor.RED);
                    msg(player, "Request expired.", ChatColor.RED);
                }
            }.runTaskLater(plugin, 20 * 30);
            return true;
        }

        if (player.getWorld() != playerSender.getWorld())
        {
            PaperLib.teleportAsync(playerSender, player.getLocation());
        }

        player.addPassenger(playerSender);
        msg(player, playerSender.getName() + " is now riding you, run /eject to eject them.");
        return true;
    }
}