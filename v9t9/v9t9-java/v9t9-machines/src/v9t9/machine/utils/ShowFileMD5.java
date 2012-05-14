/**
 * 
 */
package v9t9.machine.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import v9t9.common.files.PathFileLocator;
import v9t9.common.settings.BasicSettingsHandler;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryEntryFactory;

/**
 * @author ejs
 *
 */
public class ShowFileMD5 {

	public static void main(String[] args) throws IOException {
		ShowFileMD5 runner = new ShowFileMD5();
		runner.run(args);
	}

	private Memory memory;
	private PathFileLocator locator;
	private BasicSettingsHandler settings;
	
	/**
	 * 
	 */
	public ShowFileMD5() {
		memory = new Memory();
		locator = new PathFileLocator();
		
		settings = new BasicSettingsHandler();
		
		memory.setMemoryEntryFactory(new MemoryEntryFactory(settings, memory, locator));
	}
	
	protected void run(String[] args) throws IOException {
		
		for (String arg : args) {
			File file = new File(arg);
			URI uri = file.toURI();
			
			String md5 = locator.getContentMD5(uri);
	
			System.out.println(file + " ==> " + md5);
		}
	}
}
