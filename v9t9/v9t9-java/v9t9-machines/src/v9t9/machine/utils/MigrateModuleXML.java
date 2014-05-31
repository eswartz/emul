/*
  UpdateModuleXmlMd5Hashes.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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

import v9t9.common.files.PathFileLocator;
import v9t9.common.modules.IModule;
import v9t9.common.modules.ModuleDatabase;
import v9t9.common.modules.ModuleInfoDatabase;
import v9t9.common.settings.BasicSettingsHandler;
import v9t9.common.settings.SettingSchemaProperty;
import v9t9.engine.memory.Memory;
import v9t9.machine.ti99.machine.StandardTI994AMachineModel;
import v9t9.machine.ti99.memory.TI994AMemoryEntryFactory;
import ejs.base.properties.IProperty;

/**
 * @author ejs
 *
 */
public class MigrateModuleXML {

	public static void main(String[] args) throws IOException {
		MigrateModuleXML runner = new MigrateModuleXML();
		runner.run(args);
	}

	private Memory memory;
	private PathFileLocator locator;
	private BasicSettingsHandler settings;
	
	/**
	 * 
	 */
	public MigrateModuleXML() {
		memory = new Memory();
		locator = new PathFileLocator();
		
		IProperty prop = new SettingSchemaProperty("Paths", String.class, new ArrayList<String>());
		prop.getList().add("/usr/local/src/v9t9-data/roms");
		prop.getList().add("/usr/local/src/v9t9-data/modules");
		prop.getList().add("/usr/local/src/v9t9-data/modules/mess");
		prop.getList().add("/usr/local/src/v9t9-data/modules/tosec");
		
		locator.addReadOnlyPathProperty(prop);
		
		settings = new BasicSettingsHandler();
		
		memory.setMemoryEntryFactory(new TI994AMemoryEntryFactory(memory, settings, locator));
	}
	
	protected void run(String[] args) throws IOException {
		
		for (String arg : args) {
			File file = new File(arg);
			URI dbUri = file.toURI();
			
			InputStream is = new FileInputStream(file);
			
			try {
				ModuleInfoDatabase moduleInfoDb = ModuleInfoDatabase.loadModuleInfo(new StandardTI994AMachineModel().createMachine(settings));
				List<IModule> modules = ModuleDatabase.loadModuleListAndClose(memory, moduleInfoDb, is, dbUri);
				
				for (@SuppressWarnings("unused") IModule module : modules) {
					
//					ModuleInfo info = ModuleInfo.createForModule(module);
//					if (module.getImagePath() != null) {
//						info.setImagePath(module.getImagePath());
//						module.setInfo(info);
//						moduleInfoDb.register(module);
//					}
				}
				
				OutputStream os = new FileOutputStream(file.toString() + ".info");
				moduleInfoDb.saveModuleInfoAndClose(os);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}

}
