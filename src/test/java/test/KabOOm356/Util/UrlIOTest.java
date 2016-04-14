package test.KabOOm356.Util;

import net.KabOOm356.File.AbstractFiles.NetworkFile;
import net.KabOOm356.Util.UrlIO;
import org.junit.Test;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import test.KabOOm356.PowerMockitoTest;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest({UrlIO.class})
public class UrlIOTest extends PowerMockitoTest {
	@Mock
	public URL url;

	@Mock
	public HttpURLConnection httpURLConnection;

	@Mock
	public NetworkFile networkFile;

	@Mock
	public File file;

	@Mock
	public BufferedReader bufferedReader;

	@Mock
	public BufferedWriter bufferedWriter;

	@Test
	public void testGetResponse() throws IOException {
		when(httpURLConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
		UrlIO.getResponse(httpURLConnection);
	}

	@Test
	public void testIsResponseValid() throws IOException {
		when(httpURLConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
		assertTrue(UrlIO.isResponseValid(httpURLConnection));

		when(httpURLConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_NOT_FOUND);
		assertFalse(UrlIO.isResponseValid(httpURLConnection));
	}

	@Test
	public void testDownloadFileNull() throws IOException {
		try {
			UrlIO.downloadFile(null, null);
			fail("Expected exception not thrown!");
		} catch (final IllegalArgumentException e) {
			// Expected exception
		}

		try {
			UrlIO.downloadFile(networkFile, null);
			fail("Expected exception not thrown!");
		} catch (final IllegalArgumentException e) {
			// Expected exception
		}

		try {
			UrlIO.downloadFile(null, file);
			fail("Expected exception not thrown!");
		} catch (final IllegalArgumentException e) {
			// Expected exception
		}
	}

	@Test(expected = IOException.class)
	public void testDownloadFileBadResponse() throws Exception {
		whenNew(URL.class).withAnyArguments().thenReturn(url);
		when(url.openConnection()).thenReturn(httpURLConnection);
		when(httpURLConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_NOT_FOUND);
		UrlIO.downloadFile(networkFile, file);
	}

	@Test
	public void testDownloadFile() throws Exception {
		whenNew(URL.class).withAnyArguments().thenReturn(url);
		when(url.openConnection()).thenReturn(httpURLConnection);
		when(httpURLConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
		when(httpURLConnection.getInputStream()).thenReturn(mock(InputStream.class));

		whenNew(BufferedReader.class).withAnyArguments().thenReturn(bufferedReader);
		whenNew(InputStreamReader.class).withAnyArguments().thenReturn(mock(InputStreamReader.class));

		whenNew(BufferedWriter.class).withAnyArguments().thenReturn(bufferedWriter);
		whenNew(OutputStreamWriter.class).withAnyArguments().thenReturn(mock(OutputStreamWriter.class));
		whenNew(FileOutputStream.class).withAnyArguments().thenReturn(mock(FileOutputStream.class));

		when(bufferedReader.readLine()).thenReturn("Test", "String", null);

		UrlIO.downloadFile(networkFile, file);
		verify(bufferedWriter, times(2)).write(anyString());
		verify(httpURLConnection).disconnect();
	}

	@Test
	public void testGetVersion() {
		assertEquals("", UrlIO.getVersion(""));
		assertEquals("", UrlIO.getVersion("hello"));
		assertEquals("1", UrlIO.getVersion("hello 1"));
		assertEquals("1", UrlIO.getVersion("hello v1"));
		assertEquals("1", UrlIO.getVersion("hello V1"));
		assertEquals("", UrlIO.getVersion("hello alpha"));
		assertEquals("1-alpha", UrlIO.getVersion("hello 1 alpha"));
	}
}
