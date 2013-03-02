/*
  SettingsService.java

  (c) 2011 Edward Swartz

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
package v9t9.server.tcf.services.local;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tm.tcf.core.ErrorReport;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IErrorReport;

import ejs.base.properties.IProperty;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.IStoredSettings;
import v9t9.common.settings.SettingSchema;
import v9t9.common.settings.Settings;
import v9t9.server.tcf.services.ISettings;

/**
 * Implementation of the ISettings service.
 * @author ejs
 *
 */
public class SettingsService extends BaseServiceImpl {

	private ISettingsHandler settings;

	public SettingsService(IMachine machine, IChannel channel_) {
		super(machine, channel_, ISettings.NAME);
		
		settings = Settings.getSettings(machine);
		
		registerCommand(ISettings.COMMAND_QUERY_ALL, 0, 2);
		registerCommand(ISettings.COMMAND_SET, 2, 2);
		registerCommand(ISettings.COMMAND_GET, 1, 2);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.protocol.IService#getName()
	 */
	@Override
	public String getName() {
		return ISettings.NAME;
	}

	/* (non-Javadoc)
	 * @see v9t9.server.tcf.services.local.BaseServiceImpl#handleCommand(java.lang.String, java.lang.Object[])
	 */
	@Override
	protected Object[] handleCommand(String name, Object[] args)
			throws ErrorReport, Exception {
		if (ISettings.COMMAND_QUERY_ALL.equals(name)) {
			List<Map<String, Object>> propsList = new ArrayList<Map<String,Object>>();
			Map<IProperty, SettingSchema> all = settings.getAllSettings();
			for (Map.Entry<IProperty, SettingSchema> entry : all.entrySet()) {
				Map<String, Object> props = new HashMap<String, Object>();
				IProperty p = entry.getKey();
				props.put(ISettings.PROP_NAME, p.getName());
				if (p.getLabel() != null && !p.getName().equals(p.getLabel()))
					props.put(ISettings.PROP_LABEL, p.getLabel());
				if (p.getDescription() != null)
					props.put(ISettings.PROP_DESCRIPTION, p.getDescription());
				
				String type = null;
				Class<?> theClass = p.getType();
				SettingSchema schema = entry.getValue();
				if (schema != null && schema.getDefaultValue() != null)
					theClass = schema.getDefaultValue().getClass();
				
				if (theClass.equals(String.class)) {
					type = ISettings.TYPE_STRING;
				} else if (theClass.equals(Integer.class) || theClass.equals(Integer.TYPE)
						|| theClass.equals(Long.class) || theClass.equals(Long.TYPE)
						|| theClass.equals(Short.class) || theClass.equals(Short.TYPE)
						|| theClass.equals(Byte.class) || theClass.equals(Byte.TYPE)
						) {
					type = ISettings.TYPE_INT;
				} else if (theClass.equals(Boolean.class) || theClass.equals(Boolean.TYPE)) {
					type = ISettings.TYPE_BOOL;
				} else if (theClass.equals(List.class)) {
					type = ISettings.TYPE_LIST;
				}  
				if (type != null) {
					props.put(ISettings.PROP_TYPE, type);
				}
				
				if (schema != null) {
					props.put(ISettings.PROP_CONTEXT, schema.getContext());
					if (type != null) {
						props.put(ISettings.PROP_DEFAULT, schema.getDefaultValue());
					}
				}
				
				propsList.add(props);
			}
			return new Object[] { null, propsList };
		} else if (ISettings.COMMAND_GET.equals(name)) {
			String propName = args[0].toString();
			IProperty prop = null;
			IStoredSettings storage = settings.findSettingStorage(propName);
			prop = storage != null ? storage.find(propName) : null;
			if (storage == null || prop == null)
				throw new ErrorReport("unknown setting: " + propName, IErrorReport.TCF_ERROR_INV_CONTEXT);
			return new Object[] { null, prop.getValue() };
		} else if (ISettings.COMMAND_SET.equals(name)) {
			String propName = args[0].toString();
			IProperty prop = null;
			IStoredSettings storage = settings.findSettingStorage(propName);
			prop = storage != null ? storage.find(propName) : null;
			if (storage == null || prop == null)
				throw new ErrorReport("unknown setting: " + propName, IErrorReport.TCF_ERROR_INV_CONTEXT);
			Object oldValue = prop.getValue();
			prop.setValue(args[1]);
			return new Object[] { null, oldValue };
		}
		throw new UnsupportedOperationException();
	}

}
