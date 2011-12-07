/**
 * 
 */
package v9t9.common.dsr;

import java.util.List;


import v9t9.base.properties.IPersistable;

/**
 * Java code that handles the work of a DSR through the Idsr instruction.
 * @author ejs
 *
 */
public interface IDsrHandler extends IPersistable, IDeviceSettings {

	String GROUP_DSR_SELECTION = "Device Selection";
	String GROUP_DISK_CONFIGURATION = "Disk Configuration";

	void dispose();

	String getName();
	
	List<IDeviceIndicatorProvider> getDeviceIndicatorProviders();
}
