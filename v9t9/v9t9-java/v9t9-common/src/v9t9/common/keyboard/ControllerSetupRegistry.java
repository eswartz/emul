/*
  ControllerSetupRegistry.java

  (c) 2017 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.keyboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import v9t9.common.keyboard.ControllerConfig.ParseException;
import ejs.base.utils.TextUtils;

/**
 * A collection of controller configurations.
 *   
 * Any combination of controllers, identified by a string composed of
 * their names in sorted order, provides a unique controller -> joystick mapping/ 
 * @author ejs
 *
 */
public class ControllerSetupRegistry {

	private Map<String, ControllerSetup> configMap = new LinkedHashMap<String, ControllerSetup>();

	public List<String> toStringList() {
		List<String> strs = new ArrayList<String>();
		
		for (Entry<String, ControllerSetup> ent : configMap.entrySet()) {
			// in case the user name differs from the internal, store both
			strs.add(ent.getKey());
			strs.add(ent.getValue().toString());
		}
		
		return strs;
	}

	public void fromStringList(List<?> list) throws ParseException {
		String name = null;
		for (Object obj : list) {
			String line = obj.toString().trim();
			if (TextUtils.isEmpty(line) || line.startsWith("#"))
				continue;
			
			if (name == null) {
				name = line;
				continue;
			}
				
			ControllerSetup setup = new ControllerSetup();
			setup.fromString(line);
			
			configMap.put(name, setup);
			name = null;
		}
	}

	public void clear() {
		configMap.clear();
	}
	
	public ControllerSetup find(String controllerNames) {
		return configMap.get(controllerNames);
	}
	
	public void register(String controllerNames, ControllerSetup setup) {
		configMap.put(controllerNames, setup);
	}
}
