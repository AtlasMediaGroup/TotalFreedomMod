package me.totalfreedom.totalfreedommod.httpd.module;

import me.totalfreedom.totalfreedommod.admin.Admin;
import me.totalfreedom.totalfreedommod.admin.AdminList;
import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.httpd.NanoHTTPD;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Collection;

public class Module_list extends HTTPDModule
{

    public Module_list(NanoHTTPD.HTTPSession session)
    {
        super(session);
    }

    @Override
    public NanoHTTPD.Response getResponse()
    {
        if (params.get("json") != null && params.get("json").equals("true"))
        {
            final JSONObject responseObject = new JSONObject();

            final JSONArray owners = new JSONArray();
            final JSONArray executives = new JSONArray();
            final JSONArray developers = new JSONArray();
            final JSONArray senioradmins = new JSONArray();
            final JSONArray admins = new JSONArray();
            final JSONArray masterbuilders = new JSONArray();
            final JSONArray operators = new JSONArray();
            final JSONArray imposters = new JSONArray();

            for (Player player : Bukkit.getOnlinePlayers())
            {
                if (plugin.al.isVanished(player.getName()))
                {
                    continue;
                }

                if (isImposter(player))
                {
                    imposters.add(player.getName());
                }

                if (plugin.pl.getData(player).isMasterBuilder())
                {
                    masterbuilders.add(player.getName());
                }

                if (FUtil.DEVELOPER_NAMES.contains(player.getName()))
                {
                    developers.add(player.getName());
                }

                if (ConfigEntry.SERVER_EXECUTIVES.getList().contains(player.getName()) && !FUtil.DEVELOPERS.contains(player.getName()))
                {
                    executives.add(player.getName());
                }

                if (ConfigEntry.SERVER_OWNERS.getList().contains(player.getName()))
                {
                    owners.add(player.getName());
                }

                if (!plugin.al.isAdmin(player) && hasSpecialTitle(player))
                {
                    operators.add(player.getName());
                }

                if (hasSpecialTitle(player) && plugin.al.isAdmin(player) && !plugin.al.isVanished(player.getName()))
                {
                    Admin admin = plugin.al.getAdmin(player);
                    switch (admin.getRank())
                    {
                        case ADMIN:
                        {
                            admins.add(player.getName());
                            break;
                        }
                        case SENIOR_ADMIN:
                        {
                            senioradmins.add(player.getName());
                            break;
                        }
                        default:
                        {
                            // Do nothing
                            break;
                        }
                    }
                }
            }

            // for future refernce - any multi-worded ranks are to be delimited by underscores in the json; eg. senior_admins
            responseObject.put("owners", owners);
            responseObject.put("executives", executives);
            responseObject.put("developers", developers);
            responseObject.put("senior_admins", senioradmins);
            responseObject.put("admins", admins);
            responseObject.put("master_builders", masterbuilders);
            responseObject.put("operators", operators);
            responseObject.put("imposters", imposters);
            responseObject.put("online", FUtil.getFakePlayerCount());
            responseObject.put("max", server.getMaxPlayers());

            final NanoHTTPD.Response response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_JSON, responseObject.toString());
            response.addHeader("Access-Control-Allow-Origin", "*");
            return response;
        }
        else
        {
            final StringBuilder body = new StringBuilder();

            final Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

            int count = onlinePlayers.size() - AdminList.vanished.size();
            body.append("<p>There are ").append(count < 0 ? 0 : count).append("/")
                    .append(Bukkit.getMaxPlayers()).append(" players online:</p>\r\n");

            body.append("<ul>\r\n");

            for (Player player : onlinePlayers)
            {
                if (plugin.al.isVanished(player.getName()))
                {
                    continue;
                }
                String tag = plugin.rm.getDisplay(player).getTag();
                body.append("<li>").append(tag).append(player.getName()).append("</li>\r\n");
            }

            body.append("</ul>\r\n");

            return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_HTML, body.toString());
        }
    }

    public boolean isImposter(Player player)
    {
        return plugin.al.isAdminImpostor(player) || plugin.pl.isPlayerImpostor(player);
    }

    public boolean hasSpecialTitle(Player player)
    {
        return !FUtil.DEVELOPERS.contains(player.getUniqueId().toString()) && !ConfigEntry.SERVER_EXECUTIVES.getList().contains(player.getName()) && !ConfigEntry.SERVER_OWNERS.getList().contains(player.getName());
    }

    @Override
    public String getTitle()
    {
        return "TotalFreedom - Online Players";
    }
}
