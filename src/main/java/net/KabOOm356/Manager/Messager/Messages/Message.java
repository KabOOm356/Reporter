package net.KabOOm356.Manager.Messager.Messages;

/**
 * A class that represents a message.
 */
public abstract class Message {
	/**
	 * The message.
	 */
	private String message;

	/**
	 * Constructor.
	 *
	 * @param message The message.
	 */
	public Message(final String message) {
		this.message = message;
	}

	/**
	 * Returns the message.
	 *
	 * @return The message.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the message to the given message.
	 *
	 * @param message The message to be set to.
	 */
	public void setMessage(final String message) {
		this.message = message;
	}

	/**
	 * Checks if this message and the given messages are equal.
	 *
	 * @param message The message to compare to.
	 * @return True if both messages are equal.
	 */
	public boolean messagesEqual(final Message message) {
		return getMessage().equalsIgnoreCase(message.getMessage());
	}

	/**
	 * Checks if this message is empty.
	 *
	 * @return True if this message is empty, otherwise false.
	 */
	public abstract boolean isEmpty();

	@Override
	public String toString() {
		return "Message: " + message;
	}
}
