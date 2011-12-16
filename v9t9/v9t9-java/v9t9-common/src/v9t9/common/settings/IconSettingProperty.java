/**
 * 
 */
package v9t9.common.settings;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;

import ejs.base.settings.SettingProperty;


/**
 * @author ejs
 *
 */
public class IconSettingProperty extends SettingProperty implements ISettingDecorator {

	private final URL iconPath;

	/**
	 * @param name
	 * @param storage
	 */
	public IconSettingProperty(String name, String label, String description, Object storage, URL iconPath) {
		super(name, label, description, storage);
		this.iconPath = iconPath;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.ISettingDecorator#getIcon()
	 */
	public ImageDescriptor getIcon() {
		return ImageDescriptor.createFromURL(iconPath);
	}

}
