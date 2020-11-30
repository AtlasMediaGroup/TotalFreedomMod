package me.totalfreedom.totalfreedommod.command;

import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.rank.Rank;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = Rank.SENIOR_ADMIN, source = SourceType.BOTH)
@CommandParameters(description = "It's like forcekill, but with even more force - Allows you to kill multiple players at once.", usage = "/<command> <players>", aliases = "genocide")
public class Command_massmurder extends FreedomCommand
{

    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (!ConfigEntry.SERVER_OWNERS.getStringList().contains(playerSender.getName()))
        {
            return noPerms();
        }

        if (args.length < 1)
        {
            return false;
        }

        int count = 0;
        for (String name : args)
        {
            Player player = getPlayer(name);
            if (player != null)
            {
                player.setHealth(0);
                count++;
            }
        }

        msg("Mass murdered a school of " + count);

        return true;
    }
}
