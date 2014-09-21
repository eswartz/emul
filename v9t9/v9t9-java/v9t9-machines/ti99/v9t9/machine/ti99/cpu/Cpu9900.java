/*
  Cpu9900.java

  (c) 2005-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.cpu;

import v9t9.common.asm.IDecompilePhase;
import v9t9.common.asm.IInstructionFactory;
import v9t9.common.asm.IRawInstructionFactory;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.cpu.IExecutor;
import v9t9.common.cpu.IInstructionEffectLabelProvider;
import v9t9.common.hardware.ICruChip;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.SettingSchema;
import v9t9.common.settings.Settings;
import v9t9.engine.Dumper;
import v9t9.engine.compiler.CodeBlockCompilerStrategy;
import v9t9.engine.cpu.CpuBase;
import v9t9.engine.cpu.Executor;
import v9t9.machine.ti99.asm.HighLevelCodeInfo;
import v9t9.machine.ti99.asm.InstructionFactory9900;
import v9t9.machine.ti99.asm.RawInstructionFactory9900;
import v9t9.machine.ti99.asm.TopDownPhase;
import v9t9.machine.ti99.compiler.Compiler9900;
import v9t9.machine.ti99.interpreter.NewInterpreter9900;
import ejs.base.properties.IProperty;
import ejs.base.settings.ISettingSection;
import ejs.base.utils.HexUtils;

/**
 * The 9900 engine.
 * 
 * @author ejs
 */
public class Cpu9900 extends CpuBase {
	
	public static final SettingSchema settingForceAllIntsToLevel1 = new SettingSchema(
			ISettingsHandler.MACHINE,
			"ForceAllIntsToLevel1",
			Boolean.TRUE);
	
    public static final int PIN_INTREQ = 1 << 31;
    public static final int PIN_LOAD = 1 << 3;
    public static final int PIN_RESET = 1 << 5;
    
    /** When intreq, the interrupt level (IC* bits on the TMS9900). */
    private byte ic;
	public static final int REG_PC = 16;
	public static final int REG_ST = 17;
	public static final int REG_WP = 18;
  

	
	public static final int TMS_9900_BASE_CYCLES_PER_SEC = 3000000;
	/* interrupt pins */
	public static final int INTLEVEL_RESET = 0;
	public static final int INTLEVEL_LOAD = 1;
	public static final int INTLEVEL_INTREQ = 2;
	

    /** 
     * When set, implement TI-99/4A behavior where all interrupts
     * are perceived as level 1.
     */
    private IProperty forceIcTo1;
    
	private final IVdpChip vdp;
	private Dumper dumper;

    public Cpu9900(IMachine machine, IVdpChip vdp) {
    	super(machine, new CpuState9900(machine.getConsole()));
		this.vdp = vdp;
    	
		this.dumper = new Dumper(Settings.getSettings(machine),
			settingDumpInstructions, settingDumpFullInstructions);
		
		forceIcTo1 = Settings.get(machine, settingForceAllIntsToLevel1);
		
        cyclesPerSecond.setInt(TMS_9900_BASE_CYCLES_PER_SEC);

    }
    
    /* (non-Javadoc)
     * @see v9t9.emulator.runtime.cpu.Cpu#getBaseCyclesPerSec()
     */
    @Override
    public int getBaseCyclesPerSec() {
    	return TMS_9900_BASE_CYCLES_PER_SEC;
    }

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
//    	ic = forceIcTo1.getBoolean() ? 1 : level;
    }
    
    /**
     * 
     */
    public void contextSwitch(short newwp, short newpc) {
    	((CpuState9900) state).contextSwitch(newwp, newpc);
        noIntCount = 2;
   }

    public void contextSwitch(int addr) {
        contextSwitch(state.getConsole().readWord(addr), state.getConsole().readWord(addr+2));
    }

    /**
     * Poll the TMS9901 to see if any interrupts are pending.
     * @return true if any pending
     */
    public final boolean doCheckInterrupts() {
    	// do not allow interrupts after some instructions
	    if (noIntCount > 0) {
	    	noIntCount--;
	    	return false;
	    }
	    
	    vdp.syncVdpInterrupt(machine);
	    
	    ICruChip cruAccess = machine.getCru();
	    if (cruAccess != null) {
	    	//pins &= ~PIN_INTREQ;
	    	cruAccess.pollForPins(this);
	    	if (cruAccess.isInterruptWaiting()) {
	    		ic = forceIcTo1.getBoolean() ? 1 : cruAccess.getInterruptLevel(); 
	    		if (state.getStatus().getIntMask() >= ic) {
	    			pins |= PIN_INTREQ;
	    			cruAccess.handlingInterrupt();
	    			return true;    		
	    		} else {
	    			//System.out.print('-');
	    		}
	    	} 
	    }
	    
    	if (((pins &  PIN_LOAD + PIN_RESET) != 0)) {
    		//System.out.println("Pins set... " + Integer.toHexString(pins));
    		return true;
    	}   
    	
    	return false;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.emulator.runtime.Cpu#handleInterrupts()
	 */
    public final void handleInterrupts() {
    	dumper.info("*** Aborted");
        
    	// non-maskable
    	if ((pins & PIN_LOAD) != 0) {
            // non-maskable
            
        	// this is ordinarily reset by external hardware, but
        	// we don't yet have a way to scan instruction execution
        	pins &= ~PIN_LOAD;

            ic = 0;
            
        	setIdle(false);
        	dumper.info("*** NMI ***");
            System.out.println("**** NMI ****");
            contextSwitch(0xfffc);
            
            cycleCounts.addExecute(22);
        } else if ((pins & PIN_RESET) != 0) {
        	pins &= ~PIN_RESET;
        	setIdle(false);
        	
        	dumper.info("*** RESET ***");
            System.out.println("**** RESET ****");
            state.getStatus().expand((short) 0);
            contextSwitch(0);
            cycleCounts.addExecute(26);
            
            pins = 0;
            ic = 0;
            
            // ensure the startup code has enough time to clear memory
            //noIntCount = 10000;
            
            machine.getExecutor().interpretOneInstruction();
            //throw new AbortedException();
        } else if ((pins & PIN_INTREQ) != 0) {
//        	System.out.println(System.currentTimeMillis());
			if (state.getStatus().getIntMask() >= ic) {	// already checked int mask in status
			    // maskable
				pins &= ~PIN_INTREQ;
				
				//System.out.print('=');
				//interrupts++;
			    contextSwitch(0x4 * ic);
			    cycleCounts.addExecute(22);
			    
			    // no more interrupt until 9901 gives us another
			    ic = 0;
			    setIdle(false);
			        
			    // for now, we need to do this, otherwise the compiled code may check intlevel and immediately ... oh, I dunno
			    machine.getExecutor().interpretOneInstruction();
			} else {
				System.out.print('?');
			}
		}
    }

	public void saveState(ISettingSection section) {
		super.saveState(section);
		section.put("PC", state.getPC());
		section.put("WP", ((CpuState9900) state).getWP());
		section.put("status", state.getStatus().flatten());
	}

	public void loadState(ISettingSection section) {
		if (section == null) {
			setPin(INTLEVEL_RESET);
			return;
		}
		
		state.setPC((short) section.getInt("PC"));
		((CpuState9900) state).setWP((short) section.getInt("WP"));
		state.getStatus().expand((short) section.getInt("status"));
		super.loadState(section);
		
	}

	@Override
	public String getCurrentStateString() {
		return "WP=>" 
		+ HexUtils.toHex4(((CpuState9900) state).getWP())
		+ "\t\tST=" +state.getStatus();
	}

	/* (non-Javadoc)
	 * @see v9t9.common.cpu.ICpu#reset()
	 */
	@Override
	public void reset() {
		//contextSwitch(0);
		setPin(PIN_RESET);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.Cpu#nmi()
	 */
	@Override
	public void nmi() {
		setPin(PIN_LOAD);		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.Cpu#irq()
	 */
//	@Override
//	public void irq() {
//		setPin(PIN_INTREQ);		
//	}
	
	@Override
	public boolean shouldDebugCompiledCode(short pc) {
		return ((pc >= 0x6000 && pc < 0x8000) 
				&& Settings.get(this, Compiler9900.settingDumpModuleRomInstructions).getBoolean());
	}

	/**
	 * @return
	 */
	public int getWP() {
		return ((CpuState9900) state).getWP();
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#getPC()
	 */
	public short getPC() {
		return state.getPC();
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#getST()
	 */
	public short getST() {
		return state.getST();
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#setPC(short)
	 */
	public void setPC(short pc) {
		state.setPC(pc);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#setST(short)
	 */
	public void setST(short st) {
		state.setST(st);
	}

	/**
	 * @param wp
	 */
	public void setWP(short wp) {
		((CpuState9900) state).setWP(wp);
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
	 * @see v9t9.common.cpu.ICpu#applyCycles(int)
	 */
	@Override
	public void applyCycles(int cycles) {
		if (vdp != null)
			vdp.addCpuCycles(cycles);
		super.applyCycles(cycles);		
	}
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.ICpu#getInstructionFactory()
	 */
	@Override
	public IRawInstructionFactory getRawInstructionFactory() {
		return RawInstructionFactory9900.INSTANCE;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.ICpu#getInstructionFactory()
	 */
	@Override
	public IInstructionFactory getInstructionFactory() {
		return InstructionFactory9900.INSTANCE;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.ICpu#createExecutor(v9t9.common.cpu.ICpuMetrics)
	 */
	@Override
	public IExecutor createExecutor() {
		return new Executor(machine, this,  
//				new Interpreter9900((IMachine) getMachine()),
				new NewInterpreter9900((IMachine) getMachine()),
				new Compiler9900(this),
				new CodeBlockCompilerStrategy(),
				new DumpFullReporter9900(this), 
				new DumpReporter9900(this));
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.ICpu#createDecompiler()
	 */
	@Override
	public IDecompilePhase createDecompiler() {
		return new TopDownPhase(getState(), new HighLevelCodeInfo(getState(), 
				new InstructionFactory9900()));
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.ICpu#getInstructionEffectLabelProvider()
	 */
	@Override
	public IInstructionEffectLabelProvider createInstructionEffectLabelProvider() {
		return new InstructionEffectLabelProvider9900();
	}
}