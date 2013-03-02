/*
  EmulatorSettings.java

  (c) 2009-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.server.settings;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.settings.StaticStoredSettings;


/**
 * This maintains settings global to the user (and saved automagically in
 * a config file) as opposed to state-specific settings.
 * <p>
 * @author ejs
 *
 */
public class EmulatorSettings extends StaticStoredSettings {
	//public static final EmulatorSettings INSTANCE = new EmulatorSettings();
	/* (non-Javadoc)
	 * @see v9t9.emulator.common.BaseStoredSettings#getConfigFileName()
	 */
	
	protected EmulatorSettings() {
		super(ISettingsHandler.USER, "config");
	}
}
