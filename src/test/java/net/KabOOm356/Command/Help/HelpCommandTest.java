package net.KabOOm356.Command.Help;

import static org.mockito.Mockito.*;

import java.util.*;
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
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import test.test.Answer.LocaleEntryAnswer;
import test.test.MockitoTest;

public class HelpCommandTest extends MockitoTest {
  private static final int numberOfCommands = 6;
  private static final int numberOfUsagesPerCommand = 4;

  private HelpCommand help;

  @Mock private Player sender;

  @Mock private ReporterCommandManager commandManager;

  @Mock private ServiceModule serviceModule;

  @Mock private PermissionService permissionService;

  @Mock private Locale locale;

  @Before
  public void setupMocks() {
    try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
        MockedStatic<BukkitUtil> bukkitUtil = mockStatic(BukkitUtil.class)) {
      bukkit.when(() -> Bukkit.getOfflinePlayer(anyString())).thenReturn(null);
      bukkitUtil.when(() -> BukkitUtil.isPlayer(any(CommandSender.class))).thenReturn(false);
    }

    final Map<String, ReporterCommand> commands = new LinkedHashMap<>(16, 0.75F, false);
    for (int commandNumber = 0; commandNumber < numberOfCommands; commandNumber++) {
      final ReporterCommand command = mock(ReporterCommand.class);
      final List<Usage> usages = new ArrayList<>();
      final int startNumber = commandNumber * numberOfUsagesPerCommand;
      for (int usageNumber = startNumber + 1;
          usageNumber <= startNumber + numberOfUsagesPerCommand;
          usageNumber++) {
        final Usage usage = new Usage("key:" + usageNumber, "value:" + usageNumber);
        usages.add(usage);
      }
      when(command.getUsages()).thenReturn(usages);
      commands.put("command:" + commandNumber, command);
    }
    final Collection<ReporterCommand> commandCollection = commands.values();
    when(commandManager.getServiceModule()).thenReturn(serviceModule);
    when(serviceModule.getPermissionService()).thenReturn(permissionService);
    when(locale.getString(ArgumentMatchers.<Entry<String>>any()))
        .thenAnswer(LocaleEntryAnswer.instance);

    final HelpCommandDisplay.Builder builder = new HelpCommandDisplay.Builder();
    builder
        .setHeader(HelpPhrases.reportHelpHeader)
        .setAlias(HelpPhrases.reportHelpAliases)
        .setNext(HelpPhrases.nextReportHelpPage)
        .setHint(GeneralPhrases.tryReportHelp);
    final HelpCommandDisplay helpCommandDisplay = builder.build();
    help = new HelpCommand(commandManager, locale, commandCollection, helpCommandDisplay);
  }

  @Test(expected = RequiredPermissionException.class)
  public void testPrintHelpRequiredPermission()
      throws RequiredPermissionException, NoLastViewedReportException, IndexNotANumberException,
          IndexOutOfRangeException {
    try (MockedStatic<BukkitUtil> bukkitUtil = mockStatic(BukkitUtil.class)) {
      bukkitUtil.when(() -> BukkitUtil.isPlayer(sender)).thenReturn(true);
      when(permissionService.hasPermission(eq(sender), anyString())).thenReturn(false);
      help.execute(sender, Collections.emptyList());
    }
  }

  @Test
  public void testPrintHelpValidPages()
      throws RequiredPermissionException, NoLastViewedReportException, IndexNotANumberException,
          IndexOutOfRangeException {
    try (MockedStatic<BukkitUtil> bukkitUtil = mockStatic(BukkitUtil.class)) {
      bukkitUtil.when(() -> BukkitUtil.isPlayer(any(CommandSender.class))).thenReturn(false);

      for (int page = 1; page <= help.getNumberOfHelpPages(); page++) {
        help.execute(sender, Collections.singletonList(Integer.toString(page)));
      }
      // Verify that there was not an error phrase displayed
      verify(locale, never()).getString(HelpPhrases.pageNumberOutOfRange);
      verify(locale, never()).getString(HelpPhrases.numberOfHelpPages);
    }
  }

  @Test
  public void testPrintHelpPageBelowRange()
      throws RequiredPermissionException, NoLastViewedReportException, IndexNotANumberException,
          IndexOutOfRangeException {
    try (MockedStatic<BukkitUtil> bukkitUtil = mockStatic(BukkitUtil.class)) {
      bukkitUtil.when(() -> BukkitUtil.isPlayer(any(CommandSender.class))).thenReturn(false);

      help.execute(sender, Collections.singletonList(Integer.toString(0)));
      verify(sender)
          .sendMessage(ChatColor.RED + locale.getString(HelpPhrases.pageNumberOutOfRange));
      verify(sender).sendMessage(ChatColor.RED + locale.getString(GeneralPhrases.tryReportHelp));
    }
  }

  @Test
  public void testPrintHelpPageAboveRange()
      throws RequiredPermissionException, NoLastViewedReportException, IndexNotANumberException,
          IndexOutOfRangeException {
    try (MockedStatic<BukkitUtil> bukkitUtil = mockStatic(BukkitUtil.class)) {
      bukkitUtil.when(() -> BukkitUtil.isPlayer(any(CommandSender.class))).thenReturn(false);

      final int numberOfHelpPages = help.getNumberOfHelpPages();
      help.execute(sender, Collections.singletonList(Integer.toString(numberOfHelpPages + 1)));
      verify(sender)
          .sendMessage(
              ChatColor.RED
                  + locale
                      .getString(HelpPhrases.numberOfHelpPages)
                      .replaceAll("%p", Integer.toString(help.getNumberOfHelpPages())));
    }
  }
}
