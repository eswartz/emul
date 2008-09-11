/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 30, 2004
 *
 */
package v9t9.emulator.runtime;

import java.io.PrintWriter;

import v9t9.emulator.handlers.CruHandler;
import v9t9.emulator.hardware.TI994A;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.Operand;
import v9t9.engine.cpu.Status;
import v9t9.engine.memory.Gpl;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.Vdp;
import v9t9.utils.Utils;

/** This is the interface to the runtime-generated class. */
abstract public class CompiledCode {
    protected v9t9.emulator.runtime.Cpu cpu;
    protected MemoryDomain memory;
    protected Executor exec;
    protected CruHandler cru;
    protected int nInstructions;
    
    // used for debugging
    protected Vdp vdp;
    protected Gpl gpl;
    
    public CompiledCode() {
        
    }
    
    public CompiledCode(Executor exec) {
        this.exec = exec;
        this.cpu = exec.cpu;
        this.memory = exec.cpu.getMachine().CPU;
        this.cru = exec.cpu.machine.getClient().getCruHandler();
        if (exec.cpu.machine instanceof TI994A) {
        	this.vdp = ((TI994A)exec.cpu.machine).getVdpMmio();
        	this.gpl = ((TI994A)exec.cpu.machine).getGplMmio();
        }
    }

   
    abstract public boolean run();
    
    public void dump(short pc, short wp, Status status, short vdpaddr, short gromaddr) {
        PrintWriter dump = exec.getDump();
        if (dump != null) {
            dump.println(Utils.toHex4(pc)
                    + " "
                    + Utils.toHex4(wp)
                    + " "
                    + Utils.toHex4(status.flatten() & 0xffff)
                    + " "
                    + Utils.toHex4(vdpaddr)
                    + " "
                    + Utils.toHex4(gromaddr));
            dump.flush();
        }
    }
    
    public void dumpBefore(String ins, short pc, short wp, Status status, 
            short ea1, short val1, 
            short ea2, short val2,
            int op1type, int op1dest, int op2type, int op2dest) {
        PrintWriter dumpfull = exec.getDumpfull();
        
        if (dumpfull != null) {
            dumpfull.print("*" + Utils.toHex4(pc) + ": "
                    + ins + " ==> ");
            if (op1type != MachineOperand.OP_NONE
                    && op1dest != Operand.OP_DEST_KILLED) {
                String str = Utils.toHex4(val1) +"(@"+Utils.toHex4(ea1) + ")";
                dumpfull.print("op1=" + str + " ");
            }
            if (op2type != MachineOperand.OP_NONE
                    && op2dest != Operand.OP_DEST_KILLED) {
                String str = Utils.toHex4(val2) +"(@"+Utils.toHex4(ea2) + ")";
                dumpfull.print("op2=" + str);
            }
            dumpfull.print(" || ");
        }
        
    }

    public void dumpAfter(short pc, short wp, Status status, 
            short ea1, short val1, 
            short ea2, short val2,
            int op1type, int op1dest, int op2type, int op2dest) {
        PrintWriter dumpfull = exec.getDumpfull();
        if (dumpfull != null) {
            if (op1type != MachineOperand.OP_NONE
                    && op1dest != Operand.OP_DEST_FALSE) {
                String str = Utils.toHex4(val1) +"(@"+Utils.toHex4(ea1) + ")";
                dumpfull.print("op1=" + str + " ");
            }
            if (op2type != MachineOperand.OP_NONE
                    && op2dest != Operand.OP_DEST_FALSE) {
                String str = Utils.toHex4(val2) +"(@"+Utils.toHex4(ea2) + ")";
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
