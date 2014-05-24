package net.KabOOm356.Runnable.Timer;

import java.util.Calendar;
import java.util.Comparator;

import net.KabOOm356.Manager.ReportLimitManager;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * A {@link Thread} that will when run by the assigned thread manager will 'expire'.
 * <br /><br />
 * This class helps with the report limiting of {@link Player}.
 * 
 * @see ReportLimitManager
 */
public class ReportTimer extends Thread implements Comparable<ReportTimer>
{
	/**
	 * {@link ReportTimer} comparator that compares the based on the time remaining.
	 */
	public static class CompareByTimeRemaining implements Comparator<ReportTimer>
	{
		@Override
		public int compare(ReportTimer arg0, ReportTimer arg1)
		{
			return arg0.getTimeRemaining() - arg1.getTimeRemaining();
		}
	}
	
	/** An instance of {@link CompareByTimeRemaining}. */
	public static final CompareByTimeRemaining compareByTimeRemaining = new CompareByTimeRemaining();
	
	/** The manager that will be reported to when this thread 'expires'. */
	private ReportLimitManager manager;
	/** Flag that will state whether this thread is finished or currently unassigned. */
	public boolean isFinished;
	/** The {@link Player} that has submitted a report. */
	private Player player;
	/** The {@link OfflinePlayer} that was reported. */
	private OfflinePlayer reported;
	/** When this thread should *Hopefully* be executed by the thread manager. */
	private long executionTime;
	
	/**
	 * Constructor.
	 */
	public ReportTimer()
	{
		this.isFinished = true;
		
		this.setDaemon(false);
	}
	
	/**
	 * Initializes values for this thread.
	 * 
	 * @param manager The {@link ReportLimitManager} to report to when finished.
	 * @param player The {@link Player} that has submitted a report that is being tracked.
	 * @param executionTime When this thread should *Hopefully* be executed by the thread manager.
	 */
	public void init(ReportLimitManager manager, Player player, OfflinePlayer reported, long executionTime)
	{
		this.manager = manager;
		
		this.player = player;
		
		this.reported = reported;
		
		this.executionTime = executionTime;
		
		this.isFinished = false;
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run()
	{
		this.isFinished = true;
		manager.limitExpired(this);
	}
	
	/**
	 * Returns the {@link Player} that has submitted a report that is being tracked.
	 * 
	 * @return The {@link Player} that has submitted a report that is being tracked.
	 */
	public Player getPlayer()
	{
		return player;
	}
	
	/**
	 * Returns the {@link OfflinePlayer} that was reported.
	 * 
	 * @return The {@link OfflinePlayer} that was reported.
	 */
	public OfflinePlayer getReported()
	{
		return reported;
	}
	
	/**
	 * Returns when this thread should *Hopefully* be executed by the thread manager.
	 * 
	 * @return When this thread should *Hopefully* be executed by the thread manager.
	 */
	public long getExecutionTime()
	{
		return executionTime;
	}
	
	/**
	 * Returns the amount of time, in seconds, until this timer expires.
	 * 
	 * @return The amount of time, in seconds, until this timer expires.
	 */
	public int getTimeRemaining()
	{
		long executionTime = getExecutionTime();
		long currentTime = Calendar.getInstance().getTimeInMillis();
		
		return (int) ((executionTime - currentTime)/1000);
	}

	@Override
	public int compareTo(ReportTimer arg0)
	{
		return compareByTimeRemaining.compare(this, arg0);
	}
}
