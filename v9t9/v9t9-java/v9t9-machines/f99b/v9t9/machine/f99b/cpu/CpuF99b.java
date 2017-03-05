/*
  CpuF99b.java

  (c) 2010-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.f99b.cpu;

import java.io.PrintWriter;

import ejs.base.settings.ISettingSection;
import ejs.base.settings.Logging;
import ejs.base.utils.HexUtils;


import v9t9.common.asm.IDecompilePhase;
import v9t9.common.asm.IInstructionFactory;
import v9t9.common.asm.IRawInstructionFactory;
import v9t9.common.cpu.AbortedException;
import v9t9.common.cpu.ChangeBlock;
import v9t9.common.cpu.IExecutor;
import v9t9.common.cpu.IInstructionEffectLabelProvider;
import v9t9.common.hardware.ICruChip;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.machine.IMachine;
import v9t9.engine.compiler.NullCompilerStrategy;
import v9t9.engine.cpu.CpuBase;
import v9t9.engine.cpu.Executor;
import v9t9.machine.f99b.asm.ChangeBlockF99b;
import v9t9.machine.f99b.asm.StatusF99b;
import v9t9.machine.f99b.interpreter.InterpreterF99b;
import v9t9.machine.ti99.machine.InternalCruF99;

/**
 * The F99b engine.
 * 
 * @author ejs
 */
public class CpuF99b extends CpuBase {
    /**
	 * 
	 */
	public static final int MAX_STACK = 0x1000;
	
	public static final int PIN_INTREQ = 1 << 31;
    public static final int PIN_NMI = 1 << 3;
    public static final int PIN_RESET = 1 << 5;
    
    public static final int PC = 0;
	public static final int SP = 1;
	public static final int SP0 = 2;
	public static final int RP = 3;
	public static final int RP0 = 4;
	public static final int UP = 5;
	public static final int UP0 = 6;
	public static final int LP = 7;
	public static final int SR = 8;
	
	public static final int REG_COUNT = 9;

    /**
	 * 
	 */
	public static final int INT_BASE = 0xffe0;
	public static final int BASE_CYCLES_PER_SEC = 1000000;
	private final CpuStateF99b stateF99b;

	private final IVdpChip vdp;
	
	public CpuF99b(IMachine machine, IVdpChip vdp) {
		super(machine, new CpuStateF99b(machine.getConsole()));
		this.vdp = vdp;
		stateF99b = (CpuStateF99b) state;
        cyclesPerSecond.setInt(BASE_CYCLES_PER_SEC);
    }
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.Cpu#getBaseCyclesPerSec()
	 */
	@Override
	public int getBaseCyclesPerSec() {
		return BASE_CYCLES_PER_SEC;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(state.toString());
		sb.append(" [");
		int sp = stateF99b.getSP();
		sb.append(HexUtils.toHex4(state.getConsole().readWord(sp)));
		sb.append(' ');
		sb.append(HexUtils.toHex4(state.getConsole().readWord(sp + 2)));
		sb.append(' ');
		sb.append(HexUtils.toHex4(state.getConsole().readWord(sp + 4))); 
		sb.append(']');
		return  sb.toString();
	}
	
	
    /* (non-Javadoc)
	 * @see v9t9.emulator.runtime.Cpu#getPC()
	 */
	public short getPC() {
        return state.getPC();
    }

    /* (non-Javadoc)
	 * @see v9t9.emulator.runtime.Cpu#setPC(short)
	 */
	public void setPC(short pc) {
       	state.setPC(pc);
    }

    /** When intreq, the interrupt level */
    byte ic;
    
	 /* (non-Javadoc)
	 * @see v9t9.emulator.runtime.Cpu#resetInterruptRequest()
	 */
    public void resetInterruptRequest() {
    	pins &= ~PIN_INTREQ;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.emulator.runtime.Cpu#setInterruptRequest(byte)
	 */
    public void setInterruptRequest(byte level) {
    	setPin(PIN_INTREQ);
    }
    
	/**
     * Poll the TMS9901 to see if any interrupts are pending.
     * @return true if any pending
     */
    @Override
	public final boolean doCheckInterrupts() {
    	// do not allow interrupts after some instructions
//	    if (noIntCount > 0) {
//	    	noIntCount--;
//	    	return false;
//	    }
	    
	    vdp.syncVdpInterrupt(machine);
	    
	    ICruChip cruAccess = machine.getCru();
	    
	    if (cruAccess != null) {
	    	//pins &= ~PIN_INTREQ;
	    	cruAccess.pollForPins(this);
	    	if (cruAccess.isInterruptWaiting()) {
	    		ic = cruAccess.getInterruptLevel(); 
	    		int mask = state.getStatus().getIntMask();
    			if (mask >= ic) {
	    			pins |= PIN_INTREQ;
	    			cruAccess.handlingInterrupt();
	    			return true;    		
	    		}
	    	} 
	    }
	    
    	if (((pins &  PIN_NMI + PIN_RESET) != 0)) {
    		System.out.println("Pins set... "+Integer.toHexString(pins));
    		return true;
    	}   
    	
    	return false;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.emulator.runtime.Cpu#handleInterrupts()
	 */
    @Override
	public final void handleInterrupts() {
    	PrintWriter dumpfull = Logging.getLog(settingDumpFullInstructions());
		if (dumpfull != null) {
    		dumpfull.println("*** Aborted");
		}
        PrintWriter dump = Logging.getLog(settingDumpInstructions());
		if (dump != null) {
        	dump.println("*** Aborted");
		}
        
    	// non-maskable
    	if ((pins & PIN_NMI) != 0) {
            // non-maskable
            
        	// this is ordinarily reset by external hardware, but
        	// we don't yet have a way to scan instruction execution
        	pins &= ~PIN_NMI;
        	
            System.out.println("**** NMI ****");

            triggerInterrupt(InternalCruF99.INT_NMI);
        } else if ((pins & PIN_RESET) != 0) {
        	pins &= ~PIN_RESET;
            System.out.println("**** RESET ****");
            
           // reset();
            
            // ensure the startup code has enough time to clear memory
    		//noIntCount += 10000;
    		
    		state.setRegister(CpuF99b.SR, 0);
            
            // ROM should set these!
    		getState().setSP((short) 0xff80);
    		getState().setBaseSP((short) 0xff80);
    		getState().setRP((short) 0xffc0);
    		getState().setBaseRP((short) 0xffc0);
    		getState().setUP((short) 0xff00);
    		
    		setIdle(false);
    		
    		contextSwitch((short) 0x400);
    		
    		machine.getExecutor().breakAfterExecution(0);
            machine.getExecutor().interpretOneInstruction();
        } else if ((pins & PIN_INTREQ) != 0 && state.getStatus().getIntMask() >= ic) {	// already checked int mask in status
            // maskable
        	pins &= ~PIN_INTREQ;
        	
        	//System.out.print('=' + ic);

        	triggerInterrupt(ic);
            
            // no more interrupt until 9901 gives us another
            ic = 0;
                
            // for now, we need to do this, otherwise the compiled code may check intlevel and immediately ... oh, I dunno
            machine.getExecutor().interpretOneInstruction();
        }
    }

	@Override
	public void saveState(ISettingSection section) {
		section.put("SP", ((CpuStateF99b)state).getSP());
		section.put("SP0", ((CpuStateF99b)state).getBaseSP());
		section.put("RP", ((CpuStateF99b)state).getRP());
		section.put("RP0", ((CpuStateF99b)state).getBaseRP());
		section.put("UP", ((CpuStateF99b)state).getUP());
		section.put("UP0", ((CpuStateF99b)state).getBaseUP());
		section.put("ST", ((CpuStateF99b)state).getST());
		
		super.saveState(section);
	}

	@Override
	public void loadState(ISettingSection section) {
		if (section == null) {
			//setPin(INTLEVEL_RESET);
			return;
		}
		
		stateF99b.setSP((short) section.getInt("SP"));
		stateF99b.setBaseSP((short) section.getInt("SP0"));
		stateF99b.setRP((short) section.getInt("RP"));
		stateF99b.setBaseRP((short) section.getInt("RP0"));
		stateF99b.setUP((short) section.getInt("UP"));
		stateF99b.setBaseUP((short) section.getInt("UP0"));
		state.setST((short) section.getInt("ST"));
		
		super.loadState(section);
	}

	@Override
	public String getCurrentStateString() {
		return "SP=" + HexUtils.toHex4(state.getRegister(CpuF99b.SP)) 
		+ "\t\tSR=" + state.getStatus().toString();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.ICpu#reset()
	 */
	@Override
	public void reset() {
		setPin(PIN_RESET);

	}

	@Override
	public void nmi() {
		setPin(PIN_NMI);		
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.Cpu#irq()
	 */
//	@Override
//	public void irq() {
//		setPin(PIN_INTREQ);		
//	}
	/**
	 *	Issue a fault:  this places RP and SP on the stack under the
	 *	pushed status and PC, on a new stack. 
	 */
	public void fault() {
		if ((pins & PIN_RESET) != 0) {
			handleInterrupts();
			pins &= ~PIN_RESET;
			setIdle(false);
			throw new AbortedException();
		}
		
		int currentInt = ((StatusF99b) stateF99b.getStatus()).getCurrentInt();
		boolean doubleFault = currentInt == InternalCruF99.INT_FAULT || currentInt == InternalCruF99.INT_NMI;
		
    	PrintWriter dumpfull = Logging.getLog(this.settingDumpFullInstructions());
		if (dumpfull != null) {
			dumpfull.println("*** FAULT");
		}

		if (doubleFault) {
			if (dumpfull != null) { 
				dumpfull.println("*** DOUBLE FAULT ==> RESET");
				System.err.println("*** DOUBLE FAULT ==> RESET");
			}
			machine.reset();
			machine.setPaused(true);
			throw new AbortedException();
		}

		short oldrp = getState().getRP();
		short oldsp = getState().getSP();
		
		getState().setBaseSP((short) 0xffe0);
		getState().setSP((short) 0xffe0);
		getState().setBaseRP((short) 0xffd0);
		getState().setRP((short) 0xffd0);

		push(oldrp);
		push(oldsp);
		
		triggerInterrupt(InternalCruF99.INT_FAULT);
		
		throw new AbortedException();
	}

	public final CpuStateF99b getState() {
		return (CpuStateF99b) state;
	}
	@Override
	public boolean shouldDebugCompiledCode(short pc) {
		return true;
	}

	public short getST() {
		return state.getST();
	}

	public void setST(short st) {
		state.setST(st);
	}

	public final void push(short val) {
		stateF99b.push(val);
	}
	
	public final short peek() {
		return state.getConsole().readWord(stateF99b.getSP());
	}

	public final short pop() {
		return stateF99b.pop(this);
	}

	public final void rpush(short val) {
		stateF99b.rpush(val);
	}

	public final short rpeek() {
		return stateF99b.rpeek();
	}
	public final short rpop() {
		return stateF99b.rpop(this);
	}

	/**
	 * @param intNmi
	 * @return
	 */
	private short getIntVecAddr(int intNum) {
		return (short) (INT_BASE + intNum * 2);
	}

	/**
	 * @param pc2
	 */
	public void contextSwitch(short vec) {
		rpush(state.getPC());
		short addr = machine.getConsole().readWord(vec);
		state.setPC(addr);
	}

	/**
	 * @param intr
	 */
	public void triggerInterrupt(int intr) {
		idle = false;
		rpush(getST());
		((StatusF99b)state.getStatus()).setIntMask(0);
		((StatusF99b)state.getStatus()).setCurrentInt(intr);
		state.setRegister(SR, state.getStatus().flatten());		// TODO: why?
		short addr = getIntVecAddr(intr);
		contextSwitch(addr);
	}

	/**
	 * @return
	 */
	public int popd() {
		int hi = pop();
    	int val = (hi << 16) | (pop() & 0xffff);
		return val;
	}

	/**
	 * @param v
	 */
	public void pushd(int v) {
		push((short) (v & 0xffff));
		push((short) (v >> 16));
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.CpuBase#applyCycles()
	 * TODO: this should not depend on vdp
	 */
	@Override
	public void applyCycles() {
		if (vdp != null)
			vdp.addCpuCycles(cycleCounts.getTotal());
		super.applyCycles();
	}

	
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.ICpu#getInstructionFactory()
	 */
	@Override
	public IInstructionFactory getInstructionFactory() {
		throw new UnsupportedOperationException();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.ICpu#getInstructionFactory()
	 */
	@Override
	public IRawInstructionFactory getRawInstructionFactory() {
		return F99bInstructionFactory.INSTANCE;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.ICpu#createExecutor(v9t9.common.cpu.ICpuMetrics)
	 */
	@Override
	public IExecutor createExecutor() {
		return new Executor(machine, this,
				new InterpreterF99b(machine),
				null,
				new NullCompilerStrategy(),
				new DumpFullReporterF99b(this, null), 
				new DumpReporterF99b(this));
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.ICpu#createDecompiler()
	 */
	@Override
	public IDecompilePhase createDecompiler() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.ICpu#createInstructionEffectLabelProvider()
	 */
	@Override
	public IInstructionEffectLabelProvider createInstructionEffectLabelProvider() {
		return new InstructionEffectLabelProviderF99b();
	}
	
	@Override
	public ChangeBlock createChangeBlock(int pc) {
		synchronized (machine.getExecutor().getExecutionLock()) {
			return new ChangeBlockF99b(this, pc);
		}
	}

	
}