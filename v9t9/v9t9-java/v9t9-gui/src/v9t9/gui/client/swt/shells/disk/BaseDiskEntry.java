/*
  BaseDiskEntry.java

  (c) 2012-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.disk;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import v9t9.common.events.NotifyException;
import v9t9.common.events.NotifyEvent.Level;
import v9t9.common.files.Catalog;
import v9t9.common.files.IDiskDriveSetting;
import v9t9.common.files.IEmulatedFileHandler;
import v9t9.common.files.IFileExecutor;
import ejs.base.properties.IProperty;

/**
 * Base entry for disk image or disk file editing, providing
 * a catalog and run button
 * @author ejs
 *
 */
abstract class BaseDiskEntry extends FileEntry {
	protected Button runButton;
	protected Button catalogButton;
	
	public BaseDiskEntry(IDeviceSelectorDialog dialog, final Composite parent, IProperty setting_) {
		super(dialog, parent, setting_, SWT.NONE);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.disk.FileEntry#createControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createControls(Composite parent) {
		super.createControls(parent);
		

		final IEmulatedFileHandler fileHandler = machine.getEmulatedFileHandler();
		if (fileHandler != null) {
			catalogButton = new Button(parent, SWT.PUSH);
			GridDataFactory.fillDefaults().grab(false, false).applyTo(catalogButton);
			catalogButton.setText("Catalog...");
			catalogButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Catalog catalog;
					try {
						catalog = readCatalog(fileHandler, setting);
						showCatalogDialog(setting, catalog);
					} catch (Throwable e1) {
						machine.notifyEvent(Level.ERROR,
								MessageFormat.format("Could not read catalog for disk image ''{0}''\n\n{1}",
										setting.getString(), e1.getMessage()));
					}
				}
			});
			
			runButton = new Button(parent, SWT.PUSH);
			GridDataFactory.fillDefaults().grab(false, false).applyTo(runButton);
			runButton.setText("Run...");
			runButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Catalog catalog;
					try {
						catalog = readCatalog(fileHandler, setting);
						showRunDialog(setting, catalog);
					} catch (IOException e1) {
						machine.notifyEvent(Level.ERROR,
								MessageFormat.format("Could not read catalog for disk image ''{0}''\n\n{1}",
										setting.getString(), e1.getMessage()));
					}
				}
			});

		}
	}
	
	abstract protected Catalog readCatalog(IEmulatedFileHandler fileHandler, IProperty setting) throws IOException;

	protected void showCatalogDialog(final IProperty setting,
			final Catalog catalog) {
		Dialog dialog = new DiskCatalogDialog(getShell(), machine, catalog);
		dialog.open();
	}

	protected void showRunDialog(final IProperty setting,
			final Catalog catalog) {
		DiskRunDialog dialog = new DiskRunDialog(getShell(), machine, 
				setting instanceof IDiskDriveSetting ? ((IDiskDriveSetting) setting).getDrive() : 1,
				catalog);
		int ret = dialog.open();
		if (ret == Dialog.OK) {
			IFileExecutor exec = dialog.getFileExecutor();
			if (exec != null) {
				try {
					exec.run(machine);
					
					// close disk selector
					getShell().close();
				} catch (NotifyException e) {
					machine.getEventNotifier().notifyEvent(e.getEvent());
				}
			}
		}
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.disk.FileEntry#validateFile(java.io.File)
	 */
	@Override
	protected String validateFile(File file) {
		String err = validateDiskExists(file);
		if (err == null) {
			if (catalogButton != null)
				catalogButton.setEnabled(true);
			if (runButton != null)
				runButton.setEnabled(true);
		} else {
			if (catalogButton != null)
				catalogButton.setEnabled(false);
			if (runButton != null)
				runButton.setEnabled(false);
		}
		return err;
	}
	
	abstract protected String validateDiskExists(File file);

}