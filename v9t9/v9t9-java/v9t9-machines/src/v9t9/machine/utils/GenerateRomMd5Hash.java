/*
  UpdateModuleXmlMd5Hashes.java

  (c) 2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import v9t9.common.files.IMD5SumFilter;
import v9t9.common.files.MD5FilterAlgorithms;
import v9t9.common.files.PathFileLocator;
import v9t9.common.settings.BasicSettingsHandler;
import v9t9.common.settings.SettingSchemaProperty;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryEntryFactory;
import ejs.base.properties.IProperty;

/**
 * @author ejs
 *
 */
public class GenerateRomMd5Hash {

	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			System.out.println("Run as GenerateRomMd5Hash [file] [algorithm] ...");
			return;
		}
		GenerateRomMd5Hash runner = new GenerateRomMd5Hash();
		for (int i = 0; i < args.length; i += 2) {
			runner.run(args[i+0], args[i+1]);
		}
	}

	private Memory memory;
	private PathFileLocator locator;
	private BasicSettingsHandler settings;
	
	/**
	 * 
	 */
	public GenerateRomMd5Hash() {
		memory = new Memory();
		locator = new PathFileLocator();
		
		IProperty prop = new SettingSchemaProperty("Paths", String.class, new ArrayList<String>());
		prop.getList().add("/usr/local/src/v9t9-data/roms");
		prop.getList().add("/usr/local/src/v9t9-data/roms/pcode");
		prop.getList().add("/usr/local/src/v9t9-data/modules");
		prop.getList().add("/usr/local/src/v9t9-data/modules/mess");
		prop.getList().add("/usr/local/src/v9t9-data/modules/tosec");
		
		locator.addReadOnlyPathProperty(prop);
		
		settings = new BasicSettingsHandler();
		
		memory.setMemoryEntryFactory(new MemoryEntryFactory(settings, memory, locator));
	}
	
	protected void run(String fileName, String algorithm) throws IOException {

		URI uri = locator.findFile(fileName);
		if (uri == null)
			throw new FileNotFoundException(fileName);
		
		IMD5SumFilter filter = MD5FilterAlgorithms.create(algorithm);
		if (filter == null)
			throw new IOException("unknown filter: " + algorithm);
			
		String md5 = locator.getContentMD5(uri, filter);
		System.out.println(uri + " @ " + filter.getId() + " = " + md5);
	}
}
