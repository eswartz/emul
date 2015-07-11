/*
  CountCycles.java

  (c) 2013-2014 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryDomain;
import v9t9.tools.cycler.CycleCounter;
import v9t9.tools.utils.Category;
import v9t9.tools.utils.ToolUtils;
import ejs.base.logging.LoggingUtils;
import ejs.base.utils.HexUtils;
import ejs.base.utils.Pair;
import gnu.getopt.Getopt;

/**
 * @author ejs
 *
 */
@Category(Category.DEVELOPMENT)
public class CountCycles {
	private static final String PROGNAME = CountCycles.class.getSimpleName();
	
	private static void help(IMachine machine) {
        System.out.println("\n"
                        + "V9t9 Cycle Counter\n"
                        + "\n" 
                        + PROGNAME + " {-m<domain> <address> <raw file>} {<FIAD memory image>}\n" +
           			 "-e <addr> [-s <addr>] [-t] [-n <count>] [-l<list file>]  \n" +
           			 "\n"+
           			 "-m<domain> <address> <raw file>: load <raw file> into memory at <address>\n"+
    				"Domains:");
        for (IMemoryDomain domain : machine.getMemory().getDomains()) {
        	System.out.println("\t" + domain.getIdentifier());
        }
        System.out.println("\n"+
        			"Remaining files are assumed to have a memory image header and load into RAM.\n"+
        			"\n"+
        			"By default, emulation starts at the entry point and stops when the program\n"+
        			"returns or enters a 'JMP $' loop.  The -s or -n arguments modify this.\n"+
        			"\n"+
        			"The cycle counts are listed as (F)etch, (L)oad, (S)tore, (E)xecute, (O)ther.\n"+
        			"\n"+
           			 "-e <addr>: start executing at addr (via subroutine branch)\n" +
           			 "-s <addr>: stop executing at addr, if code does not return\n" +
           			 "-n <count>: execute at most <count> instructions\n" +
           			 "-t: only get the totals, do not dump any instructions\n"+
           			 "-o sends a listing to the given file (- for stdout)\n"
           			 );

    }

	public static void main(String[] args) {
		LoggingUtils.setupNullLogging();
        
		IMachine machine = ToolUtils.createMachine();
		
        boolean gotEntry = false;
        boolean gotFile = false;
        
        if (args.length == 0) {
        	help(machine);
        	System.exit(0);
        }

        CycleCounter cycler = new CycleCounter();

		List<Pair<Integer, Integer>> ranges = new ArrayList<Pair<Integer,Integer>>();

        Getopt getopt = new Getopt(PROGNAME, args, "?m:::e:o:s:n:tM:");
        int opt;
        PrintStream out = null;
		while ((opt = getopt.getopt()) != -1) {
            switch (opt) {
            case '?':
            	help(machine);
                break;
            case 'm': {
            	String domainName = getopt.getOptarg();
            	int idx = getopt.getOptind();
            	if (domainName == null) {
            		domainName = args[idx++];
            	}
            	String addrStr = args[idx];
            	String fileName = args[idx+1];
            	getopt.setOptind(idx+2);

            	
            	try {
					ToolUtils.loadMemory(machine, domainName, fileName, addrStr, ranges);
				} catch (IOException e) {
					if (e.getCause() != null)
						System.err.println(e.getMessage() + "\n" + e.getCause());
					else
						System.err.println(e.getMessage());
					System.exit(1);
				}
            	break;
            }
            case 'e': {
            	cycler.setStartAddress(HexUtils.parseHexInt(getopt.getOptarg()));
            	gotEntry = true;
            	break;
            }
            case 's': {
            	cycler.setStopAddress(HexUtils.parseHexInt(getopt.getOptarg()));
            	break;
            }
            case 'o':
            	String name = getopt.getOptarg();
            	if (name.equals("-")) {
					cycler.setPrintStream(System.out);
				} else {
					try {
						out = new PrintStream(new File(name));
	            		cycler.setPrintStream(out);
	            	} catch (IOException e) {
	            		System.err.println("Failed to create list file: " + e.getMessage());
	            		System.exit(1);
	            	}
				}
            	break;   
            case 'n': {
            	cycler.setInstructionCount(Integer.parseInt(getopt.getOptarg()));
            	break;
            }
            case 't':
            	cycler.setShowTotal(true);
            	break;
            case 'M':
            	machine = ToolUtils.createMachine(getopt.getOptarg());
            	break;
            default:
            	//throw new AssertionError();
    
            }
        }

        // leftover files are FIAD
        int idx = getopt.getOptind();
        while (idx < args.length) {
        	String name = args[idx++];
        	try {
				ToolUtils.loadMemoryImage(machine, name, ranges);
			} catch (IOException e) {
				System.err.println("failed to load file: " + e.getMessage());
				System.exit(1);
			}
        	gotFile = true;
        }
        		
        if (!gotFile) {
        	machine.getCpu().reset();
        	System.err.println("no files specified, using machine ROM");
        }
        if (!gotEntry) {
        	System.err.println("no entry point specified");
        	System.exit(1);
        }
        
        machine.getMemoryModel().loadMemory(machine.getEventNotifier());

		cycler.setMachine(machine);

        try {
	        cycler.run();
        } finally {
        	if (out != null && out != System.out)
        		out.close();
        }
	}


}
