package net.KabOOm356.Util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import net.KabOOm356.File.AbstractFiles.NetworkFile;
import org.w3c.dom.Element;

/**
 * A class to help with {@link URL} based input/output operations.
 */
public class UrlIO
{
	/** The Date format to use. */
	static SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
	/** The encoding to use when writing files. */
	public static Charset outputCharset = Charset.forName("UTF-8");
	
	/**
	 * Returns the response code from the given URLConnection.
	 * 
	 * @param connection The connection to get the response from.
	 * 
	 * @return The response code from the given connection.
	 * 
	 * @throws IOException
	 */
	public static int checkResponse(URLConnection connection) throws IOException
	{
		return ((HttpURLConnection) connection).getResponseCode();
	}
	
	/**
	 * Checks if the response from the given URLConnection is valid (200).
	 * 
	 * @param connection The URLConnection to get check if the response is valid to.
	 * 
	 * @return True if the response is valid (200), otherwise false.
	 * 
	 * @throws IOException
	 */
	public static boolean isResponseValid(URLConnection connection) throws IOException
	{
		return checkResponse(connection) == HttpURLConnection.HTTP_OK;
	}
	
	/**
	 * Downloads the given {@link NetworkFile} to the given destination.
	 * 
	 * @param abstractFile The {@link NetworkFile} to download.
	 * @param destination The destination to download the {@link NetworkFile} to.
	 * 
	 * @return Returns true if the file is successfully downloaded.
	 * 
	 * @throws IOException
	 */
	public static boolean downloadFile(NetworkFile abstractFile, File destination) throws IOException
	{
		if(abstractFile == null && destination == null)
			throw new IllegalArgumentException("Both the abstract file and destination file cannot be null!");
		else if(abstractFile == null)
			throw new IllegalArgumentException("The abstract file cannot be null!");
		else if(destination == null)
			throw new IllegalArgumentException("The destination file cannot be null!");
		
		if(!destination.exists())
			destination.createNewFile();
		
		URL url = new URL(abstractFile.getURL());
		
		URLConnection dlUrlConnection = url.openConnection();
		HttpURLConnection connection = (HttpURLConnection)dlUrlConnection;
		int response;
		
		if((response = connection.getResponseCode()) != 200)
			throw new IOException("Connection response " + response + " is not allowed!");
		
		BufferedReader in = null;
		BufferedWriter out = null;
		
		try
		{
			in = new BufferedReader(new InputStreamReader(connection.getInputStream(), abstractFile.getEncoding()));
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destination), outputCharset.name()));
			
			String line;
			
			while((line = in.readLine()) != null)
			{
				out.write(line);
				out.newLine();
			}
			
			destination.setLastModified(new Date().getTime());
		}
		finally
		{
			try
			{
				out.close();
			}
			catch(IOException e)
			{
			}
			
			try
			{
				in.close();
			}
			catch(IOException e)
			{
			}
			
			connection.disconnect();
		}
		
		return true;
	}
	
	/**
	 * Returns a String representation of a file's version given it's file name without the extension.
	 * 
	 * @param fileName The file name of the file without the extension to get the version from.
	 * 
	 * @return A String representation of the version parsed from the given file name.
	 */
	public static String getVersion(String fileName)
	{
		if(fileName.contains(" v"))
			return fileName.substring(fileName.lastIndexOf(" v") + 2);
		else if(fileName.contains(" V"))
			return fileName.substring(fileName.lastIndexOf(" V") + 2);
		else if(fileName.contains(" "))
		{
			int lastIndex = fileName.lastIndexOf(" ");
			String version = fileName.substring(lastIndex + 1);
			fileName = fileName.substring(0, lastIndex);
			
			/*
			 * If the release level is appended behind the version separated with a space
			 * append it to the numeral version and re-parse for a space.
			 */
			if(version.equalsIgnoreCase("alpha") || version.equalsIgnoreCase("beta") || version.equalsIgnoreCase("rc"))
				version = getVersion(fileName + "-" + version);
			
			return version;
		}
		else return "";
	}
	
	/**
	 * Returns the node value from the given {@link Element} with the given tag name.
	 * <br/>
	 * If the given tag name cannot be found the given default value will be returned.
	 * 
	 * @param element The {@link Element} to get the given node value from.
	 * @param tagName The tag name to get from the given {@link Element}.
	 * @param defaultValue The default value to return if the given tag name is not set in the given {@link Element}.
	 * 
	 * @return The node value for the given tag name if it is set, otherwise the default value is returned.
	 */
	public static String getNodeValue(Element element, String tagName, String defaultValue)
	{
		return (getNodeValue(element, tagName) != null) ? getNodeValue(element, tagName) : defaultValue;
	}
	
	/**
	 * Returns the node value from the given {@link Element} with the given tag name.
	 * 
	 * @param element The {@link Element} to get the given node value from.
	 * @param tagName The tag name to get from the given {@link Element}.
	 * 
	 * @return The node value for the given tag name if it is set, otherwise null.
	 */
	public static String getNodeValue(Element element, String tagName)
	{
		return element.getElementsByTagName(tagName).item(0).getChildNodes().item(0).getNodeValue();
	}
}
