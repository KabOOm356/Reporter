package net.KabOOm356.Service.Store.type;

import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.UUID;
import net.KabOOm356.Runnable.Timer.ReportTimer;
import org.bukkit.OfflinePlayer;

public class PlayerReportQueue extends HashMap<UUID, PriorityQueue<ReportTimer>> {
  private static final long serialVersionUID = 5873280632943697440L;

  /**
   * Gets a queue of {@link ReportTimer} associated with the given {@link OfflinePlayer}.
   *
   * @param reported The {@link OfflinePlayer}.
   * @return A {@link PriorityQueue} of {@link ReportTimer}s that are associated with the given
   *     {@link OfflinePlayer}.
   */
  public PriorityQueue<ReportTimer> get(final OfflinePlayer reported) {
    return get(reported.getUniqueId());
  }

  /**
   * Adds the given {@link ReportTimer} to the given {@link OfflinePlayer}'s queue.
   *
   * @param reported The {@link OfflinePlayer}.
   * @param timer The {@link ReportTimer}.
   */
  public void put(final OfflinePlayer reported, final ReportTimer timer) {
    if (reported.getUniqueId() != null) {
      add(reported.getUniqueId(), timer);
    }
  }

  /**
   * Removes the given {@link ReportTimer} from the given {@link OfflinePlayer}'s queue.
   *
   * @param reported The {@link OfflinePlayer}.
   * @param timer The {@link ReportTimer}.
   */
  public void remove(final OfflinePlayer reported, final ReportTimer timer) {
    final PriorityQueue<ReportTimer> queue = get(reported);
    if (queue != null) {
      queue.remove(timer);
    }
  }

  private void add(final UUID key, final ReportTimer timer) {
    final PriorityQueue<ReportTimer> queue = (containsKey(key)) ? get(key) : new PriorityQueue<>();
    queue.add(timer);
    put(key, queue);
  }
}
