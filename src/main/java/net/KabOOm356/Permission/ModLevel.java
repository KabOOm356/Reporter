package net.KabOOm356.Permission;

import org.bukkit.ChatColor;

import net.KabOOm356.Util.Util;

/**
 * Enumerated type to represent a moderation level/priority.
 */
public enum ModLevel
{
	/** An unknown or unparseable {@link ModLevel}. */
	UNKNOWN("UNKNOWN", -1),
	/** The lowest {@link ModLevel}. */
	NONE("None", 0),
	/** The low {@link ModLevel}. */
	LOW("Low", 1),
	/** The normal {@link ModLevel}. */
	NORMAL("Normal", 2),
	/** The high {@link ModLevel}. */
	HIGH("High", 3);
	
	/** The common name of this ModLevel. */
	private String name;
	/** The level of this ModLevel. */
	private int level;
	
	/**
	 * ModLevel Constructor.
	 * 
	 * @param name The common name of this ModLevel.
	 * @param level The level of this ModLevel.
	 */
	ModLevel(String name, int level)
	{
		this.name = name;
		this.level = level;
	}
	
	/**
	 * Checks if the given String is a valid ModLevel.
	 * 
	 * @param modLevel The String to attempt to parse into a ModLevel.
	 * 
	 * @return True if the given String can be parsed into a valid ModLevel, otherwise false.
	 */
	public static boolean modLevelInBounds(String modLevel)
	{
		return modLevelInBounds(getModLevel(modLevel).level);
	}
	
	/**
	 * Checks if the given Integer is a valid ModLevel.
	 * <br /><br />
	 * <b>NOTE:</b> UNKNOWN ModLevel is considered out of bounds.
	 * 
	 * @param modLevel The level to check.
	 * 
	 * @return True if the given level is in bounds to be a ModLevel level, otherwise false.
	 */
	public static boolean modLevelInBounds(int modLevel)
	{
		if(modLevel >= 0 && modLevel < 4)
			return true;
		return false;
	}
	
	/**
	 * Returns the ModLevel associated with the given String, 
	 * if the given String can be parsed into a ModLevel 
	 * by name or if it can be parsed into an Integer then 
	 * parsed into a valid ModLevel by level.
	 * 
	 * @param modLevel The String representation to attempt to parse into a ModLevel.
	 * 
	 * @return The ModLevel associated with the given String, either by name or level.
	 */
	public static ModLevel getModLevel(String modLevel)
	{
		ModLevel level = getByName(modLevel);
		
		if(level == UNKNOWN)
		{
			if(Util.isInteger(modLevel))
				level = getByLevel(Util.parseInt(modLevel));
		}
		return level;
	}
	
	/**
	 * Returns a ModLevel associated with the given Integer.
	 * 
	 * @param level The level to get the associated ModLevel for.
	 * 
	 * @return The ModLevel associated with the given level if one exists, otherwise {@link ModLevel#UNKNOWN} is returned.
	 */
	public static ModLevel getByLevel(int level)
	{
		switch(level)
		{
			case 0:
				return NONE;
			case 1:
				return LOW;
			case 2:
				return NORMAL;
			case 3:
				return HIGH;
			default: return UNKNOWN;
		}
	}
	
	/**
	 * Returns a ModLevel Associated with the given String.
	 * 
	 * @param level The name of the ModLevel to return.
	 * 
	 * @return The ModLevel associated with the given name if one exists, otherwise {@link ModLevel#UNKNOWN} is returned.
	 */
	// Java 1.6 Safe
	public static ModLevel getByName(String level)
	{
		boolean isInteger = Util.isInteger(level);
		
		if(level.equalsIgnoreCase(LOW.name) || isInteger && Integer.parseInt(level) == LOW.level)
			return LOW;
		else if(level.equalsIgnoreCase(NORMAL.name) || isInteger && Integer.parseInt(level) == NORMAL.level)
			return NORMAL;
		else if(level.equalsIgnoreCase(HIGH.name) || isInteger && Integer.parseInt(level) == HIGH.level)
			return HIGH;
		else if(level.equalsIgnoreCase(NONE.name) || isInteger && Integer.parseInt(level) == NONE.level)
			return NONE;
		return UNKNOWN;
	}
	
	/**
	 * Returns a {@link ChatColor} associated with the given ModLevel.
	 * 
	 * @param level The ModLevel to get the ChatColor for.
	 * 
	 * @return A {@link ChatColor} associated with the given ModLevel.
	 */
	public static ChatColor getModLevelColor(ModLevel level)
	{
		if(level.equals(ModLevel.LOW))
			return ChatColor.BLUE;
		else if(level.equals(ModLevel.NORMAL))
			return ChatColor.GREEN;
		else if(level.equals(ModLevel.HIGH))
			return ChatColor.RED;
		return ChatColor.GRAY;
	}
	
	/**
	 * Returns a {@link ChatColor} associated with this ModLevel.
	 * 
	 * @return A {@link ChatColor} associated with this ModLevel.
	 */
	public ChatColor getColor()
	{
		return ModLevel.getModLevelColor(this);
	}
	
	/**
	 * Compares two ModLevels by their values.
	 * <br /><br />
	 * Returns zero if both values are equal, 
	 * positive if the first ModLevel's value is greater than the second ModLevel's value, 
	 * or negative if the second ModLevel's value is greater than the first's.
	 * 
	 * @param level1 The first ModLevel.
	 * @param level2 The second ModLevel.
	 * 
	 * @return The difference between the two ModLevel's values.
	 */
	public static int compareToByLevel(ModLevel level1, ModLevel level2)
	{
		return level1.compareToByLevel(level2);
	}
	
	/**
	 * Compares this ModLevel with the given ModLevel.
	 * <br /><br />
	 * Returns zero if both values are equal, 
	 * positive if this ModLevel's value is greater than the given ModLevel's value, 
	 * or negative if the given ModLevel's value is greater.
	 * 
	 * @param level The ModLevel whose value will be compared to this ModLevel's value.
	 * 
	 * @return The difference between this and the given ModLevel's values.
	 */
	public int compareToByLevel(ModLevel level)
	{
		return getLevel() - level.getLevel();
	}
	
	/**
	 * Returns the level of this ModLevel.
	 * 
	 * @return The level of this ModLevel.
	 */
	public int getLevel()
	{
		return level;
	}
	
	/**
	 * Returns the name of this ModLevel.
	 * 
	 * @return The name of this ModLevel.
	 */
	public String getName()
	{
		return name;
	}
	
	@Override
	public String toString()
	{
		return "Priority: " + name + "\nLevel: " + level;
	}
}
