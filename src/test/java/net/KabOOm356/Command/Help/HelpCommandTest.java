package net.KabOOm356.Command.Help;

import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Configuration.Entry;
import net.KabOOm356.Locale.Entry.LocalePhrases.GeneralPhrases;
import net.KabOOm356.Locale.Entry.LocalePhrases.HelpPhrases;
import net.KabOOm356.Locale.Locale;
import net.KabOOm356.Service.PermissionService;
import net.KabOOm356.Service.ServiceModule;
import net.KabOOm356.Throwable.IndexNotANumberException;
import net.KabOOm356.Throwable.IndexOutOfRangeException;
import net.KabOOm356.Throwable.NoLastViewedReportException;
import net.KabOOm356.Throwable.RequiredPermissionException;
import net.KabOOm356.Util.BukkitUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import test.test.Answer.LocaleEntryAnswer;
import test.test.PowerMockitoTest;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PrepareForTest({HelpCommand.class, Bukkit.class, BukkitUtil.class})
public class HelpCommandTest extends PowerMockitoTest {
	private static final int numberOfCommands = 6;
	private static final int numberOfUsagesPerCommand = 4;

	private HelpCommand help;

	@Mock
	private Player sender;

	@Mock
	private ReporterCommandManager commandManager;

	@Mock
	private ServiceModule serviceModule;

	@Mock
	private PermissionService permissionService;

	@Mock
	private Locale locale;

	@Before
	public void setupMocks() {
		mockStatic(Bukkit.class);
		when(Bukkit.getOfflinePlayer(anyString())).thenReturn(null);
		mockStatic(BukkitUtil.class);
		when(BukkitUtil.isPlayer(any(CommandSender.class))).thenReturn(false);

		final Map<String, ReporterCommand> commands = new LinkedHashMap<>(16, 0.75F, false);
		for (int commandNumber = 0; commandNumber < numberOfCommands; commandNumber++) {
			final ReporterCommand command = mock(ReporterCommand.class);
			final List<Usage> usages = new ArrayList<>();
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
		when(commandManager.getServiceModule()).thenReturn(serviceModule);
		when(serviceModule.getPermissionService()).thenReturn(permissionService);
		when(permissionService.hasPermission(any(CommandSender.class), anyString())).thenReturn(true);
		when(locale.getString(any(Entry.class))).thenAnswer(LocaleEntryAnswer.instance);

		final HelpCommandDisplay.Builder builder = new HelpCommandDisplay.Builder();
		builder.setHeader(HelpPhrases.reportHelpHeader)
				.setAlias(HelpPhrases.reportHelpAliases)
				.setNext(HelpPhrases.nextReportHelpPage)
				.setHint(GeneralPhrases.tryReportHelp);
		final HelpCommandDisplay helpCommandDisplay = builder.build();
		help = new HelpCommand(commandManager, locale, commandCollection, helpCommandDisplay);
	}

	@Test(expected = RequiredPermissionException.class)
	public void testPrintHelpRequiredPermission() throws RequiredPermissionException, NoLastViewedReportException, IndexNotANumberException, IndexOutOfRangeException {
		when(BukkitUtil.isPlayer(sender)).thenReturn(true);
		when(permissionService.hasPermission(eq(sender), anyString())).thenReturn(false);
		help.execute(sender, Collections.<String>emptyList());
	}

	@Test
	public void testPrintHelpValidPages() throws RequiredPermissionException, NoLastViewedReportException, IndexNotANumberException, IndexOutOfRangeException {
		for (int page = 1; page <= help.getNumberOfHelpPages(); page++) {
			help.execute(sender, Collections.singletonList(Integer.toString(page)));
		}
		// Verify that there was not an error phrase displayed
		verify(locale, never()).getString(HelpPhrases.pageNumberOutOfRange);
		verify(locale, never()).getString(HelpPhrases.numberOfHelpPages);
	}

	@Test
	public void testPrintHelpPageBelowRange() throws RequiredPermissionException, NoLastViewedReportException, IndexNotANumberException, IndexOutOfRangeException {
		help.execute(sender, Collections.singletonList(Integer.toString(0)));
		verify(sender).sendMessage(ChatColor.RED + locale.getString(HelpPhrases.pageNumberOutOfRange));
		verify(sender).sendMessage(ChatColor.RED + locale.getString(GeneralPhrases.tryReportHelp));
	}

	@Test
	public void testPrintHelpPageAboveRange() throws RequiredPermissionException, NoLastViewedReportException, IndexNotANumberException, IndexOutOfRangeException {
		final int numberOfHelpPages = help.getNumberOfHelpPages();
		help.execute(sender, Collections.singletonList(Integer.toString(numberOfHelpPages + 1)));
		verify(sender).sendMessage(ChatColor.RED + locale.getString(HelpPhrases.numberOfHelpPages)
				.replaceAll("%p", Integer.toString(help.getNumberOfHelpPages())));
	}
}