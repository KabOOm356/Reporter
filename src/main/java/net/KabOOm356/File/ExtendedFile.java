package net.KabOOm356.File;

import java.io.File;
import java.io.IOException;

/**
 * A class designed to extend the Java {@link File} class.
 * <br /><br />
 * Specifically made to set a {@link File} object to a new location without having to build a new {@link File} object.
 */
public class ExtendedFile {
	/**
	 * The Java {@link File} object.
	 */
	private File file;

	/**
	 * Constructor.
	 *
	 * @param parent The Java {@link File} that will be the parent or directory.
	 * @param child  The name of this ExtendedFile.
	 */
	public ExtendedFile(File parent, String child) {
		file = new File(parent, child);
	}

	/**
	 * Constructor.
	 *
	 * @param parent The path that will be the parent or directory.
	 * @param child  The name of this ExtendedFile.
	 */
	public ExtendedFile(String parent, String child) {
		file = new File(parent, child);
	}

	/**
	 * Constructor.
	 *
	 * @param file The name of this ExtendedFile.
	 */
	public ExtendedFile(String file) {
		this.file = new File(file);
	}

	/**
	 * Sets the parent or directory to a new location.
	 *
	 * @param parent The new parent location.
	 */
	public void setParent(File parent) {
		file = new File(parent, file.getName());
	}

	/**
	 * Returns the current {@link File} that is being used.
	 *
	 * @return The current {@link File} that is being used.
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @see File#createNewFile()
	 */
	public boolean createNewFile() throws IOException {
		return file.createNewFile();
	}

	// API for original Java File

	/**
	 * @see File#renameTo(File)
	 */
	public boolean renameTo(File dest) {
		return file.renameTo(dest);
	}

	/**
	 * @see File#delete()
	 */
	public boolean delete() {
		return file.delete();
	}

	/**
	 * @see File#exists()
	 */
	public boolean exists() {
		return file.exists();
	}

	/**
	 * @see File#getName()
	 */
	public String getName() {
		return file.getName();
	}

	/**
	 * Changes the name of this ExtendedFile.
	 *
	 * @param child The new name of this ExtendedFile.
	 */
	public void setName(String child) {
		String parent = file.getParent();

		if (parent != null)
			file = new File(file.getParent(), child);
		else
			file = new File(child);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String str = "Current File: " + file.getAbsolutePath();
		str += "\nParent: " + file.getParent();
		str += "\nChild: " + file.getName();
		return str;
	}
}
