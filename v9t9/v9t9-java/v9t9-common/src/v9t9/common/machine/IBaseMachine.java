/*
  IBaseMachine.java

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
package v9t9.common.machine;

import java.util.Timer;

import ejs.base.settings.ISettingSection;
import ejs.base.timer.FastTimer;


import v9t9.common.asm.IRawInstructionFactory;
import v9t9.common.client.IClient;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.ICpuMetrics;
import v9t9.common.events.IEventNotifier;
import v9t9.common.events.NotifyEvent;
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
	SettingSchema settingModuleList = new SettingSchema(
			ISettingsHandler.MACHINE,
			"ModuleListFile", "");

	ISettingsHandler getSettings();

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