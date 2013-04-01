/*
  CountCycles.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools;

import ejs.base.logging.LoggingUtils;
import ejs.base.utils.FileUtils;
import ejs.base.utils.HexUtils;
import gnu.getopt.Getopt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;

import v9t9.common.files.NativeFile;
import v9t9.common.files.NativeFileFactory;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.engine.memory.MemoryEntryInfoBuilder;
import v9t9.server.EmulatorLocalServer;
import v9t9.server.client.EmulatorServerBase;
import v9t9.tools.cycler.CycleCounter;

/**
 * @author ejs
 *
 */
public class CountCycles {
	private static final String PROGNAME = CountCycles.class.getSimpleName();
	
	private static void help(IMachine machine) {
        System.out.println("\n"
                        + "9900 Cycle Counter\n"
                        + "\n" 
                        + PROGNAME + " {-m<domain> <address> <raw file>} {<FIAD memory image>}\n" +
           			 "-e <addr> [-s <addr>] [-n <count>] [-l<list file>]  \n" +
           			 "\n"+
           			 "-m<domain> <address> <raw file>: load <raw file> into memory at <address>\n"+
    				"Domains:");
        for (IMemoryDomain domain : machine.getMemory().getDomains()) {
        	System.out.println("\t" + domain.getIdentifier());
        }
        System.out.println("\n"+
           			 "-e <addr>: start executing at addr (via BL)\n" +
           			 "-s <addr>: stop executing at addr, if code does not RT\n" +
           			 "-n <count>: execute at most <count> instructions (0 means go forever,\n"+
           			 "\tor until -s <...> or RT)\n" +
           			 "-l sends a listing to the given file (- for stdout)");

    }

	public static void main(String[] args) {
		LoggingUtils.setupNullLogging();
        
		
        EmulatorServerBase server = new EmulatorLocalServer();
        String modelId = server.getMachineModelFactory().getDefaultModel();
        IMachine machine = createMachine(server, modelId);
        int startAddr = 0, stopAddr = 0;
        boolean gotEntry = false;
        boolean gotFile = false;
        
        int numInstrs = 0;
        
        PrintStream out = System.out;
        
        if (args.length == 0) {
        	help(machine);
        	System.exit(0);
        }
        
        Getopt getopt = new Getopt(PROGNAME, args, "?m:::e:l:s:n:");
        int opt;
        while ((opt = getopt.getopt()) != -1) {
            switch (opt) {
            case '?':
            	help(machine);
                break;
            case 'm': {
            	String domainName = getopt.getOptarg();
            	String addrStr = args[getopt.getOptind()];
            	String fileName = args[getopt.getOptind()+1];
            	getopt.setOptind(getopt.getOptind()+2);
            	IMemoryDomain domain = machine.getMemory().getDomain(domainName);
            	if (domain == null) {
            		System.err.println("could not resolve memory domain '"+ domainName +'"');
            		return;
            	}
            	
            	File file = new File(fileName);
            	int addr = HexUtils.parseHexInt(addrStr);
            	IMemoryEntry userEntry = domain.getEntryAt(addr);
            	if (userEntry == null) {
	            	MemoryEntryInfo userEntryInfo = new MemoryEntryInfoBuilder(
	        			domain.isWordAccess() ? 2 : 1)
	            		.withDomain(domainName)
	            		.withAddress(HexUtils.parseHexInt(addrStr))
	            		.withSize((int) file.length())
	            		.storable(false)
	            		.create(domainName);
	            	
					try {
						userEntry = machine.getMemory().getMemoryEntryFactory().newMemoryEntry(userEntryInfo);
						System.out.println("loading " + fileName);
						machine.getMemory().addAndMap(userEntry);
					} catch (IOException e) {
						System.err.println("could not load memory to '"+ domainName +"' from '" + fileName +"'\n"+e.getMessage());
						System.exit(1);
					}
            	}
            	
            	gotFile = true;
            	try {
	        		byte[] contents = FileUtils.readInputStreamContentsAndClose(new FileInputStream(file));
	        		for (int o = 0; o < contents.length; o++) {
	        			machine.getConsole().flatWriteByte(addr + o, contents[o]);
	        		}
            	} catch (IOException e) {
					System.err.println("could not load memory to '"+ domainName +"' from '" + fileName +"'\n"+e.getMessage());
					System.exit(1);
				}
            	break;
            }
            case 'e': {
            	startAddr = HexUtils.parseHexInt(getopt.getOptarg());
            	gotEntry = true;
            	break;
            }
            case 's': {
            	stopAddr = HexUtils.parseHexInt(getopt.getOptarg());
            	break;
            }
            case 'l':
            	String name = getopt.getOptarg();
            	if (name.equals("-"))
            		out = System.out;
            	else
	            	try {
	            		out = new PrintStream(new File(name));
	            	} catch (IOException e) {
	            		System.err.println("Failed to create list file: " + e.getMessage());
	            		System.exit(1);
	            	}
            	break;   
            case 'n': {
            	numInstrs = Integer.parseInt(getopt.getOptarg());
            	break;
            }
            default:
            	//throw new AssertionError();
    
            }
        }
        
        // leftover files are FIAD
        int idx = getopt.getOptind();
        while (idx < args.length) {
        	String name = args[idx++];
        	int loadNext = 0;
        	do {
        		int size = 0;
        		int addr = 0;
	        	try {
	        		System.err.println("loading " + name);
	        		NativeFile file = NativeFileFactory.INSTANCE.createNativeFile(new File(name));
	        		byte[] contents = new byte[file.getFileSize()];
	        		file.readContents(contents, 0, 0, contents.length);
	        		if (contents.length < 6) {
	        			throw new IOException("not enough data for memory image header");
	        		}
	        		loadNext = ((contents[0] & 0xff) << 8) | (contents[1] & 0xff);
	        		size = ((contents[2] & 0xff) << 8) | (contents[3] & 0xff);
	        		addr = ((contents[4] & 0xff) << 8) | (contents[5] & 0xff);
	        		if (!((addr >= 0x2000 && addr < 0x4000) || (addr >= 0xA000 || addr <= 0xffff))) {
	        			throw new IOException("malformed memory image header: content not targeting RAM");
	        		}
	        		if (addr + size > 0x10000) {
	        			throw new IOException("malformed memory image header: addr + size > 64k");
	        		}
	        		
	        		for (int o = 0; o < size; o++) {
	        			machine.getConsole().flatWriteByte(addr + o, contents[o + 6]);
	        		}
	        		
	        		gotFile = true;
				} catch (IOException e) {
					System.err.println("failed to load file: " + e.getMessage());
	        		System.exit(1);
				}
	        	
	        	name = name.substring(0, name.length() - 1) +(char)  ( name.charAt(name.length() - 1) + 1);
        	} while (loadNext != 0);
        }
        		
        if (!gotFile) {
        	System.err.println("no files specified");
    		System.exit(1);
        }
        if (!gotEntry) {
        	System.err.println("no entry point specified");
        	System.exit(1);
        }

        machine.getMemoryModel().loadMemory(machine.getEventNotifier());

        try {
	        CycleCounter cycler = new CycleCounter(machine, startAddr, stopAddr, numInstrs, out);
	        cycler.run();
        } finally {
        	if (out != System.out)
        		out.close();
        }
	}

	/**
	 * @param modelId
	 * @return
	 */
	private static IMachine createMachine(EmulatorServerBase server, String modelId) {
		if (modelId == null)
			modelId = server.getMachineModelFactory().getDefaultModel();
		try {
			server.init(modelId);
		} catch (IOException e) {
			System.err.println("Failed to contact or create server:" + modelId);
			e.printStackTrace();
			System.exit(23);
			return null;
		}
		return server.getMachine();
	}


}
