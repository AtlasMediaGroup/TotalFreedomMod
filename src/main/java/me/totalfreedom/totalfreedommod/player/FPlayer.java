package me.totalfreedom.totalfreedommod.player;

import java.util.ArrayList;
import java.util.List;
import me.totalfreedom.totalfreedommod.TotalFreedomMod;
import me.totalfreedom.totalfreedommod.caging.CageData;
import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.freeze.FreezeData;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class FPlayer
{
    public static final long AUTO_PURGE_TICKS = 5L * 60L * 20L;


    private final TotalFreedomMod plugin;

    private final String name;

    private final String ip;
    //
    private final FreezeData freezeData = new FreezeData(this);
    private final CageData cageData = new CageData(this);
    private final List<LivingEntity> mobThrowerQueue = new ArrayList<>();
    private Player player;
    //
    private BukkitTask unmuteTask;
    private double fuckoffRadius = 0;
    private int messageCount = 0;
    private int totalBlockDestroy = 0;
    private int totalBlockPlace = 0;
    private int freecamDestroyCount = 0;
    private int freecamPlaceCount = 0;
    private boolean isOrbiting = false;
    private double orbitStrength = 10.0;
    private boolean mobThrowerEnabled = false;
    private EntityType mobThrowerEntity = EntityType.PIG;
    private double mobThrowerSpeed = 4.0;
    private BukkitTask mp44ScheduleTask = null;
    private boolean mp44Armed = false;
    private boolean mp44Firing = false;
    private BukkitTask lockupScheduleTask = null;
    private boolean lockedUp = false;
    private String lastMessage = "";
    private boolean inAdminchat = false;
    private boolean allCommandsBlocked = false;


    private boolean superadminIdVerified = false;
    private String lastCommand = "";
    private boolean cmdspyEnabled = false;
    private String tag = null;
    private int warningCount = 0;


    private boolean editBlocked = false;


    private boolean pvpBlocked = false;


    private boolean invSee = false;

    public FPlayer(TotalFreedomMod plugin, Player player)
    {
        this(plugin, player.getName(), FUtil.getIp(player));
    }

    private FPlayer(TotalFreedomMod plugin, String name, String ip)
    {
        this.plugin = plugin;
        this.name = name;
        this.ip = ip;
    }

    public static long getAutoPurgeTicks()
    {
        return AUTO_PURGE_TICKS;
    }

    public Player getPlayer()
    {
        if (player != null && !player.isOnline())
        {
            player = null;
        }

        if (player == null)
        {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers())
            {
                if (FUtil.getIp(onlinePlayer).equals(ip))
                {
                    player = onlinePlayer;
                    break;
                }
            }
        }

        return player;
    }

    public void setPlayer(Player player)
    {
        this.player = player;
    }

    public boolean isOrbiting()
    {
        return isOrbiting;
    }

    public void setOrbiting(boolean orbiting)
    {
        isOrbiting = orbiting;
    }

    public void startOrbiting(double strength)
    {
        this.isOrbiting = true;
        this.orbitStrength = strength;
    }

    public void stopOrbiting()
    {
        this.isOrbiting = false;
    }

    public double orbitStrength()
    {
        return orbitStrength;
    }

    public boolean isFuckOff()
    {
        return fuckoffRadius > 0;
    }

    public void setFuckoff(double radius)
    {
        this.fuckoffRadius = radius;
    }

    public void disableFuckoff()
    {
        this.fuckoffRadius = 0;
    }

    public void resetMsgCount()
    {
        this.messageCount = 0;
    }

    public int incrementAndGetMsgCount()
    {
        return this.messageCount++;
    }

    public int incrementAndGetBlockDestroyCount()
    {
        return this.totalBlockDestroy++;
    }

    public void resetBlockDestroyCount()
    {
        this.totalBlockDestroy = 0;
    }

    public int incrementAndGetBlockPlaceCount()
    {
        return this.totalBlockPlace++;
    }

    public void resetBlockPlaceCount()
    {
        this.totalBlockPlace = 0;
    }

    public int incrementAndGetFreecamDestroyCount()
    {
        return this.freecamDestroyCount++;
    }

    public void resetFreecamDestroyCount()
    {
        this.freecamDestroyCount = 0;
    }

    public int incrementAndGetFreecamPlaceCount()
    {
        return this.freecamPlaceCount++;
    }

    public void resetFreecamPlaceCount()
    {
        this.freecamPlaceCount = 0;
    }

    public void enableMobThrower(EntityType mobThrowerCreature, double mobThrowerSpeed)
    {
        this.mobThrowerEnabled = true;
        this.mobThrowerEntity = mobThrowerCreature;
        this.mobThrowerSpeed = mobThrowerSpeed;
    }

    public void disableMobThrower()
    {
        this.mobThrowerEnabled = false;
    }

    public EntityType mobThrowerCreature()
    {
        return this.mobThrowerEntity;
    }

    public double mobThrowerSpeed()
    {
        return this.mobThrowerSpeed;
    }

    public boolean mobThrowerEnabled()
    {
        return this.mobThrowerEnabled;
    }

    public void enqueueMob(LivingEntity mob)
    {
        mobThrowerQueue.add(mob);
        if (mobThrowerQueue.size() > 4)
        {
            LivingEntity oldmob = mobThrowerQueue.remove(0);
            if (oldmob != null)
            {
                oldmob.damage(500.0);
            }
        }
    }

    public void startArrowShooter(TotalFreedomMod plugin)
    {
        this.stopArrowShooter();
        this.mp44ScheduleTask = new ArrowShooter(this.player).runTaskTimer(plugin, 1L, 1L);
        this.mp44Firing = true;
    }

    public void stopArrowShooter()
    {
        if (this.mp44ScheduleTask != null)
        {
            this.mp44ScheduleTask.cancel();
            this.mp44ScheduleTask = null;
        }
        this.mp44Firing = false;
    }

    public void armMP44()
    {
        this.mp44Armed = true;
        this.stopArrowShooter();
    }

    public void disarmMP44()
    {
        this.mp44Armed = false;
        this.stopArrowShooter();
    }

    public boolean isMP44Armed()
    {
        return this.mp44Armed;
    }

    public boolean toggleMP44Firing()
    {
        this.mp44Firing = !this.mp44Firing;
        return mp44Firing;
    }

    public boolean isMuted()
    {
        return unmuteTask != null;
    }

    public void setMuted(boolean muted, int minutes)
    {
        FUtil.cancel(unmuteTask);
        plugin.mu.MUTED_PLAYERS.remove(getPlayer().getName());
        unmuteTask = null;

        if (!muted)
        {
            return;
        }

        if (getPlayer() == null)
        {
            return;
        }

        plugin.mu.MUTED_PLAYERS.add(getPlayer().getName());

        // TODO: Simplify this into a Consumer<BukkitTask> lambda?
        unmuteTask = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (getPlayer() != null)
                {
                    FUtil.adminAction(ConfigEntry.SERVER_NAME.getString(), "Unmuting " + getPlayer().getName(), false);
                    setMuted(false);
                }
                else
                {
                    FUtil.adminAction(ConfigEntry.SERVER_NAME.getString(), "Unmuting " + getName(), false);
                    plugin.mu.MUTED_PLAYERS.remove(getName());
                }
            }
        }.runTaskLater(plugin, minutes * (60L * 20L));
    }

    public void setMuted(boolean muted)
    {
        setMuted(muted, 5);
    }

    public BukkitTask getLockupScheduleID()
    {
        return this.lockupScheduleTask;
    }

    public void setLockupScheduleId(BukkitTask id)
    {
        this.lockupScheduleTask = id;
    }

    public boolean isLockedUp()
    {
        return this.lockedUp;
    }

    public void setLockedUp(boolean lockedUp)
    {
        this.lockedUp = lockedUp;
    }

    public String getLastMessage()
    {
        return lastMessage;
    }

    public void setLastMessage(String message)
    {
        this.lastMessage = message;
    }

    public void setAdminChat(boolean inAdminchat)
    {
        this.inAdminchat = inAdminchat;
    }

    public boolean inAdminChat()
    {
        return this.inAdminchat;
    }

    public boolean allCommandsBlocked()
    {
        return this.allCommandsBlocked;
    }

    public void setCommandsBlocked(boolean commandsBlocked)
    {
        this.allCommandsBlocked = commandsBlocked;
    }

    public String getLastCommand()
    {
        return lastCommand;
    }

    public void setLastCommand(String lastCommand)
    {
        this.lastCommand = lastCommand;
    }

    public void setCommandSpy(boolean enabled)
    {
        this.cmdspyEnabled = enabled;
    }

    public boolean cmdspyEnabled()
    {
        return cmdspyEnabled;
    }

    public String getTag()
    {
        return this.tag;
    }

    public void setTag(String tag)
    {
        if (tag == null)
        {
            this.tag = null;
        }
        else
        {
            this.tag = FUtil.colorize(tag) + ChatColor.WHITE;
        }
    }

    public int getWarningCount()
    {
        return this.warningCount;
    }

    public void setWarningCount(int warningCount)
    {
        this.warningCount = warningCount;
    }

    public void incrementWarnings()
    {
        this.warningCount++;

        if (this.warningCount % 2 == 0)
        {
            Player p = getPlayer();
            p.getWorld().strikeLightning(p.getLocation());
            FUtil.playerMsg(p, ChatColor.RED + "You have been warned at least twice now, make sure to read the rules at " + ConfigEntry.SERVER_BAN_URL.getString());
        }
    }

    public TotalFreedomMod getPlugin()
    {
        return plugin;
    }

    public String getName()
    {
        return name;
    }

    public String getIp()
    {
        return ip;
    }

    public BukkitTask getUnmuteTask()
    {
        return unmuteTask;
    }

    public void setUnmuteTask(BukkitTask unmuteTask)
    {
        this.unmuteTask = unmuteTask;
    }

    public FreezeData getFreezeData()
    {
        return freezeData;
    }

    public double getFuckoffRadius()
    {
        return fuckoffRadius;
    }

    public void setFuckoffRadius(double fuckoffRadius)
    {
        this.fuckoffRadius = fuckoffRadius;
    }

    public int getMessageCount()
    {
        return messageCount;
    }

    public void setMessageCount(int messageCount)
    {
        this.messageCount = messageCount;
    }

    public int getTotalBlockDestroy()
    {
        return totalBlockDestroy;
    }

    public void setTotalBlockDestroy(int totalBlockDestroy)
    {
        this.totalBlockDestroy = totalBlockDestroy;
    }

    public int getTotalBlockPlace()
    {
        return totalBlockPlace;
    }

    public void setTotalBlockPlace(int totalBlockPlace)
    {
        this.totalBlockPlace = totalBlockPlace;
    }

    public int getFreecamDestroyCount()
    {
        return freecamDestroyCount;
    }

    public void setFreecamDestroyCount(int freecamDestroyCount)
    {
        this.freecamDestroyCount = freecamDestroyCount;
    }

    public int getFreecamPlaceCount()
    {
        return freecamPlaceCount;
    }

    public void setFreecamPlaceCount(int freecamPlaceCount)
    {
        this.freecamPlaceCount = freecamPlaceCount;
    }

    public CageData getCageData()
    {
        return cageData;
    }

    public double getOrbitStrength()
    {
        return orbitStrength;
    }

    public void setOrbitStrength(double orbitStrength)
    {
        this.orbitStrength = orbitStrength;
    }

    public boolean isMobThrowerEnabled()
    {
        return mobThrowerEnabled;
    }

    public void setMobThrowerEnabled(boolean mobThrowerEnabled)
    {
        this.mobThrowerEnabled = mobThrowerEnabled;
    }

    public EntityType getMobThrowerEntity()
    {
        return mobThrowerEntity;
    }

    public void setMobThrowerEntity(EntityType mobThrowerEntity)
    {
        this.mobThrowerEntity = mobThrowerEntity;
    }

    public double getMobThrowerSpeed()
    {
        return mobThrowerSpeed;
    }

    public void setMobThrowerSpeed(double mobThrowerSpeed)
    {
        this.mobThrowerSpeed = mobThrowerSpeed;
    }

    public List<LivingEntity> getMobThrowerQueue()
    {
        return mobThrowerQueue;
    }

    public BukkitTask getMp44ScheduleTask()
    {
        return mp44ScheduleTask;
    }

    public void setMp44ScheduleTask(BukkitTask mp44ScheduleTask)
    {
        this.mp44ScheduleTask = mp44ScheduleTask;
    }

    public boolean isMp44Armed()
    {
        return mp44Armed;
    }

    public void setMp44Armed(boolean mp44Armed)
    {
        this.mp44Armed = mp44Armed;
    }

    public boolean isMp44Firing()
    {
        return mp44Firing;
    }

    public void setMp44Firing(boolean mp44Firing)
    {
        this.mp44Firing = mp44Firing;
    }

    public BukkitTask getLockupScheduleTask()
    {
        return lockupScheduleTask;
    }

    public void setLockupScheduleTask(BukkitTask lockupScheduleTask)
    {
        this.lockupScheduleTask = lockupScheduleTask;
    }

    public boolean isInAdminchat()
    {
        return inAdminchat;
    }

    public void setInAdminchat(boolean inAdminchat)
    {
        this.inAdminchat = inAdminchat;
    }

    public boolean isAllCommandsBlocked()
    {
        return allCommandsBlocked;
    }

    public void setAllCommandsBlocked(boolean allCommandsBlocked)
    {
        this.allCommandsBlocked = allCommandsBlocked;
    }

    public boolean isSuperadminIdVerified()
    {
        return superadminIdVerified;
    }

    public void setSuperadminIdVerified(boolean superadminIdVerified)
    {
        this.superadminIdVerified = superadminIdVerified;
    }

    public boolean isCmdspyEnabled()
    {
        return cmdspyEnabled;
    }

    public void setCmdspyEnabled(boolean cmdspyEnabled)
    {
        this.cmdspyEnabled = cmdspyEnabled;
    }

    public boolean isEditBlocked()
    {
        return editBlocked;
    }

    public void setEditBlocked(boolean editBlocked)
    {
        this.editBlocked = editBlocked;
    }

    public boolean isPvpBlocked()
    {
        return pvpBlocked;
    }

    public void setPvpBlocked(boolean pvpBlocked)
    {
        this.pvpBlocked = pvpBlocked;
    }

    public boolean isInvSee()
    {
        return invSee;
    }

    public void setInvSee(boolean invSee)
    {
        this.invSee = invSee;
    }

    private static class ArrowShooter extends BukkitRunnable
    {

        private final Player player;

        private ArrowShooter(Player player)
        {
            this.player = player;
        }

        @Override
        public void run()
        {
            Arrow shot = player.launchProjectile(Arrow.class);
            shot.setVelocity(shot.getVelocity().multiply(2.0));
        }
    }
}