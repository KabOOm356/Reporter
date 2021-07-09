package net.KabOOm356.Permission;

import org.junit.Test;

import static org.junit.Assert.*;

public class ModLevelTest {
	@Test
	public void testModLevelInBoundsString() {
		assertTrue(ModLevel.modLevelInBounds("None"));
		assertTrue(ModLevel.modLevelInBounds("NOne"));
		assertTrue(ModLevel.modLevelInBounds("NoNe"));
		assertTrue(ModLevel.modLevelInBounds("0"));
		assertTrue(ModLevel.modLevelInBounds("Low"));
		assertTrue(ModLevel.modLevelInBounds("LOw"));
		assertTrue(ModLevel.modLevelInBounds("LoW"));
		assertTrue(ModLevel.modLevelInBounds("1"));
		assertTrue(ModLevel.modLevelInBounds("Normal"));
		assertTrue(ModLevel.modLevelInBounds("NoRmAl"));
		assertTrue(ModLevel.modLevelInBounds("2"));
		assertTrue(ModLevel.modLevelInBounds("High"));
		assertTrue(ModLevel.modLevelInBounds("HigH"));
		assertTrue(ModLevel.modLevelInBounds("3"));

		assertFalse(ModLevel.modLevelInBounds("-1"));
		assertFalse(ModLevel.modLevelInBounds("No"));
		assertFalse(ModLevel.modLevelInBounds("dne"));
		assertFalse(ModLevel.modLevelInBounds(""));
		assertFalse(ModLevel.modLevelInBounds("4"));
	}

	@Test
	public void testModLevelInBoundsInt() {
		assertTrue(ModLevel.modLevelInBounds(0));
		assertTrue(ModLevel.modLevelInBounds(1));
		assertTrue(ModLevel.modLevelInBounds(2));
		assertTrue(ModLevel.modLevelInBounds(3));

		assertFalse(ModLevel.modLevelInBounds(-1));
		assertFalse(ModLevel.modLevelInBounds(4));
	}

	@Test
	public void testGetByLevel() {
		assertEquals(ModLevel.UNKNOWN, ModLevel.getByLevel(-1));
		assertEquals(ModLevel.NONE, ModLevel.getByLevel(0));
		assertEquals(ModLevel.LOW, ModLevel.getByLevel(1));
		assertEquals(ModLevel.NORMAL, ModLevel.getByLevel(2));
		assertEquals(ModLevel.HIGH, ModLevel.getByLevel(3));
		assertEquals(ModLevel.UNKNOWN, ModLevel.getByLevel(4));
	}

	@Test
	public void testGetByName() {
		assertEquals(ModLevel.UNKNOWN, ModLevel.getByName("Unknown"));
		assertEquals(ModLevel.NONE, ModLevel.getByName("None"));
		assertEquals(ModLevel.LOW, ModLevel.getByName("Low"));
		assertEquals(ModLevel.NORMAL, ModLevel.getByName("Normal"));
		assertEquals(ModLevel.HIGH, ModLevel.getByName("High"));
		assertEquals(ModLevel.UNKNOWN, ModLevel.getByName(""));
	}

	@Test
	public void testCompareToByLevelModLevelModLevel() {
		assertEquals(0, ModLevel.compareToByLevel(ModLevel.UNKNOWN, ModLevel.UNKNOWN));
		assertTrue(ModLevel.compareToByLevel(ModLevel.UNKNOWN, ModLevel.NONE) < 0);
		assertTrue(ModLevel.compareToByLevel(ModLevel.UNKNOWN, ModLevel.LOW) < 0);
		assertTrue(ModLevel.compareToByLevel(ModLevel.UNKNOWN, ModLevel.NORMAL) < 0);
		assertTrue(ModLevel.compareToByLevel(ModLevel.UNKNOWN, ModLevel.HIGH) < 0);

		assertTrue(ModLevel.compareToByLevel(ModLevel.NONE, ModLevel.UNKNOWN) > 0);
		assertEquals(0, ModLevel.compareToByLevel(ModLevel.NONE, ModLevel.NONE));
		assertTrue(ModLevel.compareToByLevel(ModLevel.NONE, ModLevel.LOW) < 0);
		assertTrue(ModLevel.compareToByLevel(ModLevel.NONE, ModLevel.NORMAL) < 0);
		assertTrue(ModLevel.compareToByLevel(ModLevel.NONE, ModLevel.HIGH) < 0);

		assertTrue(ModLevel.compareToByLevel(ModLevel.LOW, ModLevel.UNKNOWN) > 0);
		assertTrue(ModLevel.compareToByLevel(ModLevel.LOW, ModLevel.NONE) > 0);
		assertEquals(0, ModLevel.compareToByLevel(ModLevel.LOW, ModLevel.LOW));
		assertTrue(ModLevel.compareToByLevel(ModLevel.LOW, ModLevel.NORMAL) < 0);
		assertTrue(ModLevel.compareToByLevel(ModLevel.LOW, ModLevel.HIGH) < 0);

		assertTrue(ModLevel.compareToByLevel(ModLevel.NORMAL, ModLevel.UNKNOWN) > 0);
		assertTrue(ModLevel.compareToByLevel(ModLevel.NORMAL, ModLevel.NONE) > 0);
		assertTrue(ModLevel.compareToByLevel(ModLevel.NORMAL, ModLevel.LOW) > 0);
		assertEquals(0, ModLevel.compareToByLevel(ModLevel.NORMAL, ModLevel.NORMAL));
		assertTrue(ModLevel.compareToByLevel(ModLevel.NORMAL, ModLevel.HIGH) < 0);

		assertTrue(ModLevel.compareToByLevel(ModLevel.HIGH, ModLevel.UNKNOWN) > 0);
		assertTrue(ModLevel.compareToByLevel(ModLevel.HIGH, ModLevel.NONE) > 0);
		assertTrue(ModLevel.compareToByLevel(ModLevel.HIGH, ModLevel.LOW) > 0);
		assertTrue(ModLevel.compareToByLevel(ModLevel.HIGH, ModLevel.NORMAL) > 0);
		assertEquals(0, ModLevel.compareToByLevel(ModLevel.HIGH, ModLevel.HIGH));
	}

	@Test
	public void testCompareToByLevelModLevel() {
		assertEquals(0, ModLevel.UNKNOWN.compareToByLevel(ModLevel.UNKNOWN));
		assertTrue(ModLevel.UNKNOWN.compareToByLevel(ModLevel.NONE) < 0);
		assertTrue(ModLevel.UNKNOWN.compareToByLevel(ModLevel.LOW) < 0);
		assertTrue(ModLevel.UNKNOWN.compareToByLevel(ModLevel.NORMAL) < 0);
		assertTrue(ModLevel.UNKNOWN.compareToByLevel(ModLevel.HIGH) < 0);

		assertTrue(ModLevel.NONE.compareToByLevel(ModLevel.UNKNOWN) > 0);
		assertEquals(0, ModLevel.NONE.compareToByLevel(ModLevel.NONE));
		assertTrue(ModLevel.NONE.compareToByLevel(ModLevel.LOW) < 0);
		assertTrue(ModLevel.NONE.compareToByLevel(ModLevel.NORMAL) < 0);
		assertTrue(ModLevel.NONE.compareToByLevel(ModLevel.HIGH) < 0);

		assertTrue(ModLevel.LOW.compareToByLevel(ModLevel.UNKNOWN) > 0);
		assertTrue(ModLevel.LOW.compareToByLevel(ModLevel.NONE) > 0);
		assertEquals(0, ModLevel.LOW.compareToByLevel(ModLevel.LOW));
		assertTrue(ModLevel.LOW.compareToByLevel(ModLevel.NORMAL) < 0);
		assertTrue(ModLevel.LOW.compareToByLevel(ModLevel.HIGH) < 0);

		assertTrue(ModLevel.NORMAL.compareToByLevel(ModLevel.UNKNOWN) > 0);
		assertTrue(ModLevel.NORMAL.compareToByLevel(ModLevel.NONE) > 0);
		assertTrue(ModLevel.NORMAL.compareToByLevel(ModLevel.LOW) > 0);
		assertEquals(0, ModLevel.NORMAL.compareToByLevel(ModLevel.NORMAL));
		assertTrue(ModLevel.NORMAL.compareToByLevel(ModLevel.HIGH) < 0);

		assertTrue(ModLevel.HIGH.compareToByLevel(ModLevel.UNKNOWN) > 0);
		assertTrue(ModLevel.HIGH.compareToByLevel(ModLevel.NONE) > 0);
		assertTrue(ModLevel.HIGH.compareToByLevel(ModLevel.LOW) > 0);
		assertTrue(ModLevel.HIGH.compareToByLevel(ModLevel.NORMAL) > 0);
		assertEquals(0, ModLevel.HIGH.compareToByLevel(ModLevel.HIGH));
	}
}
