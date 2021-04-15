package me.totalfreedom.totalfreedommod.command;

import me.totalfreedom.totalfreedommod.player.PlayerData;
import me.totalfreedom.totalfreedommod.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(source = SourceType.BOTH,level = Rank.OP)
@CommandParameters(description = "Pay OtherPlayers with your coins",usage = "/<command> <playername> <coins>")
public class Command_paycoins extends FreedomCommand
{

    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if(args.length < 2)
        {
            return false;
        }
        if(!isStringInt(args[1]))
        {
            sender.sendMessage(ChatColor.RED + "The Amount of coins to sent must be an Integer.");
            return true;
        }
        int coins_to_send = 0;

        try
        {
            coins_to_send = Integer.parseInt(args[1]);
        }catch (Exception ez)
        { }


        if(!senderIsConsole)
        {
            PlayerData playerData = plugin.pl.getData(playerSender);
            Player target_player = Bukkit.getPlayerExact(args[0]);
            PlayerData target_playerdata = plugin.pl.getData(target_player);

            if(!(coins_to_send >= playerData.getCoins()))
            {
                playerSender.sendMessage(ChatColor.RED + "You don't have " + coins_to_send + " coins!");
                return true;
            }

            if(target_player == null)
            {
                playerSender.sendMessage(ChatColor.RED + "Player " + args[0] + " not found!");
                return true;
            }

            if(target_player.getUniqueId().toString().equalsIgnoreCase(playerSender.getUniqueId().toString()))
            {
                playerSender.sendMessage(ChatColor.YELLOW + "You cannot send coins to your self!");
                return true;
            }

            playerData.removeCoins(coins_to_send);
            target_playerdata.addCoins(coins_to_send);

            plugin.pl.save(playerData);
            plugin.pl.save(target_playerdata);

            playerSender.sendMessage(ChatColor.YELLOW + "You have sent " + coins_to_send + " to " + args[0] + "!");
            return true;
        }else
        {
            Player target_player = Bukkit.getPlayerExact(args[0]);
            PlayerData target_playerdata = plugin.pl.getData(target_player);

            target_playerdata.addCoins(coins_to_send);
            plugin.pl.save(target_playerdata);
            sender.sendMessage(ChatColor.YELLOW + "You have sent " + coins_to_send + " to " + args[0] + "!");
        }
        return true;
    }

    private boolean isStringInt(String str)
    {
        try
        {
            Integer.parseInt(str);
            return true;
        }catch (Exception ez)
        { }

        return false;
    }
}
