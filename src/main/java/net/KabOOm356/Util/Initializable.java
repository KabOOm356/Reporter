package net.KabOOm356.Util;

/** An interface to determine if an Object is initialized or not. */
public interface Initializable {
  /** Default value for whether an Object is initialized. */
  boolean isInitialized = false;

  /**
   * Gets whether this Object is initialized or not.
   *
   * @return True if the Object is initialized, otherwise false.
   */
  boolean isInitialized();

  /** Sets this Object to either being initialized or not. */
  void initialized();
}
