/*
  IconSettingProperty.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.settings;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;



/**
 * @author ejs
 *
 */
public class IconSettingProperty extends SettingSchemaProperty implements ISettingDecorator {

	private final URL iconPath;

	/**
	 * @param name
	 * @param storage
	 */
	public IconSettingProperty(SettingSchema schema, URL iconPath) {
		super(schema);
		this.iconPath = iconPath;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.ISettingDecorator#getIcon()
	 */
	public ImageDescriptor getIcon() {
		return ImageDescriptor.createFromURL(iconPath);
	}

}
