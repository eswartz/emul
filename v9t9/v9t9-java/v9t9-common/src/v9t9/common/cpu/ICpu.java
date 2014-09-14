/*
  ICpu.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.cpu;


import java.util.concurrent.Semaphore;

import ejs.base.properties.IPersistable;
import ejs.base.properties.IProperty;
import v9t9.common.asm.IDecompilePhase;
import v9t9.common.asm.IInstructionFactory;
import v9t9.common.asm.IRawInstructionFactory;
import v9t9.common.asm.RawInstruction;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.machine.IBaseMachine;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.settings.SettingSchema;

public interface ICpu extends IPersistable {
	SettingSchema settingDumpInstructions = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"DumpInstructions", Boolean.FALSE);
	SettingSchema settingDumpFullInstructions = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"DumpFullInstructions", Boolean.FALSE);

	SettingSchema settingRealTime = new SettingSchema(
			ISettingsHandler.MACHINE,
			"RealTime", Boolean.TRUE);
	SettingSchema settingCyclesPerSecond = new SettingSchema(
			ISettingsHandler.MACHINE,
			"CyclesPerSecond", new Integer(0));

	SettingSchema settingDebugging = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"Debugging", Boolean.FALSE);

	SettingSchema settingDetectCrash = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"DetectCrash", Boolean.FALSE);

	SettingSchema settingRunForCount = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"RunForCount", new Integer(0));
	SettingSchema settingTestSuccessSymbol = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"TestSuccessSymbol", "");
	SettingSchema settingTestFailureSymbol = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"TestFailureSymbol", "");

	
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


	IMemoryDomain getConsole();
	/**
	 * Poll the interrupt controller to see if any interrupts are pending.
	 * @throws AbortedException if interrupt waiting
	 */
	@InvokedByCompiledCode
	void checkInterrupts();

	/**
	 * Called by toplevel in response to the AbortedException from above
	 */
	void handleInterrupts();

	void checkAndHandleInterrupts();

	CycleCounts getCycleCounts();
	
	/**
	 * Flush CycleCounts into internal timing
	 */
	void applyCycles();

	void applyCycles(int cycles);
	
	void tick();

	int getCurrentCycleCount();

	int getCurrentTargetCycleCount();

	long getTotalCycleCount();
	
	long getTotalCurrentCycleCount();

	/** Get the tick count, in ms */
	int getTickCount();

	/** Get target # cycles to be executed per tick */
	int getTargetCycleCount();

	/**
	 * @return
	 */
	Semaphore getAllocatedCycles();
	
	/** 
	 * Acknowledge the interrupt at the given level, and tell whether
	 * the interrupt is now acknowledged
	 * @param level
	 */
	void acknowledgeInterrupt(int level);
	int getAndResetInterruptCount();

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
	//void irq();

	IInstructionFactory getInstructionFactory();
	IRawInstructionFactory getRawInstructionFactory();
	

	IExecutor createExecutor();
	/**
	 * @return
	 */
	IDecompilePhase createDecompiler();

	void addListener(ICpuListener listener);
	void removeListener(ICpuListener listener);
	
	IInstructionEffectLabelProvider createInstructionEffectLabelProvider();
	/**
	 * Decode the instruction at the current PC.  Do not affect CPU state otherwise. 
	 * @return new instruction
	 */
	RawInstruction getCurrentInstruction();
	/**
	 * @param i
	 */
	void addDebugCount(int i);

}