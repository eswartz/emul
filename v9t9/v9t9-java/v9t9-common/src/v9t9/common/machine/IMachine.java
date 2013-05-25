/*
  IMachine.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.machine;

import java.io.File;
import java.net.URI;
import java.util.Collection;

import v9t9.common.client.IEmulatorContentSourceProvider;
import v9t9.common.client.IKeyboardHandler;
import v9t9.common.cpu.IExecutor;
import v9t9.common.demos.IDemoHandler;
import v9t9.common.demos.IDemoManager;
import v9t9.common.events.IEventNotifier;
import v9t9.common.files.IEmulatedFileHandler;
import v9t9.common.files.IPathFileLocator;
import v9t9.common.hardware.ICassetteChip;
import v9t9.common.hardware.ICruChip;
import v9t9.common.hardware.ISoundChip;
import v9t9.common.hardware.ISpeechChip;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.keyboard.IKeyboardMapping;
import v9t9.common.keyboard.IKeyboardModeListener;
import v9t9.common.keyboard.IKeyboardState;
import v9t9.common.modules.IModule;
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
	 * Scan the directory or a file for modules
	 * @param databaseURI uri of module database
	 * @param base directory or file
	 * @return array of entries
	 */
	Collection<IModule> scanModules(URI databaseURI, File base);

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
}