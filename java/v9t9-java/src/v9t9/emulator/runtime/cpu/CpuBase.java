package v9t9.emulator.runtime.cpu;

import org.ejs.coffee.core.properties.IPersistable;
import org.ejs.coffee.core.properties.IProperty;
import org.ejs.coffee.core.properties.IPropertyListener;
import org.ejs.coffee.core.settings.ISettingSection;

import v9t9.emulator.common.Machine;
import v9t9.emulator.hardware.CruAccess;
import v9t9.engine.VdpHandler;
import v9t9.engine.cpu.Status;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.MemoryDomain.MemoryAccessListener;

public abstract class CpuBase  implements MemoryAccessListener, IPersistable, Cpu {

	protected Machine machine;

	public void loadState(ISettingSection section) {
		settingRealTime.loadState(section);
		settingCyclesPerSecond.loadState(section);
		cruAccess.loadState(section.getSection("CRU"));
	}

	public void saveState(ISettingSection section) {
		settingRealTime.saveState(section);
		settingCyclesPerSecond.saveState(section);
		cruAccess.saveState(section.addSection("CRU"));
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
	private int currentcycles = 0;
	/** total # current cycles executed */
	protected long totalcurrentcycles;
	protected int interruptTick;
	protected final VdpHandler vdp;
	/** State of the pins above  */
	protected int pins;
	
	protected CruAccess cruAccess;
	/**
	 * Called when hardware triggers another pin.
	 */
	public void setPin(int mask) {
		pins |= mask;
	}

	private int ticks;
	protected boolean allowInts;
	protected int interrupts;

	protected CpuState state;

	public CpuBase(Machine machine, CpuState state, int interruptTick, VdpHandler vdp) {
		 this.machine = machine;
		this.vdp = vdp;
		this.state = state;
        this.state.getConsole().setAccessListener(this);
        this.interruptTick = interruptTick;
        
        settingCyclesPerSecond.addListener(new IPropertyListener() {

			public void propertyChanged(IProperty setting) {
				baseclockhz = setting.getInt();
				targetcycles = (int)((long) baseclockhz * CpuBase.this.interruptTick / 1000);
		        currenttargetcycles = targetcycles;
		        System.out.println("target: " + targetcycles);
			}
        	
        });
        
        settingRealTime.addListener(new IPropertyListener() {

			public void propertyChanged(IProperty setting) {
				tick();
				if (setting.getBoolean()) {
					totalcurrentcycles = totaltargetcycles;
					currenttargetcycles = settingCyclesPerSecond.getInt() * CpuBase.this.interruptTick / 1000;
				}
			}
        	
        });
	}

	public Machine getMachine() {
	    return machine;
	}

	public void setConsole(MemoryDomain console) {
		state.setConsole(console);
	}

	public final MemoryDomain getConsole() {
		return state.getConsole();
	}


    public Status getStatus() {
        return state.getStatus();
    }

    public void setStatus(Status status) {
        state.setStatus(status);
    }

    
	public synchronized void addCycles(int cycles) {
		if (cycles != 0) {
			this.currentcycles += cycles; 
			vdp.addCpuCycles(cycles);
		}
	}

	public synchronized void tick() {
		totalcurrentcycles += currentcycles;
		
		// if we went over, aim for fewer this time
		currenttargetcycles = (int) (totaltargetcycles - totalcurrentcycles);
		if (currenttargetcycles > settingCyclesPerSecond.getInt() * 2) {
			// something really threw us off -- just start over
			totalcurrentcycles = totaltargetcycles;
		}
		currentcycles = 0;
		
		totaltargetcycles += targetcycles;
	
		ticks++;
	}

	public synchronized boolean isThrottled() {
		return (currentcycles >= currenttargetcycles);
	}

	public void access(MemoryEntry entry) {
		addCycles(entry.getLatency());
	}

	public synchronized int getCurrentCycleCount() {
		return currentcycles;
	}

	public synchronized int getCurrentTargetCycleCount() {
		return currenttargetcycles;
	}

	public synchronized long getTotalCycleCount() {
		return totalcurrentcycles;
	}

	public synchronized long getTotalCurrentCycleCount() {
		return totalcurrentcycles + currentcycles;
	}

	public synchronized int getTickCount() {
		return ticks;
	}

	public boolean isAllowInts() {
		return allowInts;
	}

	public void setAllowInts(boolean allowInts) {
		this.allowInts = allowInts;
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

	
}