package net.KabOOm356.Util;

import net.KabOOm356.Database.Database;
import net.KabOOm356.Database.DatabaseType;
import net.KabOOm356.Throwable.WrongNumberOfSQLParametersException;
import org.junit.Test;
import org.mockito.Mock;
import test.test.MockitoTest;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DatabaseUtilTest extends MockitoTest {
	private static final char queryParameter = DatabaseUtil.queryParameter;
	private static final String query = "SELECT * FROM table WHERE x=" + queryParameter + " AND y=" + queryParameter;

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

	@Test
	public void testCheckPreparedStatementParametersEmpty() {
		final List<String> parameters = new ArrayList<>();
		assertTrue(DatabaseUtil.checkPreparedStatementParameters("", parameters));
	}

	@Test
	public void testCheckPreparedStatementParameters() {
		final List<String> parameters = new ArrayList<>();
		parameters.add("param1");
		parameters.add("param2");
		assertTrue(DatabaseUtil.checkPreparedStatementParameters(query, parameters));
	}

	@Test
	public void testCheckPreparedStatementParametersInEqual() {
		final List<String> parameters = new ArrayList<>();
		parameters.add("param1");
		assertFalse(DatabaseUtil.checkPreparedStatementParameters(query, parameters));
	}

	@Test
	public void testBindParametersToPreparedStatement() throws SQLException {
		final PreparedStatement preparedStatement = mock(PreparedStatement.class);
		final List<String> parameters = new ArrayList<>();
		parameters.add("param1");
		parameters.add("param2");
		DatabaseUtil.bindParametersToPreparedStatement(preparedStatement, query, parameters);
		verify(preparedStatement).setString(1, parameters.get(0));
		verify(preparedStatement).setString(2, parameters.get(1));
	}

	@Test(expected = WrongNumberOfSQLParametersException.class)
	public void testBindParametersToPreparedStatementWrongNumberOfParameters() throws SQLException {
		final PreparedStatement preparedStatement = mock(PreparedStatement.class);
		final List<String> parameters = new ArrayList<>();
		parameters.add("param1");
		DatabaseUtil.bindParametersToPreparedStatement(preparedStatement, query, parameters);
	}

	@Test
	public void testBindParametersToPreparedStatementSQLException() throws SQLException {
		final PreparedStatement preparedStatement = mock(PreparedStatement.class);
		final List<String> parameters = new ArrayList<>();
		parameters.add("param1");
		parameters.add("param2");
		doNothing().when(preparedStatement).setString(1, parameters.get(0));
		doThrow(new SQLException("Test Exception")).when(preparedStatement).setString(2, parameters.get(1));
		try {
			DatabaseUtil.bindParametersToPreparedStatement(preparedStatement, query, parameters);
		} catch (final SQLException e) {
			// We don't want this type of exception.
			if (e instanceof WrongNumberOfSQLParametersException) {
				throw e;
			}
		}
	}
}
