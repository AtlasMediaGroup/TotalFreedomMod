package me.totalfreedom.totalfreedommod;

import me.totalfreedom.totalfreedommod.banning.IndefiniteBanList;
import me.totalfreedom.totalfreedommod.config.YamlConfig;
import me.totalfreedom.totalfreedommod.permissions.PermissionConfig;
import me.totalfreedom.totalfreedommod.punishments.PunishmentList;
import me.totalfreedom.totalfreedommod.util.FLog;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.bukkit.util.FileUtil;

import java.io.File;

public class BackupManager extends FreedomService
{

    @Override
    public void onStart()
    {
    }

    @Override
    public void onStop()
    {
    }

    public void createBackups(String file)
    {
        createBackups(file, false);
    }

    public void createAllBackups()
    {
        createBackups(TotalFreedomMod.CONFIG_FILENAME, true);
        createBackups(IndefiniteBanList.CONFIG_FILENAME);
        createBackups(PermissionConfig.PERMISSIONS_FILENAME, true);
        createBackups(PunishmentList.CONFIG_FILENAME);
        createBackups("database.db");
    }

    public void createBackups(String file, boolean onlyWeekly)
    {
        final String save = file.split("\\.")[0];
        final YamlConfig config = new YamlConfig(plugin, "backup/backup.yml", false);
        config.load();

        // Weekly
        if (!config.isLong(save + ".weekly"))
        {
            performBackup(file, "weekly");
            config.set(save + ".weekly", FUtil.getUnixTime());
        }
        else
        {
            long lastBackupWeekly = config.getLong(save + ".weekly");

            if (FUtil.parseLongOffset(lastBackupWeekly, "1w") < FUtil.getUnixTime())
            {
                performBackup(file, "weekly");
                config.set(save + ".weekly", FUtil.getUnixTime());
            }
        }

        if (onlyWeekly)
        {
            config.save();
            return;
        }

        // Daily
        if (!config.isLong(save + ".daily"))
        {
            performBackup(file, "daily");
            config.set(save + ".daily", FUtil.getUnixTime());
        }
        else
        {
            long lastBackupDaily = config.getLong(save + ".daily");

            if (FUtil.parseLongOffset(lastBackupDaily, "1d") < FUtil.getUnixTime())
            {
                performBackup(file, "daily");
                config.set(save + ".daily", FUtil.getUnixTime());
            }
        }

        config.save();
    }

    private void performBackup(String file, String type)
    {
        FLog.info("Backing up " + file + " to " + file + "." + type + ".bak");
        final File backupFolder = new File(plugin.getDataFolder(), "backup");

        if (!backupFolder.exists())
        {
            backupFolder.mkdirs();
        }

        final File oldYaml = new File(plugin.getDataFolder(), file);
        final File newYaml = new File(backupFolder, file + "." + type + ".bak");
        FileUtil.copy(oldYaml, newYaml);
    }
}
