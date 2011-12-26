package v9t9.gui.client.swt.shells.debugger;

/**
 * @author ejs
 *
 */
public interface IRegisterProvider {
	String getLabel();
	int getNumDigits();
	IRegister[] getRegisters(int start, int count);
	int getRegisterCount();
}
