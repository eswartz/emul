/**
 * 
 */
package v9t9.emulator.common;

import java.io.IOException;
import java.util.Timer;

import org.ejs.coffee.core.properties.SettingProperty;
import org.ejs.coffee.core.settings.ISettingSection;

import v9t9.emulator.runtime.cpu.Cpu;
import v9t9.emulator.runtime.cpu.CpuMetrics;
import v9t9.engine.cpu.IRawInstructionFactory;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryModel;

/**
 * @author ejs
 *
 */
public interface IBaseMachine {

	static public final SettingProperty settingPauseMachine = new SettingProperty(
			"PauseMachine", new Boolean(false));
	static public final SettingProperty settingThrottleInterrupts = new SettingProperty(
			"ThrottleVDPInterrupts", new Boolean(false));
	static public final SettingProperty settingModuleList = new SettingProperty(
			"ModuleListFile", new String("modules.xml"));

	void notifyEvent(IEventNotifier.Level level, String string);

	void notifyEvent(NotifyEvent event);

	Memory getMemory();

	boolean isAlive();

	void start();

	/**
	 * Forcibly stop the machine and throw TerminatedException
	 */
	void stop();

	void setNotRunning();

	Cpu getCpu();

	void setCpu(Cpu cpu);

	/** Get the primary memory */
	MemoryDomain getConsole();

	MemoryModel getMemoryModel();

	void saveState(ISettingSection settings);

	void loadState(ISettingSection section) throws IOException;

	int getCpuTicksPerSec();

	Object getExecutionLock();

	/**
	 * @return
	 */
	boolean isExecuting();

	void asyncExec(Runnable runnable);

	CpuMetrics getCpuMetrics();

	/**
	 * @return
	 */
	IRawInstructionFactory getInstructionFactory();

	Timer getMachineTimer();

	/**
	 * 
	 */
	void interrupt();

}