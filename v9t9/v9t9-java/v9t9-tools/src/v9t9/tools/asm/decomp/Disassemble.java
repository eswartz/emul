/*
 * (c) Ed Swartz, 2010
 *
 */
package v9t9.tools.asm.decomp;

import ejs.base.utils.HexUtils;
import gnu.getopt.Getopt;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;


import v9t9.common.asm.MemoryRange;
import v9t9.machine.ti99.asm.TopDownPhase;

public class Disassemble {

    private static final String PROGNAME = Disassemble.class.getName();
    
    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        Getopt getopt;
        int opt;
        
        Decompiler dc = new Decompiler9900();
        
        int baseAddr = 0;
        
        getopt = new Getopt(PROGNAME, args, "?o:a:b:r:d:");
        String outfilename = null;
		while ((opt = getopt.getopt()) != -1) {
			switch (opt) {
            case '?':
                help();
                break;
            case 'o':
            	outfilename = getopt.getOptarg();
                break;
            case 'b':
                baseAddr = HexUtils.parseInt(getopt.getOptarg()) & 0xfffe;
                break;
            case 'a':
                dc.addFile(getopt.getOptarg(), baseAddr);
                break;
            case 'r':
                dc.addRangeFromArgv(getopt.getOptarg(), true /* code */);
                break;
            case 'd':
                dc.addRangeFromArgv(getopt.getOptarg(), false /* code */);
                break;
            default:
                throw new AssertionError();
    
            }
        }
        
        if (getopt.getOptind() < args.length) {
            System.err.println(PROGNAME + ": unexpected argument: " + args[getopt.getOptind() ]);
            System.exit(1);
        }
        
        if (dc.highLevel.getMemoryRanges().isEmpty()) {
            System.err.println(PROGNAME + ": no code added: use -r or -d options to add content");
            System.exit(1);
        }

        TopDownPhase llp = new TopDownPhase(dc.state, dc.highLevel);
        Collection<MemoryRange> ranges = llp.disassemble();
        
        
        PrintStream os = outfilename != null ? new PrintStream(new File(outfilename)) : System.out;
        llp.dumpInstructions(os, ranges);
        if (outfilename != null)
        	os.close();
    }

    private static void help() {
        System.out
                .println("\n"
                        + "tidisasm Disassembler v1.0\n"
                        + "\n"
                        + "Usage:   " + PROGNAME + " [options] { -b <addr> -a <file> } { -r from:to -d from:to }\n"
                        + "\n"
                        + PROGNAME + " will 'decompile' 99/4A files or binaries\n"
                        + "\n"
                        + "Options:\n"
                        + "\t\t-?        -- this help\n"
                        + "\t\t-o <file> -- send output to <file> (else stdout)\n"
                        + "\t\t-b <addr> -- specify logical base address of next -a binary\n"
                        + "\t\t-a <file> -- specify file to incorporate\n"
                        + "\t\t-r <addr>:<addr> -- specify range to disassemble\n"
                        + "\t\t-d <addr>:<addr> -- specify range to treat as data\n"
                        + "\n");

    }

}