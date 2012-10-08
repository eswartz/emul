/**
 * 
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
			ISettingsHandler.WORKSPACE,
			"Compile", new Boolean(false));
	SettingSchema settingSingleStep = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"SingleStep", new Boolean(false));

	IProperty settingCompile();
	IProperty settingSingleStep();
	
	void interpretOneInstruction();

	/** 
	 * Run an unbounded amount of code.  Some external factor
	 * tells the execution unit when to stop.  The interpret/compile
	 * setting is sticky until execution is interrupted.
	 * @throws AbortedException when interrupt or other machine event stops execution
	 */
	void execute();

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

}