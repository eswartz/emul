/*
  ScanROMs.java

  (c) 2015 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.engine.memory.MemoryEntryInfoBuilder;
import v9t9.machine.ti99.machine.TI99RomUtils;
import v9t9.tools.utils.Category;
import v9t9.tools.utils.ToolUtils;
import ejs.base.logging.LoggingUtils;
import ejs.base.utils.FileUtils;
import ejs.base.utils.HexUtils;
import gnu.getopt.Getopt;

/**
 * @author ejs
 *
 */
@Category(Category.SETUP)
public class ScanROMs {

    private static final String PROGNAME = ScanROMs.class.getName();
    
    private static void help() {
	   System.out
	            .println("\n"
	                    + "V9t9 ROM Scanner\n"
	                    + "\n"
	                    + "Usage:   " + PROGNAME + " [options] directory ...\n"
	                    + "\n"
	                    + PROGNAME + " will detect the ROMs in the given directories.\n"
	                    + "\n"
						+"-v:  print verbose description of memory\n"
	                    + "\n");
	}

    
    public static void main(String[] args) {
		LoggingUtils.setupNullLogging();

        Getopt getopt;
        
        getopt = new Getopt(PROGNAME, args, "?v");
		int opt;
		
		boolean verbose = false;
		
		while ((opt = getopt.getopt()) != -1) {
			switch (opt) {
            case '?':
                help();
                break;
            case 'v':
            	verbose = true;
            	break;
            default:
                throw new AssertionError();
    
            }
        }
		
		ScanROMs roms = new ScanROMs(verbose);
		
		boolean any = false;
		int i = getopt.getOptind();
        while (i < args.length) {
        	any = true;
			String arg = args[i++];
			roms.scan(arg);
		}
        
        if (!any) {
        	help();
        	return;
        }
    	roms.list();
	}

	
	private IMachine machine;
	private boolean verbose;

	private List<MemoryEntryInfo> infos = new ArrayList<MemoryEntryInfo>();
	
	public ScanROMs(boolean verbose) {
        this.verbose = verbose;
		machine = ToolUtils.createMachine();
		
	}
	
	public void scan(String path) {
		File dir = new File(path);
		if (!dir.isDirectory()) {
			System.err.println("Not a directory: " + path);
			return;
		}
		
		File[] ents = dir.listFiles();
		for (File ent : ents) {
			if (ent.isDirectory())
				continue;
			
			byte[] content;
			try {
				content = FileUtils.readInputStreamContentsAndClose(new FileInputStream(ent));
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			
			if (!TI99RomUtils.hasId(content))
				continue;
			
			MemoryEntryInfoBuilder bld = MemoryEntryInfoBuilder
					.byteMemoryEntry()
					.withFilename(ent.getPath());
					
			if (ent.getName().matches("(?i).*(grom|gpl).*")) {
				bld = bld.withDomain(IMemoryDomain.NAME_GRAPHICS);
			}
			
			MemoryEntryInfo info = bld.create(ent.getName());

			TI99RomUtils.fetchMD5(machine.getRomPathFileLocator(), info, false);
			
			infos.add(info);
		}
	}

	public void list() {
		for (MemoryEntryInfo info : infos) {
			System.out.println(info.getName() + " in " + info.getDomainName());
			if (verbose) {
				System.out.println("\t" + info);
			} else {
				System.out.print("\t" + info.getFilename());
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
					System.out.print("\t\t" + info.getEffectiveFileMD5Algorithm() + " = " + info.getFileMD5());
					if (info.getFile2MD5() != null)
						System.out.print(",\n\t\t" + info.getEffectiveFile2MD5Algorithm() + " = " + info.getFile2MD5());
					System.out.println();
				}
			}
		}
		System.out.println();
	}
}
