package me.totalfreedom.totalfreedommod.banning;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.UUID;
import me.totalfreedom.totalfreedommod.config.IConfig;
import me.totalfreedom.totalfreedommod.util.FLog;
import org.bukkit.configuration.ConfigurationSection;

public class IndefiniteBan implements IConfig
{
    private final List<String> ips = Lists.newArrayList();
    private String username = null;
    private UUID uuid = null;
    private String reason = null;

    public IndefiniteBan()
    {
    }

    @Override
    public void loadFrom(ConfigurationSection cs)
    {
        this.username = cs.getName();
        try
        {
            String strUUID = cs.getString("uuid", null);
            if (strUUID != null)
            {
                UUID uuid = UUID.fromString(strUUID);
                this.uuid = uuid;
            }
        }
        catch (IllegalArgumentException e)
        {
            FLog.warning("Failed to load indefinite banned UUID for " + this.username + ". Make sure the UUID is in the correct format with dashes.");
        }
        this.ips.clear();
        this.ips.addAll(cs.getStringList("ips"));
        this.reason = cs.getString("reason", null);
    }

    @Override
    public void saveTo(ConfigurationSection cs)
    {
        // The indefinite ban list is only intended to be modified manually. It is not intended to save.
    }

    @Override
    public boolean isValid()
    {
        return username != null;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public UUID getUuid()
    {
        return uuid;
    }

    public void setUuid(UUID uuid)
    {
        this.uuid = uuid;
    }

    public List<String> getIps()
    {
        return ips;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }
}