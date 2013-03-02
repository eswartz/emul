/*
  EmulatorSettings.java

  (c) 2009-2013 Edward Swartz

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
