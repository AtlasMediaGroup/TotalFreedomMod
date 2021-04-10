package me.totalfreedom.totalfreedommod.command;

import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = Rank.OP, source = SourceType.BOTH, cooldown = 30)
@CommandParameters(description = "OP everyone on the server.", usage = "/<command>")
public class Command_opall extends FreedomCommand
{

    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        FUtil.adminAction(sender.getName(), "Opping all players on the server", false);

        for (Player player : server.getOnlinePlayers())
        {
            if (!player.isOp())
            {
                player.setOp(true);
                msg(player, YOU_ARE_OP);
                plugin.rm.updateDisplay(player);
            }
        }

        return true;
    }
}