/**
 * 
 */
package v9t9.gui.client.swt.shells.disk;

import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public interface IDeviceSelectorDialog {
	void warnResetNeeded();
	IMachine getMachine();
}
