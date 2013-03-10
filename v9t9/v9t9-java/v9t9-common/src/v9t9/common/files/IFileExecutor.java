/**
 * 
 */
package v9t9.common.files;

import v9t9.common.events.NotifyException;
import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public interface IFileExecutor {

	/**
	 * @return
	 */
	String getLabel();

	String getDescription();
	
	void run(IMachine machine) throws NotifyException;
}
