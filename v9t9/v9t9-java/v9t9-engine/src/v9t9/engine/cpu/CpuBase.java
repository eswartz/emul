/*
  CpuBase.java

  (c) 2010-2016 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.cpu;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import ejs.base.properties.IPersistable;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.settings.ISettingSection;
import ejs.base.utils.ListenerList;
import ejs.base.utils.ListenerList.IFire;


import v9t9.common.asm.RawInstruction;
import v9t9.common.cpu.AbortedException;
import v9t9.common.cpu.CycleCounts;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.ICpuListener;
import v9t9.common.cpu.ICpuState;
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
	protected final AtomicInteger currenttargetcycles = new AtomicInteger();
	/**  total # target cycles expected throughout execution */
	protected long totaltargetcycles;
	/** current cycles per tick */
	protected final AtomicInteger currentcycles = new AtomicInteger();
	protected final CycleCounts cycleCounts;
	/** total # current cycles executed */
	protected long totalcurrentcycles;
	/** State of the pins above  */
	protected int pins;
	
	protected volatile boolean idle;
	protected final Semaphore interruptWaiting;
	
	/**
	 * Called when hardware triggers another pin.
	 */
	public void setPin(int mask) {
		pins |= mask;
	}

	private int ticks;
	protected int interrupts;

	protected ICpuState state;

	protected IProperty cyclesPerSecond;
	protected IProperty realTime;
	private IProperty dumpInstructions;
	private IProperty dumpFullInstructions;

	private ListenerList<ICpuListener> listeners = new ListenerList<ICpuListener>();

	private Semaphore allocatedCycles;

	private IProperty runForCountProp;

	protected int runForCount;

	public CpuBase(IMachine machine_, ICpuState state) {
		this.machine = machine_;
		this.state = state;
        this.state.getConsole().setAccessListener(this);
        this.cycleCounts = state.getCycleCounts();
        
        allocatedCycles = new Semaphore(0);
        interruptWaiting = new Semaphore(0);
        
        cyclesPerSecond = Settings.get(this, ICpu.settingCyclesPerSecond);
        realTime = Settings.get(this, ICpu.settingRealTime);
        runForCountProp = Settings.get(this, ICpu.settingRunForCount);
        dumpFullInstructions = Settings.get(this, ICpu.settingDumpFullInstructions);
        dumpInstructions = Settings.get(this, ICpu.settingDumpInstructions);
        
        cyclesPerSecond.addListenerAndFire(new IPropertyListener() {

			public void propertyChanged(IProperty setting) {

				baseclockhz = setting.getInt();
				targetcycles = (int)((long) baseclockhz / machine.getTicksPerSec());
		        currenttargetcycles.set(0);
		        cycleCounts.getAndResetTotal();
		        
		        allocatedCycles.drainPermits();
		        if (realTime.getBoolean())
		        	allocatedCycles.release(targetcycles);
		        //System.out.println("target: " + targetcycles);
			}
        	
        });
        
        realTime.addListenerAndFire(new IPropertyListener() {

			public void propertyChanged(IProperty setting) {
				tick();
				if (setting.getBoolean()) {
					totalcurrentcycles = totaltargetcycles;
					currenttargetcycles.set(cyclesPerSecond.getInt() / machine.getTicksPerSec());
				}
		        allocatedCycles.drainPermits();
		        allocatedCycles.release(targetcycles);

			}
        	
        });
        
        runForCountProp.addListenerAndFire(new IPropertyListener() {

			public void propertyChanged(IProperty setting) {
				runForCount = setting.getInt();
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

	/* (non-Javadoc)
	 * @see v9t9.common.cpu.ICpu#getCycleCounts()
	 */
	@Override
	public final CycleCounts getCycleCounts() {
		return cycleCounts;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.cpu.ICpu#getAllocatedCycles()
	 */
	@Override
	public Semaphore getAllocatedCycles() {
		return allocatedCycles;
	}
	public synchronized void tick() {
		applyCycles();
		int tickCycles = currentcycles.getAndSet(0);
		totalcurrentcycles += tickCycles;
		
		if (runForCount > 0 && totalcurrentcycles >= runForCount) {
			getMachine().stop();
		}
		
		int newTargetCycles = targetcycles;
		
		newTargetCycles = (int) (totaltargetcycles - totalcurrentcycles);
		
		if (true||newTargetCycles < targetcycles / 10 || newTargetCycles > targetcycles * 2) {
			if (newTargetCycles < 0) {
				// something really threw us off -- just start over
				//totalcurrentcycles = totaltargetcycles;
			}
			newTargetCycles = targetcycles;
		}
		
		
//		long now = System.currentTimeMillis();
//		if (firstTime != 0) {
//			long expTotalCycles = (cyclesPerSecond.getInt() * (now - firstTime) 
//					/ 1000);
//			newTargetCycles = (int) (expTotalCycles - totalcurrentcycles);
//			if (newTargetCycles < targetcycles / 2 || newTargetCycles > targetcycles * 2) {
//				newTargetCycles = targetcycles;
//				firstTime = now;
//				totalcurrentcycles = 0;
//			}
//			//System.out.println(newTargetCycles);
//		} else {
//			firstTime = now;
//		}

		currenttargetcycles.set(newTargetCycles);
		
//		System.out.println(System.currentTimeMillis()+": " + currentcycles + " -> " + currenttargetcycles.get());
		
		if (realTime.getBoolean()) {
			allocatedCycles.drainPermits();
			allocatedCycles.release(newTargetCycles);
		}
		
		totaltargetcycles += targetcycles;
	
		ticks++;
		
		if (isIdle())
			interruptWaiting.release();
		
		if (!listeners.isEmpty()) {
			listeners.fire(new IFire<ICpuListener>() {
				@Override
				public void fire(ICpuListener listener) {
					listener.ticked(CpuBase.this);
				}
			});
		}
	}

	@Override
	public void read(IMemoryEntry entry, int addr) {
		cycleCounts.addLoad(entry.getLatency(addr));
	}
	@Override
	public void write(IMemoryEntry entry, int addr) {
		cycleCounts.addStore(entry.getLatency(addr));
	}

	public int getCurrentCycleCount() {
		return currentcycles.get() + cycleCounts.getTotal();
	}

	public int getCurrentTargetCycleCount() {
		return currenttargetcycles.get();
	}

	public long getTotalCycleCount() {
		return totalcurrentcycles;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.cpu.ICpu#getTotalCurrentCycleCount()
	 */
	@Override
	public synchronized long getTotalCurrentCycleCount() {
		return totalcurrentcycles + currentcycles.get() + cycleCounts.getTotal();
	}
	public int getTickCount() {
		return ticks;
	}

	public int getTargetCycleCount() {
		return targetcycles;
	}

	public void acknowledgeInterrupt(int level) {
		interrupts++;
	}
	public int getAndResetInterruptCount() {
		int n = interrupts;
		interrupts = 0;
		return n;
	}

	public void resetCycleCounts() {
		currentcycles.set(0);
		cycleCounts.getAndResetTotal();
		currenttargetcycles.set(0);
		synchronized (this) {
			totalcurrentcycles = totaltargetcycles = 0;
		}
		allocatedCycles.drainPermits();
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
	public void checkAndHandleInterrupts() {
    	if (doCheckInterrupts()) {
    		//interruptWaiting.release();
    		handleInterrupts();
    	}
    }
	
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.ICpu#applyCycles()
	 */
	@Override
	public void applyCycles() {
		currentcycles.addAndGet(cycleCounts.getAndResetTotal());
	}
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.ICpu#applyCycles(int)
	 */
	@Override
	public void applyCycles(int cycles) {
		currentcycles.addAndGet(cycles);
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.ICpu#addListener(v9t9.common.cpu.ICpuListener)
	 */
	@Override
	public void addListener(ICpuListener listener) {
		listeners.add(listener);		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.ICpu#removeListener(v9t9.common.cpu.ICpuListener)
	 */
	@Override
	public void removeListener(ICpuListener listener) {
		listeners.remove(listener);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.ICpu#currentInstruction()
	 */
	@Override
	public RawInstruction getCurrentInstruction() {
		ICpu cpu = machine.getCpu();
		cpu.getCycleCounts().saveState();
		
		RawInstruction inst = machine.getInstructionFactory().decodeInstruction(
				cpu.getState().getPC(), machine.getConsole());
		
		cpu.getCycleCounts().restoreState();

		return inst;
	}
	
	private int debugCount;
	

	public void addDebugCount(int i) {
    	int oldCount = debugCount; 
    	debugCount += i;
    	if ((oldCount == 0) != (debugCount == 0))
    		settingDumpFullInstructions().setBoolean(i > 0);
	}

	
}