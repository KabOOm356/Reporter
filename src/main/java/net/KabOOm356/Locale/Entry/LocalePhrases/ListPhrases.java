package net.KabOOm356.Locale.Entry.LocalePhrases;

import net.KabOOm356.Locale.Entry.LocalePhrase;

/**
 * Class containing static {@link LocalePhrase}s that are used by {@link
 * net.KabOOm356.Command.Commands.ListCommand}.
 */
public abstract class ListPhrases {
  /** You have access to the following reports: %i */
  public static final LocalePhrase listReportsAvailable =
      new LocalePhrase("listReportsAvailable", "You have access to the following reports: %i");

  /** You do not have access to any reports! */
  public static final LocalePhrase listNoReportsAvailable =
      new LocalePhrase("listNoReportsAvailable", "You do not have access to any reports!");

  /** You currently have %n claimed report(s)! */
  public static final LocalePhrase listClaimed =
      new LocalePhrase("listClaimed", "You currently have %n claimed report(s)!");

  /** You have %n report(s) claimed with priority %p! */
  public static final LocalePhrase listClaimedPriorityCount =
      new LocalePhrase(
          "listClaimedPriorityCount", "You have %n report(s) claimed with priority %p!");

  /** You have claimed reports at indexes: %i */
  public static final LocalePhrase listClaimedIndexes =
      new LocalePhrase("listClaimedIndexes", "You have claimed reports at indexes: %i");

  /** Your report(s) claimed with priority %p are at indexes %i! */
  public static final LocalePhrase listClaimedPriorityIndexes =
      new LocalePhrase(
          "listClaimedPriorityIndexes",
          "Your report(s) claimed with priority %p are at indexes %i!");

  /** You do not have any claimed reports with priority %p! */
  public static final LocalePhrase listNoClaimedPriorityIndexes =
      new LocalePhrase(
          "listNoClaimedPriorityIndexes", "You do not have any claimed reports with priority %p!");

  /** There are %n report(s) with priority %p! */
  public static final LocalePhrase listPriorityCount =
      new LocalePhrase("listPriorityCount", "There are %n report(s) with priority %p!");

  /** Reports with priority %p are at indexes: %i */
  public static final LocalePhrase listPriorityIndexes =
      new LocalePhrase("listPriorityIndexes", "Reports with priority %p are at indexes: %i");

  /** There are no reports with priority %p! */
  public static final LocalePhrase listNoReportsWithPriority =
      new LocalePhrase("listNoReportsWithPriority", "There are no reports with priority %p!");

  /** There are %r reports submitted%nThere are %c completed reports */
  public static final LocalePhrase reportList =
      new LocalePhrase(
          "reportList", "There are %r reports submitted%nThere are %c completed reports");

  /** Complete reports are at indexes: %i */
  public static final LocalePhrase listReportCompleteIndexes =
      new LocalePhrase("listReportCompleteIndexes", "Complete reports are at indexes: %i");

  /** There are no completed reports! */
  public static final LocalePhrase listReportNoCompleteIndexes =
      new LocalePhrase("listReportNoCompleteIndexes", "There are no completed reports!");

  /** Incomplete reports are at indexes: %i */
  public static final LocalePhrase listReportIncompleteIndexes =
      new LocalePhrase("listReportIncompleteIndexes", "Incomplete reports are at indexes: %i");

  /** There are no incomplete reports! */
  public static final LocalePhrase listReportNoIncompleteIndexes =
      new LocalePhrase("listReportNoIncompleteIndexes", "There are no incomplete reports!");

  /** Lists the number of incomplete and completed reports, or their indexes. */
  public static final LocalePhrase listHelpDetails =
      new LocalePhrase(
          "listHelpDetails",
          "Lists the number of incomplete and completed reports, or their indexes.");

  /** Lists the number of reports at all priority levels, or their indexes. */
  public static final LocalePhrase listHelpPriorityDetails =
      new LocalePhrase(
          "listHelpPriorityDetails",
          "Lists the number of reports at all priority levels, or their indexes.");

  /** Lists the number of reports you have claimed, or their indexes. */
  public static final LocalePhrase listHelpClaimedDetails =
      new LocalePhrase(
          "listHelpClaimedDetails",
          "Lists the number of reports you have claimed, or their indexes.");

  /** Lists the number of reports you have claimed by their priority, or their indexes. */
  public static final LocalePhrase listHelpClaimedPriorityDetails =
      new LocalePhrase(
          "listHelpClaimedPriorityDetails",
          "Lists the number of reports you have claimed by their priority, or their indexes.");
}
