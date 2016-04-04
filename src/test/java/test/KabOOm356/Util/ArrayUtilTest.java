package test.KabOOm356.Util;

import net.KabOOm356.Util.ArrayUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ArrayUtilTest
{
	@Test(expected = IllegalArgumentException.class)
	public void testArrayToArrayListNull()
	{
		ArrayUtil.arrayToArrayList(null);
	}
	
	@Test
	public void testArrayToArrayList()
	{
		Integer[] emptyIntArray = new Integer[0];
		ArrayList<Integer> intList = new ArrayList<Integer>();
		
		assertEquals(intList, ArrayUtil.arrayToArrayList(emptyIntArray));
		
		Integer[] oneIntArray = {2};
		intList.add(2);
		assertEquals(intList, ArrayUtil.arrayToArrayList(oneIntArray));
		
		intList.clear();
		intList.add(5);
		assertNotEquals(intList, ArrayUtil.arrayToArrayList(oneIntArray));
		
		Integer[] intArray = {1,2,3,4,5};
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
	public void testArrayToSetNull()
	{
		ArrayUtil.arrayToSet(null);
	}
	
	@Test
	public void testArrayToSet()
	{
		Integer[] emptyIntArray = new Integer[0];
		HashSet<Integer> intSet = new HashSet<Integer>();
		
		assertEquals(intSet, ArrayUtil.arrayToSet(emptyIntArray));
		
		Integer[] oneIntArray = {2};
		intSet.add(2);
		assertEquals(intSet, ArrayUtil.arrayToSet(oneIntArray));
		
		intSet.clear();
		intSet.add(5);
		assertNotEquals(intSet, ArrayUtil.arrayToSet(oneIntArray));
		
		Integer[] intArray = {1,2,3,4,5};
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
		ArrayUtil.indexesToString((ArrayList<String>)null);
	}
	
	@Test
	public void testIndexesToString()
	{
		ArrayList<Integer> list = new ArrayList<Integer>();
		
		String value = ArrayUtil.indexesToString(list);
		assertEquals("", value);
		
		list.add(1);
		
		value = ArrayUtil.indexesToString(list);
		assertEquals("1", value);
		
		for(int LCV = 2; LCV < 10; LCV++)
		{
			list.add(LCV);
		}
		
		value = ArrayUtil.indexesToString(list);
		assertEquals("1, 2, 3, 4, 5, 6, 7, 8, 9", value);
	}
}
