/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 30, 2004
 *
 */
package v9t9.emulator.runtime.compiler;

import java.io.PrintWriter;

import org.ejs.coffee.core.utils.HexUtils;

import v9t9.emulator.hardware.TI99Machine;
import v9t9.emulator.hardware.memory.mmio.GplMmio;
import v9t9.emulator.hardware.memory.mmio.VdpMmio;
import v9t9.emulator.runtime.cpu.Cpu;
import v9t9.emulator.runtime.cpu.Executor;
import v9t9.engine.CruHandler;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.Operand;
import v9t9.engine.cpu.Status;
import v9t9.engine.memory.MemoryDomain;

/** This is the interface to the runtime-generated class. */
abstract public class CompiledCode {
    protected Cpu cpu;
    protected MemoryDomain memory;
    protected Executor exec;
    protected CruHandler cru;
    protected int nInstructions, nCycles;
    
    // used for debugging
    protected VdpMmio vdpMmio;
    protected GplMmio gplMmio;
    
    public CompiledCode() {
        
    }
    
    public CompiledCode(Executor exec) {
        this.exec = exec;
        this.cpu = exec.cpu;
        this.memory = exec.cpu.getConsole();
        if (exec.cpu.getMachine() instanceof TI99Machine) {
        	TI99Machine ti99Machine = (TI99Machine) exec.cpu.getMachine();
			this.cru = ti99Machine.getCruManager();
        	this.vdpMmio = ti99Machine.getVdpMmio();
        	this.gplMmio = ti99Machine.getGplMmio();
        }
    }

   
    abstract public boolean run();
    
    public void dump(short pc, short wp, Status status, int vdpaddr, int gromaddr) {
        PrintWriter dump = Executor.getDump();
        if (dump != null) {
            dump.println(HexUtils.toHex4(pc)
                    + " "
                    + HexUtils.toHex4(wp)
                    + " "
                    + HexUtils.toHex4((status.flatten() & 0xffff))
                    + " "
                    + HexUtils.toHex4(vdpaddr)
                    + " "
                    + HexUtils.toHex4(gromaddr));
            dump.flush();
        }
    }
    
    /**
	 * @param wp  
     * @param status 
	 */
    public void dumpBefore(String ins, short pc, short wp, Status status, 
            short ea1, short val1, 
            short ea2, short val2,
            int op1type, int op1dest, int op2type, int op2dest) {
        PrintWriter dumpfull = Executor.getDumpfull();
        
        if (dumpfull != null) {
            dumpfull.print("*" + HexUtils.toHex4(pc) + ": "
                    + ins + " ==> ");
            if (op1type != MachineOperand.OP_NONE
                    && op1dest != Operand.OP_DEST_KILLED) {
                String str = HexUtils.toHex4(val1) +"(@"+HexUtils.toHex4(ea1) + ")";
                dumpfull.print("op1=" + str + " ");
            }
            if (op2type != MachineOperand.OP_NONE
                    && op2dest != Operand.OP_DEST_KILLED) {
                String str = HexUtils.toHex4(val2) +"(@"+HexUtils.toHex4(ea2) + ")";
                dumpfull.print("op2=" + str);
            }
            dumpfull.print(" || ");
        }
        
    }

    /**
	 * @param pc  
	 */
    public void dumpAfter(short pc, short wp, Status status, 
            short ea1, short val1, 
            short ea2, short val2,
            int op1type, int op1dest, int op2type, int op2dest) {
        PrintWriter dumpfull = Executor.getDumpfull();
        if (dumpfull != null) {
            if (op1type != MachineOperand.OP_NONE
                    && op1dest != Operand.OP_DEST_FALSE) {
                String str = HexUtils.toHex4(val1) +"(@"+HexUtils.toHex4(ea1) + ")";
                dumpfull.print("op1=" + str + " ");
            }
            if (op2type != MachineOperand.OP_NONE
                    && op2dest != Operand.OP_DEST_FALSE) {
                String str = HexUtils.toHex4(val2) +"(@"+HexUtils.toHex4(ea2) + ")";
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
