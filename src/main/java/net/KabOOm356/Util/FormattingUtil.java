package net.KabOOm356.Util;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;

/**
 * Utility class to help with formatting.
 */
public class FormattingUtil
{
	/**
	 * Character representation of a tab.
	 */
	public static final char tabCharacter = '\t';
	
	/**
	 * Character representation of a new line (EOL).
	 */
	public static final char newLineCharacter = '\n';
	
	/**
	 * String representation of a tab.
	 */
	public static final String tab = Character.toString(tabCharacter);
	
	/**
	 * String representation of a new line (EOL).
	 */
	public static final String newLine = Character.toString(newLineCharacter);
	
	/**
	 * Appends a character behind all occurrences of another character.
	 * 
	 * @param str The string.
	 * @param character The character to append the addition character behind.
	 * @param additionCharacter The character to append.
	 * @param additions The number of addition characters to append behind the other character.
	 * 
	 * @return The string with the addition character placed behind all occurrences of the given character.
	 */
	public static String appendCharacterAfter(
			String str,
			String character,
			String additionCharacter,
			int additions)
	{
		if(str == null)
			throw new IllegalArgumentException("Parameter 'str' cannot be null!");
		if(character == null)
			throw new IllegalArgumentException("Parameter 'character' cannot be null!");
		if(additionCharacter == null)
			throw new IllegalArgumentException("Parameter 'additionCharacter' cannot be null!");
		if(additions < 0)
			throw new IllegalArgumentException("Parameter 'additions' must be greater than zero(0)!\n"
					+ "Recieved value: " + additions);
		
		// If there are no additions to be made, return the string.
		if(additions == 0)
			return str;
		
		StringBuilder strb = new StringBuilder();
		
		// Build a String that has the addition character repeated as many times as needed.
		for(int LCV = 0; LCV < additions; LCV++)
			strb.append(additionCharacter);
		
		String repeatedAdditionCharacter = strb.toString();
		
		// Replace all occurrences of the character with the character
		// and the addition character appended behind.
		str = str.replaceAll(character, character + repeatedAdditionCharacter);
		
		return str;
	}
	
	/**
	 * Appends a character behind all occurrences of another character.
	 * 
	 * @param str The string.
	 * @param character The character to append the addition character behind.
	 * @param additionCharacter The character to append.
	 * @param additions The number of addition characters to append behind the other character.
	 * 
	 * @return The string with the addition character placed behind all occurrences of the given character.
	 */
	public static String appendCharacterAfter(
			String str,
			char character,
			char additionCharacter,
			int additions)
	{
		String c = Character.toString(character);
		String ac = Character.toString(additionCharacter);
		
		return appendCharacterAfter(str, c, ac, additions);
	}
	
	/**
	 * Adds a character to all new lines.
	 * 
	 * @param str The string to add the character to.
	 * @param additionCharacter The character to add to all new lines.
	 * @param additions The number of times to add the addition character to each new line.
	 * 
	 * @return The string with the character inserted after each new line character.
	 */
	public static String addCharacterToNewLines(String str, char additionCharacter, int additions)
	{
		return appendCharacterAfter(str, newLineCharacter, additionCharacter, additions);
	}
	
	/**
	 * Adds a character to all new lines.
	 * 
	 * @param str The string to add the character to.
	 * @param additionCharacter The character to add to all new lines.
	 * @param additions The number of times to add the addition character to each new line.
	 * 
	 * @return The string with the character inserted after each new line character.
	 */
	public static String addCharacterToNewLines(String str, String additionCharacter, int additions)
	{
		return appendCharacterAfter(str, newLine, additionCharacter, additions);
	}

	/**
	 * Adds the given number of tabs to all new lines.
	 * 
	 * @param str The string to add the tabs to.
	 * @param tabs The number of tabs to add to each new line.
	 * 
	 * @return The string with the character inserted after each new line character.
	 */
	public static String addTabsToNewLines(String str, int tabs)
	{
		return addCharacterToNewLines(str, tab, tabs);
	}
	
	/**
	 *  Returns the given String in lower case with the first character capitalized.
	 * 
	 * @param str The String to convert to lower case then capitalized the first character.
	 * 
	 * @return The given String in lower case with the first character capitalized.
	 */
	public static String capitalizeFirstCharacter(String str)
	{
		if(str == null)
			throw new IllegalArgumentException("Parameter 'str' cannot be null!");
		
		if(str.length() <= 0)
			return "";
		
		str = str.toLowerCase();
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(Character.toUpperCase(str.charAt(0)));
		
		sb.append(str.substring(1));
		
		return sb.toString();
	}
	
	/**
	 * Formats the given line with the amount of time given in seconds.
	 * 
	 * @param formatLine The line to place the formatted time into.
	 * @param seconds The amount of seconds to format to the line.
	 * 
	 * @return The line with the seconds formatted into.
	 */
	public static String formatTimeRemaining(final String formatLine, final int seconds) {
		Validate.notNull(formatLine, "The format line cannot be null!");
		// Convert the seconds to hours and drop the remainder.
		int hours = TimeUtil.getHours(seconds);
		int remainingSeconds = seconds % TimeUtil.secondsPerHour;
		
		// Convert the seconds to minutes and drop the remainder.
		int minutes = TimeUtil.getMinutes(remainingSeconds);
		remainingSeconds = remainingSeconds % TimeUtil.secondsPerMinute;
		
		String line = formatLine.replaceAll("%h", ChatColor.GOLD + Integer.toString(hours) + ChatColor.WHITE);
		line = line.replaceAll("%m", ChatColor.GOLD + Integer.toString(minutes) + ChatColor.WHITE);
		line = line.replaceAll("%s", ChatColor.GOLD + Integer.toString(remainingSeconds) + ChatColor.WHITE);
		
		return line;
	}
}
