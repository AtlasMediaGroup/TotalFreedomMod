package me.totalfreedom.totalfreedommod.discord;

import me.totalfreedom.totalfreedommod.TotalFreedomMod;
import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.rank.Title;
import me.totalfreedom.totalfreedommod.util.FLog;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class DiscordToMinecraftListener extends ListenerAdapter
{
    public void onMessageReceived(MessageReceivedEvent event)
    {
        String chat_channel_id = ConfigEntry.DISCORD_CHAT_CHANNEL_ID.getString();
        if (event.getMember() != null && !chat_channel_id.isEmpty() && event.getChannel().getId().equals(chat_channel_id))
        {
            if (!event.getAuthor().getId().equals(Discord.bot.getSelfUser().getId()))
            {
                Member member = event.getMember();
                String tag = getDisplay(member);
                StringBuilder message = new StringBuilder(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_AQUA + "Discord" + ChatColor.DARK_GRAY + "]");
                Message msg = event.getMessage();
                if (tag != null)
                {
                    message.append(" ").append(tag);
                }
                message.append(" ").append(ChatColor.RED).append(ChatColor.stripColor(member.getEffectiveName())).append(ChatColor.DARK_GRAY).append(":").append(ChatColor.RESET);
                ComponentBuilder builder = new ComponentBuilder(message.toString());
                if (!msg.getContentDisplay().isEmpty())
                {
                    builder.append(" ").append(ChatColor.stripColor(msg.getContentDisplay()));
                    message.append(" ").append(ChatColor.stripColor(msg.getContentDisplay())); // for logging
                }
                if (!msg.getAttachments().isEmpty())
                {
                    for (Message.Attachment attachment : msg.getAttachments())
                    {
                        attachment.getUrl();
                        builder.append(" ");
                        TextComponent text = new TextComponent(ChatColor.YELLOW + "[Media]");
                        text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, attachment.getUrl()));
                        builder.append(text);
                        message.append(" [Media]"); // for logging
                    }
                }
                for (Player player : Bukkit.getOnlinePlayers())
                {
                    if (TotalFreedomMod.getPlugin().pl.getData(player).doesDisplayDiscord())
                    {
                        player.spigot().sendMessage(builder.create());
                    }
                }
                FLog.info(message.toString());
            }
        }
    }

    public String getDisplay(Member member)
    {
        Guild server = Discord.bot.getGuildById(ConfigEntry.DISCORD_SERVER_ID.getString());
        // Server Owner
        assert server != null;
        if (member.getRoles().contains(server.getRoleById(ConfigEntry.DISCORD_SERVER_OWNER_ROLE_ID.getString())))
        {
            return Title.OWNER.getColoredTag();
        }
        // Developers
        else if (member.getRoles().contains(server.getRoleById(ConfigEntry.DISCORD_DEVELOPER_ROLE_ID.getString())))
        {
            return Title.DEVELOPER.getColoredTag();
        }
        // Executives
        else if (member.getRoles().contains(server.getRoleById(ConfigEntry.DISCORD_EXECUTIVE_ROLE_ID.getString())))
        {
            return Title.EXECUTIVE.getColoredTag();
        }
        // Senior Admins
        else if (member.getRoles().contains(server.getRoleById(ConfigEntry.DISCORD_SENIOR_ADMIN_ROLE_ID.getString())))
        {
            return Rank.SENIOR_ADMIN.getColoredTag();
        }
        // Admins
        else if (member.getRoles().contains(server.getRoleById(ConfigEntry.DISCORD_NEW_ADMIN_ROLE_ID.getString())))
        {
            return Rank.ADMIN.getColoredTag();
        }
        // Master Builders
        else if (member.getRoles().contains(server.getRoleById(ConfigEntry.DISCORD_MASTER_BUILDER_ROLE_ID.getString())))
        {
            return Title.MASTER_BUILDER.getColoredTag();
        }
        // None
        else
        {
            return null;
        }
    }
}