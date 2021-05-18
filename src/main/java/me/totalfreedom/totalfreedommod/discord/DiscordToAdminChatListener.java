package me.totalfreedom.totalfreedommod.discord;

import com.google.common.base.Strings;
import me.totalfreedom.totalfreedommod.TotalFreedomMod;
import me.totalfreedom.totalfreedommod.admin.Admin;
import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.rank.Displayable;
import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.rank.Title;
import me.totalfreedom.totalfreedommod.util.FLog;
import me.totalfreedom.totalfreedommod.util.FUtil;
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

public class DiscordToAdminChatListener extends ListenerAdapter
{

    DiscordToMinecraftListener dtml = new DiscordToMinecraftListener();

    public static net.md_5.bungee.api.ChatColor getColor(Displayable display)
    {
        return display.getColor();
    }

    public void onMessageReceived(MessageReceivedEvent event)
    {
        String chat_channel_id = ConfigEntry.DISCORD_ADMINCHAT_CHANNEL_ID.getString();
        if (event.getMember() != null && !chat_channel_id.isEmpty() && event.getChannel().getId().equals(chat_channel_id) && !event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId()))
        {
            Member member = event.getMember();
            String tag = dtml.getDisplay(member);
            Message msg = event.getMessage();
            String mediamessage = ChatColor.YELLOW + "[Media]";

            StringBuilder logmessage = new StringBuilder(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_AQUA + "Discord" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET);
            String lm = ChatColor.DARK_RED + member.getEffectiveName() + " "
                    + ChatColor.DARK_GRAY + tag + ChatColor.DARK_GRAY
                    + ChatColor.WHITE + ": " + ChatColor.GOLD + FUtil.colorize(msg.getContentDisplay());
            logmessage.append(lm);

            if (!msg.getAttachments().isEmpty())
            {
                
                logmessage.append(mediamessage); // Actually for logging...

            }
            FLog.info(logmessage.toString());

            Bukkit.getOnlinePlayers().stream().filter(player -> TotalFreedomMod.getPlugin().al.isAdmin(player)).forEach(player ->
            {
                StringBuilder message = new StringBuilder(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_AQUA + "Discord" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET);

                ComponentBuilder builder = new ComponentBuilder(message.toString());

                Admin admin = TotalFreedomMod.getPlugin().al.getAdmin(player);
                String format = admin.getAcFormat();
                if (!Strings.isNullOrEmpty(format))
                {
                    Displayable display = getDisplay(member);
                    net.md_5.bungee.api.ChatColor color = getColor(display);
                    String m = format.replace("%name%", member.getEffectiveName())
                            .replace("%rank%", display.getAbbr())
                            .replace("%rankcolor%", color.toString())
                            .replace("%msg%", FUtil.colorize(msg.getContentDisplay()));
                    builder.append(FUtil.colorize(m));

                }
                else
                {
                    String m = ChatColor.DARK_RED + member.getEffectiveName() + " "
                            + ChatColor.DARK_GRAY + tag + ChatColor.DARK_GRAY
                            + ChatColor.WHITE + ": " + ChatColor.GOLD + FUtil.colorize(msg.getContentDisplay());
                    builder.append(m);
                }

                if (!msg.getAttachments().isEmpty())
                {
                    for (Message.Attachment attachment : msg.getAttachments())
                    {
                        TextComponent text = new TextComponent(mediamessage);
                        text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, attachment.getUrl()));
                        if (!msg.getContentDisplay().isEmpty())
                        {
                            builder.append(" ");
                        }
                        builder.append(text);
                    }
                }
                player.spigot().sendMessage(builder.create());

            });
        }
    }

    // Needed to display tags in custom AC messages
    public Displayable getDisplay(Member member)
    {
        Guild server = Discord.bot.getGuildById(ConfigEntry.DISCORD_SERVER_ID.getString());
        // Server Owner
        if (member.getRoles().contains(server.getRoleById(ConfigEntry.DISCORD_SERVER_OWNER_ROLE_ID.getString())))
        {
            return Title.OWNER;
        }
        // Developers
        else if (member.getRoles().contains(server.getRoleById(ConfigEntry.DISCORD_DEVELOPER_ROLE_ID.getString())))
        {
            return Title.DEVELOPER;
        }
        // Executives
        else if (member.getRoles().contains(server.getRoleById(ConfigEntry.DISCORD_EXECUTIVE_ROLE_ID.getString())))
        {
            return Title.EXECUTIVE;
        }
        // Assistant Executives
        else if (member.getRoles().contains(server.getRoleById(ConfigEntry.DISCORD_ASSISTANT_EXECUTIVE_ROLE_ID.getString())))
        {
            return Title.ASSTEXEC;
        }
        // Senior Admins
        else if (member.getRoles().contains(server.getRoleById(ConfigEntry.DISCORD_SENIOR_ADMIN_ROLE_ID.getString())))
        {
            return Rank.SENIOR_ADMIN;
        }
        // Admins
        else if (member.getRoles().contains(server.getRoleById(ConfigEntry.DISCORD_NEW_ADMIN_ROLE_ID.getString())))
        {
            return Rank.ADMIN;
        }
        // Master Builders
        else if (member.getRoles().contains(server.getRoleById(ConfigEntry.DISCORD_MASTER_BUILDER_ROLE_ID.getString())))
        {
            return Title.MASTER_BUILDER;
        }
        // OP, returning null breaks?
        else
        {
            return Rank.OP;
        }
    }
}
