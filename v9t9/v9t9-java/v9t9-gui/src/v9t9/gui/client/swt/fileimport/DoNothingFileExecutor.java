/**
 * 
 */
package v9t9.gui.client.swt.fileimport;

import v9t9.common.events.NotifyException;
import v9t9.common.files.IFileExecutor;
import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public class DoNothingFileExecutor implements IFileExecutor {

	/* (non-Javadoc)
	 * @see v9t9.common.files.IFileExecutor#getLabel()
	 */
	@Override
	public String getLabel() {
		return "Nothing (just load disk)";
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IFileExecutor#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Change the current disk mapping to point to this image";
	}

	/* (non-Javadoc)
	 * @see v9t9.common.files.IFileExecutor#run(v9t9.common.machine.IMachine)
	 */
	@Override
	public void run(IMachine machine) throws NotifyException {
		
	}
}
