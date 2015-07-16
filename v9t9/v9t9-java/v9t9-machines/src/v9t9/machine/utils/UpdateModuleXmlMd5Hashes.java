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
import java.util.Arrays;
import java.util.List;

import v9t9.common.events.NotifyException;
import v9t9.common.files.MD5FilterAlgorithms;
import v9t9.common.files.PathFileLocator;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.modules.IModule;
import v9t9.common.modules.ModuleDatabase;
import v9t9.common.settings.BasicSettingsHandler;
import v9t9.common.settings.SettingSchemaProperty;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryEntryFactory;
import ejs.base.properties.IProperty;

/**
 * @author ejs
 *
 */
public class UpdateModuleXmlMd5Hashes {

	private static void help() {
		System.out.println("Run as UpdateModuleXmlMd5Hashes modules.xml [-o output.xml]");
		System.exit(0);
	}
	
	public static void main(String[] args) throws IOException {
		UpdateModuleXmlMd5Hashes runner = new UpdateModuleXmlMd5Hashes();
		if (args.length == 0) {
			help();
			
		}
		
		List<String> argList = new ArrayList<String>(Arrays.asList(args));
		String output = null;
		for (int i = 0; i < args.length; i++) {
			if ("-o".equals(args[i])) {
				if (i + 1 >= args.length) {
					help();
				}
				output = args[i+1];
				argList.remove(i);
				argList.remove(i);
				break;
			}
		}
		runner.run(argList.get(0), output);
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
		
		IProperty prop = new SettingSchemaProperty("Paths", String.class, new ArrayList<String>());
		prop.getList().add("/usr/local/src/v9t9-data/roms");
		prop.getList().add("/usr/local/src/v9t9-data/modules");
		prop.getList().add("/usr/local/src/v9t9-data/modules/mess");
		prop.getList().add("/usr/local/src/v9t9-data/modules/tosec");
		prop.getList().add("/usr/local/src/v9t9-data/modules/ftp.whtech.com/emulators/cartridges/zip");
		prop.getList().add("/usr/local/src/v9t9-data/modules/ftp.whtech.com/emulators/cartridges/rpk");
		prop.getList().add("/usr/local/src/v9t9-data/modules/ftp.whtech.com/emulators/cartridges/rpk.old");
		
		locator.addReadOnlyPathProperty(prop);
		
		settings = new BasicSettingsHandler();
		
		memory.setMemoryEntryFactory(new MemoryEntryFactory(settings, memory, locator));
	}
	
	protected void run(String arg, String output) throws IOException {
		if (output == null)
			output = arg;
		
		File file = new File(arg);
		File outfile = new File(output);
		URI dbUri = file.toURI();
		
		InputStream is = new FileInputStream(file);
		
		try {
			List<IModule> modules = ModuleDatabase.loadModuleListAndClose(memory, null, is, dbUri);
			
			for (IModule module : modules) {
				for (MemoryEntryInfo info : module.getMemoryEntryInfos()) {
					updateMD5(MemoryEntryInfo.FILE_MD5, MemoryEntryInfo.FILE_MD5_ALGORITHM, 
							info, info.getFilename(), 
							info.getFileMD5(), info.getFileMD5Algorithm());
					updateMD5(MemoryEntryInfo.FILE2_MD5, MemoryEntryInfo.FILE2_MD5_ALGORITHM,
							info, info.getFilename2(), 
							info.getFile2MD5(), info.getFile2MD5Algorithm());
				}
				module.setMD5(null);
				
			}
			
			File backup = new File(outfile.getAbsolutePath() + "~");
			backup.delete();
			outfile.renameTo(backup);
			
			OutputStream os = new FileOutputStream(outfile);
			ModuleDatabase.saveModuleListAndClose(memory, os, dbUri, modules);
			
		} catch (NotifyException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * @param info
	 * @param filename
	 * @param fileMD5
	 * @return
	 * @throws IOException 
	 */
	private void updateMD5(String md5Prop, String md5AlgProp,
			MemoryEntryInfo info, String filename,
			String fileMD5, String fileMD5Prop) throws IOException {
		
		if (info.isStored())
			return;
		
		if (filename == null)
			return;
		

		info.getProperties().remove(MemoryEntryInfo.FILE2_MD5_LIMIT);
		info.getProperties().remove(MemoryEntryInfo.FILE2_MD5_OFFSET);
		info.getProperties().remove(MemoryEntryInfo.FILE_MD5_OFFSET);
		info.getProperties().remove(MemoryEntryInfo.FILE_MD5_LIMIT);
		if (info.getSize() < 0)
			info.getProperties().remove(MemoryEntryInfo.SIZE);
		if (info.getSize2() < 0)
			info.getProperties().remove(MemoryEntryInfo.SIZE2);
		
		URI uri = locator.findFile(filename);
		if (uri == null) {
//			if (info.getProperties().get(md5Prop) == null)
			System.err.println("Cannot find " + filename);
			return;
		}
		
		String md5Alg = null;
		if (IMemoryDomain.NAME_GRAPHICS.equals(info.getDomainName())) {
			md5Alg = MD5FilterAlgorithms.ALGORITHM_GROM; 
		} else {
			md5Alg = MD5FilterAlgorithms.ALGORITHM_FULL; 
		}
		
		String md5 = locator.getContentMD5(uri, MD5FilterAlgorithms.create(md5Alg), true);
		
		System.out.println(filename + " ==> " + md5);
		info.getProperties().put(md5Prop, md5);
		info.getProperties().put(md5AlgProp, md5Alg);
		
	}
}
