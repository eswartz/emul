/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 30, 2004
 *
 */
package v9t9.cpu;

import v9t9.CruHandler;
import v9t9.MemoryDomain;

/** This is the interface to the runtime-generated class. */
abstract public class CompiledCode {
    protected v9t9.cpu.Cpu cpu;
    protected MemoryDomain memory;
    protected Executor exec;
    protected CruHandler cru;
    protected v9t9.vdp.Vdp vdp;
    protected v9t9.Gpl gpl;
    protected int nInstructions;
    java.io.PrintWriter dump, dumpfull;
    
    public CompiledCode() {
        
    }
    
    public CompiledCode(Executor exec) {
        this.exec = exec;
        this.cpu = exec.cpu;
        this.memory = exec.cpu.memory.CPU;
        this.cru = exec.cpu.getMachine().getCru();
        this.vdp = exec.cpu.memory.getVdpMmio();
        this.gpl = exec.cpu.memory.getGplMmio();
    }

    /** Run code
     * 
     * @param exec the execution context
     * @return true if code exited normally (i.e. max # instructions invoked, 
     * or jumped outside its own block), false if exec.cpu.PC refers to
     * instruction that must be emulated. 
     */
    abstract public boolean run();
    
    public void dump(short pc, short wp, Status status, short vdpaddr, short gromaddr) {
        if (dump != null) {
            dump.println(v9t9.Globals.toHex4(pc)
                    + " "
                    + v9t9.Globals.toHex4(status.flatten() & 0xffff)
                    + " "
                    + v9t9.Globals.toHex4(vdpaddr)
                    + " "
                    + v9t9.Globals.toHex4(gromaddr));
            dump.flush();
        }
    }
    
    public void dumpBefore(String ins, short pc, short wp, Status status, 
            short ea1, short val1, 
            short ea2, short val2,
            int op1type, int op1dest, int op2type, int op2dest) {
        if (dumpfull != null) {
            dumpfull.print("*" + Integer.toHexString(pc).toUpperCase() + ": "
                    + ins + " ==> ");
            if (op1type != Operand.OP_NONE
                    && op1dest != Operand.OP_DEST_KILLED) {
                String str = v9t9.Globals.toHex4(val1) +"(@"+v9t9.Globals.toHex4(ea1) + ")";
                dumpfull.print("op1=" + str + " ");
            }
            if (op2type != Operand.OP_NONE
                    && op2dest != Operand.OP_DEST_KILLED) {
                String str = v9t9.Globals.toHex4(val2) +"(@"+v9t9.Globals.toHex4(ea2) + ")";
                dumpfull.print("op2=" + str);
            }
            dumpfull.print(" || ");
        }
        
    }

    public void dumpAfter(short pc, short wp, Status status, 
            short ea1, short val1, 
            short ea2, short val2,
            int op1type, int op1dest, int op2type, int op2dest) {
        if (dumpfull != null) {
            if (op1type != Operand.OP_NONE
                    && op1dest != Operand.OP_DEST_FALSE) {
                String str = v9t9.Globals.toHex4(val1) +"(@"+v9t9.Globals.toHex4(ea1) + ")";
                dumpfull.print("op1=" + str + " ");
            }
            if (op2type != Operand.OP_NONE
                    && op2dest != Operand.OP_DEST_FALSE) {
                String str = v9t9.Globals.toHex4(val2) +"(@"+v9t9.Globals.toHex4(ea2) + ")";
                dumpfull.print("op2=" + str + " ");
            }
            dumpfull.print("st="
                    + Integer.toHexString(status.flatten() & 0xffff)
                            .toUpperCase() + " wp="
                    + Integer.toHexString(wp & 0xffff).toUpperCase());
            dumpfull.println();
            dumpfull.flush();
        }
        
    }
}
