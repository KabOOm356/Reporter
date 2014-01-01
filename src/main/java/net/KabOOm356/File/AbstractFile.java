package net.KabOOm356.File;

import java.util.Date;

/**
 * Holds information about a file that is not actually on the disk.
 */
public abstract class AbstractFile
{
	/** The default character set. */
	public static String defaultCharset = "UTF-8";
	
	/** The name of the file. */
	private String name;
	/** The encoding of the file. */
	private String encoding;
	/** The extension of the file. */
	private String extension;
	/** The file name as it would appear on the disk. */
	private String fileName;
	/** The last time the file was modified. */
	private Date modificationDate;
	
	/**
	 * AbstractFile Constructor.
	 * 
	 * @param fileName The file name of the file.
	 */
	public AbstractFile(String fileName)
	{
		this.name = AbstractFile.getName(fileName);
		this.extension = AbstractFile.getExtension(fileName);
		this.fileName = fileName;
		this.encoding = defaultCharset;
		this.modificationDate = null;
	}
	
	/**
	 * AbstractFile Constructor.
	 *
	 * @param fileName The file name of the file.
	 * @param encoding The encoding of the file.
	 */
	public AbstractFile(String fileName, String encoding)
	{
		this.name = AbstractFile.getName(fileName);
		this.extension = AbstractFile.getExtension(fileName);
		this.fileName = fileName;
		this.encoding = encoding;
		this.modificationDate = null;
	}
	
	/**
	 * AbstractFile Constructor.
	 *
	 * @param name The name of the file.
	 * @param extension The extension of the file.
	 * @param fileName The file name of the file.
	 */
	public AbstractFile(String name, String extension, String fileName)
	{
		this.name = name;
		this.encoding = defaultCharset;
		this.extension = extension;
		this.fileName = fileName;
		this.modificationDate = null;
	}
	
	/**
	 * AbstractFile Constructor.
	 *
	 * @param name The name of the file.
	 * @param extension The extension of the file.
	 * @param fileName The file name of the file.
	 * @param encoding The encoding of the file.
	 */
	public AbstractFile(String name, String extension, String fileName, String encoding)
	{
		this.name = name;
		this.encoding = encoding;
		this.extension = extension;
		this.fileName = fileName;
		this.modificationDate = null;
	}
	
	/**
	 * AbstractFile Constructor.
	 *
	 * @param name The name of the file.
	 * @param extension The extension of the file.
	 * @param fileName The file name of the file.
	 * @param modificationDate The last time the file was modified.
	 */
	public AbstractFile(String name, String extension, String fileName, Date modificationDate)
	{
		this.name = name;
		this.extension = extension;
		this.encoding = defaultCharset;
		this.fileName = fileName;
		this.modificationDate = modificationDate;
	}
	
	/**
	 * AbstractFile Constructor.
	 *
	 * @param name The name of the file.
	 * @param extension The extension of the file.
	 * @param fileName The file name of the file.
	 * @param encoding The encoding of the file.
	 * @param modificationDate The last time the file was modified.
	 */
	public AbstractFile(String name, String extension, String fileName, String encoding, Date modificationDate)
	{
		this.name = name;
		this.extension = extension;
		this.fileName = fileName;
		this.encoding = encoding;
		this.modificationDate = modificationDate;
	}
	
	/**
	 * @return The name of the file.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * @return The time the file was last modified.
	 */
	public Date getModificationDate()
	{
		return modificationDate;
	}
	
	/**
	 * Sets the time this file was last modified.
	 *
	 * @param modificationDate The date this file was last modified.
	 */
	public void setModificationDate(Date modificationDate)
	{
		this.modificationDate = modificationDate;
	}
	
	/**
	 * @return The extension of the file.
	 */
	public String getExtension()
	{
		return extension;
	}
	
	/**
	 * @return The file name of this file.
	 */
	public String getFileName()
	{
		return fileName;
	}
	
	/**
	 * @return The encoding of the file.
	 */
	public String getEncoding()
	{
		return encoding;
	}
	
	/**
	 * Returns the name from the given file name.
	 * 
	 * @param fileName The file name to get the name of.
	 * 
	 * @return The name from the file name given.
	 */
	private static String getName(String fileName)
	{
		int index = fileName.lastIndexOf(".");
		if(index != -1)
			return fileName.substring(0, index);
		return fileName;
	}
	
	/**
	 * Returns the extension from the given file name.
	 * 
	 * @param fileName The file name to get the extension of.
	 * 
	 * @return The extension from the file name given.
	 */
	private static String getExtension(String fileName)
	{
		int index = fileName.lastIndexOf(".");
		if(index != -1 && index+1 < fileName.length())
			return fileName.substring(index+1, fileName.length());
		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		String string = "Name: " + getName();
		string += "\nExtension: " + getExtension();
		string += "\nFile Name: " + getFileName();
		string += "\nEncoding: " + encoding;
		string += "\nModification Date: " + getModificationDate();
		
		return string;
	}
}
