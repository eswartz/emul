/**
 * 
 */
package v9t9.machine.common.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.files.IPathFileLocator;
import v9t9.common.machine.IMachine;
import v9t9.common.modules.IModule;
import v9t9.common.settings.BasicSettingsHandler;
import v9t9.machine.ti99.machine.StandardTI994AMachineModel;

/**
 * @author ejs
 *
 */
public class ManualTestTI99ModuleDetection {

	private ISettingsHandler settings;
	private IMachine machine;
	private IPathFileLocator locator;

	@Before
	public void setup() throws Exception {
		settings = new BasicSettingsHandler();  
		machine = new StandardTI994AMachineModel().createMachine(settings);
		locator = machine.getRomPathFileLocator();
	}
	
	@Test
	public void testMyModules() throws Exception {
		testDirectory("/usr/local/src/v9t9-data/modules", 
				"(?i).*\\.bin",
				"c.bin", "g.bin", 
				"forthc.bin", "nforthc.bin", "0forth.bin"
				);
	}

	@Test
	public void testMessModules() throws Exception {
		testDirectory("/usr/local/src/v9t9-data/modules/mess",
				"(?i).*\\.bin");
	}
	@Test
	public void testToSecModules() throws Exception {
		testDirectory("/usr/local/src/v9t9-data/modules/tosec",
				"(?i).*\\.bin");
	}
	@Test
	public void testToWhtechZipModules() throws Exception {
		testDirectory("/usr/local/src/v9t9-data/modules/ftp.whtech.com/emulators/cartridges/zip",
				"(?i).*\\.zip");
	}
	@Test
	public void testToWhtechRpkModules() throws Exception {
		testDirectory("/usr/local/src/v9t9-data/modules/ftp.whtech.com/emulators/cartridges/rpk",
				"(?i).*\\.rpk");
	}
	@Test
	public void testToWhtechRpkConvertedModules() throws Exception {
		testDirectory("/usr/local/src/v9t9-data/modules/ftp.whtech.com/emulators/cartridges/rpk/converted",
				"(?i).*\\.rpk");
	}
	

	protected void testDirectory(String path, final String pattern, final String... ignore) {
		File dir = new File(path);
		assertTrue(dir.exists());
		
		Set<File> allFiles = new LinkedHashSet<File>(Arrays.asList(dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory() || !pathname.getName().matches(pattern))
					return false;
				for (String ign : ignore) {
					if (pathname.getName().equals(ign))
						return false;
					if (pathname.getName().indexOf('\uFFFD') >= 0)
						return false;
				}
				return true;
			}
		}))); 
		
		URI databaseURI = URI.create("test.xml");
		Collection<IModule> modules = machine.scanModules(databaseURI, dir);
		
		System.out.println("Found " + modules.size() + " modules in " + dir);
		for (IModule module : modules) {
			System.out.println(module);
			
			Collection<File> files = module.getUsedFiles(locator);
			assertFalse(files.isEmpty());
			for (File file : files) {
				System.out.println("\t" + file);
			}
			allFiles.removeAll(files);
		}
		
		StringBuilder sb = new StringBuilder();
		for (File file : allFiles) {
			sb.append("*** did not detect ").append(file).append('\n');
		}

		if (sb.length() > 0) {
			System.err.print(sb);
			fail(sb.toString());
		}
	}
	
}
