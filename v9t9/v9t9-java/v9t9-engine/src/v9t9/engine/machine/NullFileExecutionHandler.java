/**
 * 
 */
package v9t9.engine.machine;

import v9t9.common.files.Catalog;
import v9t9.common.files.IFileExecutionHandler;
import v9t9.common.files.IFileExecutor;
import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public class NullFileExecutionHandler implements IFileExecutionHandler {

	/* (non-Javadoc)
	 * @see v9t9.common.files.IFileExecutionHandler#analyze(v9t9.common.files.Catalog)
	 */
	@Override
	public IFileExecutor[] analyze(IMachine machine, int drive, Catalog catalog) {
		return new IFileExecutor[0];
	}

}
