package net.KabOOm356.Util;

import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Database.SQLResultSet;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;

import java.util.*;
import java.util.Map.Entry;

/**
 * Utility class to handle arrays.
 */
public final class ArrayUtil {
	public static final String defaultEntrySeparator = ", ";
	public static final String defaultElementSeparator = "=";

	// Prevent instantiation.
	private ArrayUtil() {
	}

	/**
	 * Converts a Java array to an {@link ArrayList}.
	 *
	 * @param array The array to convert to an {@link ArrayList}.
	 * @return An {@link ArrayList} containing all elements of the given array.
	 */
	public static <T> ArrayList<T> arrayToArrayList(final T[] array) {
		if (array == null) {
			throw new IllegalArgumentException("Parameter 'array' cannot be null!");
		}

		final ArrayList<T> arrayList = new ArrayList<T>();
		Collections.addAll(arrayList, array);

		return arrayList;
	}

	/**
	 * Converts an array to a Set.
	 *
	 * @param array The array to convert to a Set.
	 * @return A {@link HashSet} containing the elements of the given array.
	 */
	public static <T> Set<T> arrayToSet(final T[] array) {
		Validate.notNull(array, "Parameter 'array' cannot be null!");

		final Set<T> set = new HashSet<T>();
		Collections.addAll(set, array);

		return set;
	}

	/**
	 * Converts a map to a String.
	 * The output format is: key1=value1, key2=value2
	 *
	 * @param map The map to convert to a String.
	 * @return A String of all the elements of the given map.
	 */
	public static <T extends Map<K, V>, K, V> String indexesToString(final T map) {
		return indexesToString(map, defaultElementSeparator, defaultEntrySeparator);
	}

	/**
	 * Converts a map to a String.
	 * The output format is: key1[elementSeparator]value1[entrySeparator]key2[elementSeparator]value2
	 *
	 * @param map              The map to convert to a String.
	 * @param elementSeparator The separator to be between the elements.
	 * @param entrySeparator   The separator to be between the entries.
	 * @return A String of all elements of the given map.
	 */
	public static <T extends Map<K, V>, K, V> String indexesToString(final T map, final String elementSeparator, final String entrySeparator) {
		Validate.notNull(map, "Parameter 'map' cannot be null!");
		Validate.notNull(elementSeparator);
		Validate.notNull(entrySeparator);
		final StringBuilder builder = new StringBuilder();

		for (final Entry<K, V> e : map.entrySet()) {
			builder.append(e.getKey().toString());
			builder.append(elementSeparator);
			builder.append(e.getValue().toString());
			builder.append(entrySeparator);
		}

		if (builder.length() > entrySeparator.length() && builder.lastIndexOf(entrySeparator) != -1) {
			return builder.substring(0, builder.lastIndexOf(entrySeparator));
		}

		return builder.toString();
	}

	/**
	 * Converts the given array into a string representation.
	 *
	 * @param array The array to format.
	 * @return The string representation of the given indexes.
	 */
	public static <T extends Iterable<V>, V> String indexesToString(final T array) {
		return indexesToString(array, "", defaultEntrySeparator);
	}

	public static <V, T extends Iterable<V>> String indexesToString(final T array, final String indexPrefix, final String indexPostfix) {
		Validate.notNull(array, "Parameter 'array' cannot be null!");
		Validate.notNull(indexPrefix);
		Validate.notNull(indexPostfix);

		final StringBuilder builder = new StringBuilder();

		for (final V index : array) {
			builder.append(indexPrefix).append(index.toString()).append(indexPostfix);
		}

		if (builder.length() > indexPostfix.length() && builder.lastIndexOf(indexPostfix) != -1) {
			return builder.substring(0, builder.lastIndexOf(indexPostfix));
		}

		return builder.toString();
	}

	/**
	 * Converts the given array into a string representation.
	 *
	 * @param array          The array to format.
	 * @param indexColor     The color to make the indexes.
	 * @param separatorColor The color to make the separator.
	 * @return The string representation of the given array.
	 */
	public static <T extends Iterable<V>, V> String indexesToString(final T array, final ChatColor indexColor, final ChatColor separatorColor) {
		Validate.notNull(indexColor);
		Validate.notNull(separatorColor);
		return indexesToString(array, indexColor.toString(), separatorColor + defaultEntrySeparator);
	}

	/**
	 * Converts the given {@link SQLResultSet} of indexes to a string representation.
	 *
	 * @param resultSet      The indexes in {@link SQLResultSet} format.
	 * @param columnName     The name of the column in the {@link SQLResultSet}.
	 * @param indexColor     The color to make the indexes.
	 * @param separatorColor The color to make the separator.
	 * @return The string representation of the given indexes.
	 */
	public static String indexesToString(final SQLResultSet resultSet, final String columnName, final ChatColor indexColor, final ChatColor separatorColor) {
		Validate.notNull(resultSet);
		Validate.notNull(columnName);
		Validate.notNull(indexColor);
		Validate.notNull(separatorColor);

		final ArrayList<String> array = new ArrayList<String>();
		for (final ResultRow row : resultSet) {
			array.add(row.getString(columnName));
		}
		return indexesToString(array, indexColor.toString(), separatorColor + defaultEntrySeparator);
	}
}
