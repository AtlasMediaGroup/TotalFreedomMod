package me.totalfreedom.totalfreedommod.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import me.totalfreedom.totalfreedommod.player.PlayerData;
import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = Rank.ADMIN, source = SourceType.BOTH)
@CommandParameters(description = "Manage notes for a player", usage = "/<command> <name> <list | add <note> | remove <id> | clear>")
public class Command_notes extends FreedomCommand
{

    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (args.length < 2)
        {
            return false;
        }

        PlayerData playerData;

        final Player player = getPlayer(args[0]);
        if (player == null)
        {
            final PlayerData entry = plugin.pl.getData(args[0]);

            if (entry == null)
            {
                msg("Can't find that user. If target is not logged in, make sure that you spelled the name exactly.");
                return true;
            }

            playerData = plugin.pl.getData(entry.getName());
        }
        else
        {
            playerData = plugin.pl.getData(player);
        }

        switch (args[1])
        {
            case "list":
            {
                final StringBuilder noteList = new StringBuilder();
                noteList.append(ChatColor.GREEN).append("Player notes for ").append(playerData.getName()).append(":");
                int id = 1;
                for (String note : playerData.getNotes())
                {
                    String noteLine = id + ". " + note;
                    noteList.append("\n").append(ChatColor.GOLD).append(noteLine);
                    id++;
                }
                msg(noteList.toString());
                return true;
            }

            case "add":
            {
                if (args.length < 3)
                {
                    return false;
                }
                String note = sender.getName() + ": " + StringUtils.join(ArrayUtils.subarray(args, 2, args.length), " ");
                playerData.addNote(note);
                plugin.pl.save(playerData);
                msg("Note added.", ChatColor.GREEN);
                return true;
            }

            case "remove":
            {
                if (args.length < 3)
                {
                    return false;
                }

                int id;
                try
                {
                    id = Integer.parseInt(args[2]);
                }
                catch (NumberFormatException e)
                {
                    msg("Invalid number: " + args[2], ChatColor.RED);
                    return true;
                }

                id--;

                if (playerData.removeNote(id))
                {
                    plugin.pl.save(playerData);
                    msg("Note removed.");
                }
                else
                {
                    msg("No note with the ID of " + args[2] + " exists.", ChatColor.RED);
                }
                return true;
            }

            case "clear":
            {
                int count = playerData.getNotes().size();
                playerData.clearNotes();
                plugin.pl.save(playerData);
                msg("Cleared " + count + " notes.", ChatColor.GREEN);
                return true;
            }

            default:
            {
                return false;
            }
        }
    }

    @Override
    public List<String> getTabCompleteOptions(CommandSender sender, Command command, String alias, String[] args)
    {
        if (args.length == 1)
        {
            return FUtil.getPlayerList();
        }
        else if (args.length == 2)
        {
            return Arrays.asList("list", "add", "remove", "clear");
        }
        else if (args.length > 2 && (args[1].equals("add")))
        {
            return FUtil.getPlayerList();
        }
        return Collections.emptyList();
    }
}