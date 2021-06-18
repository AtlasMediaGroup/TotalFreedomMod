package me.totalfreedom.totalfreedommod.httpd.module;

import me.totalfreedom.totalfreedommod.admin.Admin;
import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.httpd.NanoHTTPD;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Module_players extends HTTPDModule
{

    public Module_players(NanoHTTPD.HTTPSession session)
    {
        super(session);
    }

    @Override
    @SuppressWarnings("unchecked")
    public NanoHTTPD.Response getResponse()
    {
        final JSONObject responseObject = new JSONObject();

        final JSONArray players = new JSONArray();
        final JSONArray onlineadmins = new JSONArray(); // updated, never queried.
        final JSONArray masterbuilders = new JSONArray();
        final JSONArray admins = new JSONArray();
        final JSONArray senioradmins = new JSONArray();
        final JSONArray developers = new JSONArray();
        final JSONArray executives = new JSONArray();

        // All online players
        for (Player player : Bukkit.getOnlinePlayers())
        {
            if (!plugin.al.isVanished(player.getName()))
            {
                players.add(player.getName());
                if (plugin.al.isAdmin(player) && !plugin.al.isAdminImpostor(player))
                {
                    onlineadmins.add(player.getName());
                }
            }
        }

        // Admins
        for (Admin admin : plugin.al.getActiveAdmins())
        {
            final String username = admin.getName();
            switch (admin.getRank())
            {
                case ADMIN:
                {
                    admins.add(username);
                    break;
                }
                case SENIOR_ADMIN:
                {
                    senioradmins.add(username);
                    break;
                }
                default:
                {
                    // Do nothing
                    break;
                }
            }
        }

        masterbuilders.addAll(plugin.pl.getMasterBuilderNames());

        // Developers
        developers.addAll(FUtil.DEVELOPER_NAMES);

        // Executives
        executives.addAll(ConfigEntry.SERVER_EXECUTIVES.getList());

        responseObject.put("players", players);
        responseObject.put("masterbuilders", masterbuilders);
        responseObject.put("admins", admins);
        responseObject.put("senioradmins", senioradmins);
        responseObject.put("developers", developers);
        responseObject.put("executives", executives);

        final NanoHTTPD.Response response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_JSON, responseObject.toString());
        response.addHeader("Access-Control-Allow-Origin", "*");
        return response;
    }
}