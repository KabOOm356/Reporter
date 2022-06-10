package net.KabOOm356.Locale.Entry.LocalePhrases;

import net.KabOOm356.Locale.Entry.LocalePhrase;

/**
 * Class containing static {@link LocalePhrase}s that are used by {@link
 * net.KabOOm356.Command.Commands.MoveCommand}.
 */
public abstract class MovePhrases {
  /** You have been unassigned from report %i by %s! */
  public static final LocalePhrase unassignedFromReport =
      new LocalePhrase("unassignedFromReport", "You have been unassigned from report %i by %s!");

  /** The report at index %i was successfully moved to priority %p! */
  public static final LocalePhrase moveReportSuccess =
      new LocalePhrase(
          "moveReportSuccess", "The report at index %i was successfully moved to priority %p!");

  /** /report move Index/last Priority */
  public static final LocalePhrase moveHelp =
      new LocalePhrase("moveHelp", "/report move <Index/last> <Priority>");

  /** Moves the specified report to a new priority. Priority can be None, Low, Normal or High. */
  public static final LocalePhrase moveHelpDetails =
      new LocalePhrase(
          "moveHelpDetails",
          "Moves the specified report to a new priority.  Priority can be None, Low, Normal or High.");
}
