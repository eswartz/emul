package v9t9.server.settings;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.settings.BaseSettingsHandler;

/**
 * @author ejs
 *
 */
public final class SettingsHandler extends BaseSettingsHandler implements ISettingsHandler {
	public SettingsHandler(String workspaceName) {
		super(new WorkspaceSettings(workspaceName), new EmulatorSettings());
	}
}