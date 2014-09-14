/*
  CpuStateF99b.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.f99b.cpu;


import java.util.HashMap;
import java.util.Map;

import ejs.base.utils.HexUtils;
import ejs.base.utils.ListenerList;

import v9t9.common.cpu.ICpuState;
import v9t9.common.cpu.IStatus;
import v9t9.common.cpu.InstructionWorkBlock;
import v9t9.common.machine.IRegisterAccess;
import v9t9.common.memory.IMemoryDomain;
import v9t9.machine.f99b.asm.InstructionWorkBlockF99b;
import v9t9.machine.f99b.asm.StatusF99b;

/**
 * @author Ed
 *
 */
public class CpuStateF99b implements ICpuState {

	/**
	 * 
	 */
	private static final int ALIGNED_REGMASK = ((1 << CpuF99b.SP) | (1 << CpuF99b.SP0) 
							| (1 << CpuF99b.RP) | (1 << CpuF99b.RP0)
							| (1 << CpuF99b.UP) | (1 << CpuF99b.UP0)
							| (1 << CpuF99b.LP));

	private final static Map<Integer, String> regNames = new HashMap<Integer, String>();
	private final static Map<String, Integer> regIds = new HashMap<String, Integer>();
	private static void register(int reg, String id) {
		regNames.put(reg, id);
		regIds.put(id, reg);
	}
	
	static {
		register(CpuF99b.PC, "PC");
		register(CpuF99b.SP, "SP");
		register(CpuF99b.SP0, "SP0");
		register(CpuF99b.RP, "RP");
		register(CpuF99b.RP0, "RP0");
		register(CpuF99b.UP, "UP");
		register(CpuF99b.UP0, "UP0");
		register(CpuF99b.SR, "SR");
		register(CpuF99b.LP, "LP");
	}
	
	private final IMemoryDomain console;
	private IStatus status;

	private short regs[] = new short[16];

	private ListenerList<IRegisterWriteListener> listeners = new ListenerList<IRegisterAccess.IRegisterWriteListener>();
	
	public CpuStateF99b(IMemoryDomain console) {
		this.console = console;
		this.status = createStatus();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PC=" + HexUtils.toHex4(getPC()) + "; SP=" + HexUtils.toHex4(getSP()) + "; RP=" + HexUtils.toHex4(getRP());
	}
	
	
	public short getPC() {
	    return (short) getRegister(CpuF99b.PC);
	}

	public void setPC(short pc) {
		setRegister(CpuF99b.PC, pc);
	}
	
    public String getGroupName() {
    	return "F99b Registers";
    }

    /* (non-Javadoc)
     * @see v9t9.common.machine.IRegisterAccess#getFirstRegister()
     */
    @Override
    public int getFirstRegister() {
    	return 0;
    }
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#getRegisterCount()
	 */
	@Override
	public int getRegisterCount() {
		return CpuF99b.REG_COUNT;
	}

	public int getRegister(int reg) {
	    return regs[reg];
	}

	@Override
	public int setRegister(int reg, int val) {
		int old;
		
		// always aligned
		if (((1 << reg) & ALIGNED_REGMASK) != 0) {	
			val &= ~1;
		}
		old = regs[reg];
		regs[reg] = (short) val;
		if (reg == CpuF99b.SR) {
			getStatus().expand(regs[reg]);
		}
		
		fireRegisterChanged(reg, val);
		
		return old & 0xffff;
		
	}
	
	protected String getRegisterId(int reg) {
		return regNames.get(reg);
	}

	@Override
	public int getRegisterNumber(String id) {
		Integer num = regIds.get(id);
		if (num == null)
			return Integer.MIN_VALUE;
		return num;
	}



	protected String getRegisterDescription(int reg) {
		switch (reg) {
		case CpuF99b.PC:
			return "Program Counter";
		case CpuF99b.SP:
			return "Stack pointer";
		case CpuF99b.SP0:
			return "Upper Stack Limit";
		case CpuF99b.RP:
			return "R-Stack Pointer";
		case CpuF99b.RP0:
			return "Upper R-Stack Limit";
		case CpuF99b.UP0:
			return "Default User Base";
		case CpuF99b.UP:
			return "User Base";
		case CpuF99b.SR:
			return "Status Register";
		case CpuF99b.LP:
			return "Locals Pointer";
		}
		return null;
	}
	
	protected int getRegisterFlags(int reg) {
		switch (reg) {
		case CpuF99b.PC:
			return IRegisterAccess.FLAG_ROLE_PC;
		case CpuF99b.SP:
			return IRegisterAccess.FLAG_ROLE_SP;
		case CpuF99b.SP0:
		case CpuF99b.RP:
		case CpuF99b.RP0:
		case CpuF99b.UP0:
		case CpuF99b.UP:
			return IRegisterAccess.FLAG_ROLE_GENERAL;
		case CpuF99b.SR:
			return IRegisterAccess.FLAG_ROLE_ST;
		case CpuF99b.LP:
			return IRegisterAccess.FLAG_ROLE_FP;
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getRegisterDescription(int)
	 */
	@Override
	public RegisterInfo getRegisterInfo(int reg) {
		String id = getRegisterId(reg);
		if (id == null)
			return null;
		
		return new RegisterInfo(id, getRegisterFlags(reg),
				2, getRegisterDescription(reg));
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#getRegisterTooltip(int)
	 */
	@Override
	public String getRegisterTooltip(int reg) {
		switch (reg) {
		case CpuF99b.SR:
			return getStatus().toString();
		}
		return null;
	}

	@Override
	public IStatus createStatus() {
		return new StatusF99b();
	}

	public short getSP() {
		return (short) getRegister(CpuF99b.SP);
	}
	
	public void setSP(short sp) {
		setRegister(CpuF99b.SP, sp);
		
	}
	

	public short getRP() {
		return (short) getRegister(CpuF99b.RP);
	}
	
	public void setRP(short sp) {
		setRegister(CpuF99b.RP, sp);
	}
	

	public short getUP() {
		return (short) getRegister(CpuF99b.UP);
	}
	
	public void setUP(short up) {
		setRegister(CpuF99b.UP, up);
		
	}
	public short getLP() {
		return (short) getRegister(CpuF99b.LP);
	}
	
	public void setLP(short sp) {
		setRegister(CpuF99b.LP, sp);
	}
	
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#getConsole()
	 */
	@Override
	public final IMemoryDomain getConsole() {
		return console;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#getStatus()
	 */
	@Override
	public final IStatus getStatus() {
		return status;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#setStatus(v9t9.engine.cpu.Status)
	 */
	@Override
	public final void setStatus(IStatus status) {
		this.status = status;
	}

	public final short getST() {
	    return (short) getRegister(CpuF99b.SR);
	}

	public final void setST(short st) {
		setRegister(CpuF99b.SR, st);
	}

	public final short getBaseSP() {
		return (short) getRegister(CpuF99b.SP0);
	}
	
	public final void setBaseSP(short v) {
		setRegister(CpuF99b.SP0, v);
	}

	public final short getBaseRP() {
		return (short) getRegister(CpuF99b.RP0);
	}
	
	public final void setBaseRP(short v) {
		setRegister(CpuF99b.RP0, v);
		
	}

	public final short getBaseUP() {
		return (short) getRegister(CpuF99b.UP0);
	}
	
	public final void setBaseUP(short v) {
		setRegister(CpuF99b.UP0, v);
	}

	final void push(short val) {
		regs[CpuF99b.SP] -= 2;
		short sp = regs[CpuF99b.SP];
		console.writeWord(sp, val);
		
		/*if (sp < regs[CpuF99b.SP0] - CpuF99b.MAX_STACK) {
			System.err.println("Stack overflow!");
			cpu.fault();
		}*/			
	}
	

	final short pop(CpuF99b cpu) {
		short val = console.readWord(regs[CpuF99b.SP]);
		regs[CpuF99b.SP] += 2;
		if (regs[CpuF99b.SP] > regs[CpuF99b.SP0]) {
			System.err.println("Stack underflow!");
			cpu.fault();
		}
		return val;
	}


	public final short rpeek() {
		return console.readWord(regs[CpuF99b.RP]);
	}

	final void rpush(short val) {
		regs[CpuF99b.RP] -= 2;
		short rp = regs[CpuF99b.RP];
		console.writeWord(rp, val);
		/*if (rp < regs[CpuF99b.RP0] - CpuF99b.MAX_STACK) {
			System.err.println("R-Stack overflow!");
			cpu.fault();
		}*/	
				
	}
	final short rpop(CpuF99b cpu) {
		short val = console.readWord(regs[CpuF99b.RP]);
		regs[CpuF99b.RP] += 2;
		if (regs[CpuF99b.RP] > regs[CpuF99b.RP0]) {
			System.err.println("R-Stack underflow!");
			cpu.fault();
		}
		return val;
	}

	protected final void fireRegisterChanged(int reg, int value) {
		if (!listeners.isEmpty()) {
			for (IRegisterWriteListener listener : listeners) {
				try {
					listener.registerChanged(reg, value);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}
	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#addWriteListener(v9t9.common.machine.IRegisterAccess.IRegisterWriteListener)
	 */
	@Override
	public void addWriteListener(IRegisterWriteListener listener) {
		listeners.add(listener);
	}
	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#removeWriteListener(v9t9.common.machine.IRegisterAccess.IRegisterWriteListener)
	 */
	@Override
	public void removeWriteListener(IRegisterWriteListener listener) {
		listeners.remove(listener);
	}
}