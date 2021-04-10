package me.totalfreedom.totalfreedommod.command;

import java.util.List;
import me.totalfreedom.totalfreedommod.bridge.CoreProtectBridge;
import me.totalfreedom.totalfreedommod.player.PlayerData;
import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = Rank.OP, source = SourceType.ONLY_IN_GAME)
@CommandParameters(description = "Block inspector tool for operators", usage = "/<command> [history] <page>", aliases = "ins")
public class Command_inspect extends FreedomCommand
{

    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (args.length == 0)
        {
            PlayerData playerData = plugin.pl.getData(playerSender);
            playerData.setInspect(!playerData.hasInspection());
            plugin.pl.save(playerData);
            msg("Block inspector " + (playerData.hasInspection() ? "enabled." : "disabled."));
            return true;
        }

        if (args[0].equalsIgnoreCase("history"))
        {
            int pageIndex = 1;

            if (args.length >= 2)
            {
                try
                {
                    pageIndex = Integer.parseInt(args[1]);
                }
                catch (NumberFormatException e)
                {
                    msg("Invalid number", ChatColor.RED);
                }
            }

            FUtil.PaginationList<String> paged = CoreProtectBridge.HISTORY_MAP.get(playerSender);
            if (paged != null)
            {
                if (pageIndex < 1 || pageIndex > paged.getPageCount())
                {
                    msg("Not a valid page number", ChatColor.RED);
                    return true;
                }

                msg("---- " + net.md_5.bungee.api.ChatColor.of("#30ade4") + "Block Inspector" + ChatColor.WHITE + " ---- ", ChatColor.WHITE);

                List<String> page = paged.getPage(pageIndex);
                for (String entries : page)
                {
                    msg(entries);
                }

                msg("Page " + pageIndex + "/" + paged.getPageCount() + " | To index through the pages, type " + net.md_5.bungee.api.ChatColor.of("#30ade4") + "/ins history <page>", ChatColor.WHITE);
                return true;
            }
        }
        return true;
    }
}