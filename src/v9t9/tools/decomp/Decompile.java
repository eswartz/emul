/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.tools.decomp;

import gnu.getopt.Getopt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ejs.emul.core.utils.HexUtils;

public class Decompile {

    private static final String PROGNAME = Decompile.class.getName();
    
    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        Decompiler dc = new Decompiler();
        
        int baseAddr = 0;
        List<Integer> refDefTables = new ArrayList<Integer>();
        
        Getopt getopt = new Getopt(PROGNAME, args, "?e:o:nb:a:r:d:hcvs:");
        int opt;
        while ((opt = getopt.getopt()) != -1) {
            switch (opt) {
            case '?':
                help();
                break;
            case 'e':
                dc.ext = getopt.getOptarg();
                break;
            case 'o':
                dc.outfilename = getopt.getOptarg();
                break;
            case 'f':
                dc.forceNonBinary = true;
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
            case 'h':
                dc.showOpcodeAddr = true;
                break;
            case 'c':
                dc.showComments = true;
                break;
            case 'v':
                dc.verbose++;
                break;
            case 'n':
                dc.nativeFile = true;
                break;
            case 's': {
                int addr = HexUtils.parseInt(getopt.getOptarg());
                refDefTables.add(new Integer(addr));
            }
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

        dc.getOptions().refDefTables = refDefTables;

        dc.decompile();
    }

    private static void help() {
        System.out
                .println("\n"
                        + "tidecomp 9900 Disassembler v1.0\n"
                        + "\n"
                        + "Usage:   " + PROGNAME + " [options] { -b <addr> -a <file> } { -r from:to -d from:to }\n"
                        + "\n"
                        + PROGNAME + " will 'decompile' 99/4A files or binaries\n"
                        + "\n"
                        + "Options:\n"
                        + "\t\t-?        -- this help\n"
                        + "\t\t-e <ext>  -- specify extension (default: " + Decompiler.DEFAULT_EXT + ")\n"
                        + "\t\t-o <file> -- send output to <file> (else stdout)\n"
                        + "\t\t-n        -- treat file as native binary (raw dump)\n"
                        + "\t\t-f        -- force disassembly of non-PROGRAM v9t9 files\n"
                        + "\t\t-b <addr> -- specify logical base address of next -a binary\n"
                        + "\t\t-a <file> -- specify file to incorporate\n"
                        + "\t\t-r <addr>:<addr> -- specify range to disassemble\n"
                        + "\t\t-d <addr>:<addr> -- specify range to treat as data\n"
                        + "\t\t-h        -- show opcode and address\n"
                        + "\t\t-c        -- show comments\n"
                        + "\t\t-v        -- verbose output\n"
                        + "\t\t-s <addr> -- add new REF/DEF symbol table address (end of table)\n"
                        + "\n");

    }

}
