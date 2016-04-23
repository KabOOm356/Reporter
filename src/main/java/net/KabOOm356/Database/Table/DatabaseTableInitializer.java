package net.KabOOm356.Database.Table;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;

public abstract class DatabaseTableInitializer {
	private static final Logger log = LogManager.getLogger(DatabaseTableInitializer.class);

	public void initialize() throws InterruptedException, SQLException, ClassNotFoundException {
		if (log.isDebugEnabled()) {
			log.trace("Begin table initialization!");
		}
		try {
			create();
			migrate();
			update();
		} catch (final InterruptedException e) {
			log.warn("Failed to initialize table!");
			throw e;
		} catch (final SQLException e) {
			log.warn("Failed to initialize table!");
			throw e;
		} catch (final ClassNotFoundException e) {
			log.warn("Failed to initialize table!");
			throw e;
		}
	}

	protected void create() throws InterruptedException, SQLException, ClassNotFoundException {
		final DatabaseTableCreator creator = getCreator();
		if (creator != null) {
			creator.create();
		} else if (log.isDebugEnabled()) {
			log.warn("No table creator given!");
		}
	}

	protected void migrate() throws InterruptedException, SQLException, ClassNotFoundException {
		final DatabaseTableMigrator migrator = getMigrator();
		if (migrator != null) {
			migrator.migrate();
		} else if (log.isDebugEnabled()) {
			log.warn("No table migrator given!");
		}
	}

	protected void update() throws InterruptedException, SQLException, ClassNotFoundException {
		final DatabaseTableUpdater updater = getUpdater();
		if (updater != null) {
			updater.update();
		} else if (log.isDebugEnabled()) {
			log.warn("No table updater given!");
		}
	}

	protected abstract DatabaseTableCreator getCreator();

	protected abstract DatabaseTableMigrator getMigrator();

	protected abstract DatabaseTableUpdater getUpdater();
}
