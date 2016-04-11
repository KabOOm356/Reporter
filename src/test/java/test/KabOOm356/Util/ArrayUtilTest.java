package test.KabOOm356.Util;

import net.KabOOm356.Database.ResultRow;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Util.ArrayUtil;
import org.bukkit.ChatColor;
import org.junit.Test;

import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.*;

public class ArrayUtilTest {
	@Test(expected = IllegalArgumentException.class)
	public void testArrayToArrayListNull() {
		ArrayUtil.arrayToArrayList(null);
	}

	@Test
	public void testArrayToArrayList() {
		Integer[] emptyIntArray = new Integer[0];
		ArrayList<Integer> intList = new ArrayList<Integer>();

		assertEquals(intList, ArrayUtil.arrayToArrayList(emptyIntArray));

		Integer[] oneIntArray = {2};
		intList.add(2);
		assertEquals(intList, ArrayUtil.arrayToArrayList(oneIntArray));

		intList.clear();
		intList.add(5);
		assertNotEquals(intList, ArrayUtil.arrayToArrayList(oneIntArray));

		Integer[] intArray = {1, 2, 3, 4, 5};
		intList.clear();
		intList.add(1);
		intList.add(2);
		intList.add(3);
		intList.add(4);
		intList.add(5);
		assertEquals(intList, ArrayUtil.arrayToArrayList(intArray));

		intList.clear();
		intList.add(3);
		intList.add(2);
		intList.add(1);
		intList.add(5);
		intList.add(4);
		assertNotEquals(intList, ArrayUtil.arrayToArrayList(intArray));

		intArray[3] = 2;
		intList.clear();
		intList.add(1);
		intList.add(2);
		intList.add(3);
		intList.add(2);
		intList.add(5);
		assertEquals(intList, ArrayUtil.arrayToArrayList(intArray));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testArrayToSetNull() {
		ArrayUtil.arrayToSet(null);
	}

	@Test
	public void testArrayToSet() {
		Integer[] emptyIntArray = new Integer[0];
		HashSet<Integer> intSet = new HashSet<Integer>();

		assertEquals(intSet, ArrayUtil.arrayToSet(emptyIntArray));

		Integer[] oneIntArray = {2};
		intSet.add(2);
		assertEquals(intSet, ArrayUtil.arrayToSet(oneIntArray));

		intSet.clear();
		intSet.add(5);
		assertNotEquals(intSet, ArrayUtil.arrayToSet(oneIntArray));

		Integer[] intArray = {1, 2, 3, 4, 5};
		intSet.clear();
		intSet.add(1);
		intSet.add(2);
		intSet.add(3);
		intSet.add(4);
		intSet.add(5);
		assertEquals(intSet, ArrayUtil.arrayToSet(intArray));

		intSet.clear();
		intSet.add(3);
		intSet.add(2);
		intSet.add(1);
		intSet.add(5);
		intSet.add(4);
		assertEquals(intSet, ArrayUtil.arrayToSet(intArray));

		intArray[3] = 2;
		intSet.clear();
		intSet.add(1);
		intSet.add(2);
		intSet.add(3);
		intSet.add(2);
		intSet.add(5);
		assertEquals(intSet, ArrayUtil.arrayToSet(intArray));

		intArray[0] = 2;
		intArray[1] = 2;
		intArray[2] = 4;
		intArray[4] = 1;
		intSet.clear();
		intSet.add(1);
		intSet.add(2);
		intSet.add(4);
		assertEquals(intSet, ArrayUtil.arrayToSet(intArray));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIndexesToStringNull() {
		ArrayUtil.indexesToString((ArrayList<String>) null);
	}

	@Test
	public void testIndexesToString() {
		ArrayList<Integer> list = new ArrayList<Integer>();

		String value = ArrayUtil.indexesToString(list);
		assertEquals("", value);

		list.add(1);

		value = ArrayUtil.indexesToString(list);
		assertEquals("1", value);

		for (int LCV = 2; LCV < 10; LCV++) {
			list.add(LCV);
		}

		value = ArrayUtil.indexesToString(list);
		assertEquals("1, 2, 3, 4, 5, 6, 7, 8, 9", value);
	}

	@Test
	public void testIndexesToStringMapNull() {
		try {
			ArrayUtil.indexesToString((Map) null);
			fail("Expected exception not thrown!");
		} catch (final IllegalArgumentException e) {
			// Expected exception
		}

		try {
			ArrayUtil.indexesToString((Map) null, "", "");
			fail("Expected exception not thrown!");
		} catch (final IllegalArgumentException e) {
			// Expected exception
		}

		try {
			ArrayUtil.indexesToString(new HashMap(), null, "");
			fail("Expected exception not thrown!");
		} catch (final IllegalArgumentException e) {
			// Expected exception
		}

		try {
			ArrayUtil.indexesToString(new HashMap(), "", null);
			fail("Expected exception not thrown!");
		} catch (final IllegalArgumentException e) {
			// Expected exception
		}
	}

	@Test
	public void testIndexesToStringMap() {
		final Map<String, String> testMap = new LinkedHashMap<String, String>(16, 0.75f, true);
		testMap.put("FirstKey", "FirstValue");
		testMap.put("SecondKey", "SecondValue");
		testMap.put("LastKey", "LastValue");

		String returned = ArrayUtil.indexesToString(new HashMap());
		assertEquals("", returned);

		returned = ArrayUtil.indexesToString(testMap);
		assertEquals("FirstKey=FirstValue, SecondKey=SecondValue, LastKey=LastValue", returned);

		returned = ArrayUtil.indexesToString(testMap, ":", " ");
		assertEquals("FirstKey:FirstValue SecondKey:SecondValue LastKey:LastValue", returned);
	}

	@Test
	public void testIndexToStringChatColor() {
		final ArrayList<String> testArray = new ArrayList<String>();
		testArray.add("FirstElement");
		testArray.add("SecondElement");
		testArray.add("LastElement");

		String returned = ArrayUtil.indexesToString(new ArrayList(), ChatColor.RED, ChatColor.WHITE);
		String expected = "";
		assertEquals(expected, returned);

		returned = ArrayUtil.indexesToString(testArray, ChatColor.RED, ChatColor.WHITE);
		expected = ChatColor.RED + "FirstElement" + ChatColor.WHITE + ", " + ChatColor.RED + "SecondElement" + ChatColor.WHITE + ", " + ChatColor.RED + "LastElement";
		assertEquals(expected, returned);
	}

	@Test
	public void testIndexesToStringSQLResult() throws SQLException {
		final SQLResultSet resultSet = new SQLResultSet();
		ResultRow resultRow = new ResultRow();
		resultRow.put("TestColumn", 1);
		resultSet.add(resultRow);
		resultRow = new ResultRow();
		resultRow.put("TestColumn", 3);
		resultSet.add(resultRow);
		resultRow = new ResultRow();
		resultRow.put("TestColumn", "LastValue");
		resultSet.add(resultRow);

		String returned = ArrayUtil.indexesToString(new SQLResultSet(), "TestColumn", ChatColor.RED, ChatColor.WHITE);
		String expected = "";
		assertEquals(expected, returned);

		returned = ArrayUtil.indexesToString(resultSet, "TestColumn", ChatColor.RED, ChatColor.WHITE);
		expected = ChatColor.RED + "1" + ChatColor.WHITE + ", " + ChatColor.RED + "3" + ChatColor.WHITE + ", " + ChatColor.RED + "LastValue";
		assertEquals(expected, returned);
	}
}
