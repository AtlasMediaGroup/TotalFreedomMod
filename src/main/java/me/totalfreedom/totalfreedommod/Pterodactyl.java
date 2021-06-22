package me.totalfreedom.totalfreedommod;

import com.mattmalec.pterodactyl4j.Permission;
import com.mattmalec.pterodactyl4j.PowerAction;
import com.mattmalec.pterodactyl4j.PteroAction;
import com.mattmalec.pterodactyl4j.PteroBuilder;
import com.mattmalec.pterodactyl4j.application.entities.ApplicationUser;
import com.mattmalec.pterodactyl4j.application.entities.PteroApplication;
import com.mattmalec.pterodactyl4j.application.managers.UserAction;
import com.mattmalec.pterodactyl4j.client.entities.ClientServer;
import com.mattmalec.pterodactyl4j.client.entities.ClientSubuser;
import com.mattmalec.pterodactyl4j.client.entities.PteroClient;
import joptsimple.internal.Strings;
import me.totalfreedom.totalfreedommod.admin.Admin;
import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FLog;

public class Pterodactyl extends FreedomService
{
    public final String URL = ConfigEntry.PTERO_URL.getString();
    private final String ADMIN_KEY = ConfigEntry.PTERO_ADMIN_KEY.getString();
    private final String CLIENT_KEY = ConfigEntry.PTERO_CLIENT_KEY.getString();
    private final String IDENTIFIER = ConfigEntry.PTERO_SERVER_UUID.getString();
    private final PteroApplication adminAPI = PteroBuilder.createApplication(URL, ADMIN_KEY);
    private final PteroClient clientAPI = PteroBuilder.createClient(URL, CLIENT_KEY);
    private final ClientServer server = clientAPI.retrieveServerByIdentifier(IDENTIFIER).execute();

    private boolean enabled = !Strings.isNullOrEmpty(URL);

    public void onStart()
    {
    }

    public void onStop()
    {
    }

    public void updateAccountStatus(Admin admin)
    {
        String id = admin.getPteroID();

        if (Strings.isNullOrEmpty(id) || !enabled)
        {
            return;
        }

        if (!admin.isActive() || admin.getRank() != Rank.SENIOR_ADMIN)
        {
            FLog.debug("Disabling Pterodactyl account");
            removeAccountFromServer(id);
            return;
        }

        FLog.debug("Enabling Pterodactyl account");
        addAccountToServer(id);
    }

    public String createAccount(String username, String password)
    {
        UserAction action = adminAPI.getUserManager().createUser()
                .setUserName(username)
                .setEmail(username.toLowerCase() + "@" + ConfigEntry.PTERO_DEFAULT_EMAIL_DOMAIN.getString())
                .setFirstName(username)
                .setLastName("\u200E") // Required - make it appear empty
                .setPassword(password);

        return action.execute().getId();
    }

    public void deleteAccount(String id)
    {
        ApplicationUser username = adminAPI.retrieveUserById(id).execute();
        PteroAction<Void> action = adminAPI.getUserManager().deleteUser(username);
        action.execute();
    }

    public void addAccountToServer(String id)
    {
        ApplicationUser username = adminAPI.retrieveUserById(id).execute();
        String email = username.getEmail();
        PteroAction<ClientServer> server = clientAPI.retrieveServerByIdentifier(IDENTIFIER);
        server.execute().getSubuserManager().createUser()
                .setEmail(email)
                .setPermissions(Permission.CONTROL_PERMISSIONS).execute();
    }

    public void removeAccountFromServer(String id)
    {
        ApplicationUser username = adminAPI.retrieveUserById(id).execute();
        PteroAction<ClientServer> server = clientAPI.retrieveServerByIdentifier(IDENTIFIER);
        ClientSubuser clientSubuser = server.execute().getSubuser(username.getUUID()).retrieve().execute();
        server.execute().getSubuserManager().deleteUser(clientSubuser).execute();
    }

    public void stopServer()
    {
        clientAPI.setPower(server, PowerAction.STOP).execute();
    }

    public void restartServer()
    {
        clientAPI.setPower(server, PowerAction.RESTART).execute();
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
}