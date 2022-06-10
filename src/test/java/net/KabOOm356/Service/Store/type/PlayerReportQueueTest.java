package net.KabOOm356.Service.Store.type;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.PriorityQueue;
import java.util.UUID;
import net.KabOOm356.Runnable.Timer.ReportTimer;
import org.bukkit.OfflinePlayer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import test.test.MockitoTest;

public class PlayerReportQueueTest extends MockitoTest {
  private static final UUID playerUUID = UUID.randomUUID();
  @Spy private final PlayerReportQueue playerReportQueue = new PlayerReportQueue();
  @Mock private OfflinePlayer player;
  @Mock private ReportTimer timer;

  @Before
  public void setupMocks() {
    when(player.getUniqueId()).thenReturn(playerUUID);
  }

  @Test
  public void testPut() {
    playerReportQueue.put(player, timer);
    assertEquals(timer, playerReportQueue.get(player).peek());
  }

  @Test
  public void testPutNoPlayerUniqueId() {
    when(player.getUniqueId()).thenReturn(null);

    playerReportQueue.put(player, timer);

    assertNull(playerReportQueue.get(player));
  }

  @Test
  public void testPutExisting() {
    final PriorityQueue<ReportTimer> priorityQueue = spy(new PriorityQueue<>());
    playerReportQueue.put(playerUUID, priorityQueue);

    playerReportQueue.put(player, timer);
    verify(priorityQueue).add(timer);
  }

  @Test
  public void testGetEmpty() {
    final PriorityQueue<ReportTimer> returned = playerReportQueue.get(player);
    assertNull(returned);
  }

  @Test
  public void testGetUUID() {
    final PriorityQueue<ReportTimer> priorityQueue = new PriorityQueue<>();
    final ReportTimer timer = mock(ReportTimer.class);
    priorityQueue.add(timer);
    playerReportQueue.put(playerUUID, priorityQueue);

    final PriorityQueue<ReportTimer> returned = playerReportQueue.get(player);
    assertFalse(returned.isEmpty());
    assertEquals(1, returned.size());
    assertTrue(returned.contains(timer));
  }
}
