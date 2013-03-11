/*
  FileImportHandler.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.fileimport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import ejs.base.properties.IProperty;

import v9t9.common.events.NotifyException;
import v9t9.common.files.Catalog;
import v9t9.common.files.IDiskImage;
import v9t9.common.files.IDiskImageMapper;
import v9t9.common.files.IFileExecutor;
import v9t9.common.files.IFileImportHandler;
import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public class FileImportHandler implements IFileImportHandler {

	private IMachine machine;
	private List<String> history = new ArrayList<String>();
	private Shell shell;

	/**
	 * @param machine
	 */
	public FileImportHandler(Shell shell, IMachine machine) {
		this.shell = shell;
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

		if (!tryDiskImage(file)) {
			MessageDialog.openError(shell, "Unsupported File", 
					"V9t9 does not know how to handle " + file);
		}
	}

	/**
	 * @param file
	 * @return
	 */
	private boolean tryDiskImage(File file) {
		IDiskImageMapper imageMapper = machine.getEmulatedFileHandler().getDiskImageMapper();
		Catalog catalog = null;
		try {
			IDiskImage image = imageMapper.createDiskImage("DSK1", file);
			if (!image.isFormatted())
				return false;
			catalog = imageMapper.createCatalog("DSK1", file);
			
		} catch (IOException e1) {
			e1.printStackTrace();
			// not disk image
			return false;
		}
		
		Map<String, IProperty> diskSettingsMap = imageMapper.getDiskSettingsMap();
		
		IProperty enabledProperty = imageMapper.getImageSupportProperty();
		if (enabledProperty == null || diskSettingsMap.isEmpty()) {
			MessageDialog.openError(shell, "Not Supported", "This machine does not support disk images");
			return true;
		}
		
		if (!enabledProperty.getBoolean()) {
			boolean go = MessageDialog.openConfirm(shell, "Enable Disk Image?", 
					"Disk image support is not enabled.\n\n"+
					"To use this disk, V9t9 needs to turn on that support and reset the emulator.\n\n"+
							"Continue?");
			if (!go)
				return true;
			
			enabledProperty.setBoolean(true);
			machine.reset();
		}

		SelectDiskImageDialog dialog = new SelectDiskImageDialog(shell, "Select Disk",
				machine,
				diskSettingsMap, 
				catalog,
				catalog.volumeName);

		int ret = dialog.open();
		if (ret == Window.OK) {
			dialog.getDiskProperty().setString(file.getAbsolutePath());
			
			IFileExecutor exec = dialog.getFileExecutor();
			if (exec != null) {
				try {
					exec.run(machine);
				} catch (NotifyException e) {
					machine.getEventNotifier().notifyEvent(e.getEvent());
				}
			}
		}
		return true;
	}
}
