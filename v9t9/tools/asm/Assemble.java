/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.tools.asm;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.IOException;

public class Assemble {

    private static final String PROGNAME = Assembler.class.getName();
    
    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
    	AssemblerOptions options = new AssemblerOptions();
        Assembler assembler = new Assembler(options);
        
        Getopt getopt = new Getopt(PROGNAME, args, "?r:m:d:g:");
        int opt;
        while ((opt = getopt.getopt()) != -1) {
            switch (opt) {
            case '?':
                help();
                break;
            default:
            	//throw new AssertionError();
    
            }
        }
        
        if (getopt.getOptind() < args.length) {
        	FileEntry entry;
        	String name = args[getopt.getOptind()]; 
        	try {
        		entry = new FileEntry(new File(name));
        		assembler.pushFileEntry(entry);
        		getopt.setOptind(getopt.getOptind() + 1);
        	} catch (IOException e) {
        		System.err.println(PROGNAME + ": failed to read: " + name);
                System.exit(1);
            }
        } else {
        	System.err.println(PROGNAME + ": no files specified");
        }
        
        assembler.assemble();
    }

    private static void help() {
        System.out
                .println("\n"
                        + "tiasm 9900 Disassembler v2.0\n"
                        + "\n" 
                        +
                        "TIASM <input file> [-r <console ROM output>] [-m <module ROM output>]\n" +
           			 "[-d <DSR ROM output>] [-g <console GROM output>] [<list file>]\n" +
           			 "\n"+
           			 "-r saves the 8k memory block at >0000.\n" +
           			 "-m saves the 8k memory block at >6000.\n" +
           			 "-d saves the 8k memory block at >4000.\n" +
           			 "-g saves the 24k memory block at >0000.  This can only be used with -m.\n");

    }

}
