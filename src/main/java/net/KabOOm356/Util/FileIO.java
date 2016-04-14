package net.KabOOm356.Util;

import net.KabOOm356.File.RevisionFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

/**
 * A class to help with file input and output.
 */
public class FileIO {
	/**
	 * The encoding to always use when writing to a file.
	 */
	public static final String encoding = "UTF-8";
	public static final String BACKUP_FILE_EXTENSION = ".backup";
	private static final Logger log = LogManager.getLogger(FileIO.class);

	/**
	 * Copies a given text {@link File} to another {@link File}.
	 * <br /><br />
	 * Input file is assumed to be encoded in UTF-8.
	 * <br />
	 * Output file is written in UTF-8
	 *
	 * @param in  The {@link File} to copy.
	 * @param out The {@link File} to copy to.
	 * @throws IOException
	 * @throws IllegalArgumentException If either the file to read from or the file to write to are null.
	 * @see FileIO#copyTextFile(File, File, String, String)
	 */
	public static void copyTextFile(final File in, final File out) throws IOException {
		copyTextFile(in, out, encoding, encoding);
	}

	/**
	 * Copies a given text {@link File} to another {@link File}.
	 *
	 * @param in          The {@link File} to copy.
	 * @param out         The {@link File} to copy to.
	 * @param inEncoding  The encoding of the file to be copied.
	 * @param outEncoding The encoding to write the file to.
	 * @throws IOException
	 * @throws IllegalArgumentException If either the file to read from or the file to write to are null.
	 */
	public static void copyTextFile(final File in, final File out, final String inEncoding, final String outEncoding) throws IOException {
		if (in == null) {
			throw new IllegalArgumentException("Input file cannot be null!");
		}
		if (out == null) {
			throw new IllegalArgumentException("Output file cannot be null!");
		}

		if (!out.exists()) {
			out.createNewFile();
		}

		if (!in.exists()) {
			in.createNewFile();
		}

		BufferedReader input = null;
		BufferedWriter output = null;
		String line;

		try {
			input = new BufferedReader(new InputStreamReader(in.toURI().toURL().openStream(), inEncoding));
			output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out), outEncoding));

			while ((line = input.readLine()) != null) {
				output.write(line);
				output.newLine();
			}

			output.flush();
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (final IOException e) {
				if (log.isDebugEnabled()) {
					log.warn("Closing input failed!", e);
				}
			}

			try {
				if (output != null) {
					output.close();
				}
			} catch (final IOException e) {
				if (log.isDebugEnabled()) {
					log.warn("Closing output failed!", e);
				}
			}
		}
	}

	/**
	 * Creates a backup.
	 *
	 * @param file The file to backup.
	 * @return A {@link RevisionFile} where the given File was saved.
	 */
	public static RevisionFile createBackup(final File file) {
		RevisionFile backup = null;

		try {
			backup = new RevisionFile(file.getParent(), file.getName() + BACKUP_FILE_EXTENSION);
			backup.incrementToNextRevision();
			backup.createNewFile();
			FileIO.copyTextFile(file, backup.getFile());
		} catch (final Exception e) {
			log.error("Failed to create backup file for " + file.getName(), e);
			if (backup != null) {
				backup.delete();
			}
			return null;
		}

		return backup;
	}
}
