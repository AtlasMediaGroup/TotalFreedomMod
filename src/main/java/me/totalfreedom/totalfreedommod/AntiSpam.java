package me.totalfreedom.totalfreedommod;

import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.player.FPlayer;
import me.totalfreedom.totalfreedommod.util.FSync;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class AntiSpam extends FreedomService
{

    public static final int MSG_PER_CYCLE = 8;
    public static final int TICKS_PER_CYCLE = 2 * 10;
    //
    public BukkitTask cycleTask = null;
    private Map<Player, Integer> muteCounts = new HashMap<>();

    @Override
    public void onStart()
    {
        new BukkitRunnable()
        {

            @Override
            public void run()
            {
                cycle();
            }
        }.runTaskTimer(plugin, TICKS_PER_CYCLE, TICKS_PER_CYCLE);
    }

    @Override
    public void onStop()
    {
        FUtil.cancel(cycleTask);
    }

    private void cycle()
    {
        for (Player player : server.getOnlinePlayers())
        {
            final FPlayer playerdata = plugin.pl.getPlayer(player);

            // TODO: Move each to their own section
            playerdata.resetMsgCount();
            playerdata.resetBlockDestroyCount();
            playerdata.resetBlockPlaceCount();
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event)
    {
        final Player player = event.getPlayer();

        if (plugin.al.isAdmin(player))
        {
            return;
        }

        final FPlayer playerdata = plugin.pl.getPlayerSync(player);
        int count = muteCounts.getOrDefault(player, 0);
        int minutes = ConfigEntry.ANTISPAM_MINUTES.getInteger();

        // Check for spam
        if (playerdata.incrementAndGetMsgCount() > MSG_PER_CYCLE && !playerdata.isMuted())
        {
            count++;
            muteCounts.put(player, count);

            int time = count * minutes;
            playerdata.setMuted(true, time);

            FSync.bcastMsg(String.format("%s has automatically been muted for %d minutes for spamming chat.",
                    player.getName(),
                    time),
                    ChatColor.RED);

            playerdata.resetMsgCount();
            event.setCancelled(true);
        }
        else if (playerdata.incrementAndGetMsgCount() > MSG_PER_CYCLE / 2)
        {
            FUtil.playerMsg(player, "Please refrain from spamming chat.", ChatColor.GRAY);
            event.setCancelled(true);
        }

    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        String command = event.getMessage();
        final Player player = event.getPlayer();
        final FPlayer fPlayer = plugin.pl.getPlayer(player);
        fPlayer.setLastCommand(command);

        if (fPlayer.allCommandsBlocked())
        {
            FUtil.playerMsg(player, "Your commands have been blocked by an admin.", ChatColor.RED);
            event.setCancelled(true);
            return;
        }

        if (plugin.al.isAdmin(player))
        {
            return;
        }

        if (fPlayer.incrementAndGetMsgCount() > MSG_PER_CYCLE)
        {
            FUtil.bcastMsg(player.getName() + " was automatically kicked for spamming commands.", ChatColor.RED);
            plugin.ae.autoEject(player, "Kicked for spamming commands.");

            fPlayer.resetMsgCount();
            event.setCancelled(true);
        }
    }
}