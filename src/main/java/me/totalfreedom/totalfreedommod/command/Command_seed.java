package me.totalfreedom.totalfreedommod.command;

import me.totalfreedom.totalfreedommod.rank.Rank;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@CommandPermissions(level = Rank.NON_OP, source = SourceType.BOTH)
@CommandParameters(description = "Get the seed of the world you are currently in.", usage = "/seed [world]")
public class Command_seed extends FreedomCommand
{
    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        World world;

        if (args.length > 0)
        {
            world = server.getWorld(args[0]);
            if (world == null)
            {
                msg("That world could not be found", ChatColor.RED);
                return true;
            }
        }
        else
        {
            // If the sender is a Player, use that world. Otherwise, use the overworld as a fallback.
            if (!senderIsConsole)
            {
                world = playerSender.getWorld();
            }
            else
            {
                world = server.getWorlds().get(0);
            }
        }

        // If the sender is not a Player, use the usual msg method to
        if (senderIsConsole)
        {
            msg("Seed: [" + ChatColor.GREEN + world.getSeed() + ChatColor.WHITE + "]", ChatColor.WHITE);
        }
        else
        {
            // Gets the seed for later uses
            String seed = String.valueOf(world.getSeed());

            // This is a really stupid hack to get things to play nicely, but it works so I don't give a damn
            BaseComponent[] components = {new TranslatableComponent("chat.copy.click")};
            TextComponent seedAsComponent = new TextComponent(seed);

            // Style the message like in vanilla Minecraft.
            seedAsComponent.setColor(ChatColor.GREEN.asBungee());
            seedAsComponent.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, seed));
            seedAsComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(components)));

            // Enclose the seed with brackets
            TextComponent seedString = new TextComponent("[");
            seedString.addExtra(seedAsComponent);
            seedString.addExtra("]");

            // Send the message to the player.
            TranslatableComponent response = new TranslatableComponent("commands.seed.success", seedString);
            playerSender.spigot().sendMessage(response);
        }
        return true;
    }

    @Override
    public List<String> getTabCompleteOptions(CommandSender sender, Command command, String alias, String[] args)
    {
        if (args.length == 1)
        {
            // Returns a list of worlds on the server and returns it
            List<String> worlds = new ArrayList<>();
            for (World world : server.getWorlds())
            {
                worlds.add(world.getName());
            }
            return worlds;
        }

        return null;
    }
}