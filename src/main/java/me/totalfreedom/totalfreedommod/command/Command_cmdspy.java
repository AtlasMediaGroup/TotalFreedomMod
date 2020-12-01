package me.totalfreedom.totalfreedommod.command;

import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.staff.StaffMember;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = Rank.ADMIN, source = SourceType.ONLY_IN_GAME)
@CommandParameters(description = "Allows Admins to view commands executed by lower ranks.", usage = "/<command>", aliases = "commandspy")
public class Command_cmdspy extends FreedomCommand
{

    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        StaffMember staffMember = plugin.sl.getAdmin(playerSender);
        staffMember.setCommandSpy(!staffMember.getCommandSpy());
        msg("CommandSpy " + (staffMember.getCommandSpy() ? "enabled." : "disabled."));
        plugin.sl.save(staffMember);
        plugin.sl.updateTables();

        return true;
    }
}
