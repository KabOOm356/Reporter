package net.KabOOm356.Service;

import net.KabOOm356.Throwable.IndexNotANumberException;
import net.KabOOm356.Throwable.NoLastViewedReportException;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import test.test.service.ServiceTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class LastViewedReportServiceTest extends ServiceTest {
	private static final int noLastViewedIndex = LastViewedReportService.noLastViewedIndex;
	private static final String lastViewedIndex = LastViewedReportService.lastViewedIndex;
	private Map<CommandSender, Integer> lastViewed;
	private LastViewedReportService manager;

	@Override
	@Before
	public void setupMocks() throws Exception {
		super.setupMocks();

		lastViewed = super.getLastViewedHandler();
		manager = spy(new LastViewedReportService(getModule()));
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
		when(sender.getName()).thenReturn("testPlayer");
		manager.getLastViewed(sender);
	}

	@Test
	public void getIndexOrLastViewedReportLast() throws NoLastViewedReportException, IndexNotANumberException {
		final CommandSender sender = mock(CommandSender.class);
		lastViewed.put(sender, 5);
		assertEquals(5, manager.getIndexOrLastViewedReport(sender, lastViewedIndex));
	}

	@Test
	public void getIndexOrLastViewedReportIndex() throws NoLastViewedReportException, IndexNotANumberException {
		final CommandSender sender = mock(CommandSender.class);
		lastViewed.put(sender, 5);
		assertEquals(8, manager.getIndexOrLastViewedReport(sender, "8"));
	}

	@Test(expected = NoLastViewedReportException.class)
	public void getIndexOrLastViewedReportNoLastReport() throws NoLastViewedReportException, IndexNotANumberException {
		final CommandSender sender = mock(CommandSender.class);
		when(sender.getName()).thenReturn("testPlayer");
		try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
			manager.getIndexOrLastViewedReport(sender, lastViewedIndex);
		}
	}

	@Test(expected = IndexNotANumberException.class)
	public void getIndexOrLastViewedReportIndexNotANumber() throws NoLastViewedReportException, IndexNotANumberException {
		final CommandSender sender = mock(CommandSender.class);
		manager.getIndexOrLastViewedReport(sender, "123fdsa987");
	}

	@Test(expected = IllegalArgumentException.class)
	public void getIndexOrLastViewedReportNullSender() throws NoLastViewedReportException, IndexNotANumberException {
		manager.getIndexOrLastViewedReport(null, "1");
	}

	@Test(expected = IllegalArgumentException.class)
	public void getIndexOrLastViewedReportNullIndex() throws NoLastViewedReportException, IndexNotANumberException {
		manager.getIndexOrLastViewedReport(mock(CommandSender.class), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getIndexOrLastViewedReportEmptyIndex() throws NoLastViewedReportException, IndexNotANumberException {
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

		final List<Integer> remainingIndexes = new ArrayList<>();
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

		final List<Integer> remainingIndexes = new ArrayList<>();
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

		final List<Integer> remainingIndexes = new ArrayList<>();
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

		final List<Integer> remainingIndexes = new ArrayList<>();
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

		final List<Integer> remainingIndexes = new ArrayList<>();
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
	 * The logic of the should match or be similar to the logic in {@link LastViewedReportService#hasLastViewed(CommandSender)}.
	 * But for testing purposes we should not rely on the manager logic.
	 */
	private boolean hasLastViewed(final CommandSender sender) {
		return lastViewed.containsKey(sender) && lastViewed.get(sender) != noLastViewedIndex;
	}
}