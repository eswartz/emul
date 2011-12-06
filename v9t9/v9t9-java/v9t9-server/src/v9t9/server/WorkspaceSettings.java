/**
 * 
 */
package v9t9.server;

import java.io.File;
import java.io.IOException;

import v9t9.base.properties.IProperty;
import v9t9.base.settings.SettingProperty;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.settings.BaseStoredSettings;
import v9t9.common.settings.IStoredSettings;


/**
 * This maintains settings for a given workspace (and saved automagically in
 * a config file) as opposed to global settings or state-specific settings.
 * <p>
 * @author ejs
 *
 */
public class WorkspaceSettings extends BaseStoredSettings {

	public static IProperty currentWorkspace = new SettingProperty(
			//ISettingsHandler.GLOBAL,
			"CurrentWorkspace", "workspace");

	//public static WorkspaceSettings CURRENT = new WorkspaceSettings(currentWorkspace.getString());
	
	private String workspaceName;

	private String workspacePath;

	public WorkspaceSettings(String workspaceName) {
		super(ISettingsHandler.WORKSPACE);
		File file = new File(workspaceName);
		if (file.isAbsolute()) {
			this.workspaceName = file.getName();
			this.workspacePath = file.getParent();
		} else {
			this.workspaceName = workspaceName;
			this.workspacePath = super.getConfigDirectory();
		}
		currentWorkspace.setString(getConfigFileName());
		//EmulatorSettings.INSTANCE.register(currentWorkspace);
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
		current.load();
	}

}
