package net.KabOOm356.Command.Help;

import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Locale.Entry.LocalePhrases.HelpPhrases;
import net.KabOOm356.Locale.Locale;
import net.KabOOm356.Reporter.Reporter;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A command that will display how to use the other commands.
 */
public final class HelpCommand {
	private static final float commandsPerPage = 5f;
	private static final String format = ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.RED + "%usage" + ChatColor.WHITE + " - %description";
	private final Locale locale;
	private final ArrayList<ArrayList<Usage>> pages = new ArrayList<ArrayList<Usage>>();
	private final HelpCommandDisplay display;

	public HelpCommand(final Locale locale, final Collection<ReporterCommand> commands, final HelpCommandDisplay display) {
		Validate.notNull(locale);
		Validate.notNull(commands);
		Validate.notNull(display);
		this.locale = locale;
		this.display = display;
		createPages(commands);
	}

	private static ArrayList<Usage> createPage(final ArrayList<Usage> help, final int pageNumber) {
		final ArrayList<Usage> page = new ArrayList<Usage>();
		final int startIndex = getPageStartIndex(pageNumber);
		final int endIndex = getPageEndIndex(pageNumber, help.size());
		for (int index = startIndex; index < endIndex; index++) {
			page.add(help.get(index));
		}
		return page;
	}

	private static int getPageStartIndex(final int page) {
		final Float pageStartIndex = page * commandsPerPage;
		return pageStartIndex.intValue();
	}

	private static int getPageEndIndex(final int page, final int total) {
		final Float endIndex = getPageStartIndex(page) + commandsPerPage;
		return (endIndex > total) ? total : endIndex.intValue();
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
		final Double roundedNumberOfPages = Math.ceil(numberOfPages);
		return roundedNumberOfPages.intValue();
	}

	public void printHelp(final CommandSender sender, final int page) {
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
		final ArrayList<Usage> help = getHelp(commands);
		final int pageCount = calculateNumberOfPages(help.size());
		for (int page = 0; page < pageCount; page++) {
			pages.add(createPage(help, page));
		}
	}

	private ArrayList<Usage> getHelp(final Collection<ReporterCommand> commands) {
		final ArrayList<Usage> help = new ArrayList<Usage>();
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
		final ArrayList<Usage> currentPage = pages.get(pageIndex);
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
