/*
  FindDuplicateRomFiles.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import v9t9.common.files.IPathFileLocator.FileInfo;
import v9t9.common.files.PathFileLocator;
import v9t9.common.settings.BasicSettingsHandler;
import v9t9.common.settings.SettingSchemaProperty;
import v9t9.engine.memory.Memory;
import v9t9.machine.ti99.memory.TI994AMemoryEntryFactory;

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
	private BasicSettingsHandler settings;
	private SettingSchemaProperty pathProp;
	
	/**
	 * 
	 */
	public FindDuplicateRomFiles() {
		memory = new Memory();
		locator = new PathFileLocator();
		
		pathProp = new SettingSchemaProperty("Paths", String.class, new ArrayList<String>());
		pathProp.getList().add("/usr/local/src/v9t9-data/roms");
		pathProp.getList().add("/usr/local/src/v9t9-data/modules");
		pathProp.getList().add("/usr/local/src/v9t9-data/modules/mess");
		pathProp.getList().add("/usr/local/src/v9t9-data/modules/tosec");
		
		locator.addReadOnlyPathProperty(pathProp);
		
		settings = new BasicSettingsHandler();
		
		memory.setMemoryEntryFactory(new TI994AMemoryEntryFactory(memory, settings, locator));
	}
	
	protected void run(String[] args) throws IOException, URISyntaxException {
		
		Map<String, Collection<URI>> matches = new HashMap<String, Collection<URI>>();
		
		for (Object obj : pathProp.getList()) {
			URI dirURI = locator.createURI(obj.toString());
			Map<String, FileInfo> files = locator.getDirectoryListing(dirURI);
			
			for (Map.Entry<String, FileInfo> info : files.entrySet()) {
				try {
					URI fileURI = info.getValue().uri;
					String md5 = info.getValue().md5;
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
