/*
  IExecutor.java

  (c) 2011-2013 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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
	
	void interpretOneInstruction();

	/** 
	 * Run an unbounded amount of code.  Some external factor
	 * tells the execution unit when to stop.  The interpret/compile
	 * setting is sticky until execution is interrupted.
	 * @return TODO
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

}