package v9t9.emulator.runtime.cpu;

import org.ejs.coffee.core.properties.IPersistable;
import org.ejs.coffee.core.properties.SettingProperty;

import v9t9.emulator.common.Machine;
import v9t9.emulator.hardware.CruAccess;
import v9t9.engine.cpu.Status;
import v9t9.engine.memory.MemoryDomain;

public interface Cpu extends IPersistable {

	short getPC();

	void setPC(short pc);

	short getST();

	void setST(short st);

	void resetInterruptRequest();

	void setCruAccess(CruAccess access);
	CruAccess getCruAccess();
	
	/**
	 * Called by the interrupt controller to indicate an interrupt is available.
	 * @param level
	 */
	void setInterruptRequest(byte level);

	static public final String sRealTime = "RealTime";
	static public final SettingProperty settingRealTime = new SettingProperty(
			sRealTime, new Boolean(false));
	static public final String sCyclesPerSecond = "CyclesPerSecond";
	static public final SettingProperty settingCyclesPerSecond = new SettingProperty(
			sCyclesPerSecond, new Integer(0));

	/**
	 * @return
	 */
	Machine getMachine();

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

	int getRegister(int reg);

	void setConsole(MemoryDomain console);

	MemoryDomain getConsole();

	void addCycles(int cycles);

	void tick();

	boolean isThrottled();

	int getCurrentCycleCount();

	int getCurrentTargetCycleCount();

	long getTotalCycleCount();

	long getTotalCurrentCycleCount();

	/** Get the tick count, in ms */
	int getTickCount();

	boolean isAllowInts();

	void setAllowInts(boolean allowInts);

	/** Get target # cycles to be executed per tick */
	int getTargetCycleCount();

	int getAndResetInterruptCount();

	void addAllowedCycles(int i);

	void resetCycleCounts();

	 /**
     * Called when hardware triggers a CPU-specific pin.
     */
    void setPin(int mask);

	Status createStatus();

	Status getStatus();

	String getCurrentStateString();

	void reset();

	/** Tell whether the code compiled at this address is 
	 * likely to need compiler debugging */
	boolean shouldDebugCompiledCode(short pc);
}