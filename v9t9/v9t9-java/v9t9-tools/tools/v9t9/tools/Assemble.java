/*
  Assemble.java

  (c) 2008-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import v9t9.common.files.PathFileLocator;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.settings.SettingSchemaProperty;
import v9t9.engine.memory.DiskMemoryEntry;
import v9t9.engine.memory.MemoryEntryFactory;
import v9t9.engine.memory.MemoryEntryInfoBuilder;
import v9t9.tools.asm.Assembler;
import v9t9.tools.asm.FileContentEntry;
import v9t9.tools.asm.inst9900.Assembler9900;
import v9t9.tools.utils.Category;
import ejs.base.logging.LoggingUtils;
import gnu.getopt.Getopt;

@Category(Category.DEVELOPMENT)
public class Assemble {

    private static final String PROGNAME = Assemble.class.getName();
	private static MemoryEntryFactory memoryEntryFactory;
    
    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
    	LoggingUtils.setupNullLogging();
    	
    	if (args.length == 0) {
    		help();
    		return;
    	}
    	
        Assembler9900 assembler = new Assembler9900();
        
        assembler.setList(null);
        
        PathFileLocator locator = new PathFileLocator();
		memoryEntryFactory = new MemoryEntryFactory(null, assembler.getMemory(), locator);
        locator.setReadWritePathProperty(new SettingSchemaProperty("Output", "."));
        
        boolean selectedProcessor = false;
        int romStart = 0, romSize = 0x2000;
        
        boolean hadOutput = false;
        
        Getopt getopt = new Getopt(PROGNAME, args, "?r:m:d:g:l:D:e:L:H:v92Ox");
        int opt;
        while ((opt = getopt.getopt()) != -1) {
            switch (opt) {
            case '?':
                help();
                break;
            case 'r': {
            	String file = getopt.getOptarg();
            	int size = romSize;
            	int idx = file.lastIndexOf('#');
            	if (idx >= 0) {
            		size = Integer.parseInt(file.substring(idx+1));
            		file = file.substring(0, idx);
            	}
            	assembler.addMemoryEntry(createMemoryEntry(romStart, size, "CPU ROM", file));
            	hadOutput = true;
            	break;
            }
            case 'e': {
            	assembler.addMemoryEntry(
            			createMemoryEntry(romStart, romSize * 2, "CPU ROM", getopt.getOptarg()));
            	hadOutput = true;
            	break;
            }
            case 'm': {
            	assembler.addMemoryEntry(
            			createMemoryEntry(0x6000, 0x2000, "Module ROM", getopt.getOptarg()));
            	hadOutput = true;
            	break;
            }
            case 'd': {
            	assembler.addMemoryEntry(
            			createMemoryEntry(0x4000, 0x2000, "DSR ROM", getopt.getOptarg()));
            	hadOutput = true;
            	break;  
            }
            case 'L': {
            	assembler.addMemoryEntry(
            			createMemoryEntry(0x2000, 0x2000, "Low 8K Expansion RAM", getopt.getOptarg()));
            	hadOutput = true;
            	break;  
            }
            case 'H': {
            	assembler.addMemoryEntry(
            			createMemoryEntry(0xA000, 0x6000, "High 24K Expansion RAM", getopt.getOptarg()));
            	hadOutput = true;
            	break;  
            }
            case 'g': {
            	String file = getopt.getOptarg();
            	int size = 0x6000;
            	int idx = file.lastIndexOf('#');
            	if (idx >= 0) {
            		size = Integer.parseInt(file.substring(idx+1));
            		file = file.substring(0, idx);
            	}
            	assembler.addMemoryEntry(
            			createMemoryEntry(0x0000, size, "GROM", file));
            	hadOutput = true;
            	break; 
            }
            case 'l':
            	String name = getopt.getOptarg();
            	if (name.equals("-")) {
					assembler.setList(System.out);
				} else {
					try {
	            		assembler.setList(new PrintStream(new File(name)));
	            	} catch (IOException e) {
	            		System.err.println("Failed to create list file: " + e.getMessage());
	            		System.exit(1);
	            	}
				}
            	break;   
            case 'D': {
            	String equ = getopt.getOptarg();
            	assembler.defineEquate(equ);
            	break;    
            }
            case '9':
            	assembler.setProcessor(Assembler.PROC_9900);
            	romStart = 0;
            	romSize = 0x2000;
            	selectedProcessor = true;
            	break;
            case '2':
            	assembler.setProcessor(Assembler.PROC_MFP201);
            	romStart = 0xf000;
            	romSize = 0x1000;
            	selectedProcessor = true;
            	break;
            case 'O':
            	assembler.setOptimize(true);
            	break;
            case 'x':
            	assembler.setOptimize(false);
            	break;
            default:
            	//throw new AssertionError();
    
            }
        }
        
        if (!hadOutput) {
        	System.err.println(PROGNAME + ": no output file(s) specified");
        	System.exit(1);
        }
        if (!selectedProcessor) {
        	assembler.setProcessor(Assembler.PROC_9900);
        }
        
        if (getopt.getOptind() < args.length) {
        	while (getopt.getOptind() < args.length) {
	        	FileContentEntry entry;
	        	String name = args[getopt.getOptind()]; 
	        	try {
	        		entry = new FileContentEntry(new File(name));
	        		assembler.pushContentEntry(entry);
	        		getopt.setOptind(getopt.getOptind() + 1);
	        	} catch (IOException e) {
	        		System.err.println(PROGNAME + ": failed to read: " + name);
	                System.exit(1);
	            }
        	}
        } else {
        	System.err.println(PROGNAME + ": no input file(s) specified");
        }
        
        if (!assembler.assemble()) {
        	System.err.println("Errors during assembly");
        	for (IMemoryEntry ent : assembler.getMemoryEntries()) {
        		new File(((DiskMemoryEntry) ent).getSymbolFileName()).delete();
        	}
        	System.exit(1);
        }
    }

	private static IMemoryEntry createMemoryEntry(int romStart,
			int size, String string, String file) throws IOException {
		MemoryEntryInfo info = MemoryEntryInfoBuilder.wordMemoryEntry()
			.withAddress(romStart).withSize(size).withFilename(file).
			storable(true).create(string);

		return memoryEntryFactory.newMemoryEntry(info);
	}

	private static void help() {
        System.out
                .println("\n"
                        + "9900 Assembler v2.0\n"
                        + "\n" 
                        +
                        PROGNAME + " <input file>\n"+
                        "\t[-r|e <console ROM output>[#size]] [-m <module ROM output>]\n" +
                        "\t[-d <DSR ROM output>] [-g <console GROM output>[#size]]\n"+
           			 	"\t[-L <low 8k output>] [-H <high 24K output>]\n"+
                        "\t[-Dequ=val] [-l<list file>]\n" +
           			 "\n"+
           			 "-r|e saves the 8k/16k console ROM block at >0000.\n" +
           			 "-m saves the 8k module ROM block at >6000.\n" +
           			 "-d saves the 8k DSR ROM block at >4000.\n" +
           			 "-g saves the 24k GROM block at >0000.\n"+
           			 "-L saves the 8k expansion RAM block at >2000.\n"+
           			 "-H saves the 24k expansion RAM block at >A000.\n"+
           			 "-l sends a listing to the given file (- for stdout)");

    }

}
