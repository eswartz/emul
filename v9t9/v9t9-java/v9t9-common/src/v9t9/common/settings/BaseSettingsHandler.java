/**
 * 
 */
package v9t9.common.settings;

import v9t9.base.properties.IProperty;
import v9t9.common.client.ISettingsHandler;

/**
 * @author ejs
 *
 */
public class BaseSettingsHandler implements ISettingsHandler {

	private IStoredSettings transientSettings = new BaseStoredSettings(ISettingsHandler.TRANSIENT) {
	
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

}