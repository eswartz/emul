/**
 * 
 */
package v9t9.tools.cycler;

import ejs.base.utils.HexUtils;
import gnu.getopt.Getopt;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;

import v9t9.common.files.NativeFile;
import v9t9.common.files.NativeFileFactory;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.engine.memory.MemoryEntryInfoBuilder;
import v9t9.server.EmulatorLocalServer;
import v9t9.server.client.EmulatorServerBase;

/**
 * @author ejs
 *
 */
public class Cycler9900 {
	private static final String PROGNAME = Cycler9900.class.getSimpleName();
	
	private static void help(IMachine machine) {
        System.out.println("\n"
                        + "9900 Cycle Counter\n"
                        + "\n" 
                        + PROGNAME + " {-m<domain> <address> <raw file>} {<FIAD memory image>}\n" +
           			 "-e <addr> [-l<list file>]\n" +
           			 "\n"+
           			 "-m<domain> <address> <raw file>: load <raw file> into memory at <address>\n"+
    				"Domains:");
        for (IMemoryDomain domain : machine.getMemory().getDomains()) {
        	System.out.println("\t" + domain.getIdentifier());
        }
        System.out.println("\n"+
           			 "-e <addr>: start executing at addr (via BL)\n" +
           			 "-s <addr>: stop executing at addr, if code does not RT\n" +
           			 "-l sends a listing to the given file (- for stdout)");

    }

	public static void main(String[] args) {;
		String logName = Cycler9900.class.getName() + "/../log4j.properties";
		URL logURL = Cycler9900.class.getResource(logName);
		if (logURL != null)
			System.setProperty("log4j.configuration", logURL.toString());
        
        EmulatorServerBase server = new EmulatorLocalServer();
        String modelId = server.getMachineModelFactory().getDefaultModel();
        IMachine machine = createMachine(server, modelId);
        int startAddr = 0, stopAddr = 0;
        boolean gotEntry = false;
        boolean gotFile = false;
        
        PrintStream out = System.out;
        
        Getopt getopt = new Getopt(PROGNAME, args, "?m::e:l:s:");
        int opt;
        while ((opt = getopt.getopt()) != -1) {
            switch (opt) {
            case '?':
            	help(machine);
                break;
            case 'm': {
            	String domainName = getopt.getOptarg();
            	String addrStr = getopt.getOptarg();
            	String fileName = getopt.getOptarg();
            	IMemoryDomain domain = machine.getMemory().getDomain(domainName);
            	if (domain == null) {
            		System.err.println("could not resolve memory domain '"+ domainName +'"');
            		return;
            	}
            	MemoryEntryInfo userEntryInfo = new MemoryEntryInfoBuilder(
        			domain.isWordAccess() ? 2 : 1)
            		.withDomain(domainName)
            		.withAddress(HexUtils.parseInt(addrStr))
            		.withSize(-0x10000)
            		.storable(false)
            		.withFilename(fileName)
            		.create(domainName);
            	
            	IMemoryEntry userEntry;
				try {
					userEntry = machine.getMemory().getMemoryEntryFactory().newMemoryEntry(userEntryInfo);
					System.out.println("loading " + fileName);
					machine.getMemory().addAndMap(userEntry);
					gotFile = true;
				} catch (IOException e) {
					System.err.println("could not load memory to '"+ domainName +"' from '" + fileName +"'\n"+e.getMessage());
					System.exit(1);
				}
            	break;
            }
            case 'e': {
            	startAddr = HexUtils.parseInt(getopt.getOptarg());
            	gotEntry = true;
            	break;
            }
            case 's': {
            	stopAddr = HexUtils.parseInt(getopt.getOptarg());
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
	        Cycler cycler = new Cycler(machine, startAddr, stopAddr, out);
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
