package me.totalfreedom.totalfreedommod.rank;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

public enum Rank implements Displayable
{
    IMPOSTOR("an", "Impostor", Type.PLAYER, "Imp", ChatColor.YELLOW, null, false),
    NON_OP("a", "Non-Op", Type.PLAYER, "", ChatColor.WHITE, null, false),
    OP("an", "Operator", Type.PLAYER, "OP", ChatColor.GREEN, null, false),
    NEW_ADMIN("a", "Moderator", Type.STAFF, "Mod", ChatColor.DARK_GREEN, org.bukkit.ChatColor.DARK_GREEN, true),
    SENIOR_ADMIN("an", "Administrator", Type.STAFF, "Admin", ChatColor.GOLD, org.bukkit.ChatColor.GOLD, true),
    NEW_ADMIN_CONSOLE("the", "Console", Type.STAFF_CONSOLE, "Console", ChatColor.DARK_PURPLE, null, false),
    SENIOR_CONSOLE("the", "Console", Type.STAFF_CONSOLE, "Console", ChatColor.DARK_PURPLE, null, false);
    @Getter
    private final Type type;
    @Getter
    private final String name;
    @Getter
    private final String abbr;
    private final String determiner;
    @Getter
    private final String tag;
    @Getter
    private final String coloredTag;
    @Getter
    private final ChatColor color;
    @Getter
    private final org.bukkit.ChatColor teamColor;
    @Getter
    private final boolean hasTeam;

    Rank(String determiner, String name, Type type, String abbr, ChatColor color, org.bukkit.ChatColor teamColor, Boolean hasTeam)
    {
        this.type = type;
        this.name = name;
        this.abbr = abbr;
        this.determiner = determiner;
        this.tag = abbr.isEmpty() ? "" : "[" + abbr + "]";
        this.coloredTag = abbr.isEmpty() ? "" : ChatColor.DARK_GRAY + "[" + color + abbr + ChatColor.DARK_GRAY + "]" + color;
        this.color = color;
        this.teamColor = teamColor;
        this.hasTeam = hasTeam;
    }

    @Override
    public String getColoredName()
    {
        return color + name;
    }

    @Override
    public String getColoredLoginMessage()
    {
        return determiner + " " + color + name;
    }

    @Override
    public boolean hasTeam()
    {
        return hasTeam;
    }

    @Override
    public String getAbbr()
    {
        return abbr;
    }

    public boolean isConsole()
    {
        return getType() == Type.STAFF_CONSOLE;
    }

    public int getLevel()
    {
        return ordinal();
    }

    public boolean isAtLeast(Rank rank)
    {
        if (getLevel() < rank.getLevel())
        {
            return false;
        }

        if (!hasConsoleVariant() || !rank.hasConsoleVariant())
        {
            return true;
        }

        return getConsoleVariant().getLevel() >= rank.getConsoleVariant().getLevel();
    }

    public boolean isStaff()
    {
        return getType() == Type.STAFF || getType() == Type.STAFF_CONSOLE;
    }

    public boolean hasConsoleVariant()
    {
        return getConsoleVariant() != null;
    }

    public Rank getConsoleVariant()
    {
        switch (this)
        {
            case NEW_ADMIN:
            case NEW_ADMIN_CONSOLE:
                return NEW_ADMIN_CONSOLE;
            case SENIOR_ADMIN:
            case SENIOR_CONSOLE:
                return SENIOR_CONSOLE;
            default:
                return null;
        }
    }

    public static Rank findRank(String string)
    {
        try
        {
            return Rank.valueOf(string.toUpperCase());
        }
        catch (Exception ignored)
        {
        }

        return Rank.NON_OP;
    }

    public static enum Type
    {

        PLAYER,
        STAFF,
        STAFF_CONSOLE;

        public boolean isStaff()
        {
            return this != PLAYER;
        }
    }
}