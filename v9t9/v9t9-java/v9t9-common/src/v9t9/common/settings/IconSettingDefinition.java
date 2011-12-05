/**
 * 
 */
package v9t9.common.settings;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;

import v9t9.base.settings.SettingProperty;

/**
 * @author ejs
 *
 */
public class IconSettingDefinition extends SettingDefinition {

	private final URL iconPath;

	/**
	 * @param name
	 * @param storage
	 */
	public IconSettingDefinition(String context, String name, String label, String description, Object storage, URL iconPath) {
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
