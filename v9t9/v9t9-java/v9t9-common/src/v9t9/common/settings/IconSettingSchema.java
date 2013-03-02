/*
  IconSettingSchema.java

  (c) 2010-2011 Edward Swartz

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
public class IconSettingSchema extends SettingSchema {

	private final URL iconPath;

	/**
	 * @param name
	 * @param storage
	 */
	public IconSettingSchema(String context, String name, String label, String description, Object storage, URL iconPath) {
		super(context, name, label, description, storage);
		this.iconPath = iconPath;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.ISettingDecorator#getIcon()
	 */
	public ImageDescriptor getIcon() {
		return ImageDescriptor.createFromURL(iconPath);
	}

}
