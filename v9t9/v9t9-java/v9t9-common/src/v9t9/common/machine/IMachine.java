/*
  IMachine.java

  (c) 2011-2012 Edward Swartz

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

import v9t9.common.client.IKeyboardHandler;
import v9t9.common.cpu.IExecutor;
import v9t9.common.demos.IDemoHandler;
import v9t9.common.demos.IDemoManager;
import v9t9.common.events.IEventNotifier;
import v9t9.common.files.IFileHandler;
import v9t9.common.files.IPathFileLocator;
import v9t9.common.hardware.ICruChip;
import v9t9.common.hardware.ISoundChip;
import v9t9.common.hardware.ISpeechChip;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.keyboard.IKeyboardMapping;
import v9t9.common.keyboard.IKeyboardModeListener;
import v9t9.common.keyboard.IKeyboardState;
import v9t9.common.modules.IModuleManager;

/**
 * @author ejs
 *
 */
public interface IMachine extends IBaseMachine {


	IExecutor getExecutor();

	void setExecutor(IExecutor executor);

	IKeyboardState getKeyboardState();

	ISoundChip getSound();
	ISpeechChip getSpeech();

	/**
	 * @return the moduleManager
	 */
	IModuleManager getModuleManager();

	IVdpChip getVdp();

	/**
	 * @return
	 */
	IMachineModel getModel();

	/** Called when keyboardState changes */
	void keyStateChanged();

	/**
	 * @return
	 */
	ICruChip getCru();
	void setCru(ICruChip cru);

	IFileHandler getFileHandler();
	
	IPathFileLocator getRomPathFileLocator();

	/**
	 * @return
	 */
	IEventNotifier getEventNotifier();

	IDemoHandler getDemoHandler();
	void setDemoHandler(IDemoHandler handler);

	IDemoManager getDemoManager();
	void setDemoManager(IDemoManager manager);

	IKeyboardMapping getKeyboardMapping();
	void setKeyboardMapping(IKeyboardMapping mapping);
	
	/** Get the identifier of the current keyboard mode 
	 * @see IKeyboardMapping#getMode(String) */
	String getKeyboardMode();
	
	void addKeyboardModeListener(IKeyboardModeListener listener);
	void removeKeyboardModeListener(IKeyboardModeListener listener);

	IKeyboardHandler getKeyboardHandler();
	void setKeyboardHandler(IKeyboardHandler keyboardHandler);
	
}