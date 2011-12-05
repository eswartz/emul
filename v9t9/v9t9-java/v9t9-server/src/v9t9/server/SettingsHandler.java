package v9t9.server;

import v9t9.base.settings.SettingProperty;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.settings.IStoredSettings;
import v9t9.common.settings.SettingDefinition;
import v9t9.engine.settings.EmulatorSettings;
import v9t9.engine.settings.WorkspaceSettings;

/**
 * @author ejs
 *
 */
public final class SettingsHandler implements ISettingsHandler {
	@Override
	public IStoredSettings getWorkspaceSettings() {
		return WorkspaceSettings.CURRENT;
	}

	@Override
	public IStoredSettings getInstanceSettings() {
		return EmulatorSettings.INSTANCE;
	}

	@Override
	public SettingProperty get(SettingDefinition def) {
		return getSettings(def.getContext()).findOrCreate(def);
	}

	@Override
	public <T extends SettingProperty> T get(String context,
			T defaultProperty) {
		return getSettings(context).findOrCreate(defaultProperty);
	}

	/**
	 * @param def
	 * @return
	 */
	protected IStoredSettings getSettings(String context) {
		IStoredSettings settings;
		if (WORKSPACE.equals(context))
			settings = getWorkspaceSettings();
		else if (INSTANCE.equals(context))
			settings = getInstanceSettings();
		else
			throw new IllegalArgumentException();
		return settings;
	}
}