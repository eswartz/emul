/**
 * 
 */
package v9t9.gui.client.swt.handlers;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;

import v9t9.common.client.IEmulatorContentHandler;
import v9t9.common.events.NotifyException;
import v9t9.common.files.EmulatedDiskContentSource;
import v9t9.common.files.IDiskImage;
import v9t9.common.files.IDiskImageMapper;
import v9t9.common.files.IFileExecutor;
import v9t9.common.files.IFilesInDirectoryMapper;
import v9t9.common.machine.IMachine;
import ejs.base.properties.IProperty;

/**
 * @author ejs
 *
 */
public class FileExecutorContentHandler implements IEmulatorContentHandler {

	private EmulatedDiskContentSource diskSource;
	private IFileExecutor exec;
	private IMachine machine;
	private Shell shell;

	/**
	 * @param diskSource
	 * @param iFileExecutor
	 */
	public FileExecutorContentHandler(Shell shell, 
			IMachine machine, 
			EmulatedDiskContentSource diskSource,
			IFileExecutor exec) {
		this.shell = shell;
		this.machine = machine;
		this.diskSource = diskSource;
		this.exec = exec;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.IEmulatorContentHandler#getLabel()
	 */
	@Override
	public String getLabel() {
		return exec.getLabel();
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.IEmulatorContentHandler#getImage()
	 */
	@Override
	public ImageDescriptor getImage() {
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.IEmulatorContentHandler#getDescription()
	 */
	@Override
	public String getDescription() {
		return exec.getDescription();
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.IEmulatorContentHandler#handleContent()
	 */
	@Override
	public void handleContent() throws NotifyException {
		// load disk
		
		if (diskSource.getContent() instanceof IDiskImage) {
			IDiskImageMapper imageMapper = machine.getEmulatedFileHandler().getDiskImageMapper();
			
			IProperty enabledProperty = imageMapper.getImageSupportProperty();
			if (!enabledProperty.getBoolean()) {
				boolean go = MessageDialog.openQuestion(shell, "Enable Disk Image?", 
						"Disk image support is not enabled.\n\n"+
						"To use this disk, V9t9 needs to turn on that support and reset the emulator.\n\n"+
								"Continue?");
				if (!go)
					return;
				
				enabledProperty.setBoolean(true);
				machine.reset();
			}
		} else {
			IFilesInDirectoryMapper fiadMapper = machine.getEmulatedFileHandler().getFilesInDirectoryMapper();
			IProperty enabledProperty = fiadMapper.getDirectorySupportProperty();

			if (!enabledProperty.getBoolean()) {
				boolean go = MessageDialog.openQuestion(shell, "Enable Files in Directories?", 
						"Support for files in a directory is not enabled.\n\n"+
						"To use this disk, V9t9 needs to turn on that support.\n\n"+
								"Continue?");
				if (!go)
					return;
				
				enabledProperty.setBoolean(true);
				machine.reset();
			}

		}

		// select disk
		diskSource.getDiskImageProperty().setString(diskSource.getContent().getPath());
		
		// do the action
		exec.run(machine);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.IEmulatorContentHandler#requireConfirmation()
	 */
	@Override
	public boolean requireConfirmation() {
		return true;
	}

}
