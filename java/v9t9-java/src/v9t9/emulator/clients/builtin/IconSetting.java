/**
 * 
 */
package v9t9.emulator.clients.builtin;

import org.eclipse.jface.resource.ImageDescriptor;
import org.ejs.coffee.core.properties.SettingProperty;

/**
 * @author ejs
 *
 */
public class IconSetting extends SettingProperty implements ISettingDecorator {

	private final String iconPath;

	/**
	 * @param name
	 * @param storage
	 */
	public IconSetting(String name, String label, String description, Object storage, String iconPath) {
		super(name, label, description, storage);
		this.iconPath = iconPath;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.ISettingDecorator#getIcon()
	 */
	public ImageDescriptor getIcon() {
		return ImageDescriptor.createFromFile(null, iconPath);
	}

}
