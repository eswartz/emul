/**
 * 
 */
package v9t9.machine.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import v9t9.common.events.NotifyException;
import v9t9.common.files.PathFileLocator;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.modules.IModule;
import v9t9.common.modules.ModuleDatabase;
import v9t9.common.settings.BasicSettingsHandler;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryEntryFactory;
import ejs.base.properties.IProperty;
import ejs.base.settings.SettingProperty;

/**
 * @author ejs
 *
 */
public class UpdateModuleXmlMd5Hashes {

	public static void main(String[] args) throws IOException {
		UpdateModuleXmlMd5Hashes runner = new UpdateModuleXmlMd5Hashes();
		runner.run(args);
	}

	private Memory memory;
	private PathFileLocator locator;
	private BasicSettingsHandler settings;
	
	/**
	 * 
	 */
	public UpdateModuleXmlMd5Hashes() {
		memory = new Memory();
		locator = new PathFileLocator();
		
		IProperty prop = new SettingProperty("Paths", String.class, new ArrayList<String>());
		prop.getList().add("/usr/local/src/v9t9-data/roms");
		prop.getList().add("/usr/local/src/v9t9-data/modules");
		prop.getList().add("/usr/local/src/v9t9-data/modules/mess");
		prop.getList().add("/usr/local/src/v9t9-data/modules/tosec");
		
		locator.addReadOnlyPathProperty(prop);
		
		settings = new BasicSettingsHandler();
		
		memory.setMemoryEntryFactory(new MemoryEntryFactory(settings, memory, locator));
	}
	
	protected void run(String[] args) throws IOException {
		
		for (String arg : args) {
			File file = new File(arg);
			URI dbUri = file.toURI();
			
			InputStream is = new FileInputStream(file);
			
			try {
				List<IModule> modules = ModuleDatabase.loadModuleListAndClose(memory, is, dbUri);
				
				for (IModule module : modules) {
					for (MemoryEntryInfo info : module.getMemoryEntryInfos()) {
						updateMD5(MemoryEntryInfo.FILE_MD5, info, info.getFilename(), info.getFileMD5());
						updateMD5(MemoryEntryInfo.FILE2_MD5, info, info.getFilename2(), info.getFile2MD5());
					}
				}
				
				File backup = new File(file.getAbsolutePath() + "~");
				backup.delete();
				file.renameTo(backup);
				
				OutputStream os = new FileOutputStream(file);
				ModuleDatabase.saveModuleListAndClose(memory, os, dbUri, modules);
				
			} catch (NotifyException e) {
				e.printStackTrace();
			}
			
		}
	}

	/**
	 * @param info
	 * @param filename
	 * @param fileMD5
	 * @return
	 * @throws IOException 
	 */
	private void updateMD5(String property, MemoryEntryInfo info, String filename,
			String fileMD5) throws IOException {
		if (fileMD5 != null)
			return;
		
		if (filename == null)
			return;
		
		URI uri = locator.findFile(filename);
		if (uri == null) {
			return;
		}
		
		String md5 = locator.getContentMD5(uri);

		System.out.println(filename + " ==> " + md5);
		info.getProperties().put(property, md5);
	}
}
