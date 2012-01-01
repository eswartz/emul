/**
 * 
 */
package v9t9.machine.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import v9t9.common.files.PathFileLocator;
import v9t9.common.tests.TestSettingsHandler;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryEntryFactory;
import ejs.base.settings.SettingProperty;

/**
 * @author ejs
 *
 */
public class FindDuplicateRomFiles {

	public static void main(String[] args) throws IOException, URISyntaxException {
		FindDuplicateRomFiles runner = new FindDuplicateRomFiles();
		runner.run(args);
	}

	private Memory memory;
	private PathFileLocator locator;
	private TestSettingsHandler settings;
	private SettingProperty pathProp;
	
	/**
	 * 
	 */
	public FindDuplicateRomFiles() {
		memory = new Memory();
		locator = new PathFileLocator();
		
		pathProp = new SettingProperty("Paths", String.class, new ArrayList<String>());
		pathProp.getList().add("/usr/local/src/v9t9-data/roms");
		pathProp.getList().add("/usr/local/src/v9t9-data/modules");
		pathProp.getList().add("/usr/local/src/v9t9-data/modules/mess");
		pathProp.getList().add("/usr/local/src/v9t9-data/modules/tosec");
		
		locator.addReadOnlyPathProperty(pathProp);
		
		settings = new TestSettingsHandler();
		
		memory.setMemoryEntryFactory(new MemoryEntryFactory(settings, memory, locator));
	}
	
	protected void run(String[] args) throws IOException, URISyntaxException {
		
		Map<String, Collection<URI>> matches = new HashMap<String, Collection<URI>>();
		
		for (Object obj : pathProp.getList()) {
			URI dirURI = locator.createURI(obj.toString());
			Collection<String> files = locator.getDirectoryListing(dirURI);
			
			for (String file : files) {
				try {
					URI fileURI = locator.resolveInsideURI(dirURI, file);
					String md5 = locator.getContentMD5(fileURI);
					Collection<URI> dirs = matches.get(md5);
					if (dirs == null) {
						dirs = new ArrayList<URI>();
						matches.put(md5, dirs);
					}
					dirs.add(fileURI);
				} catch (Throwable e) {
					e.printStackTrace();
				}
				
			}
		}
		
		for (Map.Entry<String, Collection<URI>> entry : matches.entrySet()) {
			if (entry.getValue().size() > 1) {
				System.out.println(entry.getKey());
				for (URI uri : entry.getValue())
					System.out.println("\t" + uri);
			}
			
		}
		
	}
}
