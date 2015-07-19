/*
  DetectModules.java

  (c) 2015 Ed Swartz

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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import v9t9.common.machine.IMachine;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.modules.IModule;
import v9t9.common.modules.IModuleDetector;
import v9t9.common.modules.ModuleDatabase;
import v9t9.tools.utils.Category;
import v9t9.tools.utils.ToolUtils;
import ejs.base.logging.LoggingUtils;
import ejs.base.utils.HexUtils;
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
						+"-s:  do not include stock modules in the output\n"
						+"-f:  remove filenames from file output\n"
						+"-v:  print verbose description of module\n"
	                    + "\n");
	}

    
    public static void main(String[] args) {
		LoggingUtils.setupNullLogging();

        Getopt getopt;
        
        getopt = new Getopt(PROGNAME, args, "?nro:vsf");
		int opt;
		
		boolean noStockLookup = false;
		boolean readHeaders = false;
		boolean verbose = false;
		boolean noStockOutput = false;
		boolean noFilenames = false;
		String outfile = null;
		
		while ((opt = getopt.getopt()) != -1) {
			switch (opt) {
            case '?':
                help();
                return;
            case 'n': 
            	noStockLookup = true;
            	break;
            case 'r':
            	readHeaders = true;
                break;
            case 'o':
            	outfile = getopt.getOptarg();
            	break;
            case 'v':
            	verbose = true;
            	break;
            case 's': 
            	noStockOutput = true;
            	break;
            case 'f': 
            	noFilenames = true;
            	break;
            default:
                throw new AssertionError();
            }
        }
		
		DetectModules modules = new DetectModules(noStockLookup, readHeaders, verbose, noStockOutput, noFilenames);
		int i = getopt.getOptind();
		boolean any = false;
        while (i < args.length) {
        	any = true;
			String arg = args[i++];
			modules.scan(arg);
		}
        
        if (!any) {
        	help();
        	return;
        }

        if (outfile != null)
        	modules.write(outfile);
        else
        	modules.list();
	}

	
	private IMachine machine;
	private IModuleDetector detector;
	private boolean verbose;
	private boolean noStockOutput;
	private boolean noFilenames;

	public DetectModules(boolean noStock, boolean readHeader, boolean verbose, boolean noStockOutput, boolean noFilenames) {
        this.verbose = verbose;
		this.noStockOutput = noStockOutput;
		this.noFilenames = noFilenames;
		machine = ToolUtils.createMachine();
		
		detector = machine.getModuleDetector();
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

		List<IModule> simpleModules = detector.simplifyModules(noFilenames);
		
		if (noStockOutput) {
			removeStocks(simpleModules);
		}
		
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


	/**
	 * @return
	 */
	private Set<String> getStockMD5s() {
		Set<String> stockMd5s = new HashSet<String>();
		for (IModule mod : machine.getModuleManager().getStockModules()) {
			stockMd5s.add(mod.getMD5());
		}
		return stockMd5s;
	}
	
	private void removeStocks(List<IModule> modules) {
		Set<String> stockMd5s = getStockMD5s();

		for (Iterator<IModule> iterator = modules.iterator(); iterator
				.hasNext();) {
			IModule module = iterator.next();
			if (stockMd5s.contains(module.getMD5()))
				iterator.remove();
		}
	}
	public void list() {
		Map<String, List<IModule>> md5ToModules = detector.gatherDuplicatesByMD5();
		
		if (noStockOutput) {
			Set<String> stockMd5s = getStockMD5s();
			for (String md5 : stockMd5s)
				md5ToModules.remove(md5);
		}

		
		for (Map.Entry<String, List<IModule>> ent : md5ToModules.entrySet()) {
			System.out.println(ent.getKey() + ":");
			for (IModule module : ent.getValue()) {
				System.out.print("\t" + module.getName());
				if (machine.getModuleManager().findStockModuleByMd5(ent.getKey()) != null)
					System.out.print(" [stock]");
				System.out.println();
				
				for (MemoryEntryInfo info : module.getMemoryEntryInfos()) {
					if (verbose) {
						System.out.println("\t\t" + info);
					} else {
						System.out.print("\t\t" + info.getFilename());
						if (info.getSize() > 0)
							 System.out.print(" @ " + HexUtils.toHex4(info.getSize()));
						if (info.getFilename2() != null) {
							System.out.print(", " + info.getFilename2());
							if (info.getSize2() > 0)
								 System.out.print(" @ " + HexUtils.toHex4(info.getSize2()));
						}
						if (info.isBanked())
							System.out.print(", banked");
						System.out.println();
						
						if (!info.isStored()) {
							System.out.print("\t\t\t" + info.getEffectiveFileMD5Algorithm() + " = " + info.getFileMD5());
							if (info.getFile2MD5() != null)
								System.out.print(",\n\t\t\t" + info.getEffectiveFile2MD5Algorithm() + " = " + info.getFile2MD5());
							System.out.println();
						}
					}
				}
			}
			System.out.println();
		}
	}
}
