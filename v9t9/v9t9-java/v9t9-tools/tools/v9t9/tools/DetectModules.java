/*
  ManualTestTI99ModuleDetection.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import v9t9.common.machine.IMachine;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.modules.IModule;
import v9t9.common.modules.IModuleDetector;
import v9t9.common.modules.ModuleDatabase;
import v9t9.tools.utils.Category;
import v9t9.tools.utils.ToolUtils;
import ejs.base.logging.LoggingUtils;
import gnu.getopt.Getopt;

/**
 * @author ejs
 *
 */
@Category(Category.SETUP)
public class DetectModules {

    private static final String PROGNAME = DetectModules.class.getName();
    
    private static void help() {
	   System.out
	            .println("\n"
	                    + "V9t9 Module Detector\n"
	                    + "\n"
	                    + "Usage:   " + PROGNAME + " [options] directory ...\n"
	                    + "\n"
	                    + PROGNAME + " will detect the modules in the given directories.\n"
	                    + "\n"
	                    +"-o [file]:  write new modules.xml file\n"
	                    +"-n:  do not update modules from stock module database\n"
						+"-r:  read headers from ROM instead of replying on .rpk metadata\n"
	                    + "\n");
	}

    
    public static void main(String[] args) {
		LoggingUtils.setupNullLogging();

        Getopt getopt;
        
        getopt = new Getopt(PROGNAME, args, "?nro:");
		int opt;
		
		boolean noStock = false;
		boolean readHeaders = false;
		String outfile = null;
		
		while ((opt = getopt.getopt()) != -1) {
			switch (opt) {
            case '?':
                help();
                break;
            case 'n': 
            	noStock = true;
            	break;
            case 'r':
            	readHeaders = true;
                break;
            case 'o':
            	outfile = getopt.getOptarg();
            	break;
            default:
                throw new AssertionError();
    
            }
        }
		
		DetectModules modules = new DetectModules(noStock, readHeaders);
		int i = getopt.getOptind();
        while (i < args.length) {
			String arg = args[i++];
			modules.scan(arg);
		}
        
        if (outfile != null)
        	modules.write(outfile);
        else
        	modules.list();
	}

	
	private IMachine machine;
	private IModuleDetector detector;

	public DetectModules(boolean noStock, boolean readHeader) {
        machine = ToolUtils.createMachine();
		
		URI databaseURI = URI.create("test.xml");
		detector = machine.createModuleDetector(databaseURI);
		detector.setIgnoreStock(noStock);
		detector.setReadHeaders(readHeader);
	}
	
	public void scan(String path) {
		File dir = new File(path);
		if (!dir.isDirectory()) {
			System.err.println("Not a directory: " + path);
			return;
		}
		
		Collection<IModule> modules = detector.scan(dir);
		System.out.println("Found " + modules.size() + " modules in " + dir);
	}
	
	
	public void write(String path) {
		System.out.println("Writing to " + path + "...");

		List<IModule> simpleModules = detector.simplifyModules();
		
		File outfile = new File(path);
		File backup = new File(outfile.getAbsolutePath() + "~");
		backup.delete();
		outfile.renameTo(backup);
		
		OutputStream os = null; 
		try {
			os = new FileOutputStream(outfile);
			ModuleDatabase.saveModuleListAndClose(machine.getMemory(), os, null, 
					simpleModules);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					
				}
			}
		}
		
	}
	public void list() {
		Map<String, List<IModule>> md5ToModules = detector.gatherDuplicates();
		for (Map.Entry<String, List<IModule>> ent : md5ToModules.entrySet()) {
			System.out.println(ent.getKey() + ":");
			for (IModule module : ent.getValue()) {
				System.out.println("\t" + module.getName());
				for (MemoryEntryInfo info : module.getMemoryEntryInfos()) {
					System.out.print("\t\t" + info.getFilename());
					if (info.getFilename2() != null)
						System.out.print(", " + info.getFilename2());
					if (info.isBanked())
						System.out.print(", banked");
					System.out.println();
					
					System.out.print("\t\t" + info.getFileMD5Algorithm() + " = " + info.getFileMD5());
					if (info.getFile2MD5() != null)
						System.out.print(",\n\t\t" + info.getFile2MD5Algorithm() + " = " + info.getFile2MD5());
					System.out.println();
				}
			}
			System.out.println();
		}
	}

	
}
