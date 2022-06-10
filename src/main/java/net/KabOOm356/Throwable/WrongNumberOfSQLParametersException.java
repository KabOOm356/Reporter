package net.KabOOm356.Throwable;

import java.sql.SQLException;

public class WrongNumberOfSQLParametersException extends SQLException {
  private static final long serialVersionUID = -1746576651777309541L;

  public WrongNumberOfSQLParametersException(final String message) {
    super(message);
  }
}
