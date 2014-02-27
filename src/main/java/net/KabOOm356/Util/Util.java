package net.KabOOm356.Util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Database.SQLResultSet;

import org.bukkit.ChatColor;

/**
 * General utility helper class.
 */
public class Util
{	
	/**
	 * Counts the number of times a character occurs in a string.
	 * 
	 * @param s	The string.
	 * @param c	The character to look for.
	 * 
	 * @return	The number of times a character occurs in the string.
	 */
	public static int countOccurrences(String s, char c)
	{
		// If the string does not contain the character return zero.
		if(!s.contains(Character.toString(c)))
			return 0;
		
		int it = 0;
		int count = 0;
		
		while(it != s.lastIndexOf(c))
		{
			it = s.indexOf(c, it+1);
			count++;
		}
		
		return count;
	}
	
	/**
	 * Converts a Java array to an {@link ArrayList}.
	 * 
	 * @param array The array to convert to an {@link ArrayList}.
	 * 
	 * @return An {@link ArrayList} containing all elements of the given array.
	 */
	public static <T> ArrayList<T> arrayToArrayList(T[] array)
	{
		ArrayList<T> arrayList = new ArrayList<T>();
		
		for(T element : array)
			arrayList.add(element);
		
		return arrayList;
	}
	
	/**
	 * Checks if the given String is an Integer.
	 * 
	 * @param s The String to check if is an Integer.
	 * 
	 * @return True if the String can be parsed to an Integer, otherwise false.
	 */
	public static boolean isInteger(String s)
	{
		if(parseInt(s) != -1)
			return true;
		else if(s.equals("-1"))
			return true;
		return false;
	}
	
	/**
	 * Parses a String into an int.
	 * <br /><br />
	 * <b>NOTE:</b> This will return -1 if the given String cannot be parsed.
	 * 
	 * @param s The String to parse into an int.
	 * 
	 * @return The int that was parsed from the String.
	 */
	public static int parseInt(String s)
	{
		if(s == null)
			throw new IllegalArgumentException("Given string cannot be null!");
		
		int i = -1;
		
		try
		{
			i = Integer.parseInt(s);
		}
		catch(Exception ex)
		{
			return -1;
		}
		
		return i;
	}
	
	/**
	 * Converts the given array into a string representation.
	 * 
	 * @param indexes The indexes in ArrayList format.
	 * 
	 * @return The string representation of the given indexes.
	 */
	public static <T> String indexesToString(List<T> indexes)
	{
		String indexString = "";
		
		for(T index : indexes)
			indexString = indexString + index.toString() + ", ";
		
		if(!indexString.equals(""))
			indexString = indexString.substring(0, indexString.length() - 2);
		
		return indexString;
	}
	
	/**
	 * Converts the given array into a string representation.
	 * 
	 * @param indexes The ArrayList to format.
	 * @param indexColor The color to make the indexes.
	 * @param separatorColor The color to make the separator.
	 * 
	 * @return The string representation of the given indexes.
	 */
	public static <T> String indexesToString(ArrayList<T> indexes, ChatColor indexColor, ChatColor separatorColor)
	{
		String indexString = "";
		
		for(T index : indexes)
			indexString = indexString + indexColor + index.toString() + separatorColor + ", ";
		
		if(!indexString.equals(""))
			indexString = indexString.substring(0, indexString.length() - 2);
		
		return indexString;
	}
	
	/**
	 * Converts the given {@link SQLResultSet} of indexes to a string representation.
	 * 
	 * @param resultSet The indexes in {@link SQLResultSet} format.
	 * @param columnName The name of the column in the {@link SQLResultSet}.
	 * @param indexColor The color to make the indexes.
	 * @param separatorColor The color to make the separator.
	 * 
	 * @return The string representation of the given indexes.
	 * 
	 * @throws SQLException
	 */
	public static String indexesToString(SQLResultSet resultSet, String columnName, ChatColor indexColor, ChatColor separatorColor)
	{
		String indexString = "";
		
		for(ResultRow row : resultSet)
			indexString = indexString + indexColor + row.getString(columnName) + separatorColor + ", ";
		
		if(!indexString.equals(""))
			indexString = indexString.substring(0, indexString.length() - 2);
		
		return indexString;
	}
	
	/**
	 * Checks if the given String ends with the given suffix, ignoring case.
	 * 
	 * @param str The String to check.
	 * @param suffix The suffix to check the String for.
	 * 
	 * @return True if the given String ends with the given suffix, otherwise false.
	 */
	public static boolean endsWithIgnoreCase(String str, String suffix)
	{
		if(str.length() < suffix.length())
			return false;
		
		String ending = str.substring(str.length()-suffix.length());
		
		return ending.equalsIgnoreCase(suffix);
	}
	
	/**
	 * Checks if the given String begins with the given prefix, ignoring case.
	 * 
	 * @param str The String to check.
	 * @param prefix The prefix to check the String for.
	 * 
	 * @return True if the given String begins with the given prefix, otherwise false.
	 */
	public static boolean startsWithIgnoreCase(String str, String prefix)
	{
		if(str.length() < prefix.length())
			return false;
		
		int offset = (prefix.length()+1 <= str.length()) ? prefix.length() : str.length();
		
		String beginning = str.substring(0, offset);
		
		return beginning.equalsIgnoreCase(prefix);
	}
	
	/**
	 *  Returns the given String in lower case with the first character capitalized.
	 * 
	 * @param word The String to convert to lower case then capitalized the first character.
	 * 
	 * @return The given String in lower case with the first character capitalized.
	 */
	public static String capitalizeFirstCharacter(String word)
	{
		if(word.length() <= 0)
			return "";
		
		word = word.toLowerCase();
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(Character.toUpperCase(word.charAt(0)));
		
		sb.append(word.substring(1));
		
		return sb.toString();
	}
}
