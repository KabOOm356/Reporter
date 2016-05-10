package net.KabOOm356.Manager.Messager.Messages;

/**
 * A representation of a simple message.
 */
public class SimpleMessage extends Message {
	/**
	 * Constructor.
	 *
	 * @param message The message.
	 */
	public SimpleMessage(String message) {
		super(message);
	}

	@Override
	public boolean isEmpty() {
		return false;
	}
}
