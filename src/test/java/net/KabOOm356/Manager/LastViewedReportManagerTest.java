package net.KabOOm356.Manager;

import net.KabOOm356.Throwable.NoLastViewedReportException;
import net.KabOOm356.Util.BukkitUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Spy;
import org.powermock.core.classloader.annotations.PrepareForTest;
import test.test.PowerMockitoTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest({LastViewedReportManager.class, BukkitUtil.class, Bukkit.class})
public class LastViewedReportManagerTest extends PowerMockitoTest {
	private static final int noLastViewedIndex = LastViewedReportManager.noLastViewedIndex;
	private static final String lastViewedIndex = LastViewedReportManager.lastViewedIndex;
	@Spy
	private final HashMap<CommandSender, Integer> lastViewed = new HashMap<CommandSender, Integer>();
	private LastViewedReportManager manager;

	@Before
	public void setupMocks() throws Exception {
		mockStatic(Bukkit.class);
		mockStatic(BukkitUtil.class);
		whenNew(HashMap.class).withAnyArguments().thenReturn(lastViewed);
		manager = spy(new LastViewedReportManager());
	}

	@Test
	public void hasLastViewed() {
		final CommandSender sender = mock(CommandSender.class);
		final CommandSender sender2 = mock(CommandSender.class);
		final CommandSender sender3 = mock(CommandSender.class);
		lastViewed.put(sender, 1);
		lastViewed.put(sender2, 2);
		lastViewed.put(sender3, 3);
		assertTrue(manager.hasLastViewed(sender));
		assertTrue(manager.hasLastViewed(sender2));
		assertTrue(manager.hasLastViewed(sender3));
	}

	@Test
	public void hasLastViewedNotViewed() {
		assertFalse(manager.hasLastViewed(mock(CommandSender.class)));
	}

	@Test
	public void getLastViewed() throws NoLastViewedReportException {
		final CommandSender sender = mock(CommandSender.class);
		lastViewed.put(sender, 1);
		assertEquals(1, manager.getLastViewed(sender));
	}

	@Test(expected = IllegalArgumentException.class)
	public void getLastViewedNull() throws NoLastViewedReportException {
		manager.getLastViewed(null);
	}

	@Test(expected = NoLastViewedReportException.class)
	public void getLastViewedNoLastReport() throws NoLastViewedReportException {
		final CommandSender sender = mock(CommandSender.class);
		manager.getLastViewed(sender);
	}

	@Test
	public void getIndexOrLastViewedReportLast() throws NoLastViewedReportException {
		final CommandSender sender = mock(CommandSender.class);
		lastViewed.put(sender, 5);
		assertEquals(5, manager.getIndexOrLastViewedReport(sender, lastViewedIndex));
	}

	@Test
	public void getIndexOrLastViewedReportIndex() throws NoLastViewedReportException {
		final CommandSender sender = mock(CommandSender.class);
		lastViewed.put(sender, 5);
		assertEquals(8, manager.getIndexOrLastViewedReport(sender, "8"));
	}

	@Test(expected = NoLastViewedReportException.class)
	public void getIndexOrLastViewedReportNoLastReport() throws NoLastViewedReportException {
		final CommandSender sender = mock(CommandSender.class);
		manager.getIndexOrLastViewedReport(sender, lastViewedIndex);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getIndexOrLastViewedReportNullSender() throws NoLastViewedReportException {
		manager.getIndexOrLastViewedReport(null, "1");
	}

	@Test(expected = IllegalArgumentException.class)
	public void getIndexOrLastViewedReportNullIndex() throws NoLastViewedReportException {
		manager.getIndexOrLastViewedReport(mock(CommandSender.class), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getIndexOrLastViewedReportEmptyIndex() throws NoLastViewedReportException {
		manager.getIndexOrLastViewedReport(mock(CommandSender.class), "");
	}

	@Test
	public void playerViewed() {
		final CommandSender sender = mock(CommandSender.class);
		assertFalse(hasLastViewed(sender));
		manager.playerViewed(sender, 1);
		assertTrue(hasLastViewed(sender));
		assertTrue(lastViewed.containsKey(sender));
	}

	@Test
	public void removeLastViewedReport() {
		final CommandSender sender = mock(CommandSender.class);
		assertFalse(hasLastViewed(sender));
		manager.playerViewed(sender, 1);
		assertTrue(hasLastViewed(sender));
		assertTrue(lastViewed.containsKey(sender));
		manager.removeLastViewedReport(sender);
		assertFalse(hasLastViewed(sender));
		assertFalse(lastViewed.containsKey(sender));
	}

	@Test
	public void deleteIndexLast() {
		final CommandSender sender = mock(CommandSender.class);
		final CommandSender sender2 = mock(CommandSender.class);
		final CommandSender sender3 = mock(CommandSender.class);
		lastViewed.put(sender, 1);
		lastViewed.put(sender2, 2);
		lastViewed.put(sender3, 3);
		manager.deleteIndex(3);
		assertTrue(hasLastViewed(sender));
		assertTrue(hasLastViewed(sender2));
		assertFalse(hasLastViewed(sender3));
		assertEquals(1, lastViewed.get(sender).intValue());
		assertEquals(2, lastViewed.get(sender2).intValue());
	}

	@Test
	public void deleteIndexMiddle() {
		final CommandSender sender = mock(CommandSender.class);
		final CommandSender sender2 = mock(CommandSender.class);
		final CommandSender sender3 = mock(CommandSender.class);
		lastViewed.put(sender, 1);
		lastViewed.put(sender2, 2);
		lastViewed.put(sender3, 3);
		manager.deleteIndex(2);
		assertTrue(hasLastViewed(sender));
		assertFalse(hasLastViewed(sender2));
		assertTrue(hasLastViewed(sender3));
		assertEquals(1, lastViewed.get(sender).intValue());
		assertEquals(2, lastViewed.get(sender3).intValue());
	}

	@Test
	public void deleteIndexFirst() {
		final CommandSender sender = mock(CommandSender.class);
		final CommandSender sender2 = mock(CommandSender.class);
		final CommandSender sender3 = mock(CommandSender.class);
		lastViewed.put(sender, 1);
		lastViewed.put(sender2, 2);
		lastViewed.put(sender3, 3);
		manager.deleteIndex(1);
		assertFalse(hasLastViewed(sender));
		assertTrue(hasLastViewed(sender2));
		assertTrue(hasLastViewed(sender3));
		assertEquals(1, lastViewed.get(sender2).intValue());
		assertEquals(2, lastViewed.get(sender3).intValue());
	}

	@Test
	public void deleteBatchLast() {
		final CommandSender sender = mock(CommandSender.class);
		final CommandSender sender2 = mock(CommandSender.class);
		final CommandSender sender3 = mock(CommandSender.class);
		final CommandSender sender4 = mock(CommandSender.class);
		lastViewed.put(sender, 1);
		lastViewed.put(sender2, 2);
		lastViewed.put(sender3, 3);
		lastViewed.put(sender4, 4);

		final List<Integer> remainingIndexes = new ArrayList<Integer>();
		remainingIndexes.add(1);
		remainingIndexes.add(2);
		remainingIndexes.add(3);

		manager.deleteBatch(remainingIndexes);

		assertTrue(hasLastViewed(sender));
		assertTrue(hasLastViewed(sender2));
		assertTrue(hasLastViewed(sender3));
		assertFalse(hasLastViewed(sender4));

		assertEquals(1, lastViewed.get(sender).intValue());
		assertEquals(2, lastViewed.get(sender2).intValue());
		assertEquals(3, lastViewed.get(sender3).intValue());
	}

	@Test
	public void deleteBatchMiddle() {
		final CommandSender sender = mock(CommandSender.class);
		final CommandSender sender2 = mock(CommandSender.class);
		final CommandSender sender3 = mock(CommandSender.class);
		final CommandSender sender4 = mock(CommandSender.class);
		lastViewed.put(sender, 1);
		lastViewed.put(sender2, 2);
		lastViewed.put(sender3, 3);
		lastViewed.put(sender4, 4);

		final List<Integer> remainingIndexes = new ArrayList<Integer>();
		remainingIndexes.add(1);
		remainingIndexes.add(2);
		remainingIndexes.add(4);

		manager.deleteBatch(remainingIndexes);

		assertTrue(hasLastViewed(sender));
		assertTrue(hasLastViewed(sender2));
		assertFalse(hasLastViewed(sender3));
		assertTrue(hasLastViewed(sender4));

		assertEquals(1, lastViewed.get(sender).intValue());
		assertEquals(2, lastViewed.get(sender2).intValue());
		assertEquals(3, lastViewed.get(sender4).intValue());
	}

	@Test
	public void deleteBatchMiddle2() {
		final CommandSender sender = mock(CommandSender.class);
		final CommandSender sender2 = mock(CommandSender.class);
		final CommandSender sender3 = mock(CommandSender.class);
		final CommandSender sender4 = mock(CommandSender.class);
		lastViewed.put(sender, 1);
		lastViewed.put(sender2, 2);
		lastViewed.put(sender3, 3);
		lastViewed.put(sender4, 4);

		final List<Integer> remainingIndexes = new ArrayList<Integer>();
		remainingIndexes.add(1);
		remainingIndexes.add(4);

		manager.deleteBatch(remainingIndexes);

		assertTrue(hasLastViewed(sender));
		assertFalse(hasLastViewed(sender2));
		assertFalse(hasLastViewed(sender3));
		assertTrue(hasLastViewed(sender4));

		assertEquals(1, lastViewed.get(sender).intValue());
		assertEquals(2, lastViewed.get(sender4).intValue());
	}

	@Test
	public void deleteBatchFirst() {
		final CommandSender sender = mock(CommandSender.class);
		final CommandSender sender2 = mock(CommandSender.class);
		final CommandSender sender3 = mock(CommandSender.class);
		final CommandSender sender4 = mock(CommandSender.class);
		lastViewed.put(sender, 1);
		lastViewed.put(sender2, 2);
		lastViewed.put(sender3, 3);
		lastViewed.put(sender4, 4);

		final List<Integer> remainingIndexes = new ArrayList<Integer>();
		remainingIndexes.add(2);
		remainingIndexes.add(3);
		remainingIndexes.add(4);

		manager.deleteBatch(remainingIndexes);

		assertFalse(hasLastViewed(sender));
		assertTrue(hasLastViewed(sender2));
		assertTrue(hasLastViewed(sender3));
		assertTrue(hasLastViewed(sender4));

		assertEquals(1, lastViewed.get(sender2).intValue());
		assertEquals(2, lastViewed.get(sender3).intValue());
		assertEquals(3, lastViewed.get(sender4).intValue());
	}

	@Test
	public void deleteBatch() {
		final CommandSender sender = mock(CommandSender.class);
		final CommandSender sender2 = mock(CommandSender.class);
		final CommandSender sender3 = mock(CommandSender.class);
		final CommandSender sender4 = mock(CommandSender.class);
		lastViewed.put(sender, 1);
		lastViewed.put(sender2, 2);
		lastViewed.put(sender3, 3);
		lastViewed.put(sender4, 4);

		final List<Integer> remainingIndexes = new ArrayList<Integer>();
		remainingIndexes.add(2);
		remainingIndexes.add(4);

		manager.deleteBatch(remainingIndexes);

		assertFalse(hasLastViewed(sender));
		assertTrue(hasLastViewed(sender2));
		assertFalse(hasLastViewed(sender3));
		assertTrue(hasLastViewed(sender4));

		assertEquals(1, lastViewed.get(sender2).intValue());
		assertEquals(2, lastViewed.get(sender4).intValue());
	}

	/**
	 * Method to help verify if the sender has a last viewed report.
	 * The logic of the should match or be similar to the logic in {@link LastViewedReportManager#hasLastViewed(CommandSender)}.
	 * But for testing purposes we should not rely on the manager logic.
	 */
	private boolean hasLastViewed(final CommandSender sender) {
		return lastViewed.containsKey(sender) && lastViewed.get(sender) != noLastViewedIndex;
	}
}