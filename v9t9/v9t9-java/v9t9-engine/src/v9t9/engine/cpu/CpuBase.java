package v9t9.engine.cpu;

import java.util.concurrent.Semaphore;


import v9t9.base.properties.IPersistable;
import v9t9.base.properties.IProperty;
import v9t9.base.properties.IPropertyListener;
import v9t9.base.settings.ISettingSection;
import v9t9.common.cpu.AbortedException;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.ICpuState;
import v9t9.common.cpu.IStatus;
import v9t9.common.machine.IBaseMachine;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryAccessListener;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.settings.Settings;

public abstract class CpuBase  implements IMemoryAccessListener, IPersistable, ICpu {

	protected IMachine machine;

	public void loadState(ISettingSection section) {
		realTime.loadState(section);
		cyclesPerSecond.loadState(section);
	}

	public void saveState(ISettingSection section) {
		realTime.saveState(section);
		cyclesPerSecond.saveState(section);
	}

	public abstract boolean doCheckInterrupts();

	long lastInterrupt;
	
	/*	Variables for controlling a "real time" emulation of the 9900
	processor.  Each call to execute() sets an estimated cycle count
	for the instruction and parameters in "instcycles".  We waste
	time in 1/BASE_EMULATOR_HZ second quanta to maintain the appearance of a
	3.0 MHz clock. */

	
	protected int baseclockhz;
	/** target # cycles to be executed per tick */
	protected int targetcycles;
	/** target # cycles to be executed for this tick */
	protected int currenttargetcycles;
	/**  total # target cycles expected throughout execution */
	protected long totaltargetcycles;
	/** current cycles per tick */
	private volatile int currentcycles = 0;
	/** total # current cycles executed */
	protected long totalcurrentcycles;
	protected int interruptTick;
	/** State of the pins above  */
	protected int pins;
	
	protected volatile boolean idle;
	protected Semaphore interruptWaiting;
	
	/**
	 * Called when hardware triggers another pin.
	 */
	public void setPin(int mask) {
		pins |= mask;
	}

	private int ticks;
	public int noIntCount;
	protected int interrupts;

	protected ICpuState state;

	protected IProperty cyclesPerSecond;

	protected IProperty realTime;

	private IProperty dumpInstructions;

	private IProperty dumpFullInstructions;

	public CpuBase(IMachine machine, ICpuState state, int interruptTick) {
		this.machine = machine;
		this.state = state;
        this.state.getConsole().setAccessListener(this);
        this.interruptTick = interruptTick;
        
        interruptWaiting = new Semaphore(0);
        
        cyclesPerSecond = Settings.get(this, ICpu.settingCyclesPerSecond);
        realTime = Settings.get(this, ICpu.settingRealTime);
        dumpFullInstructions = Settings.get(this, ICpu.settingDumpFullInstructions);
        dumpInstructions = Settings.get(this, ICpu.settingDumpInstructions);
        
        cyclesPerSecond.addListenerAndFire(new IPropertyListener() {

			public void propertyChanged(IProperty setting) {
				baseclockhz = setting.getInt();
				targetcycles = (int)((long) baseclockhz * CpuBase.this.interruptTick / 1000);
		        currenttargetcycles = targetcycles;
		        //System.out.println("target: " + targetcycles);
			}
        	
        });
        
        realTime.addListenerAndFire(new IPropertyListener() {

			public void propertyChanged(IProperty setting) {
				tick();
				if (setting.getBoolean()) {
					totalcurrentcycles = totaltargetcycles;
					currenttargetcycles = cyclesPerSecond.getInt() * CpuBase.this.interruptTick / 1000;
				}
			}
        	
        });
	}

	/* (non-Javadoc)
	 * @see v9t9.common.cpu.ICpu#settingCyclesPerSecond()
	 */
	@Override
	public IProperty settingCyclesPerSecond() {
		return cyclesPerSecond;
	}
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.ICpu#settingRealTime()
	 */
	@Override
	public IProperty settingRealTime() {
		return realTime;
	}
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.ICpu#settingDumpFullInstructions()
	 */
	@Override
	public IProperty settingDumpFullInstructions() {
		return dumpFullInstructions;
	}
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.ICpu#settingDumpInstructions()
	 */
	@Override
	public IProperty settingDumpInstructions() {
		return dumpInstructions;
	}
	
	public IBaseMachine getMachine() {
	    return machine;
	}

	public final IMemoryDomain getConsole() {
		return state.getConsole();
	}


    public IStatus getStatus() {
        return state.getStatus();
    }

    public void setStatus(IStatus status) {
        state.setStatus(status);
    }

    
	public synchronized void addCycles(int cycles) {
		if (cycles != 0) {
			this.currentcycles += cycles; 
		}
	}

	public synchronized void tick() {
		totalcurrentcycles += currentcycles;
		
		// if we went over, aim for fewer this time
		currenttargetcycles = (int) (totaltargetcycles - totalcurrentcycles);
		
		// If really fast speeds are demanded, and/or the system is otherwise busy,
		// we can fall behind the target.  But if we fall too far behind,
		// we'll never catch up.
		if (currenttargetcycles > cyclesPerSecond.getInt() / 10) {
			// something really threw us off -- just start over
			totalcurrentcycles = totaltargetcycles;
			currenttargetcycles = targetcycles;
		}
		//System.out.println(System.currentTimeMillis()+": " + currentcycles + " -> " + currenttargetcycles);
		
		currentcycles = 0;
		
		totaltargetcycles += targetcycles;
	
		ticks++;
		
		if (isIdle())
			interruptWaiting.release();
	}

	public synchronized boolean isThrottled() {
		return (currentcycles >= currenttargetcycles);
	}

	public void access(IMemoryEntry entry) {
		addCycles(entry.getLatency());
	}

	public int getCurrentCycleCount() {
		return currentcycles;
	}

	public int getCurrentTargetCycleCount() {
		return currenttargetcycles;
	}

	public long getTotalCycleCount() {
		return totalcurrentcycles;
	}

	public synchronized long getTotalCurrentCycleCount() {
		return totalcurrentcycles + currentcycles;
	}

	public int getTickCount() {
		return ticks;
	}

	public int getTargetCycleCount() {
		return targetcycles;
	}

	public void acknowledgeInterrupt() {
		interrupts++;
	}
	public int getAndResetInterruptCount() {
		int n = interrupts;
		interrupts = 0;
		return n;
	}

	public void addAllowedCycles(int i) {
		currenttargetcycles += i;
		
	}

	public void resetCycleCounts() {
		currenttargetcycles = currentcycles = 0;
		totalcurrentcycles = totaltargetcycles = 0;
	}

	/**
	 * @return the state
	 */
	public ICpuState getState() {
		return state;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.Cpu#setIdle(boolean)
	 */
	@Override
	public void setIdle(boolean b) {
		if (this.idle != b) {
			this.idle = b;
			getMachine().interrupt();
			tick();
			/*
			if (b && interruptWaiting.tryAcquire()) {
				// ignore idle, since interrupts waiting
				//interruptWaiting.release();
			}*/
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.Cpu#isIdle()
	 */
	@Override
	public boolean isIdle() {
		return idle;
	}
	
    /* (non-Javadoc)
	 * @see v9t9.emulator.runtime.Cpu#checkInterrupts()
	 */
    public final void checkInterrupts() {
    	if (doCheckInterrupts()) {
    		//interruptWaiting.release();
    		throw new AbortedException();
    	}
    }
    
    /* (non-Javadoc)
	 * @see v9t9.emulator.runtime.Cpu#checkAndHandleInterrupts()
	 */
	public final void checkAndHandleInterrupts() {
    	if (doCheckInterrupts()) {
    		//interruptWaiting.release();
    		handleInterrupts();
    	}
    }
	
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#getRegisterCount()
	 */
	@Override
	public int getRegisterCount() {
		return state.getRegisterCount();
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#getRegisterName(int)
	 */
	@Override
	public String getRegisterName(int reg) {
		return state.getRegisterName(reg);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#getRegisterTooltip(int)
	 */
	@Override
	public String getRegisterTooltip(int reg) {
		return state.getRegisterTooltip(reg);
	}
}