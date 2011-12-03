/**
 * 
 */
package v9t9.gui.client.swt.debugger;

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
