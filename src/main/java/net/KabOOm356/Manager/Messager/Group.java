package net.KabOOm356.Manager.Messager;

/**
 * A group class.
 */
public class Group {
	/**
	 * A default group.
	 */
	public final static Group DEFAULT = new Group("Default");

	private final String groupName;

	/**
	 * Constructor.
	 *
	 * @param groupName The name of this Group.
	 */
	public Group(final String groupName) {
		this.groupName = groupName;
	}

	/**
	 * Returns the name of this Group.
	 *
	 * @return The name of this Group.
	 */
	public String getName() {
		return groupName;
	}

	/**
	 * Checks if this and the given group are equal.
	 * <br/>
	 * They are equal if their name's are equal.
	 *
	 * @param group The group to compare to.
	 * @return True if both group names are equal, otherwise false.
	 */
	public boolean equals(final Group group) {
		return this.getName().equalsIgnoreCase(group.getName());
	}

	@Override
	public String toString() {
		return "Group: " + groupName;
	}
}
