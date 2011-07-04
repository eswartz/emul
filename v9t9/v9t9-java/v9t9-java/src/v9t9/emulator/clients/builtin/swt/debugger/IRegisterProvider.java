package v9t9.emulator.clients.builtin.swt.debugger;

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
