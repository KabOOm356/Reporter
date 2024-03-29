package net.KabOOm356.File;

import java.io.File;

/**
 * A special type of {@link ExtendedFile} that has revisions and takes care of <br>
 * renaming and pointing to other revisions of the {@link ExtendedFile}. <br>
 * <br>
 * <b>NOTE:</b> Revision 0 is the base file.
 */
public class RevisionFile extends ExtendedFile {
  /** The base name of the file. */
  private final String name;

  /** The current revision number. */
  private int revision;

  /**
   * Constructor.
   *
   * @param parent The parent abstract pathname.
   * @param child The child pathname string.
   * @param revision The starting revision.
   */
  public RevisionFile(final File parent, final String child, final int revision) {
    super(parent, child);

    if (revision < 0) {
      throw new IllegalArgumentException("Revision number must be greater than zero!");
    }

    this.name = child;

    this.revision = revision;

    this.setRevision(revision);
  }

  /**
   * Constructor.
   *
   * @param parent The parent abstract pathname.
   * @param child The child pathname string.
   */
  public RevisionFile(final File parent, final String child) {
    super(parent, child);

    this.name = child;

    this.revision = 0;

    this.setRevision(revision);
  }

  /**
   * Constructor.
   *
   * @param parent The parent abstract pathname.
   * @param child The child pathname string.
   */
  public RevisionFile(final String parent, final String child) {
    super(parent, child);

    this.name = child;

    this.revision = 0;

    this.setRevision(0);
  }

  /**
   * Constructor.
   *
   * @param file The name of this RevisionFile.
   */
  public RevisionFile(final String file) {
    super(file);

    this.name = file;

    this.revision = 0;

    this.setRevision(0);
  }

  /**
   * Returns the current revision number.
   *
   * @return The current revision number.
   */
  public int getRevision() {
    return revision;
  }

  /**
   * Sets the revision number.
   *
   * @param revision The revision number to place this to.
   * @return True if the revision exists, otherwise false.
   */
  public boolean setRevision(final int revision) {
    if (revision < 0) {
      throw new IllegalArgumentException("Revision number must be greater than zero!");
    }

    this.revision = revision;

    if (revision != 0) {
      final int index = name.indexOf('.');

      if (index != -1) {
        this.setName(name.substring(0, index) + revision + name.substring(index));
      } else {
        this.setName(name + revision);
      }
    } else {
      this.setName(name);
    }

    return this.exists();
  }

  /**
   * Sets this to the next revision.
   *
   * @return True if the file exists, otherwise false.
   */
  public boolean incrementRevision() {
    this.setRevision(revision + 1);
    return super.exists();
  }

  /**
   * Sets this to the previous revision.
   *
   * @return True if the file exists, otherwise false.
   */
  public boolean decrementRevision() {
    if (this.revision != 0) {
      this.setRevision(revision - 1);
    }

    return super.exists();
  }

  /** Sets this to the latest revision. */
  public void incrementToLatestRevision() {
    this.incrementToNextRevision();
    this.decrementRevision();
  }

  /** Sets this to the next available revision. */
  public void incrementToNextRevision() {
    if (this.exists()) {
      //noinspection StatementWithEmptyBody
      while (this.incrementRevision()) {}
    }
  }

  /** Sets this to the base revision. */
  public void toBaseRevision() {
    this.setRevision(0);
  }

  /**
   * Returns the name of the base file.
   *
   * @return The base file name.
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Returns the name of the file.
   *
   * @return The name of the file.
   */
  public String getFileName() {
    return super.getName();
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = super.toString() + '\n';
    str += "Revision of file: " + name;
    str += "\nRevision Number: " + revision;
    return str;
  }
}
