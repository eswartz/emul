/**
 * 
 */
package v9t9.common.settings;

import java.util.HashMap;
import java.util.Map;

import ejs.base.properties.IProperty;

import v9t9.common.client.ISettingsHandler;

/**
 * @author ejs
 *
 */
public class BaseSettingsHandler implements ISettingsHandler {

	private IStoredSettings transientSettings = new BaseStoredSettings(
			ISettingsHandler.TRANSIENT) {
	
			@Override
			public String getConfigFileName() {
				return null;
			}
		};
	protected final IStoredSettings workspaceSettings;
	protected final IStoredSettings instanceSettings;

	
	public BaseSettingsHandler(IStoredSettings workspaceSettings,
			IStoredSettings instanceSettings) {
		this.workspaceSettings = workspaceSettings;
		this.instanceSettings = instanceSettings;
		
		workspaceSettings.setOwner(this);
		instanceSettings.setOwner(this);
		transientSettings.setOwner(this);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.ISettingsHandler#getRegisteredSettings()
	 */
	@Override
	public Map<IProperty, SettingSchema> getAllSettings() {
		Map<IProperty, SettingSchema> ret = new HashMap<IProperty, SettingSchema>();
		ret.putAll(transientSettings.getAll());
		ret.putAll(instanceSettings.getAll());
		ret.putAll(workspaceSettings.getAll());
		return ret;
	}

	@Override
	public IStoredSettings getWorkspaceSettings() {
		return workspaceSettings;
	}

	@Override
	public IStoredSettings getInstanceSettings() {
		return instanceSettings;
	}

	@Override
	public IProperty get(SettingSchema schema) {
		return getSettings(schema.getContext()).findOrCreate(schema);
	}

	@Override
	public <T extends IProperty> T get(String context, T defaultProperty) {
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
		else if (TRANSIENT.equals(context))
			settings = transientSettings;
		else
			throw new IllegalArgumentException("unknown settings context: " + context);
		return settings;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.client.ISettingsHandler#findSettingStorage(java.lang.String)
	 */
	@Override
	public IStoredSettings findSettingStorage(String settingsName) {
		if (workspaceSettings.find(settingsName) != null)
			return workspaceSettings;
		if (instanceSettings.find(settingsName) != null)
			return instanceSettings;
		if (transientSettings.find(settingsName) != null)
			return transientSettings;
		return null;
	}

}