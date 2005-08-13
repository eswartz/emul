/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.cpu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import v9t9.MemoryDomain;

/**
 * Handle executing instructions, either in interpret mode or compile mode.
 * 
 * @author ejs
 */
public class Executor {

    public Cpu cpu;

    boolean bInterpreting;

    public Interpreter interp;
    Compiler compiler;
    
    public long nInstructions;

    public PrintWriter dump, dumpfull;

    
    public Executor(Cpu cpu, boolean interpret) {
        this.bInterpreting = interpret;
        this.cpu = cpu;
        this.interp = new Interpreter(cpu);
        this.compiler = new Compiler(this);
        
        if (false)
            try {
                File file = new File("/tmp/instrs.txt");
                dump = new PrintWriter(new FileOutputStream(file));
                File file2 = new File("/tmp/instrs_full.txt");
                dumpfull = new PrintWriter(new FileOutputStream(file2));
            } catch (FileNotFoundException e) {
                System.exit(1);
            }
        
    }

    public interface Action {
        public class Block {
            /* our CPU memory */
            public MemoryDomain domain;
            /* the instruction (in) */
            public Instruction inst;	
            /* EAs for operands 1 and 2 */
            public short ea1, ea2;
            /* values for operands 1 and 2 (in: EAs or values, out: value)
            for MPY/DIV, val3 holds lo reg */
            public short val1, val2, val3;	
            /* values (in: original, out: changed, if needed) */
            public short pc, wp;
            /* status word (in/out) */
            public Status status;

        }
        void act(Block block);
    }

    /** Run an unbounded amount of code.  Some external factor
     * 	tells the execution unit when to stop.
     */
    // TODO how to stop?
    public void execute() {
        boolean interpreting = bInterpreting;
        cpu.ping();
        if (!interpreting) {
            /* try to make or run native code */
            interpreting = !compiler.execute();
            //System.out.println("out at " + v9t9.Globals.toHex4(cpu.getPC()));
        }
        if (interpreting) {
            interp.execute(cpu.console.readWord(cpu.getPC()));
            nInstructions++;
        }
    }
}