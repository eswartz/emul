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
import org.eclipse.swt.widgets.FileDialog;

import v9t9.common.files.Catalog;
import v9t9.common.files.IEmulatedDisk;
import v9t9.common.files.IEmulatedFileHandler;
import ejs.base.properties.IProperty;

public class DiskImageEntry extends BaseDiskEntry {
	
	public DiskImageEntry(IDeviceSelectorDialog dialog, final Composite parent, IProperty setting_) {
		super(dialog, parent, setting_);
	}
	
	protected Catalog readCatalog(IEmulatedFileHandler fileHandler, IProperty setting) throws IOException {
		Catalog catalog;
		
		IEmulatedDisk disk;
		disk = fileHandler.getDiskImageMapper().createDiskImage(new File(setting.getString()), "DSK" + setting.getName().charAt(setting.getName().length() - 1));
		catalog = disk.readCatalog();
		return catalog;
	}

	@Override
	protected String validateDiskExists(File file) {
		if (file.isFile())
			return null;
		return "The disk image file does not exist or is a directory";
	}

	protected String getHistoryName() {
		return "Images";
	}

	@Override
	protected void handleBrowse(IProperty setting) {
		FileDialog dialog =  new FileDialog(getShell(), SWT.OPEN);
		dialog.setText("Select image for " + setting.getName());
		String dir = new File(setting.getString()).getParent();
		String filename = new File(setting.getString()).getName();
		dialog.setFilterPath(dir);
		dialog.setFileName(filename);
		dialog.setFilterExtensions(new String[] { "*.dsk;*.DSK;*.trk;*.TRK", "*.*" });
		dialog.setFilterNames(new String[] { "Disk images (*.dsk; *.trk)", "All files" });
		if (filename.toLowerCase().endsWith(".dsk") || filename.toLowerCase().endsWith(".trk"))  {
			dialog.setFilterIndex(0);
		} else {
			dialog.setFilterIndex(1);
		}
		filename = dialog.open();
		if (filename != null) {
			switchPath(combo, filename);
			combo.setText(filename);
			commitEntry();
		}		
	}


	protected void switchPath(Combo combo, String path) {
		// always set for disks
		setting.setString(path);

		validatePath();
	}

}