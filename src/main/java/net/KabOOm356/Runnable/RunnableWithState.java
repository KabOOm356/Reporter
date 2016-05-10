package net.KabOOm356.Runnable;

/**
 * A Runnable that has a state.
 */
public interface RunnableWithState extends Runnable {
	/**
	 * If the current runnable is currently executing.
	 *
	 * @return True if the current runnable is executing, otherwise false.
	 */
	public boolean isRunning();

	/**
	 * If the current runnable is pending to start running.
	 *
	 * @return True if the current runnable is pending to start running, otherwise false.
	 */
	public boolean isPendingToRun();

	/**
	 * If the current runnable is stopped.
	 *
	 * @return True if the current runnable is stopped.
	 */
	public boolean isStopped();

	/**
	 * If the current runnable has run.
	 *
	 * @return True if the current runnable has run, otherwise false.
	 */
	public boolean hasRun();
}
