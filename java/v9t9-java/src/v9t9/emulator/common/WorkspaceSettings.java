/**
 * 
 */
package v9t9.emulator.common;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.ejs.coffee.core.properties.SettingProperty;


/**
 * This maintains settings for a given workspace (and saved automagically in
 * a config file) as opposed to global settings or state-specific settings.
 * <p>
 * @author ejs
 *
 */
public class WorkspaceSettings extends BaseStoredSettings {

	public static SettingProperty currentWorkspace = new SettingProperty("CurrentWorkspace", "workspace");

	public static WorkspaceSettings CURRENT = new WorkspaceSettings(currentWorkspace.getString());
	
	private String workspaceName;

	private String workspacePath;

	public WorkspaceSettings(String workspaceName) {
		super();
		File file = new File(workspaceName);
		if (file.isAbsolute()) {
			this.workspaceName = file.getName();
			this.workspacePath = file.getParent();
		} else {
			this.workspaceName = workspaceName;
			this.workspacePath = super.getConfigDirectory();
		}
		currentWorkspace.setString(getConfigFileName());
		EmulatorSettings.INSTANCE.register(currentWorkspace);
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
	public static void loadFrom(String file) throws IOException {
		File destFile = new File(file);
		if (!destFile.isAbsolute())
			destFile = new File(CURRENT.getConfigDirectory(), file);
		if (CURRENT.needsSave && !destFile.equals(new File(CURRENT.getConfigFilePath())))
			CURRENT.save();
		
		List<SettingProperty> props = CURRENT.trackedSettings;
		CURRENT = new WorkspaceSettings(file);
		CURRENT.trackedSettings.addAll(props);
		CURRENT.load();
	}
}
