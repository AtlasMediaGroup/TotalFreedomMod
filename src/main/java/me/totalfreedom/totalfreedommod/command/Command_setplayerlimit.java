package me.totalfreedom.totalfreedommod.command;

import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = Rank.ADMIN, source = SourceType.BOTH)
@CommandParameters(description = "Sets a specific player's WorldEdit block modification limit to the default limit or to a custom limit.", usage = "/<command> <player> [limit]", aliases = "setpl,spl")
public class Command_setplayerlimit extends FreedomCommand
{
    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        int amount;
        if (args.length > 0)
        {
            Player player = Bukkit.getPlayer(args[0]);
            if (player == null)
            {
                msg(PLAYER_NOT_FOUND);
                return true;
            }

            if (args.length == 2)
            {
                try
                {
                    amount = Math.max(-1, Math.min(plugin.web.getMaxLimit(), Integer.parseInt(args[1])));
                }
                catch (NumberFormatException ex)
                {
                    msg("Invalid number: " + args[1], ChatColor.RED);
                    return true;
                }
            }
            else
            {
                amount = plugin.web.getDefaultLimit();
            }
        }
        else
        {
            return false;
        }
        boolean success = false;
        Player player = Bukkit.getPlayer(args[0]);
        try
        {
            plugin.web.setLimit(player, amount);
            success = true;
        }
        catch (NoClassDefFoundError | NullPointerException ex)
        {
            msg("WorldEdit is not enabled on this server.");
        }
        if (success)
        {
            assert player != null;
            FUtil.adminAction(sender.getName(), "Setting " + player.getName() + "'s WorldEdit block modification limit to " + amount + ".", true);
        }
        return true;
    }
}