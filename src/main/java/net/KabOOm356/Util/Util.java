package net.KabOOm356.Util;

import java.sql.SQLException;
import java.util.ArrayList;

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
	 * @param str	The string.
	 * @param character	The character to look for.
	 * 
	 * @return	The number of times a character occurs in the string.
	 */
	public static int countOccurrences(String str, char character)
	{
		String c = Character.toString(character);
		
		return countOccurrences(str, c);
	}
	
	/**
	 * Counts the number of times a substring occurs in a string.
	 * 
	 * @param str	The string.
	 * @param needle	The String to look for.
	 * 
	 * @return	The number of times a character occurs in the string.
	 */
	public static int countOccurrences(String str, String needle)
	{
		if(str == null)
			throw new IllegalArgumentException("Parameter 'str' cannot be null!");
		if(needle == null)
			throw new IllegalArgumentException("Parameter 'needle' cannot be null!");
		
		// If the string does not contain the character return zero.
		if(!str.contains(needle))
			return 0;
		
		int it = 0;
		int count = 0;
		
		while(it != str.lastIndexOf(needle))
		{
			it = str.indexOf(needle, it+1);
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
		if(array == null)
			throw new IllegalArgumentException("Parameter 'array' cannot be null!");
		
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
	 * @param str The String to parse into an int.
	 * 
	 * @return The int that was parsed from the String.
	 */
	public static int parseInt(String str)
	{
		if(str == null)
			throw new IllegalArgumentException("Parameter 'str' cannot be null!");
		
		int i = -1;
		
		try
		{
			i = Integer.parseInt(str);
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
	 * @param array The array to format.
	 * 
	 * @return The string representation of the given indexes.
	 */
	public static <T extends Iterable<V>, V> String indexesToString(T array)
	{
		if(array == null)
			throw new IllegalArgumentException("Parameter 'array' cannot be null!");
		
		StringBuilder builder = new StringBuilder();
		
		for(V index : array)
			builder.append(index.toString()).append(", ");
		
		if(builder.length() > 2)
			return builder.substring(0, builder.length() - 2);
		
		return builder.toString();
	}
	
	/**
	 * Converts the given array into a string representation.
	 * 
	 * @param array The array to format.
	 * @param indexColor The color to make the indexes.
	 * @param separatorColor The color to make the separator.
	 * 
	 * @return The string representation of the given array.
	 */
	public static <T extends Iterable<V>, V> String indexesToString(T array, ChatColor indexColor, ChatColor separatorColor)
	{
		if(array == null)
			throw new IllegalArgumentException("Parameter 'array' cannot be null!");
		if(indexColor == null)
			throw new IllegalArgumentException("Parameter 'indexColor' cannot be null!");
		if(separatorColor == null)
			throw new IllegalArgumentException("Parameter 'separatorColor' cannot be null!");
		
		StringBuilder builder = new StringBuilder();
		
		for(V index : array)
		{
			builder.append(indexColor);
			builder.append(index.toString());
			builder.append(separatorColor);
			builder.append(", ");
		}
		
		if(builder.length() > 2)
			return builder.substring(0, builder.length() - 2);
		
		return builder.toString();
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
		if(resultSet == null)
			throw new IllegalArgumentException("Parameter 'resultSet' cannot be null!");
		if(columnName == null)
			throw new IllegalArgumentException("Parameter 'columnName' cannot be null!");
		if(indexColor == null)
			throw new IllegalArgumentException("Parameter 'indexColor' cannot be null!");
		if(separatorColor == null)
			throw new IllegalArgumentException("Parameter 'separatorColor' cannot be null!");
		
		StringBuilder indexString = new StringBuilder();
		
		for(ResultRow row : resultSet)
		{
			indexString.append(indexColor);
			indexString.append(row.getString(columnName));
			indexString.append(separatorColor);
			indexString.append(", ");
		}
		
		if(indexString.length() > 2)
			return indexString.substring(0, indexString.length() - 2);
		
		return indexString.toString();
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
		if(str == null)
			throw new IllegalArgumentException("Parameter 'str' cannot be null!");
		if(suffix == null)
			throw new IllegalArgumentException("Parameter 'suffix' cannot be null!");
		
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
		if(str == null)
			throw new IllegalArgumentException("Parameter 'str' cannot be null!");
		if(prefix == null)
			throw new IllegalArgumentException("Parameter 'prefix' cannot be null!");
		
		if(str.length() < prefix.length())
			return false;
		
		int offset = (prefix.length()+1 <= str.length()) ? prefix.length() : str.length();
		
		String beginning = str.substring(0, offset);
		
		return beginning.equalsIgnoreCase(prefix);
	}
}
