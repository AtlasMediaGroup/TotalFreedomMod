package me.totalfreedom.totalfreedommod.command;

import java.util.HashMap;
import java.util.Map;
import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@CommandPermissions(level = Rank.ADMIN, source = SourceType.BOTH)
@CommandParameters(description = "Restart the server", usage = "/<command> [reason]")
public class Command_restart extends FreedomCommand
{
    private static final Map<CommandSender, String> RESTART_CONFIRM = new HashMap<>();

    @Override
    public boolean run(final CommandSender sender, final Player playerSender, final Command cmd, final String commandLabel, final String[] args, final boolean senderIsConsole)
    {
        if (!plugin.ptero.isEnabled())
        {
            msg("Pterodactyl integration is currently disabled.", ChatColor.RED);
            return true;
        }

        String reason = "Server is restarting!";

        if (args.length != 0)
        {
            reason = StringUtils.join(args, " ");
        }

        if (sender.getName().equals("CONSOLE"))
        {
            restart(reason);
            return true;
        }
        else if (RESTART_CONFIRM.containsKey(sender))
        {
            restart(RESTART_CONFIRM.get(sender));
            return true;
        }

        msg("Warning: You're about to restart the server. Type /restart again to confirm you want to do this.");

        RESTART_CONFIRM.put(sender, reason);
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (RESTART_CONFIRM.containsKey(sender))
                {
                    RESTART_CONFIRM.remove(sender);
                    msg("Restart request expired.");
                }
            }
        }.runTaskLater(plugin, 15 * 20);
        return true;
    }

    public void restart(String reason)
    {
        FUtil.bcastMsg("Server is restarting!", ChatColor.LIGHT_PURPLE);

        for (Player player : server.getOnlinePlayers())
        {
            player.kickPlayer(ChatColor.LIGHT_PURPLE + reason);
        }

        RESTART_CONFIRM.remove(sender);
        plugin.ptero.restartServer();
    }
}
