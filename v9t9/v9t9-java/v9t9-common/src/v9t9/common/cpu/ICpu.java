package v9t9.common.cpu;


import v9t9.base.properties.IPersistable;
import v9t9.base.properties.IProperty;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.machine.IBaseMachine;
import v9t9.common.settings.SettingSchema;

public interface ICpu extends IPersistable, ICpuState {
	static public final SettingSchema settingDumpInstructions = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"DumpInstructions", new Boolean(false));
	static public final SettingSchema settingDumpFullInstructions = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"DumpFullInstructions", new Boolean(false));

	static public final SettingSchema settingRealTime = new SettingSchema(
			ISettingsHandler.WORKSPACE,
			"RealTime", new Boolean(false));
	static public final SettingSchema settingCyclesPerSecond = new SettingSchema(
			ISettingsHandler.WORKSPACE,
			"CyclesPerSecond", new Integer(0));

	IProperty settingRealTime();
	IProperty settingCyclesPerSecond();
	IProperty settingDumpInstructions();
	IProperty settingDumpFullInstructions();

	void resetInterruptRequest();
	
	/**
	 * Called by the interrupt controller to indicate an interrupt is available.
	 * @param level
	 */
	void setInterruptRequest(byte level);

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
	ICpuState getState();

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