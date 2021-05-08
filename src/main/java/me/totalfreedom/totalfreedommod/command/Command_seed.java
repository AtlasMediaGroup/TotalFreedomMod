package me.totalfreedom.totalfreedommod.command;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandParameters(description = "Checks the seed for the current world, or for the world provided.", usage = "/<command> [world]")
public class Command_seed extends FreedomCommand {
    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole) {
        if (args.length == 1) {
            World world = Bukkit.getWorld(args[0]);
            if (world == null) {
                sender.sendMessage("World " + args[0] +" not found!");
                return true;
            }
            sender.sendMessage(String.valueOf(world.getSeed()));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("To check the world seed from console, you must provide a world name!");
            return true;
        }

        Player player = (Player) sender;
        World world = player.getWorld();
        player.sendMessage(String.valueOf(world.getSeed()));
        return true;
    }
}
