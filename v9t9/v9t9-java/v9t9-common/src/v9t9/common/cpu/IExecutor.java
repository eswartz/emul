/*
  IExecutor.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.cpu;

import ejs.base.properties.IProperty;
import ejs.base.utils.ListenerList;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.compiler.ICompilerStrategy;
import v9t9.common.settings.SettingSchema;

/**
 * @author ejs
 *
 */
public interface IExecutor {

	SettingSchema settingCompile = new SettingSchema(
			ISettingsHandler.MACHINE,
			"Compile", Boolean.FALSE);
	SettingSchema settingSingleStep = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"SingleStep", Boolean.FALSE);

	IProperty settingCompile();
	IProperty settingSingleStep();

	IInterpreter getInterpreter();
	void interpretOneInstruction();

	/** 
	 * Run an unbounded amount of code.  Some external factor
	 * tells the execution unit when to stop.  The interpret/compile
	 * setting is sticky until execution is interrupted.
	 * @return number of cycles executed
	 * @throws AbortedException when interrupt or other machine event stops execution
	 */
	int execute();

	void recordMetrics();

	/**
	 * @return
	 */
	ListenerList<IInstructionListener> getInstructionListeners();

	void addInstructionListener(IInstructionListener listener);

	void removeInstructionListener(IInstructionListener listener);

	/**
	 * @return
	 */
	ICompilerStrategy getCompilerStrategy();

	/**
	 * @param cpu the cpu to set
	 */
	void setCpu(ICpu cpu);

	/**
	 * @return the cpu
	 */
	ICpu getCpu();

	/**
	 * Record a context switch
	 */
	void recordSwitch();

	/**
	 * @param nInstructions
	 * @param nCycles
	 */
	void recordCompileRun(int nInstructions, int nCycles);

	/**
	 * 
	 */
	void recordCompilation();

	/**
	 * @param count 
	 * @return
	 */
	boolean breakAfterExecution(int count);

	/**
	 * @param i
	 */
	void debugCount(int i);

	/**
	 * 
	 */
	void vdpInterrupt();

	/**
	 * 
	 */
	void resetVdpInterrupts();

	/**
	 * 
	 */
	void interruptExecution();
	/**
	 * @return
	 */
	BreakpointManager getBreakpoints();
	/**
	 * @param cpuMetrics
	 */
	void setMetrics(ICpuMetrics cpuMetrics);
	/**
	 * @param b
	 * @return
	 */
	boolean setExecuting(boolean b);
	/**
	 * @return
	 */
	boolean isExecuting();
	/**
	 * 
	 */
	void start();
	/**
	 * 
	 */
	void stop();
	/**
	 * @param runnable
	 */
	void asyncExec(Runnable runnable);
	/**
	 * 
	 */
	void tick();
	/**
	 * 
	 */
	void reset();

}