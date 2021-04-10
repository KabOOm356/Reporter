package net.KabOOm356.Command.Help;

import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Locale.Entry.LocalePhrases.HelpPhrases;
import net.KabOOm356.Locale.Locale;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Throwable.IndexNotANumberException;
import net.KabOOm356.Throwable.IndexOutOfRangeException;
import net.KabOOm356.Throwable.NoLastViewedReportException;
import net.KabOOm356.Throwable.RequiredPermissionException;
import net.KabOOm356.Util.Util;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A command that will display how to use the other commands.
 */
public final class HelpCommand extends ReporterCommand {
	private static final float commandsPerPage = 5f;
	private static final String format = ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.RED + "%usage" + ChatColor.WHITE + " - %description";

	private final static String name = "Help";
	private final static int minimumNumberOfArguments = 0;
	private final static String permissionNode = "reporter.help";

	private static final List<Usage> usages = Collections.emptyList();
	private static final List<String> aliases = Collections.emptyList();

	private final Locale locale;
	private final List<List<Usage>> pages = new ArrayList<>();
	private final HelpCommandDisplay display;

	public HelpCommand(final ReporterCommandManager manager, final Locale locale, final Collection<ReporterCommand> commands, final HelpCommandDisplay display) {
		super(manager, name, permissionNode, minimumNumberOfArguments);
		Validate.notNull(locale);
		Validate.notNull(commands);
		Validate.notNull(display);
		this.locale = locale;
		this.display = display;
		createPages(commands);
	}

	private static List<Usage> createPage(final List<Usage> help, final int pageNumber) {
		final List<Usage> page = new ArrayList<>();
		final int startIndex = getPageStartIndex(pageNumber);
		final int endIndex = getPageEndIndex(pageNumber, help.size());
		for (int index = startIndex; index < endIndex; index++) {
			page.add(help.get(index));
		}
		return page;
	}

	private static int getPageStartIndex(final int page) {
		final float pageStartIndex = page * commandsPerPage;
		return (int) pageStartIndex;
	}

	private static int getPageEndIndex(final int page, final int total) {
		final float endIndex = getPageStartIndex(page) + commandsPerPage;
		return (endIndex > total) ? total : (int) endIndex;
	}

	private static int getPageIndex(final int page) {
		return page - 1;
	}

	/**
	 * Returns the number of pages that are available.
	 *
	 * @param numberOfHelpMessages The total number of help messages.
	 * @return The number of pages that are available.
	 */
	private static int calculateNumberOfPages(final int numberOfHelpMessages) {
		final double numberOfPages = numberOfHelpMessages / commandsPerPage;
		final double roundedNumberOfPages = Math.ceil(numberOfPages);
		return (int) roundedNumberOfPages;
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) throws NoLastViewedReportException, IndexOutOfRangeException, IndexNotANumberException, RequiredPermissionException {
		hasRequiredPermission(sender);

		int page = 1;

		if (args.size() >= 1) {
			if (Util.isInteger(args.get(0))) {
				page = Util.parseInt(args.get(0));
			}
		}

		printHelp(sender, page);
	}

	@Override
	public List<Usage> getUsages() {
		return usages;
	}

	@Override
	public List<String> getAliases() {
		return aliases;
	}

	private void printHelp(final CommandSender sender, final int page) {
		if (requireValidHelpPage(sender, page)) {
			printHeader(sender, page);
			printPage(sender, page);
			printFooter(sender, page);
		}
	}

	public int getNumberOfHelpPages() {
		return pages.size();
	}

	private void createPages(final Collection<ReporterCommand> commands) {
		final List<Usage> help = getHelp(commands);
		final int pageCount = calculateNumberOfPages(help.size());
		for (int page = 0; page < pageCount; page++) {
			pages.add(createPage(help, page));
		}
	}

	private List<Usage> getHelp(final Collection<ReporterCommand> commands) {
		final List<Usage> help = new ArrayList<>();
		for (final ReporterCommand command : commands) {
			help.addAll(command.getUsages());
		}
		return help;
	}

	private void printFooter(final CommandSender sender, final int page) {
		if (page != pages.size()) {
			final String nextPage = getLocale().getString(display.getNext())
					.replaceAll("%p", Integer.toString(page + 1));
			sender.sendMessage(ChatColor.GOLD + nextPage);
		} else {
			final String aliases = getLocale().getString(display.getAlias());
			sender.sendMessage(ChatColor.BLUE + aliases);
		}
	}

	private void printPage(final CommandSender sender, final int page) {
		final int pageIndex = getPageIndex(page);
		final List<Usage> currentPage = pages.get(pageIndex);
		for (final Usage usage : currentPage) {
			final String usageString = getLocale().getString(usage.getKey());
			final String description = getLocale().getString(usage.getValue());
			sender.sendMessage(format
					.replaceAll("%usage", usageString)
					.replaceAll("%description", description));
		}
	}

	private boolean requireValidHelpPage(final CommandSender sender, final int page) {
		return requireValidHelpPageMinimum(sender, page) && requireValidHelpPageMaximum(sender, page);
	}

	private boolean requireValidHelpPageMaximum(final CommandSender sender, final int page) {
		if (page > pages.size()) {
			final String line = getLocale().getString(HelpPhrases.numberOfHelpPages)
					.replaceAll("%p", Integer.toString(pages.size()));
			sender.sendMessage(ChatColor.RED + line);
			return false;
		}
		return true;
	}

	private boolean requireValidHelpPageMinimum(final CommandSender sender, final int page) {
		if (page <= 0) {
			String line = getLocale().getString(HelpPhrases.pageNumberOutOfRange);
			sender.sendMessage(ChatColor.RED + line);

			line = getLocale().getString(display.getHint());
			sender.sendMessage(ChatColor.RED + line);
			return false;
		}
		return true;
	}

	private void printHeader(final CommandSender sender, final int page) {
		final String header = getLocale().getString(display.getHeader())
				.replaceAll("%p", Integer.toString(page))
				.replaceAll("%c", ChatColor.GOLD + Integer.toString(pages.size()));
		sender.sendMessage(ChatColor.GREEN + header);
	}

	private Locale getLocale() {
		return locale;
	}
}
