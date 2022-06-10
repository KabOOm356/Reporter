package net.KabOOm356.Service.Store.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import java.util.UUID;
import net.KabOOm356.Runnable.Timer.ReportTimer;
import org.bukkit.OfflinePlayer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import test.test.MockitoTest;

public class PlayerReportTest extends MockitoTest {
  private static final String senderName = "senderName";
  private static final UUID senderUUID = UUID.randomUUID();
  private static final String reportedPlayerName = "reportedPlayer";
  private static final UUID reportedPlayerUUID = UUID.randomUUID();
  @Spy private final PlayerReport playerReport = new PlayerReport();
  @Mock private OfflinePlayer player;
  @Mock private OfflinePlayer reportedPlayer;
  @Mock private ReportTimer timer;

  @Before
  public void setupMocks() {
    when(player.getUniqueId()).thenReturn(senderUUID);
    when(reportedPlayer.getUniqueId()).thenReturn(reportedPlayerUUID);
  }

  @Test
  public void put() {
    playerReport.put(player, reportedPlayer, timer);
    assertEquals(timer, playerReport.get(player).get(reportedPlayer).peek());
  }

  @Test
  public void putExisting() {
    final PlayerReportQueue existing = mock(PlayerReportQueue.class);
    when(playerReport.containsKey(senderUUID)).thenReturn(true);
    when(playerReport.get(senderUUID)).thenReturn(existing);
    playerReport.put(player, reportedPlayer, timer);
    verify(existing).put(reportedPlayer, timer);
  }

  @Test
  public void get() {
    final PlayerReportQueue playerReportQueueUUID = new PlayerReportQueue();
    playerReport.put(senderUUID, playerReportQueueUUID);

    final PlayerReportQueue returned = playerReport.get(player);
    assertNotNull(returned);
    assertEquals(playerReportQueueUUID, returned);
  }

  @Test
  public void testRemove() {
    final PlayerReportQueue playerReportQueueUUID = mock(PlayerReportQueue.class);
    playerReport.put(senderUUID, playerReportQueueUUID);

    playerReport.remove(player, reportedPlayer, timer);
    verify(playerReportQueueUUID).remove(reportedPlayer, timer);
  }
}
