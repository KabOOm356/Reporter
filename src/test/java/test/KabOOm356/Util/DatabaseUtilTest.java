package test.KabOOm356.Util;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.DatabaseType;
import net.KabOOm356.Util.DatabaseUtil;
import org.junit.Test;
import org.mockito.Mock;
import test.test.PowerMockitoTest;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.when;

public class DatabaseUtilTest extends PowerMockitoTest {
	@Mock
	private Database database;

	@Test
	public void testGetAutoIncrementingPrimaryKeyQueryMySQL() {
		when(database.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
		final String returned = DatabaseUtil.getAutoIncrementingPrimaryKeyQuery(database, "ID");
		assertTrue(returned.contains("ID"));
		assertTrue(returned.contains("AUTO_INCREMENT"));
	}

	@Test
	public void testGetAutoIncrementingPrimaryKeyQuerySQLite() {
		when(database.getDatabaseType()).thenReturn(DatabaseType.SQLITE);
		final String returned = DatabaseUtil.getAutoIncrementingPrimaryKeyQuery(database, "ID");
		assertTrue(returned.contains("ID"));
		assertFalse(returned.contains("AUTO_INCREMENT"));
	}

	@Test
	public void testGetAutoIncrementingPrimaryKeySuffixMySQL() {
		when(database.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
		final String returned = DatabaseUtil.getAutoIncrementingPrimaryKeySuffix(database);
		assertEquals(" AUTO_INCREMENT", returned);
	}

	@Test
	public void testGetAutoIncrementingPrimaryKeySuffixSQLite() {
		when(database.getDatabaseType()).thenReturn(DatabaseType.SQLITE);
		final String returned = DatabaseUtil.getAutoIncrementingPrimaryKeySuffix(database);
		assertEquals("", returned);
	}

	@Test
	public void testGetColumnSizeNameMySQL() {
		when(database.getDatabaseType()).thenReturn(DatabaseType.MYSQL);
		final String returned = DatabaseUtil.getColumnsSizeName(database);
		assertEquals("COLUMN_SIZE", returned);
	}

	@Test
	public void testGetColumnSizeNameSQLite() {
		when(database.getDatabaseType()).thenReturn(DatabaseType.SQLITE);
		final String returned = DatabaseUtil.getColumnsSizeName(database);
		assertEquals("TYPE_NAME", returned);
	}
}
