/*
  IBaseMachine.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.machine;

import java.util.Timer;

import ejs.base.settings.ISettingSection;
import ejs.base.timer.FastTimer;


import v9t9.common.asm.IRawInstructionFactory;
import v9t9.common.client.IClient;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.ICpuMetrics;
import v9t9.common.events.NotifyEvent;
import v9t9.common.events.NotifyEvent.Level;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryModel;
import v9t9.common.settings.SettingSchema;

/**
 * @author ejs
 *
 */
public interface IBaseMachine {

	SettingSchema settingPauseMachine = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"PauseMachine", Boolean.FALSE);
	SettingSchema settingThrottleInterrupts = new SettingSchema(
			ISettingsHandler.MACHINE,
			"ThrottleVDPInterrupts", Boolean.FALSE);
	ISettingsHandler getSettings();

	IClient getClient();

	void setClient(IClient client);
	
	void notifyEvent(Level level, String string);

	void notifyEvent(NotifyEvent event);

	IMemory getMemory();

	boolean isAlive();

	void start();

	/**
	 * Forcibly stop the machine and throw TerminatedException
	 */
	void stop();

	ICpu getCpu();

	void setCpu(ICpu cpu);

	/** Get the primary memory */
	IMemoryDomain getConsole();

	IMemoryModel getMemoryModel();

	void saveState(ISettingSection settings);

	void loadState(ISettingSection section);

	void reset();
	
	/** Get the number of times per second the machine executes a chunk of work. */
	int getTicksPerSec();

	boolean isExecuting();
	
	boolean isPaused();
	/** Change pause state and return previous setting */
	boolean setPaused(boolean paused);

	void asyncExec(Runnable runnable);

	ICpuMetrics getCpuMetrics();

	IRawInstructionFactory getInstructionFactory();

	Timer getMachineTimer();

	FastTimer getFastMachineTimer();
	
	void interrupt();

}