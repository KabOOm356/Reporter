package net.KabOOm356.Locale.Entry.LocalePhrases;

import net.KabOOm356.Locale.Entry.LocalePhrase;

/**
 * Class containing static {@link LocalePhrase}s that are used by {@link
 * net.KabOOm356.Command.Commands.UnassignCommand}.
 */
public abstract class UnassignPhrases {
  /** You have successfully unassigned %p from the report at index %i! */
  public static final LocalePhrase reportUnassignSuccess =
      new LocalePhrase(
          "reportUnassignSuccess",
          "You have successfully unassigned %p from the report at index %i!");

  /** You have been unassigned from report %i by %s! */
  public static final LocalePhrase unassignedFromReport =
      new LocalePhrase("unassignedFromReport", "You have been unassigned from report %i by %s!");

  /** /report unassign Index/last */
  public static final LocalePhrase unassignHelp =
      new LocalePhrase("unassignHelp", "/report unassign <Index/last>");

  /** Opposite of assigning a report. Removes the player currently claiming the specified report. */
  public static final LocalePhrase unassignHelpDetails =
      new LocalePhrase(
          "unassignHelpDetails",
          "Opposite of assigning a report.  Removes the player currently claiming the specified report.");
}
