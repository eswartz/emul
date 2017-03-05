/*
  DiskEntry.java

  (c) 2012-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.disk;

import java.io.File;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;

import v9t9.common.files.Catalog;
import v9t9.common.files.IEmulatedDisk;
import v9t9.common.files.IEmulatedFileHandler;
import ejs.base.properties.IProperty;

public class DiskDirectoryEntry extends BaseDiskEntry {
	public DiskDirectoryEntry(IDeviceSelectorDialog dialog, final Composite parent, IProperty setting_) {
		super(dialog, parent, setting_);
	}
	
	/**
	 * @param setting
	 * @return
	 * @throws IOException 
	 */
	protected Catalog readCatalog(IEmulatedFileHandler fileHandler, IProperty setting) throws IOException {
		Catalog catalog;
		
		IEmulatedDisk disk;
		disk = fileHandler.getFilesInDirectoryMapper().createDiskDirectory(new File(setting.getString()));
		catalog = disk.readCatalog();
		return catalog;
	}
	
	protected void switchPath(Combo combo, String path) {
		if (path == null)
			return;
		setting.setString(path);

		validatePath();
	}


	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.disk.BaseDiskEntry#handleDiskBrowse(ejs.base.properties.IProperty)
	 */
	@Override
	protected void handleBrowse(IProperty setting) {
		DirectoryDialog dialog =  new DirectoryDialog(getShell(), SWT.NONE);
		dialog.setText("Select path for " + setting.getName());
		dialog.setFilterPath(setting.getString());
		String dirname = dialog.open();
		if (dirname != null) {
			switchPath(combo, dirname);
			combo.setText(dirname);
			commitEntry();
		}		
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.disk.BaseDiskEntry#validateDiskExists(java.io.File)
	 */
	@Override
	protected String validateDiskExists(File file) {
		if (file.isDirectory())
			return null;
		return "The disk directory does not exist or is a file";
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.disk.BaseDiskEntry#getHistoryName()
	 */
	@Override
	protected String getHistoryName() {
		return "Directories";
	}
	
}