package net.KabOOm356.Permission;

import net.KabOOm356.Util.Util;
import org.bukkit.ChatColor;

/**
 * Enumerated type to represent a moderation level/priority.
 */
public enum ModLevel {
	/**
	 * An unknown or unparseable {@link ModLevel}.
	 */
	UNKNOWN("UNKNOWN", -1, ChatColor.GRAY),
	/**
	 * The lowest {@link ModLevel}.
	 */
	NONE("None", 0, ChatColor.GRAY),
	/**
	 * The low {@link ModLevel}.
	 */
	LOW("Low", 1, ChatColor.BLUE),
	/**
	 * The normal {@link ModLevel}.
	 */
	NORMAL("Normal", 2, ChatColor.GREEN),
	/**
	 * The high {@link ModLevel}.
	 */
	HIGH("High", 3, ChatColor.RED);

	/**
	 * The common name of this ModLevel.
	 */
	private final String name;
	/**
	 * The level of this ModLevel.
	 */
	private final int level;
	/**
	 * The color of this ModLevel.
	 */
	private final ChatColor color;

	/**
	 * ModLevel Constructor.
	 *
	 * @param name  The common name of this ModLevel.
	 * @param level The level of this ModLevel.
	 */
	ModLevel(final String name, final int level, final ChatColor color) {
		this.name = name;
		this.level = level;
		this.color = color;
	}

	/**
	 * Checks if the given String is a valid ModLevel.
	 *
	 * @param modLevel The String to attempt to parse into a ModLevel.
	 * @return True if the given String can be parsed into a valid ModLevel, otherwise false.
	 */
	public static boolean modLevelInBounds(final String modLevel) {
		return modLevelInBounds(getModLevel(modLevel).level);
	}

	/**
	 * Checks if the given Integer is a valid ModLevel.
	 * <br /><br />
	 * <b>NOTE:</b> UNKNOWN ModLevel is considered out of bounds.
	 *
	 * @param modLevel The level to check.
	 * @return True if the given level is in bounds to be a ModLevel level, otherwise false.
	 */
	public static boolean modLevelInBounds(final int modLevel) {
		if (modLevel >= 0 && modLevel < 4)
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
	 * @return The ModLevel associated with the given String, either by name or level.
	 */
	public static ModLevel getModLevel(final String modLevel) {
		ModLevel level = getByName(modLevel);

		if (level == UNKNOWN) {
			if (Util.isInteger(modLevel))
				level = getByLevel(Util.parseInt(modLevel));
		}
		return level;
	}

	/**
	 * Returns a ModLevel associated with the given Integer.
	 *
	 * @param level The level to get the associated ModLevel for.
	 * @return The ModLevel associated with the given level if one exists, otherwise {@link ModLevel#UNKNOWN} is returned.
	 */
	public static ModLevel getByLevel(final int level) {
		switch (level) {
			case 0:
				return NONE;
			case 1:
				return LOW;
			case 2:
				return NORMAL;
			case 3:
				return HIGH;
			default:
				return UNKNOWN;
		}
	}

	/**
	 * Returns a ModLevel Associated with the given String.
	 *
	 * @param level The name of the ModLevel to return.
	 * @return The ModLevel associated with the given name if one exists, otherwise {@link ModLevel#UNKNOWN} is returned.
	 */
	// Java 1.6 Safe
	public static ModLevel getByName(final String level) {
		final boolean isInteger = Util.isInteger(level);

		if (level.equalsIgnoreCase(LOW.name) || isInteger && Integer.parseInt(level) == LOW.level)
			return LOW;
		else if (level.equalsIgnoreCase(NORMAL.name) || isInteger && Integer.parseInt(level) == NORMAL.level)
			return NORMAL;
		else if (level.equalsIgnoreCase(HIGH.name) || isInteger && Integer.parseInt(level) == HIGH.level)
			return HIGH;
		else if (level.equalsIgnoreCase(NONE.name) || isInteger && Integer.parseInt(level) == NONE.level)
			return NONE;
		return UNKNOWN;
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
	 * @return The difference between the two ModLevel's values.
	 */
	public static int compareToByLevel(final ModLevel level1, final ModLevel level2) {
		return level1.compareToByLevel(level2);
	}

	/**
	 * Returns a {@link ChatColor} associated with this ModLevel.
	 *
	 * @return A {@link ChatColor} associated with this ModLevel.
	 */
	public ChatColor getColor() {
		return this.color;
	}

	/**
	 * Compares this ModLevel with the given ModLevel.
	 * <br /><br />
	 * Returns zero if both values are equal,
	 * positive if this ModLevel's value is greater than the given ModLevel's value,
	 * or negative if the given ModLevel's value is greater.
	 *
	 * @param level The ModLevel whose value will be compared to this ModLevel's value.
	 * @return The difference between this and the given ModLevel's values.
	 */
	public int compareToByLevel(final ModLevel level) {
		return getLevel() - level.getLevel();
	}

	/**
	 * Returns the level of this ModLevel.
	 *
	 * @return The level of this ModLevel.
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * Returns the name of this ModLevel.
	 *
	 * @return The name of this ModLevel.
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "Priority: " + name + "\nLevel: " + level;
	}
}
