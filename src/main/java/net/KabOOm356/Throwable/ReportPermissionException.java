package net.KabOOm356.Throwable;

import org.bukkit.command.CommandSender;

public abstract class ReportPermissionException extends ReporterException {
	private final CommandSender sender;
	private final int index;

	public ReportPermissionException(final CommandSender sender, final int index, final String message) {
		super(message);
		this.sender = sender;
		this.index = index;
	}
}
