/*
  ModuleContentHandler.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.handlers;

import java.util.Collections;

import org.eclipse.jface.resource.ImageDescriptor;

import v9t9.common.client.IEmulatorContentHandler;
import v9t9.common.events.NotifyException;
import v9t9.common.machine.IMachine;
import v9t9.common.modules.IModule;
import v9t9.common.modules.ModuleContentSource;

/**
 * @author ejs
 *
 */
public class ModuleContentHandler implements IEmulatorContentHandler {

	private IMachine machine;
	private IModule module;

	public ModuleContentHandler(ModuleContentSource source) {
		this.machine = source.getMachine();
		this.module = source.getContent();
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.IEmulatorContentHandler#getLabel()
	 */
	@Override
	public String getLabel() {
		return "Load module '"+module.getName() +"'";
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.IEmulatorContentHandler#getImage()
	 */
	@Override
	public ImageDescriptor getImage() {
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.IEmulatorContentHandler#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Switch to this module and restart the emulated machine.";
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.IEmulatorContentHandler#handleContent()
	 */
	@Override
	public void handleContent() throws NotifyException {
		machine.getModuleManager().addModules(Collections.singletonList(module));
		
		machine.reset();
		machine.getModuleManager().unloadAllModules();
		machine.getModuleManager().loadModule(module);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.IEmulatorContentHandler#requireConfirmation()
	 */
	@Override
	public boolean requireConfirmation() {
		return true;
	}

}
