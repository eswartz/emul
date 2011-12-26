/**
 * 
 */
package v9t9.gui.client.swt.shells.debugger;

import v9t9.common.machine.IRegisterAccess;

/**
 * @author ejs
 *
 */
public interface IRegister {

	IRegisterAccess.RegisterInfo getInfo();
	String getTooltip();
	int getValue();
	void setValue(int value);
	
}
