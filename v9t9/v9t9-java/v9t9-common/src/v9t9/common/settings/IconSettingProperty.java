/**
 * 
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
