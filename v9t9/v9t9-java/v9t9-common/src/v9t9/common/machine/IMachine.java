/*
  IMachine.java

  (c) 2011-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.machine;

import v9t9.common.cassette.ICassetteChip;
import v9t9.common.client.IEmulatorContentSourceProvider;
import v9t9.common.client.IKeyboardHandler;
import v9t9.common.cpu.IExecutor;
import v9t9.common.demos.IDemoHandler;
import v9t9.common.demos.IDemoManager;
import v9t9.common.dsr.IPrinterImageHandler;
import v9t9.common.events.IEventNotifier;
import v9t9.common.files.IEmulatedFileHandler;
import v9t9.common.files.IPathFileLocator;
import v9t9.common.hardware.ICruChip;
import v9t9.common.hardware.IGplChip;
import v9t9.common.hardware.ISoundChip;
import v9t9.common.hardware.ISpeechChip;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.keyboard.IKeyboardMapping;
import v9t9.common.keyboard.IKeyboardModeListener;
import v9t9.common.keyboard.IKeyboardState;
import v9t9.common.modules.IModuleDetector;
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
	ICassetteChip getCassette();

	/**
	 * @return the moduleManager
	 */
	IModuleManager getModuleManager();

	IVdpChip getVdp();

	IGplChip getGpl();

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

	IEmulatedFileHandler getEmulatedFileHandler();
	
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

	/**
	 * Get the module detector
	 */
	IModuleDetector getModuleDetector();
	/**
	 * Create a new module detector
	 */
	IModuleDetector createModuleDetector();

	/**
	 * Reload machine (e.g. re-read ROMs and re-set module)
	 */
	void reload();
	
	/**
	 * Get the handlers for emulator content
	 */
	IEmulatorContentSourceProvider[] getEmulatorContentProviders();
	/**
	 * Add a handler for emulator content
	 */
	void addEmulatorContentProvider(IEmulatorContentSourceProvider provider);

	IPrinterImageHandler[] getPrinterImageHandlers();

}