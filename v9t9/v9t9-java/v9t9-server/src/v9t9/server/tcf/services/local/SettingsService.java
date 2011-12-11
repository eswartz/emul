/**
 * 
 */
package v9t9.server.tcf.services.local;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tm.tcf.core.ErrorReport;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IErrorReport;

import v9t9.base.properties.IProperty;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.IStoredSettings;
import v9t9.common.settings.SettingSchema;
import v9t9.common.settings.Settings;
import v9t9.server.tcf.services.ISettingsService;

/**
 * @author ejs
 *
 */
public class SettingsService extends BaseServiceImpl {

	private ISettingsHandler settings;

	public SettingsService(IMachine machine, IChannel channel_) {
		super(machine, channel_, ISettingsService.NAME);
		
		settings = Settings.getSettings(machine);
		
		registerCommand(ISettingsService.COMMAND_QUERY_ALL, 0, 1);
		registerCommand(ISettingsService.COMMAND_SET, 2, 1);
		registerCommand(ISettingsService.COMMAND_GET, 1, 1);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.protocol.IService#getName()
	 */
	@Override
	public String getName() {
		return ISettingsService.NAME;
	}

	/* (non-Javadoc)
	 * @see v9t9.server.tcf.services.local.BaseServiceImpl#handleCommand(java.lang.String, java.lang.Object[])
	 */
	@Override
	protected Object[] handleCommand(String name, Object[] args)
			throws ErrorReport, Exception {
		if (ISettingsService.COMMAND_QUERY_ALL.equals(name)) {
			List<Map<String, Object>> propsList = new ArrayList<Map<String,Object>>();
			Map<IProperty, SettingSchema> all = settings.getAllSettings();
			for (Map.Entry<IProperty, SettingSchema> entry : all.entrySet()) {
				Map<String, Object> props = new HashMap<String, Object>();
				IProperty p = entry.getKey();
				props.put(ISettingsService.PROP_NAME, p.getName());
				if (p.getLabel() != null && !p.getName().equals(p.getLabel()))
					props.put(ISettingsService.PROP_LABEL, p.getLabel());
				if (p.getDescription() != null)
					props.put(ISettingsService.PROP_DESCRIPTION, p.getDescription());
				
				String type = null;
				Class<?> theClass = p.getType();
				SettingSchema schema = entry.getValue();
				if (schema != null && schema.getDefaultValue() != null)
					theClass = schema.getDefaultValue().getClass();
				
				if (theClass.equals(String.class)) {
					type = ISettingsService.TYPE_STRING;
				} else if (theClass.equals(Integer.class) || theClass.equals(Integer.TYPE)
						|| theClass.equals(Long.class) || theClass.equals(Long.TYPE)
						|| theClass.equals(Short.class) || theClass.equals(Short.TYPE)
						|| theClass.equals(Byte.class) || theClass.equals(Byte.TYPE)
						) {
					type = ISettingsService.TYPE_INT;
				} else if (theClass.equals(Boolean.class) || theClass.equals(Boolean.TYPE)) {
					type = ISettingsService.TYPE_BOOL;
				} else if (theClass.equals(List.class)) {
					type = ISettingsService.TYPE_LIST;
				}  
				if (type != null) {
					props.put(ISettingsService.PROP_TYPE, type);
				}
				
				if (schema != null) {
					props.put(ISettingsService.PROP_CONTEXT, schema.getContext());
					if (type != null) {
						props.put(ISettingsService.PROP_DEFAULT, schema.getDefaultValue());
					}
				}
				
				propsList.add(props);
			}
			return new Object[] { propsList };
		} else if (ISettingsService.COMMAND_GET.equals(name)) {
			String propName = args[0].toString();
			IProperty prop = null;
			IStoredSettings storage = settings.findSettingStorage(propName);
			prop = storage != null ? storage.find(propName) : null;
			if (storage == null || prop == null)
				throw new ErrorReport("unknown setting: " + propName, IErrorReport.TCF_ERROR_INV_CONTEXT);
			return new Object[] { prop.getValue() };
		} else if (ISettingsService.COMMAND_SET.equals(name)) {
			String propName = args[0].toString();
			IProperty prop = null;
			IStoredSettings storage = settings.findSettingStorage(propName);
			prop = storage != null ? storage.find(propName) : null;
			if (storage == null || prop == null)
				throw new ErrorReport("unknown setting: " + propName, IErrorReport.TCF_ERROR_INV_CONTEXT);
			Object oldValue = prop.getValue();
			prop.setValue(args[1]);
			return new Object[] { oldValue };
		}
		throw new UnsupportedOperationException();
	}

}
