/*
  CompiledCode.java

  (c) 2005-2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.engine.compiler;

import java.io.PrintWriter;

import ejs.base.settings.Logging;
import ejs.base.utils.HexUtils;


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
