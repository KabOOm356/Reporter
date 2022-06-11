package net.KabOOm356.Runnable;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** A runnable that handles tracking the amount of time the task takes to execute. */
public abstract class TimedRunnable implements Runnable {
  private static final Logger log = LogManager.getLogger(TimedRunnable.class);

  private Long startTime = null;
  private Long endTime = null;

  /** This method should be called when the thread begins executing. */
  protected void start() {
    startTime = System.currentTimeMillis();
    if (log.isDebugEnabled()) {
      log.log(Level.INFO, "Starting execution of " + getClass().getName());
    }
  }

  /** This method should be called when the thread finishes executing. */
  protected void end() {
    endTime = System.currentTimeMillis();
    if (startTime != null && log.isDebugEnabled()) {
      log.log(
          Level.INFO,
          "Execution of " + getClass().getName() + " took " + getExecutionTime() + "ms!");
    }
  }

  /**
   * Returns the time this thread began executing.
   *
   * @return The time this thread began executing if {@link #start()} was called, otherwise null.
   */
  public Long getStartTime() {
    return startTime;
  }

  /**
   * Returns the time this thread finished executing.
   *
   * @return The time this thread finished execution if {@link #end()} was called, otherwise null.
   */
  public Long getEndTime() {
    return endTime;
  }

  /**
   * Returns the amount of time this thread took to execute.
   *
   * @return The amount of time this thread took to execute.
   * @throws IllegalArgumentException If {@link #start()} or {@link #end()} was not called.
   */
  public Long getExecutionTime() {
    Validate.notNull(startTime, "Thread was never started!");
    Validate.notNull(endTime, "Thread has not ended!");
    return endTime - startTime;
  }
}
