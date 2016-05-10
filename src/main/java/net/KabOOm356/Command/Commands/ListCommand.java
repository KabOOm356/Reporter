package net.KabOOm356.Command.Commands;

import net.KabOOm356.Command.ReporterCommand;
import net.KabOOm356.Command.ReporterCommandManager;
import net.KabOOm356.Database.ExtendedDatabaseHandler;
import net.KabOOm356.Database.SQLResultSet;
import net.KabOOm356.Locale.Entry.LocalePhrases.ListPhrases;
import net.KabOOm356.Permission.ModLevel;
import net.KabOOm356.Reporter.Reporter;
import net.KabOOm356.Util.ArrayUtil;
import net.KabOOm356.Util.BukkitUtil;
import net.KabOOm356.Util.ObjectPair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * A {@link ReporterCommand} that will handle listing reports.
 */
public class ListCommand extends ReporterCommand {
	private static final Logger log = LogManager.getLogger(ListCommand.class);

	private static final String name = "List";
	private static final int minimumNumberOfArguments = 0;
	private final static String permissionNode = "reporter.list";

	/**
	 * Constructor.
	 *
	 * @param manager The {@link ReporterCommandManager} managing this Command.
	 */
	public ListCommand(ReporterCommandManager manager) {
		super(manager, name, permissionNode, minimumNumberOfArguments);

		updateDocumentation();
	}

	/**
	 * Returns the name of this command.
	 *
	 * @return The name of this command.
	 */
	public static String getCommandName() {
		return name;
	}

	/**
	 * Returns the permission node of this command.
	 *
	 * @return The permission node of this command.
	 */
	public static String getCommandPermissionNode() {
		return permissionNode;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(CommandSender sender, ArrayList<String> args) {
		if (hasPermission(sender)) {
			try {
				if (args == null || args.isEmpty())
					listCommand(sender);
				else if (args.size() >= 1 && args.get(0).equalsIgnoreCase("indexes"))
					listIndexes(sender);
				else if (args.size() >= 1 && args.get(0).equalsIgnoreCase("priority")) {
					if (args.size() >= 2 && args.get(1).equalsIgnoreCase("indexes"))
						listPriorityIndexes(sender);
					else
						listPriority(sender);
				} else if (args.size() >= 1 && args.get(0).equalsIgnoreCase("claimed")) {
					if (args.size() >= 3 && args.get(1).equalsIgnoreCase("priority") && args.get(2).equalsIgnoreCase("indexes"))
						listClaimedPriorityIndexes(sender);
					else if (args.size() >= 2 && args.get(1).equalsIgnoreCase("indexes"))
						listClaimedIndexes(sender);
					else if (args.size() >= 2 && args.get(1).equalsIgnoreCase("priority"))
						listClaimedPriority(sender);
					else
						listClaimed(sender);
				} else
					sender.sendMessage(ChatColor.RED + BukkitUtil.colorCodeReplaceAll(getUsage()));
			} catch (final Exception e) {
				log.error("Failed to execute list command!", e);
				sender.sendMessage(getErrorMessage());
			}
		} else if (getManager().getConfig().getBoolean("general.canViewSubmittedReports", true)) {
			ArrayList<Integer> indexes = null;
			try {
				indexes = getManager().getViewableReports(sender);
			} catch (final Exception e) {
				sender.sendMessage(getErrorMessage());
				log.log(Level.ERROR, "Failed to list submitted reports!", e);
				return;
			}

			String indexesString = ArrayUtil.indexesToString(indexes, ChatColor.GOLD, ChatColor.WHITE);

			if (!indexesString.equals("")) {
				String out = ChatColor.WHITE + BukkitUtil.colorCodeReplaceAll(
						getManager().getLocale().getString(ListPhrases.listReportsAvailable));
				out = out.replaceAll("%i", ChatColor.GOLD + indexesString + ChatColor.WHITE);
				sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + out);
			} else
				sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() +
						ChatColor.RED + BukkitUtil.colorCodeReplaceAll(
						getManager().getLocale().getString(ListPhrases.listNoReportsAvailable)));
		} else
			sender.sendMessage(getFailedPermissionsMessage());
	}

	private void listClaimed(CommandSender sender) throws ClassNotFoundException, SQLException, InterruptedException {
		final StringBuilder query = new StringBuilder();
		query.append("SELECT COUNT(*) AS Count ");
		query.append("FROM Reports ");
		query.append("WHERE ClaimStatus = 1 AND ");

		if (BukkitUtil.isPlayer(sender)) {
			Player p = (Player) sender;
			query.append("ClaimedByUUID = '").append(p.getUniqueId()).append('\'');
		} else {
			query.append("ClaimedBy = '").append(sender.getName()).append('\'');
		}

		final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
		final int connectionId = database.openPooledConnection();
		try {
			final SQLResultSet result = database.sqlQuery(connectionId, query.toString());
			final int count = result.getInt("Count");
			String message = getManager().getLocale().getString(ListPhrases.listClaimed);
			message = message.replaceAll("%n", ChatColor.GOLD + Integer.toString(count) + ChatColor.WHITE);
			sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + message);
		} catch (SQLException e) {
			log.log(Level.ERROR, String.format("Failed to list claimed reports on connection [%d]!", connectionId));
			throw e;
		} finally {
			database.closeConnection(connectionId);
		}
	}

	private void listClaimedPriority(CommandSender sender) throws ClassNotFoundException, SQLException, InterruptedException {
		final StringBuilder queryFormat = new StringBuilder();
		queryFormat.append("SELECT COUNT(*) AS Count ");
		queryFormat.append("FROM Reports ");
		queryFormat.append("WHERE ClaimStatus = 1 ");
		if (BukkitUtil.isPlayer(sender)) {
			Player p = (Player) sender;
			queryFormat.append("AND ClaimedByUUID = '").append(p.getUniqueId()).append("' ");
		} else {
			queryFormat.append("AND ClaimedBy = '").append(sender.getName()).append("' ");
		}
		queryFormat.append("AND Priority = ");

		final String query = queryFormat.toString();

		int noPriorityCount = 0;
		int lowPriorityCount = 0;
		int normalPriorityCount = 0;
		int highPriorityCount = 0;

		final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
		final int connectionId = database.openPooledConnection();
		try {
			SQLResultSet result = database.sqlQuery(connectionId, query + ModLevel.NONE.getLevel());
			noPriorityCount = result.getInt("Count");

			result = database.sqlQuery(connectionId, query + ModLevel.LOW.getLevel());
			lowPriorityCount = result.getInt("Count");

			result = database.sqlQuery(connectionId, query + ModLevel.NORMAL.getLevel());
			normalPriorityCount = result.getInt("Count");

			result = database.sqlQuery(connectionId, query + ModLevel.HIGH.getLevel());
			highPriorityCount = result.getInt("Count");
		} catch (final SQLException e) {
			log.log(Level.ERROR, String.format("Failed to list claimed reports by priority on connection [%d]!", connectionId));
			throw e;
		} finally {
			database.closeConnection(connectionId);
		}

		printClaimedPriorityCount(sender, ModLevel.NONE, noPriorityCount);
		printClaimedPriorityCount(sender, ModLevel.LOW, lowPriorityCount);
		printClaimedPriorityCount(sender, ModLevel.NORMAL, normalPriorityCount);
		printClaimedPriorityCount(sender, ModLevel.HIGH, highPriorityCount);
	}

	private void printClaimedPriorityCount(CommandSender sender, ModLevel level, int count) {
		String format = getManager().getLocale().getString(ListPhrases.listClaimedPriorityCount);

		String output = format.replaceAll("%n", level.getColor() + Integer.toString(count) + ChatColor.WHITE);
		output = output.replaceAll("%p", level.getColor() + level.getName() + ChatColor.WHITE);

		sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + output);
	}

	private void listClaimedIndexes(CommandSender sender) throws ClassNotFoundException, SQLException, InterruptedException {
		final StringBuilder query = new StringBuilder();
		query.append("SELECT ID ");
		query.append("FROM Reports ");
		query.append("WHERE ClaimStatus = 1 AND ");

		if (BukkitUtil.isPlayer(sender)) {
			Player p = (Player) sender;
			query.append("ClaimedByUUID = '").append(p.getUniqueId()).append('\'');
		} else {
			query.append("ClaimedBy = '").append(sender.getName()).append('\'');
		}

		final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
		final int connectionId = database.openPooledConnection();
		try {
			SQLResultSet result = database.sqlQuery(connectionId, query.toString());
			String indexes = ArrayUtil.indexesToString(result, "ID", ChatColor.GOLD, ChatColor.WHITE);
			String message = getManager().getLocale().getString(ListPhrases.listClaimedIndexes);
			message = message.replaceAll("%i", indexes);

			sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + message);
		} catch (final SQLException e) {
			log.log(Level.ERROR, String.format("Failed to list claimed report indexes on connection [%d]!", connectionId));
			throw e;
		} finally {
			database.closeConnection(connectionId);
		}
	}

	private void listClaimedPriorityIndexes(CommandSender sender) throws ClassNotFoundException, SQLException, InterruptedException {
		final StringBuilder queryFormat = new StringBuilder();

		queryFormat.append("SELECT ID ");
		queryFormat.append("FROM Reports ");
		queryFormat.append("WHERE ");
		queryFormat.append("ClaimStatus = 1 ");
		if (BukkitUtil.isPlayer(sender)) {
			Player p = (Player) sender;
			queryFormat.append("AND ClaimedByUUID = '").append(p.getUniqueId()).append("' ");
		} else {
			queryFormat.append("AND ClaimedBy = '").append(sender.getName()).append("' ");
		}
		queryFormat.append("AND Priority = ");

		String noPriorityIndexes;
		String lowPriorityIndexes;
		String normalPriorityIndexes;
		String highPriorityIndexes;

		final String query = queryFormat.toString();

		SQLResultSet result;

		final ExtendedDatabaseHandler database = getManager().getDatabaseHandler();
		final int connectionId = database.openPooledConnection();
		try {
			result = database.sqlQuery(connectionId, query + ModLevel.NONE.getLevel());
			noPriorityIndexes = ArrayUtil.indexesToString(result, "ID", ModLevel.NONE.getColor(), ChatColor.WHITE);

			result = database.sqlQuery(connectionId, query + ModLevel.LOW.getLevel());
			lowPriorityIndexes = ArrayUtil.indexesToString(result, "ID", ModLevel.LOW.getColor(), ChatColor.WHITE);

			result = database.sqlQuery(connectionId, query + ModLevel.NORMAL.getLevel());
			normalPriorityIndexes = ArrayUtil.indexesToString(result, "ID", ModLevel.NORMAL.getColor(), ChatColor.WHITE);

			result = database.sqlQuery(connectionId, query + ModLevel.HIGH.getLevel());
			highPriorityIndexes = ArrayUtil.indexesToString(result, "ID", ModLevel.HIGH.getColor(), ChatColor.WHITE);
		} catch (final SQLException e) {
			log.log(Level.ERROR, String.format("Failed to list claimed report indexes by priority on connection [%d]!", connectionId));
			throw e;
		} finally {
			database.closeConnection(connectionId);
		}

		printClaimedPriorityIndexes(sender, ModLevel.NONE, noPriorityIndexes);
		printClaimedPriorityIndexes(sender, ModLevel.LOW, lowPriorityIndexes);
		printClaimedPriorityIndexes(sender, ModLevel.NORMAL, normalPriorityIndexes);
		printClaimedPriorityIndexes(sender, ModLevel.HIGH, highPriorityIndexes);
	}

	private void printClaimedPriorityIndexes(CommandSender sender, ModLevel level, String indexes) {
		String output;

		if (!indexes.equals("")) {
			output = getManager().getLocale().getString(ListPhrases.listClaimedPriorityIndexes);

			output = output.replaceAll("%p", level.getColor() + level.getName() + ChatColor.WHITE);
			output = output.replaceAll("%i", indexes);
		} else {
			output = getManager().getLocale().getString(ListPhrases.listNoClaimedPriorityIndexes);

			output = output.replaceAll("%p", level.getColor() + level.getName() + ChatColor.WHITE);
		}

		sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + output);
	}

	private void listPriority(CommandSender sender) {
		int noPriorityCount = 0;
		int lowPriorityCount = 0;
		int normalPriorityCount = 0;
		int highPriorityCount = 0;

		try {
			noPriorityCount = getManager().getNumberOfPriority(ModLevel.NONE);
			lowPriorityCount = getManager().getNumberOfPriority(ModLevel.LOW);
			normalPriorityCount = getManager().getNumberOfPriority(ModLevel.NORMAL);
			highPriorityCount = getManager().getNumberOfPriority(ModLevel.HIGH);
		} catch (final Exception e) {
			log.log(Level.ERROR, "Failed to list reports by priority!", e);
			sender.sendMessage(getErrorMessage());
			return;
		}

		printPriorityCount(sender, ModLevel.NONE, noPriorityCount);
		printPriorityCount(sender, ModLevel.LOW, lowPriorityCount);
		printPriorityCount(sender, ModLevel.NORMAL, normalPriorityCount);
		printPriorityCount(sender, ModLevel.HIGH, highPriorityCount);
	}

	private void printPriorityCount(CommandSender sender, ModLevel level, int count) {
		String format = getManager().getLocale().getString(ListPhrases.listPriorityCount);

		String output = format.replaceAll("%n", level.getColor() + Integer.toString(count) + ChatColor.WHITE);
		output = output.replaceAll("%p", level.getColor() + level.getName() + ChatColor.WHITE);

		sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + output);
	}

	private void listPriorityIndexes(CommandSender sender) {
		ArrayList<Integer> noPriorityIndexes;
		ArrayList<Integer> lowPriorityIndexes;
		ArrayList<Integer> normalPriorityIndexes;
		ArrayList<Integer> highPriorityIndexes;

		try {
			noPriorityIndexes = getManager().getIndexesOfPriority(ModLevel.NONE);
			lowPriorityIndexes = getManager().getIndexesOfPriority(ModLevel.LOW);
			normalPriorityIndexes = getManager().getIndexesOfPriority(ModLevel.NORMAL);
			highPriorityIndexes = getManager().getIndexesOfPriority(ModLevel.HIGH);
		} catch (final Exception e) {
			log.log(Level.ERROR, "Failed to list report indexes by priority!", e);
			sender.sendMessage(getErrorMessage());
			return;
		}

		printPriorityIndexes(sender, ModLevel.NONE, noPriorityIndexes);
		printPriorityIndexes(sender, ModLevel.LOW, lowPriorityIndexes);
		printPriorityIndexes(sender, ModLevel.NORMAL, normalPriorityIndexes);
		printPriorityIndexes(sender, ModLevel.HIGH, highPriorityIndexes);
	}

	private void printPriorityIndexes(CommandSender sender, ModLevel level, ArrayList<Integer> indexes) {
		String format, output;

		if (!indexes.isEmpty()) {
			format = getManager().getLocale().getString(ListPhrases.listPriorityIndexes);

			output = format.replaceAll("%p", level.getColor() + level.getName() + ChatColor.WHITE);
			output = output.replaceAll("%i", ArrayUtil.indexesToString(indexes, level.getColor(), ChatColor.WHITE));
		} else {
			format = getManager().getLocale().getString(ListPhrases.listNoReportsWithPriority);

			output = format.replaceAll("%p", level.getColor() + level.getName() + ChatColor.WHITE);
		}

		sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + output);
	}

	private void listCommand(CommandSender sender) {
		String listString = BukkitUtil.colorCodeReplaceAll(
				getManager().getLocale().getString(ListPhrases.reportList));

		int incompleteReports;
		int completeReports;
		try {
			incompleteReports = getManager().getIncompleteReports();
			completeReports = getManager().getCompletedReports();
		} catch (final Exception e) {
			log.log(Level.ERROR, "Failed to get number of complete and incomplete reports!", e);
			sender.sendMessage(getErrorMessage());
			return;
		}

		if (completeReports != -1 && incompleteReports != -1) {
			listString = listString.replaceAll("%r", ChatColor.RED + Integer.toString(incompleteReports) + ChatColor.WHITE);
			listString = listString.replaceAll("%c", ChatColor.GREEN + Integer.toString(completeReports) + ChatColor.WHITE);

			String[] parts = listString.split("%n");

			for (int LCV = 0; LCV < parts.length; LCV++) {
				listString = parts[LCV];
				sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + listString);
			}
		} else
			sender.sendMessage(getErrorMessage());
	}

	private void listIndexes(CommandSender sender) {
		ArrayList<Integer> completeIndexes;
		ArrayList<Integer> incompleteIndexes;
		try {
			completeIndexes = getManager().getCompletedReportIndexes();
			incompleteIndexes = getManager().getIncompleteReportIndexes();
		} catch (final Exception e) {
			log.log(Level.ERROR, "Failed to get number of complete and incomplete reports!", e);
			sender.sendMessage(getErrorMessage());
			return;
		}

		String complete = ArrayUtil.indexesToString(completeIndexes, ChatColor.GREEN, ChatColor.WHITE);
		String incomplete = ArrayUtil.indexesToString(incompleteIndexes, ChatColor.RED, ChatColor.WHITE);

		String out;

		if (!completeIndexes.isEmpty())
			out = BukkitUtil.colorCodeReplaceAll(
					getManager().getLocale().getString(ListPhrases.listReportCompleteIndexes));
		else
			out = BukkitUtil.colorCodeReplaceAll(
					getManager().getLocale().getString(ListPhrases.listReportNoCompleteIndexes));

		out = out.replaceAll("%i", complete);

		sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + out);

		if (!incompleteIndexes.isEmpty())
			out = BukkitUtil.colorCodeReplaceAll(
					getManager().getLocale().getString(ListPhrases.listReportIncompleteIndexes));
		else
			out = BukkitUtil.colorCodeReplaceAll(
					getManager().getLocale().getString(ListPhrases.listReportNoIncompleteIndexes));

		out = out.replaceAll("%i", incomplete);

		sender.sendMessage(ChatColor.BLUE + Reporter.getLogPrefix() + ChatColor.WHITE + out);
	}

	/**
	 * Updates the documentation for the command.
	 * <br/>
	 * This should be called after the locale has changed.
	 */
	@Override
	public void updateDocumentation() {
		ArrayList<ObjectPair<String, String>> usages = super.getUsages();

		usages.clear();

		String usage = "/report list [indexes]";
		String description = getManager().getLocale().getString(ListPhrases.listHelpDetails);

		ObjectPair<String, String> entry = new ObjectPair<String, String>(usage, description);

		usages.add(entry);

		usage = "/report list priority [indexes]";
		description = getManager().getLocale().getString(ListPhrases.listHelpPriorityDetails);

		entry = new ObjectPair<String, String>(usage, description);
		usages.add(entry);

		usage = "/report list claimed [indexes]";
		description = getManager().getLocale().getString(ListPhrases.listHelpClaimedDetails);

		entry = new ObjectPair<String, String>(usage, description);
		usages.add(entry);

		usage = "/report list claimed priority [indexes]";
		description = getManager().getLocale().getString(ListPhrases.listHelpClaimedPriorityDetails);

		entry = new ObjectPair<String, String>(usage, description);
		usages.add(entry);
	}
}
