/**
 * 
 */
package v9t9.common.machine;

import java.io.IOException;
import java.util.Timer;


import v9t9.base.settings.ISettingSection;
import v9t9.base.settings.SettingProperty;
import v9t9.common.asm.IRawInstructionFactory;
import v9t9.common.client.IClient;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.ICpuMetrics;
import v9t9.common.events.IEventNotifier;
import v9t9.common.events.NotifyEvent;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryModel;

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


	IClient getClient();

	void setClient(IClient client);
	
	void notifyEvent(IEventNotifier.Level level, String string);

	void notifyEvent(NotifyEvent event);

	IMemory getMemory();

	boolean isAlive();

	void start();

	/**
	 * Forcibly stop the machine and throw TerminatedException
	 */
	void stop();

	void setNotRunning();

	ICpu getCpu();

	void setCpu(ICpu cpu);

	/** Get the primary memory */
	IMemoryDomain getConsole();

	IMemoryModel getMemoryModel();

	void saveState(ISettingSection settings);

	void loadState(ISettingSection section) throws IOException;

	void reset();
	
	int getCpuTicksPerSec();

	Object getExecutionLock();

	/**
	 * @return
	 */
	boolean isExecuting();

	void asyncExec(Runnable runnable);

	ICpuMetrics getCpuMetrics();

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