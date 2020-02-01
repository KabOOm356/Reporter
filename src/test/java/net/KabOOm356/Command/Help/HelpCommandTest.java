package net.KabOOm356.Command.Help;

import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Locale.Entry.LocalePhrase;
import net.KabOOm356.Locale.Entry.LocalePhrases.GeneralPhrases;
import net.KabOOm356.Locale.Entry.LocalePhrases.HelpPhrases;
import net.KabOOm356.Locale.Locale;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import test.test.Answer.LocaleEntryAnswer;
import test.test.PowerMockitoTest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest(HelpCommand.class)
public class HelpCommandTest extends PowerMockitoTest {
	private static final int numberOfCommands = 6;
	private static final int numberOfUsagesPerCommand = 4;

	private HelpCommand help;

	@Mock
	private CommandSender sender;

	@Mock
	private ReporterCommandManager commandManager;

	@Mock
	private Locale locale;

	@Before
	public void setupMocks() {
		final LinkedHashMap<String, ReporterCommand> commands = new LinkedHashMap<>(16, 0.75F, false);
		for (int commandNumber = 0; commandNumber < numberOfCommands; commandNumber++) {
			final ReporterCommand command = mock(ReporterCommand.class);
			final ArrayList<Usage> usages = new ArrayList<>();
			final int startNumber = commandNumber * numberOfUsagesPerCommand;
			for (int usageNumber = startNumber + 1; usageNumber <= startNumber + numberOfUsagesPerCommand; usageNumber++) {
				final Usage usage = new Usage("key:" + usageNumber, "value:" + usageNumber);
				usages.add(usage);
			}
			when(command.getUsages()).thenReturn(usages);
			commands.put("command:" + commandNumber, command);
		}
		final Collection<ReporterCommand> commandCollection = commands.values();
		when(commandManager.getReportCommands()).thenReturn(commands);
		when(commandManager.getLocale()).thenReturn(locale);
		when(locale.getString(any(LocalePhrase.class))).thenAnswer(LocaleEntryAnswer.instance);

		final HelpCommandDisplay.Builder builder = new HelpCommandDisplay.Builder();
		builder.setHeader(HelpPhrases.reportHelpHeader)
				.setAlias(HelpPhrases.reportHelpAliases)
				.setNext(HelpPhrases.nextReportHelpPage)
				.setHint(GeneralPhrases.tryReportHelp);
		final HelpCommandDisplay helpCommandDisplay = builder.build();
		help = new HelpCommand(locale, commandCollection, helpCommandDisplay);
	}

	@Test
	public void testPrintHelpValidPages() {
		for (int page = 1; page <= help.getNumberOfHelpPages(); page++) {
			help.printHelp(sender, page);
		}
		// Verify that there was not an error phrase displayed
		verify(locale, never()).getString(HelpPhrases.pageNumberOutOfRange);
		verify(locale, never()).getString(HelpPhrases.numberOfHelpPages);
	}

	@Test
	public void testPrintHelpPageBelowRange() {
		help.printHelp(sender, 0);
		verify(sender).sendMessage(ChatColor.RED + locale.getString(HelpPhrases.pageNumberOutOfRange));
		verify(sender).sendMessage(ChatColor.RED + locale.getString(GeneralPhrases.tryReportHelp));
	}

	@Test
	public void testPrintHelpPageAboveRange() {
		final int numberOfHelpPages = help.getNumberOfHelpPages();
		help.printHelp(sender, numberOfHelpPages + 1);
		verify(sender).sendMessage(ChatColor.RED + locale.getString(HelpPhrases.numberOfHelpPages)
				.replaceAll("%p", Integer.toString(help.getNumberOfHelpPages())));
	}
}