package v9t9.emulator.runtime.cpu;

import org.ejs.coffee.core.properties.IPersistable;
import org.ejs.coffee.core.properties.SettingProperty;

import v9t9.emulator.common.IBaseMachine;
import v9t9.emulator.hardware.CruAccess;

public interface Cpu extends IPersistable, CpuState {
	static public final String sDumpInstructions = "DumpInstructions";
	static public final SettingProperty settingDumpInstructions = new SettingProperty(sDumpInstructions, new Boolean(false));
	static public final String sDumpFullInstructions = "DumpFullInstructions";
	static public final SettingProperty settingDumpFullInstructions = new SettingProperty(sDumpFullInstructions, new Boolean(false));

	void resetInterruptRequest();

	void setCruAccess(CruAccess access);
	CruAccess getCruAccess();
	
	/**
	 * Called by the interrupt controller to indicate an interrupt is available.
	 * @param level
	 */
	void setInterruptRequest(byte level);

	static public final SettingProperty settingRealTime = new SettingProperty(
			"RealTime", new Boolean(false));
	static public final SettingProperty settingCyclesPerSecond = new SettingProperty(
			"CyclesPerSecond", new Integer(0));

	/**
	 * @return
	 */
	IBaseMachine getMachine();

	/**
	 * Poll the interrupt controller to see if any interrupts are pending.
	 * @throws AbortedException if interrupt waiting
	 */
	void checkInterrupts();

	/**
	 * Called by toplevel in response to the AbortedException from above
	 */
	void handleInterrupts();

	void checkAndHandleInterrupts();

	void addCycles(int cycles);

	void tick();

	boolean isThrottled();

	int getCurrentCycleCount();

	int getCurrentTargetCycleCount();

	long getTotalCycleCount();

	long getTotalCurrentCycleCount();

	/** Get the tick count, in ms */
	int getTickCount();

	/** Get target # cycles to be executed per tick */
	int getTargetCycleCount();

	void acknowledgeInterrupt();
	int getAndResetInterruptCount();

	void addAllowedCycles(int i);

	void resetCycleCounts();

	 /**
     * Called when hardware triggers a CPU-specific pin.
     */
    void setPin(int mask);

	String getCurrentStateString();

	void reset();

	/** Tell whether the code compiled at this address is 
	 * likely to need compiler debugging */
	boolean shouldDebugCompiledCode(short pc);

	/**
	 * @return
	 */
	CpuState getState();

	int getBaseCyclesPerSec();

	/**
	 * @param b
	 */
	void setIdle(boolean b);

	/**
	 * @return
	 */
	boolean isIdle();

	/**
	 * Set NMI interrupt line
	 */
	void nmi();

	/**
	 * Set interrupt request interrupt line
	 */
	void irq();
}