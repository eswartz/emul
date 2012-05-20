/**
 * 
 */
package v9t9.common.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;

import v9t9.common.files.IPathFileLocator;
import v9t9.common.files.PathFileLocator;
import ejs.base.properties.IProperty;
import ejs.base.settings.SettingProperty;

/**
 * This test assumes a file '994arom.bin' really exists under /usr/local/src/v9t9-data/roms,
 * a file 'wumpusg.bin' really exists under /usr/local/src/v9t9-data/modules,
 * and that the hosts 12.168.24.100 and 12.168.24.101 are unreachable.
 * @author ejs
 *
 */
public class ManualTestPathFileLocator {
	
	private static final String FAKE_HOST = "12.168.24.100";
	private static final String FAKE_HOST2 = "12.168.24.101";
	private static String ROM_PATH;
	private static String MODULE_PATH;
	
	@BeforeClass
	public static void setupPaths() {
		if (File.separatorChar == '/') {
			ROM_PATH = "/usr/local/src/v9t9-data/roms";
			MODULE_PATH = "/usr/local/src/v9t9-data/modules";
		} else {
			ROM_PATH = "m:/fun/ti994a/v6.0/roms";
			MODULE_PATH = "m:/fun/ti994a/v6.0/modules";
		}
	}

	@Test
	public void testPathLists() throws URISyntaxException {

		IPathFileLocator locator = new PathFileLocator();
		IProperty bootRoms = new SettingProperty("Paths", String.class, new ArrayList<String>());

		assertEquals(0, locator.getSearchURIs().length);

		locator.addReadOnlyPathProperty(bootRoms);
		
		assertEquals(0, locator.getSearchURIs().length);
		
		bootRoms.getList().add("/tmp/foo");

		assertEquals(1, locator.getSearchURIs().length);

		bootRoms.getList().add("/tmp/foo/bar");

		assertEquals(2, locator.getSearchURIs().length);

		IProperty rwPath = new SettingProperty("RamPath", "file://tmp");
		locator.setReadWritePathProperty(rwPath);
		
		assertEquals(3, locator.getSearchURIs().length);
		
		// should correct paths
		assertEquals(new URI("file://tmp/"), locator.getSearchURIs()[0]);
		
		
	}
	
	@Test
	public void testLookups() {
		
		IPathFileLocator locator = new PathFileLocator();
		IProperty bootRoms = new SettingProperty("Paths", String.class, new ArrayList<String>());
		
		locator.addReadOnlyPathProperty(bootRoms);
		
		URI uri;
		
		uri = locator.findFile("994arom.bin");
		assertNull(uri);
		
		bootRoms.getList().add("/tmp/foo");

		uri = locator.findFile("994arom.bin");
		assertNull(uri);

		bootRoms.getList().add("/tmp/foo/bar");

		uri = locator.findFile("994arom.bin");
		assertNull(uri);

		IProperty rwPath = new SettingProperty("RamPath", "/tmp");
		locator.setReadWritePathProperty(rwPath);

		uri = locator.findFile("994arom.bin");
		assertNull(uri);

		bootRoms.getList().add("file://" + ROM_PATH);
		
		uri = locator.findFile("994arom.bin");
		assertNotNull(uri);
		uri = locator.findFile("wumpusg.bin");
		assertNull(uri);
		
		bootRoms.getList().add(MODULE_PATH);
		
		uri = locator.findFile("994arom.bin");
		assertNotNull(uri);
		uri = locator.findFile("wumpusg.bin");
		assertNotNull(uri);

	}
	

	@Test
	public void testWritePaths() throws MalformedURLException, IOException {
		
		IPathFileLocator locator = new PathFileLocator();
		IProperty bootRoms = new SettingProperty("Paths", String.class, new ArrayList<String>());
		
		locator.addReadOnlyPathProperty(bootRoms);
		
		URI uri;
		
		bootRoms.getList().add(ROM_PATH);
		bootRoms.getList().add(MODULE_PATH);

		uri = locator.findFile("994arom.bin");
		assertNotNull(uri);

		// new file should not exist yet... if not, test area needs cleaning
		String TESTFILE = "__testfile.bin";
		uri = locator.findFile(TESTFILE);
		assertNull(uri);
		
		URI writeURI;
		
		// no writeable paths registered
		writeURI = locator.getWriteURI(TESTFILE);
		assertNull(writeURI);

		File tempdir = new File(System.getProperty("java.io.tmpdir"));
		File desired = new File(tempdir, TESTFILE);
		desired.delete();
		assertFalse(desired.exists());
		
		IProperty rwPath = new SettingProperty("RamPath", tempdir.getAbsolutePath());
		locator.setReadWritePathProperty(rwPath);
		
		writeURI = locator.getWriteURI(TESTFILE);
		assertNotNull(writeURI);
		assertEquals(desired.toURI().getPath(), writeURI.getPath());

		// searching should not have written anything yet
		uri = locator.findFile(TESTFILE);
		assertNull(uri);

		assertFalse(desired.exists());
		
		OutputStream os = locator.createOutputStream(writeURI);
		os.write("foo bar.\n".getBytes());
		os.close();
		
		// any URI queried by #getWriteURI() will be checked every time
		uri = locator.findFile(TESTFILE);
		assertNotNull(uri);

		assertEquals(desired.toURI().getPath(), uri.getPath());
		
		writeURI = locator.getWriteURI(TESTFILE);
		assertNotNull(writeURI);
		assertEquals(desired.toURI().getPath(), writeURI.getPath());
		
	}

	@Test
	public void testTroublesomeLookups() {
		
		IPathFileLocator locator = new PathFileLocator();
		IProperty bootRoms = new SettingProperty("Paths", String.class, new ArrayList<String>());
		
		locator.addReadOnlyPathProperty(bootRoms);
		
		URI uri;
		
		/// PHASE ONE
		
		bootRoms.getList().add("http://" + FAKE_HOST + "/fake/path/to/check");
		
		locator.setConnectionTimeout(1000);
		
		long start, end;
		
		start = System.currentTimeMillis();
		uri = locator.findFile("file.bin");
		assertNull(uri);
		end = System.currentTimeMillis();
		
		long orig = end - start;
		
		System.out.println("first search: " + orig);
		
		assertTrue(orig < 1000 + 100);

		// second search for the same content should fail quickly
		start = System.currentTimeMillis();
		uri = locator.findFile("file.bin");
		assertNull(uri);
		end = System.currentTimeMillis();

		long second = end - start;
		
		System.out.println("second search: " + second);
		
		assertTrue(second * 100 < orig);
		

		// search for totally different content on the bad URI should fail quickly too
		start = System.currentTimeMillis();
		uri = locator.findFile("file2.bin");
		assertNull(uri);
		end = System.currentTimeMillis();

		long third = end - start;
		
		System.out.println("third search: " + third);
		
		assertTrue(third * 100 < orig);
		

		///////////////
		

		bootRoms.getList().add("http://" + FAKE_HOST2 + "/fake/path/to/check");
		
		locator.setConnectionTimeout(2500);
		
		start = System.currentTimeMillis();
		uri = locator.findFile("file.bin");
		assertNull(uri);
		end = System.currentTimeMillis();
		
		orig = end - start;
		
		System.out.println("first search: " + orig);
		
		// re-caches and re-checks
		assertTrue(orig < 2500 * 2 + 100);

		// second search for the same content should fail quickly
		start = System.currentTimeMillis();
		uri = locator.findFile("file.bin");
		assertNull(uri);
		end = System.currentTimeMillis();

		second = end - start;
		
		System.out.println("second search: " + second);
		
		assertTrue(second * 100 < orig);
		

		// search for the totally different content on the bad URI should fail quickly too
		start = System.currentTimeMillis();
		uri = locator.findFile("file2.bin");
		assertNull(uri);
		end = System.currentTimeMillis();

		third = end - start;
		
		System.out.println("third search: " + third);
		
		assertTrue(third * 100 < orig);
	}
	
	@Test
	public void testJarFile() throws URISyntaxException, IOException {

		IPathFileLocator locator = new PathFileLocator();
		IProperty bootRoms = new SettingProperty("Paths", String.class, new ArrayList<String>());
		
		File baseV9t9 = new File("../..");
		File v9t9Jar = new File(baseV9t9, "build/bin/v9t9/v9t9j.jar");
		String jarPath = "jar:" + v9t9Jar.toURI().toString() + "!/ti99/";
		bootRoms.getList().add(jarPath);
		locator.addReadOnlyPathProperty(bootRoms);		
		
		URI jarURI = locator.createURI(jarPath);
		assertNotNull(jarURI);
		assertTrue(jarURI.isOpaque());
		assertTrue(jarURI.isAbsolute());
		
		Collection<String> listing = locator.getDirectoryListing(jarURI);
		assertNotNull(listing);
		
		URI moduleXML = locator.resolveInsideURI(jarURI, "stock_modules.xml");
		assertTrue(moduleXML.toString().endsWith("!/ti99/stock_modules.xml"));
		InputStream is = locator.createInputStream(moduleXML);
		assertNotNull(is);
		

	}
	

	@Test
	public void testJarHttp() throws URISyntaxException, IOException {

		IPathFileLocator locator = new PathFileLocator();
		IProperty bootRoms = new SettingProperty("Paths", String.class, new ArrayList<String>());
		String jarPath = "jar:http://192.168.24.9:8080/v9t9/v9t9j.jar!/ti99/";
		bootRoms.getList().add(jarPath);
		locator.addReadOnlyPathProperty(bootRoms);		
		
		URI jarURI = locator.createURI(jarPath);
		assertNotNull(jarURI);
		assertTrue(jarURI.isOpaque());
		assertTrue(jarURI.isAbsolute());
		
		Collection<String> listing = locator.getDirectoryListing(jarURI);
		assertNotNull(listing);
		
		URI moduleXML = locator.resolveInsideURI(jarURI, "stock_modules.xml");
		assertTrue(moduleXML.toString().endsWith("!/ti99/stock_modules.xml"));
		InputStream is = locator.createInputStream(moduleXML);
		assertNotNull(is);
		

	}
	

	@Test
	public void testJarLocal() throws URISyntaxException, IOException {

		IPathFileLocator locator = new PathFileLocator();
		
		File baseV9t9 = new File("../..").getAbsoluteFile();
		File v9t9Jar = new File(baseV9t9, "build/bin/v9t9/v9t9j.jar");
		
		String jarPath = "jar:" + v9t9Jar.getAbsoluteFile().toURI() +"!/ti99/demos";
		URI jarURI = locator.createURI(jarPath);
		assertNotNull(jarURI);
		assertTrue(jarURI.isOpaque());
		assertTrue(jarURI.isAbsolute());
		
		Collection<String> listing = locator.getDirectoryListing(jarURI);
		assertNotNull(listing);

		for (String ent : listing)
			System.out.println(ent);
		assertEquals(2, listing.size());

	}
}
