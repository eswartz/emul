/**
 * 
 */
package v9t9.emulator.clients.builtin;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.ejs.coffee.core.utils.Setting;

/**
 * @author ejs
 *
 */
public class IconSetting extends Setting implements ISettingDecorator {

	private final String iconPath;

	/**
	 * @param name
	 * @param storage
	 */
	public IconSetting(String name, Object storage, String iconPath) {
		super(name, storage);
		this.iconPath = iconPath;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.ISettingDecorator#getIcon()
	 */
	public ImageDescriptor getIcon() {
		return ImageDescriptor.createFromFile(null, iconPath);
	}

}
