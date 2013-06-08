/*
  Decompile.java

  (c) 2008-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools;

import ejs.base.logging.LoggingUtils;
import ejs.base.utils.Pair;
import gnu.getopt.Getopt;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryDomain;
import v9t9.tools.asm.decomp.Disassembler;
import v9t9.tools.utils.Category;
import v9t9.tools.utils.ToolUtils;

@Category(Category.DEVELOPMENT)
public class Disassemble {

    private static final String PROGNAME = Disassemble.class.getName();
    
    private static void help(IMachine machine) {
	    System.out
	            .println("\n"
	                    + "V9t9 Disassembler v2.0\n"
	                    + "\n"
	                    + "Usage:   " + PROGNAME + " [options] { -r from:to -d from:to }\n"
	                    + "\t{-m<domain> <address> <raw file>} {<FIAD memory image>}\n" 
	                    + "\n"
	                    + PROGNAME + " will disassemble 99/4A files or binaries\n"
	                    + "\n"
	                    + "-m<domain> <address> <raw file>: load <raw file> into memory at <address>\n"
	    				+ "Domains:");
	            for (IMemoryDomain domain : machine.getMemory().getDomains()) {
	            	System.out.println("\t" + domain.getIdentifier());
	            }
	            System.out.println("\n"
	            		+ "Remaining files are assumed to have a memory image header and load into RAM.\n"
	                    + "\n"
	                    + "Options:\n"
	                    + "\t\t-?        -- this help\n"
	                    + "\t\t-o <file> -- send output to <file> (else stdout)\n"
	                    + "\t\t-p        -- show plain instructions, no addresses or opcodes\n"
	                    + "\n");
	
	}

	/**
     * @param args
     */
    public static void main(String[] args) throws IOException {
		LoggingUtils.setupNullLogging();
        
		Getopt getopt;
        
        IMachine machine  = ToolUtils.createMachine();
        Disassembler dc = new Disassembler(machine);

        boolean gotFile = false;
        
        getopt = new Getopt(PROGNAME, args, "?o:m:::p");
        String outfilename = null;
		int opt;
		
		List<Pair<Integer, Integer>> ranges = new ArrayList<Pair<Integer,Integer>>();
		
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
					gotFile = true;
				} catch (IOException e) {
					if (e.getCause() != null)
						System.err.println(e.getMessage() + "\n" + e.getCause());
					else
						System.err.println(e.getMessage());
					System.exit(1);
				}
            	break;
            }
            case 'o':
            	outfilename = getopt.getOptarg();
                break;
            case 'p':
            	dc.setShowPlain(true);
            	break;
            default:
                throw new AssertionError();
    
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
        	help(machine);
        	System.err.println("no files specified");
    		System.exit(1);
        }		
        
        BitSet codeAddrs = new BitSet();
        for (Pair<Integer, Integer> ent : ranges) {
        	codeAddrs.set(ent.first, ent.first + ent.second);
        }
        
        PrintStream os = outfilename != null ? new PrintStream(new File(outfilename)) : System.out;
        dc.dumpCode(codeAddrs, os);
        if (outfilename != null)
        	os.close();
    }

}
