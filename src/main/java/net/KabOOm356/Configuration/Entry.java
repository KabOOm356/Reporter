package net.KabOOm356.Configuration;

public class Entry<T> {
  /** The complete path to the entry. */
  private final String path;

  /** The default value to use if the entry is not present. */
  private final T def;

  public Entry(final String path, final T def) {
    this.path = path;
    this.def = def;
  }

  /**
   * Returns the path to this entry.
   *
   * @return The path to this entry.
   */
  public String getPath() {
    return path;
  }

  /**
   * Returns the default value for this entry.
   *
   * @return The default value for this entry.
   */
  public T getDefault() {
    return def;
  }

  @Override
  public String toString() {
    return "Path: " + getPath() + "\nDefault: " + getDefault();
  }
}
