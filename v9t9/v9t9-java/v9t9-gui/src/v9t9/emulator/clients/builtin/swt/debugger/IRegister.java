/**
 * 
 */
package v9t9.emulator.clients.builtin.swt.debugger;

/**
 * @author ejs
 *
 */
public interface IRegister {

	String getName();
	String getTooltip();
	
	int getValue();
	void setValue(int value);
	
}
