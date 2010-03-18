/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.tools.asm.assembler;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import v9t9.engine.memory.DiskMemoryEntry;

public class Assemble {

    private static final String PROGNAME = Assembler.class.getName();
    
    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        Assembler assembler = new Assembler();
        
        assembler.setList(null);
        
        Getopt getopt = new Getopt(PROGNAME, args, "?r:m:d:g:l:D:e:v");
        int opt;
        while ((opt = getopt.getopt()) != -1) {
            switch (opt) {
            case '?':
                help();
                break;
            case 'r':
            	assembler.addMemoryEntry(
            			DiskMemoryEntry.newWordMemoryFromFile(
            					0x0, 0x2000, "CPU ROM",
            					assembler.getWritableConsole(),
            					getopt.getOptarg(),
            					0x0,
            					true));
            	break;
            case 'e':
            	assembler.addMemoryEntry(
            			DiskMemoryEntry.newWordMemoryFromFile(
            					0x0, 0x4000, "CPU ROM",
            					assembler.getWritableConsole(),
            					getopt.getOptarg(),
            					0x0,
            					true));
            	break;
            case 'm':
            	assembler.addMemoryEntry(
            			DiskMemoryEntry.newWordMemoryFromFile(
            					0x6000, 0x2000, "Module ROM", 
            					assembler.getWritableConsole(),
            					getopt.getOptarg(),
            					0x0,
            					true));
            	break;
            case 'd':
            	assembler.addMemoryEntry(
            			DiskMemoryEntry.newWordMemoryFromFile(
            					0x4000, 0x2000, "DSR ROM", 
            					assembler.getWritableConsole(),
            					getopt.getOptarg(),
            					0x0,
            					true));
            	break;         
            case 'g':
            	assembler.addMemoryEntry(
            			DiskMemoryEntry.newWordMemoryFromFile(
            					0x0000, 0x6000, "GROM", 
            					assembler.getWritableConsole(),
            					getopt.getOptarg(),
            					0x0,
            					true));
            	break;            	
            case 'l':
            	String name = getopt.getOptarg();
            	if (name.equals("-"))
            		assembler.setList(System.out);
            	else
	            	try {
	            		assembler.setList(new PrintStream(new File(name)));
	            	} catch (IOException e) {
	            		System.err.println("Failed to create list file: " + e.getMessage());
	            		System.exit(1);
	            	}
            	break;   
            case 'D': {
            	String equ = getopt.getOptarg();
            	assembler.defineEquate(equ);
            	break;    
            }
            default:
            	//throw new AssertionError();
    
            }
        }
        
        if (getopt.getOptind() < args.length) {
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
        } else {
        	System.err.println(PROGNAME + ": no files specified");
        }
        
        if (!assembler.assemble()) {
        	System.err.println("Errors during assembly");
        	System.exit(1);
        }
    }

    private static void help() {
        System.out
                .println("\n"
                        + "tiasm 9900 Assembler v2.0\n"
                        + "\n" 
                        +
                        "TIASM <input file> [-r|e <console ROM output>] [-m <module ROM output>]\n" +
           			 "[-d <DSR ROM output>] [-g <console GROM output>] [-Dequ=val] [-l<list file>]\n" +
           			 "\n"+
           			 "-r saves the 8k memory block at >0000.\n" +
           			 "-m saves the 8k memory block at >6000.\n" +
           			 "-d saves the 8k memory block at >4000.\n" +
           			 "-e saves the 16k memory block at >0000.\n" +
           			 "-g saves the 24k memory block at >0000.  This can only be used with -m.\n"+
           			 "-l sends a listing to the given file (- for stdout)");

    }

}
