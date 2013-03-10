/**
 * 
 */
package v9t9.gui.client.swt.fileimport;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public class FileImportHandler implements IFileImportHandler {

	private IMachine machine;
	private List<String> history = new ArrayList<String>();

	/**
	 * @param machine
	 */
	public FileImportHandler(IMachine machine) {
		this.machine = machine;
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.fileimport.IFileImportHandler#getHistory()
	 */
	@Override
	public Collection<String> getHistory() {
		return history;
	}


	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.fileimport.IFileImportHandler#importFile(java.io.File)
	 */
	@Override
	public void importFile(File file) {
		
		System.err.println(".... try loading " + file);
	}
}
