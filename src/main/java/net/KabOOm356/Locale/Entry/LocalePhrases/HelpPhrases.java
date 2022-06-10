package net.KabOOm356.Locale.Entry.LocalePhrases;

import net.KabOOm356.Command.Help.HelpCommand;
import net.KabOOm356.Locale.Entry.LocalePhrase;

/** Class containing static {@link LocalePhrase}s that are used by {@link HelpCommand}. */
public abstract class HelpPhrases {
  /** Available /report Commands page %p of %c: */
  public static final LocalePhrase reportHelpHeader =
      new LocalePhrase("reportHelpHeader", "Available /report Commands page %p of %c:");

  /** Type /report help %p to view the next help page! */
  public static final LocalePhrase nextReportHelpPage =
      new LocalePhrase("nextReportHelpPage", "Type /report help %p to view the next help page!");

  /** /report command aliases - /rep, /rreport */
  public static final LocalePhrase reportHelpAliases =
      new LocalePhrase("reportHelpAliases", "/report command aliases - /rep, /rreport");

  /** Available /respond Commands page %p of %c: */
  public static final LocalePhrase respondHelpHeader =
      new LocalePhrase("respondHelpHeader", "Available /respond Commands page %p of %c:");

  /** Type /respond help %p to view the next help page! */
  public static final LocalePhrase nextRespondHelpPage =
      new LocalePhrase("nextRespondHelpPage", "Type /respond help %p to view the next help page!");

  /** /respond command aliases - /resp, /rrespond */
  public static final LocalePhrase respondHelpAliases =
      new LocalePhrase("respondHelpAliases", "/respond command aliases - /resp, /rrespond");

  /** The page number is outside the range! */
  public static final LocalePhrase pageNumberOutOfRange =
      new LocalePhrase("pageNumberOutOfRange", "The page number is outside the range!");

  /** There are only %p help pages! */
  public static final LocalePhrase numberOfHelpPages =
      new LocalePhrase("numberOfHelpPages", "There are only %p help pages!");
}
