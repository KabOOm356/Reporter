package net.KabOOm356.Util;

import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Database.SQLResultSet;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Utility class to handle arrays.
 */
public final class ArrayUtil
{
	public static final String defaultEntrySeparator = ", ";
	public static final String defaultElementSeparator = "=";
	
	// Prevent instantiation.
	private ArrayUtil()
	{}
	
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
	 * Converts an array to a Set.
	 * 
	 * @param array The array to convert to a Set.
	 * 
	 * @return A {@link HashSet} containing the elements of the given array.
	 */
	public static <T> Set<T> arrayToSet(T[] array)
	{
		Validate.notNull(array, "Parameter 'array' cannot be null!");
		
		Set<T> set = new HashSet<T>();
		
		for(T element : array)
		{
			set.add(element);
		}
		
		return set;
	}

	/**
	 * Converts a map to a String.
	 * The output format is: key1=value1, key2=value2
	 * 
	 * @param map The map to convert to a String.
	 * 
	 * @return A String of all the elements of the given map.
	 */
	public static <T extends Map<K, V>, K, V> String indexesToString(T map)
	{
		return indexesToString(map, defaultElementSeparator, defaultEntrySeparator);
	}
	
	/**
	 * Converts a map to a String.
	 * The output format is: key1[elementSeparator]value1[entrySeparator]key2[elementSeparator]value2
	 * 
	 * @param map The map to convert to a String.
	 * @param elementSeparator The separator to be between the elements.
	 * @param entrySeparator The separator to be between the entries.
	 * 
	 * @return A String of all elements of the given map.
	 */
	public static <T extends Map<K, V>, K, V> String indexesToString(final T map, final String elementSeparator, final String entrySeparator)
	{
		Validate.notNull(map, "Parameter 'map' cannot be null!");
		Validate.notNull(elementSeparator);
		Validate.notNull(entrySeparator);
		StringBuilder builder = new StringBuilder();
		
		for(Entry<K, V> e : map.entrySet())
		{
			builder.append(e.getKey().toString());
			builder.append(elementSeparator);
			builder.append(e.getValue().toString());
			builder.append(entrySeparator);
		}
		
		if(builder.length() > 2)
		{
			return builder.substring(0, builder.length() - 2);
		}
		
		return builder.toString();
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
		return indexesToString(array, defaultEntrySeparator);
	}
	
	/**
	 * Converts the given array into a String representation.
	 * 
	 * @param array The array to format.
	 * @param entrySeparator The separator to be between each entry.
	 * 
	 * @return The String representation of the given array.
	 */
	public static <T extends Iterable<V>, V> String indexesToString(final T array, final String entrySeparator)
	{
		Validate.notNull(array, "Parameter 'array' cannot be null!");
		Validate.notNull(entrySeparator);
		
		StringBuilder builder = new StringBuilder();
		
		for(V index : array)
		{
			builder.append(index.toString()).append(entrySeparator);
		}
		
		if(builder.length() > 2)
		{
			return builder.substring(0, builder.length() - 2);
		}
		
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
}
