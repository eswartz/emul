/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 30, 2004
 *
 */
package v9t9.engine.compiler;

import java.io.PrintWriter;


import v9t9.base.settings.Logging;
import v9t9.base.utils.HexUtils;
import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.IOperand;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.ICpuState;
import v9t9.common.cpu.IExecutor;
import v9t9.common.cpu.IStatus;
import v9t9.common.memory.IMemoryDomain;

/** This is the interface to the runtime-generated class. */
abstract public class CompiledCode {
    protected final ICpu cpu;
    protected final ICpuState cpuState;
    protected final IMemoryDomain memory;
    protected final IExecutor exec;
    protected int nInstructions, nCycles;
    
    public CompiledCode(IExecutor exec) {
        this.exec = exec;
        this.cpu = exec.getCpu();
        this.cpuState = exec.getCpu().getState();
        this.memory = exec.getCpu().getConsole();
    }

   
    abstract public boolean run();
    
    public void dump(short pc, short wp, IStatus status, int vdpaddr, int gromaddr) {
        PrintWriter dump = Logging.getLog(cpu.settingDumpInstructions());
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
    public void dumpBefore(String ins, short pc, short wp, IStatus status, 
            short ea1, short val1, 
            short ea2, short val2,
            int op1type, int op1dest, int op2type, int op2dest) {
    	PrintWriter dumpfull = Logging.getLog(cpu.settingDumpFullInstructions());
        
        if (dumpfull != null) {
            dumpfull.print("*" + HexUtils.toHex4(pc) + ": "
                    + ins + " ==> ");
            if (op1type != IMachineOperand.OP_NONE
                    && op1dest != IOperand.OP_DEST_KILLED) {
                String str = HexUtils.toHex4(val1) +"(@"+HexUtils.toHex4(ea1) + ")";
                dumpfull.print("op1=" + str + " ");
            }
            if (op2type != IMachineOperand.OP_NONE
                    && op2dest != IOperand.OP_DEST_KILLED) {
                String str = HexUtils.toHex4(val2) +"(@"+HexUtils.toHex4(ea2) + ")";
                dumpfull.print("op2=" + str);
            }
            dumpfull.print(" || ");
        }
        
    }

    /**
	 * @param pc  
	 */
    public void dumpAfter(short pc, short wp, IStatus status, 
            short ea1, short val1, 
            short ea2, short val2,
            int op1type, int op1dest, int op2type, int op2dest) {
    	PrintWriter dumpfull = Logging.getLog(cpu.settingDumpFullInstructions());
        if (dumpfull != null) {
            if (op1type != IMachineOperand.OP_NONE
                    && op1dest != IOperand.OP_DEST_FALSE) {
                String str = HexUtils.toHex4(val1) +"(@"+HexUtils.toHex4(ea1) + ")";
                dumpfull.print("op1=" + str + " ");
            }
            if (op2type != IMachineOperand.OP_NONE
                    && op2dest != IOperand.OP_DEST_FALSE) {
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
