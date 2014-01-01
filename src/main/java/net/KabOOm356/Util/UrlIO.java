package net.KabOOm356.Util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.rmi.ConnectIOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


import net.KabOOm356.File.AbstractFiles.NetworkFile;
import net.KabOOm356.File.AbstractFiles.UpdateSite;
import net.KabOOm356.File.AbstractFiles.VersionedNetworkFile;
import net.KabOOm356.File.AbstractFiles.VersionedNetworkFile.ReleaseLevel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
	 * Checks the given {@link UpdateSite} for an entry with the given name.
	 * 
	 * @param name The name of the entry to search for.
	 * @param updateSite The {@link UpdateSite} to use.
	 * @param localVersion The local version of the entity to search for.
	 * @param lowestLevel The lowest {@link ReleaseLevel} that will be allowed.
	 * 
	 * @return If there is an update a {@link VersionedNetworkFile} pointing to the newer file, otherwise null.
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws ParseException
	 * @throws IOException
	 */
	public static VersionedNetworkFile checkForUpdate(String name, UpdateSite updateSite, String localVersion, ReleaseLevel lowestLevel) throws ParserConfigurationException, SAXException, ParseException, IOException
	{
		URL url = new URL(updateSite.getURL());
		
		if(url != null && isResponseValid(url))
		{
			VersionedNetworkFile remoteFile = findLatestFile(updateSite, name, lowestLevel);
			
			if(VersionedNetworkFile.compareVersionTo(localVersion, remoteFile.getVersion()) < 0)
				return remoteFile;
		}
		
		return null;
	}
	
	/**
	 * Returns the response code from the given URL.
	 * 
	 * @param url The URL to get the response from.
	 * 
	 * @return The response code from attempting to connect to the given URL.
	 * 
	 * @throws IOException
	 */
	public static int checkResponse(URL url) throws IOException
	{
		HttpURLConnection connection = null;
		
		try
		{
			connection = (HttpURLConnection) url.openConnection();
			
			return connection.getResponseCode();
		}
		finally
		{
			if(connection != null)
				connection.disconnect();
		}
	}
	
	/**
	 * Checks if the response from the given URL is valid (200).
	 * 
	 * @param url The URL to get check if the response is valid to.
	 * 
	 * @return True if the response is valid (200), otherwise false.
	 * 
	 * @throws IOException
	 */
	public static boolean isResponseValid(URL url) throws IOException
	{
		return checkResponse(url) == HttpURLConnection.HTTP_OK;
	}
	
	/**
	 * Initiates the download process.
	 * 
	 * @param name The name of the entry to search for.
	 * @param updateSite The {@link UpdateSite} to use.
	 * @param destination The destination to download the file to.
	 * @param lowestLevel The lowest {@link ReleaseLevel} that will be allowed.
	 * 
	 * @return If the given file is successfully downloaded a {@link VersionedNetworkFile} representation of the file downloaded, otherwise null.
	 * 
	 * @throws FileNotFoundException Thrown if there are no entries that match the given name on the given {@link UpdateSite}.
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws ParseException
	 */
	public static VersionedNetworkFile downloadProcess(String name, UpdateSite updateSite, File destination, ReleaseLevel lowestLevel) throws FileNotFoundException, IOException, ParserConfigurationException, SAXException, ParseException
	{
		VersionedNetworkFile file = findLatestFile(updateSite, name, lowestLevel);
		
		if(downloadFile(file, destination))
			return file;
		return null;
	}
	
	/**
	 * Finds the latest file with the given name on the given {@link UpdateSite}.
	 * 
	 * @param name The name of the entry to search for.
	 * @param updateSite The {@link UpdateSite} to use.
	 * @param lowestLevel The lowest {@link ReleaseLevel} that will be allowed.
	 * 
	 * @return A {@link VersionedNetworkFile} representation of the latest file.
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws ParseException
	 */
	public static VersionedNetworkFile findLatestFile(UpdateSite updateSite, String name, ReleaseLevel lowestLevel) throws MalformedURLException, IOException, ParserConfigurationException, SAXException, ParseException
	{
		URL url = new URL(updateSite.getURL());
		
		if(url != null && isResponseValid(url))
		{
			if(updateSite.getType() == UpdateSite.Type.XML)
				return findLatestLocaleFileFromXML(url, name, lowestLevel);
			else if(updateSite.getType() == UpdateSite.Type.RSS)
				return findLatestPluginFromRSS(url, name, lowestLevel);
		}
		else
			throw new ConnectIOException("Error connecting to the given update site!");
		
		return null;
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
	 * Finds the latest locale file from an XML {@link UpdateSite}.
	 * <br/><br/>
	 * NOTE: This will throw a {@link FileNotFoundException} if no files are found with the given name. 
	 * 
	 * @param url The URL of the XML file to search.
	 * @param name The name of the locale file to search for.
	 * @param lowestLevel The lowest {@link ReleaseLevel} that will be allowed.
	 * 
	 * @return A {@link VersionedNetworkFile} representation of the latest locale file found.
	 * 
	 * @throws FileNotFoundException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws ParseException
	 * @throws IOException
	 */
	public static VersionedNetworkFile findLatestLocaleFileFromXML(URL url, String name, ReleaseLevel lowestLevel) throws SAXException, ParserConfigurationException, ParseException, IOException
	{
		if(url == null)
		{
			if(name == null || name.equals(""))
				throw new IllegalArgumentException("Both the URL and the name cannot be null!");
			throw new IllegalArgumentException("The URL cannot be null!");
		}
		else if(name == null || name.equals(""))
			throw new IllegalArgumentException("File name to search for cannot be null!");
		
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openStream());
		
		doc.getDocumentElement().normalize();
		
		NodeList nodeList = doc.getElementsByTagName("locale");
		
		VersionedNetworkFile file = null;
		VersionedNetworkFile latestFile = null;
		
		for(int LCV = 0; LCV < nodeList.getLength(); LCV++)
		{
			Node nNode = nodeList.item(LCV);
			if (nNode.getNodeType() == Node.ELEMENT_NODE)
			{
				Element element = (Element) nNode;
				
				if(Util.startsWithIgnoreCase(getNodeValue(element, "file_name"), name))
				{
					String fileName = getNodeValue(element, "file_name");
					String version = getNodeValue(element, "version");
					String encoding = getNodeValue(element, "encoding");
					String link = getNodeValue(element, "download_link");
					
					file = new VersionedNetworkFile(fileName, version, encoding, link);
					
					// The release level must be greater than or equal to the lowest release level specified.
					if(file.getReleaseLevel().compareToByValue(lowestLevel) >= 0)
					{
						// If latestFile is not initialized, set latestFile to the current file.
						// If the current file's version is greater than the latestFile's version, set latestFile to the current file.
						if(latestFile == null || latestFile.compareVersionTo(file) < 0)
							latestFile = file;
					}
				}
			}
		}
		
		if(latestFile == null)
			throw new FileNotFoundException("File " + name + " could not be found!");
		
		return latestFile;
	}
	
	/**
	 * Finds the latest plugin release from an RSS {@link UpdateSite}.
	 * <br/><br/>
	 * NOTE: This will throw a {@link FileNotFoundException} if no files are found with the given name. 
	 * 
	 * @param url The URL of the RSS file to search.
	 * @param name The name of the plugin file to search for.
	 * @param lowestLevel The lowest {@link ReleaseLevel} that will be allowed.
	 * 
	 * @return A {@link VersionedNetworkFile} representation of the latest plugin file found.
	 * 
	 * @throws FileNotFoundException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws ParseException
	 */
	public static VersionedNetworkFile findLatestPluginFromRSS(URL url, String name, ReleaseLevel lowestLevel) throws SAXException, IOException, ParserConfigurationException, ParseException
	{
		if(url == null)
		{
			if(name == null || name.equals(""))
				throw new IllegalArgumentException("Both the URL and the name cannot be null!");
			throw new IllegalArgumentException("The URL cannot be null!");
		}
		else if(name == null || name.equals(""))
			throw new IllegalArgumentException("File name to search for cannot be null!");
		
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openStream());
		
		doc.getDocumentElement().normalize();
			
		NodeList nodeList = doc.getElementsByTagName("item");
		
		String fileName = name + ".jar";
		
		VersionedNetworkFile file = null;
		VersionedNetworkFile latestFile = null;
		
		for(int LCV = 0; LCV < nodeList.getLength(); LCV++)
		{
			Node nNode = nodeList.item(LCV);
			if (nNode.getNodeType() == Node.ELEMENT_NODE)
			{
				Element element = (Element) nNode;
				
				if(Util.startsWithIgnoreCase(getNodeValue(element, "title"), name))
				{
					String version = getVersion(getNodeValue(element, "title"));
					String link = getNodeValue(element, "link");

					file = new VersionedNetworkFile(fileName, version, link);
					
					// The release level must be greater than or equal to the lowest release level specified.
					if(file.getReleaseLevel().compareToByValue(lowestLevel) >= 0)
					{
						// If latestFile is not initialized, set latestFile to the current file.
						// If the current file's version is greater than the latestFile's version, set latestFile to the current file.
						if(latestFile == null || latestFile.compareVersionTo(file) < 0)
							latestFile = file;
					}
				}
			}
		}
		
		if(latestFile == null)
			throw new FileNotFoundException("File " + name + " could not be found!");
		
		return latestFile;
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
