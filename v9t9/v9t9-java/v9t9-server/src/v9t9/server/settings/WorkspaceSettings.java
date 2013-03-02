/*
  WorkspaceSettings.java

  (c) 2010-2013 Edward Swartz

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

import java.io.File;
import java.io.IOException;

import ejs.base.properties.IProperty;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.settings.BaseStoredSettings;
import v9t9.common.settings.IStoredSettings;
import v9t9.common.settings.SettingSchemaProperty;


/**
 * This maintains settings for a given machine (and saved automagically in
 * a config file) as opposed to global settings or state-specific settings.
 * <p>
 * @author ejs
 *
 */
public class WorkspaceSettings extends BaseStoredSettings {

	public static IProperty currentWorkspace = new SettingSchemaProperty(
			//ISettingsHandler.GLOBAL,
			"CurrentWorkspace", "workspace");

	//public static WorkspaceSettings CURRENT = new WorkspaceSettings(currentWorkspace.getString());
	
	private String workspaceName;

	private String workspacePath;

	public WorkspaceSettings(String workspaceName) {
		super(ISettingsHandler.MACHINE);
		setWorkspaceName(workspaceName);
		//EmulatorSettings.INSTANCE.register(currentWorkspace);
	}
	
	public void setWorkspaceName(String workspaceName) {
		File file = new File(workspaceName);
		if (file.isAbsolute()) {
			this.workspaceName = file.getName();
			this.workspacePath = file.getParent();
		} else {
			this.workspaceName = workspaceName;
			this.workspacePath = super.getConfigDirectory();
		}
		currentWorkspace.setString(getConfigFileName());
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.common.BaseStoredSettings#getConfigDirectory()
	 */
	@Override
	public String getConfigDirectory() {
		return workspacePath;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.common.BaseStoredSettings#getConfigFileName()
	 */
	@Override
	public String getConfigFileName() {
		return workspaceName;
	}

	/**
	 * @param file
	 * @throws IOException 
	 */
	public static void loadFrom(IStoredSettings current, String file) throws IOException {
		File destFile = new File(file);
		if (!destFile.isAbsolute())
			destFile = new File(current.getConfigDirectory(), file);
		if (current.isDirty() && !destFile.equals(new File(current.getConfigFilePath())))
			current.save();
		
		/*
		List<SettingProperty> props = current.trackedSettings;
		current = new WorkspaceSettings(file);
		current.trackedSettings.addAll(props);
		*/
		if (current instanceof WorkspaceSettings)
			((WorkspaceSettings) current).setWorkspaceName(file);
		current.load();
	}

}
