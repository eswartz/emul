/*
  IDeviceSettings.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.dsr;

import java.util.Collection;
import java.util.Map;

import ejs.base.properties.IProperty;


/**
 * @author ejs
 *
 */
public interface IDeviceSettings {

	/**
	 * Get editable settings
	 * @return map of group label to settings
	 */
	Map<String, Collection<IProperty>> getEditableSettingGroups();
}
