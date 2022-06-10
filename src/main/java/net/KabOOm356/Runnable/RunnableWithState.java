package net.KabOOm356.Runnable;

/** A Runnable that has a state. */
public interface RunnableWithState extends Runnable {
  /**
   * If the current runnable is currently executing.
   *
   * @return True if the current runnable is executing, otherwise false.
   */
  boolean isRunning();

  /**
   * If the current runnable is pending to start running.
   *
   * @return True if the current runnable is pending to start running, otherwise false.
   */
  boolean isPendingToRun();

  /**
   * If the current runnable is stopped.
   *
   * @return True if the current runnable is stopped.
   */
  boolean isStopped();

  /**
   * If the current runnable has run.
   *
   * @return True if the current runnable has run, otherwise false.
   */
  boolean hasRun();
}
