package test.KabOOm356.Util;

import net.KabOOm356.File.RevisionFile;
import net.KabOOm356.Util.FileIO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import test.test.PowerMockitoTest;

import java.io.*;
import java.net.URI;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest({FileIO.class})
public class FileIOTest extends PowerMockitoTest {
	@Mock
	public File file;

	@Mock
	public RevisionFile revisionFile;

	@Mock
	public URL url;

	@Mock
	public URI uri;

	@Mock
	public InputStream inputStream;

	@Mock
	public InputStreamReader inputStreamReader;

	@Mock
	public BufferedReader bufferedReader;

	@Mock
	public FileOutputStream fileOutputStream;

	@Mock
	public OutputStreamWriter outputStreamWriter;

	@Mock
	public BufferedWriter bufferedWriter;

	@Before
	public void setupMocks() throws Exception {
		whenNew(BufferedReader.class).withAnyArguments().thenReturn(bufferedReader);
		whenNew(InputStreamReader.class).withAnyArguments().thenReturn(inputStreamReader);
		when(file.toURI()).thenReturn(uri);
		when(uri.toURL()).thenReturn(url);
		when(url.openStream()).thenReturn(inputStream);

		whenNew(BufferedWriter.class).withAnyArguments().thenReturn(bufferedWriter);
		whenNew(OutputStreamWriter.class).withAnyArguments().thenReturn(outputStreamWriter);
		whenNew(FileOutputStream.class).withAnyArguments().thenReturn(fileOutputStream);

		when(bufferedReader.readLine()).thenReturn("test", "string", null);

		whenNew(RevisionFile.class).withAnyArguments().thenReturn(revisionFile);
	}

	@Test
	public void testCopyTextFileNull() throws IOException {
		try {
			FileIO.copyTextFile(null, null);
			fail("Expected exception not thrown!");
		} catch (final IllegalArgumentException e) {
			// Expected exception
		}

		try {
			FileIO.copyTextFile(file, null);
			fail("Expected exception not thrown!");
		} catch (final IllegalArgumentException e) {
			// Expected exception
		}

		try {
			FileIO.copyTextFile(null, file);
			fail("Expected exception not thrown!");
		} catch (final IllegalArgumentException e) {
			// Expected exception
		}
	}

	@Test
	public void testCopyTextFile() throws IOException {
		FileIO.copyTextFile(file, file);
		verify(bufferedWriter, times(2)).write(anyString());
		verify(bufferedWriter).flush();
		verify(bufferedWriter).close();
		verify(bufferedReader).close();
	}

	@Test
	public void testCreateBackup() throws IOException {
		mockStatic(FileIO.class);
		when(FileIO.createBackup(file)).thenCallRealMethod();
		assertEquals(revisionFile, FileIO.createBackup(file));
		verify(revisionFile).incrementToNextRevision();
		verify(revisionFile).createNewFile();
	}
}
