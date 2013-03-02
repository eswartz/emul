/*
  DiskEntry.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.disk;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;

import v9t9.common.events.IEventNotifier;
import v9t9.common.files.Catalog;
import v9t9.common.files.CatalogEntry;
import v9t9.common.files.IFileHandler;
import v9t9.common.machine.IMachine;
import ejs.base.properties.IProperty;

class DiskEntry extends DiskSettingEntry {
	private Combo combo;
	private Button browse;
	private Button catalog;
	
	public DiskEntry(final Composite parent, IMachine machine, IProperty setting_) {
		super(parent, machine, setting_, SWT.NONE);
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.DiskSelector.SettingEntry#createControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createControls(final Composite parent) {
		GridLayoutFactory.fillDefaults().numColumns(5).applyTo(this);
		
		Label label = new Label(parent, SWT.NONE);
		label.setText(setting.getLabel() + ": ");
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(label);
		
		label.setToolTipText(setting.getDescription());
		
		combo = new Combo(parent, SWT.BORDER | SWT.DROP_DOWN);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(combo);
		
		combo.setToolTipText(setting.getDescription());
		
		String[] history = getHistory(getHistoryName());
		if (history != null) {
			combo.setItems(history);
		}
		
		combo.setText(setting.getString());
		
		combo.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				File dir = new File(combo.getText());
				String path = dir.getAbsolutePath();
				// always set for disks 
				if (isDiskImage())
					setting.setString(path);
				switchPath(combo, path);
			}

		});
		
		
		browse = new Button(parent, SWT.PUSH);
		GridDataFactory.fillDefaults().grab(false, false).applyTo(browse);
		browse.setText("Browse...");
		browse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (isDiskImage()) {
					FileDialog dialog =  new FileDialog(parent.getShell(), SWT.OPEN);
					dialog.setText("Select image for " + setting.getName());
					String dir = new File(setting.getString()).getParent();
					String filename = new File(setting.getString()).getName();
					dialog.setFilterPath(dir);
					dialog.setFileName(filename);
					dialog.setFilterExtensions(new String[] { "*.dsk", "*.trk", "*.*" });
					dialog.setFilterNames(new String[] { "Sector disk images", "Track disk images", "All files" });
					if (filename.endsWith(".dsk"))  {
						dialog.setFilterIndex(0);
					} else if (filename.endsWith(".trk"))  {
						dialog.setFilterIndex(1);
					} else {
						dialog.setFilterIndex(2);
					}
					filename = dialog.open();
					if (filename != null) {
						switchPath(combo, filename);
						combo.setText(filename);
					}
				} else {
					DirectoryDialog dialog =  new DirectoryDialog(parent.getShell(), SWT.NONE);
					dialog.setText("Select path for " + setting.getName());
					dialog.setFilterPath(setting.getString());
					String dirname = dialog.open();
					if (dirname != null) {
						switchPath(combo, dirname);
						combo.setText(dirname);
					}
				}
			}
		});			
		
		final IFileHandler fileHandler = machine.getFileHandler();
		if (fileHandler != null) {
			catalog = new Button(parent, SWT.PUSH);
			GridDataFactory.fillDefaults().grab(false, false).applyTo(catalog);
			catalog.setText("Catalog...");
			catalog.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Catalog catalog;
					try {
						catalog = fileHandler.createCatalog(setting, isDiskImage());
						showCatalogDialog(setting, catalog);
					} catch (IOException e1) {
						machine.notifyEvent(IEventNotifier.Level.ERROR,
								MessageFormat.format("Could not read catalog for disk image ''{0}''\n\n{1}",
										setting.getString(), e1.getMessage()));
					}
				}
			});
		}
	}
	

	/**
	 * @param setting
	 * @param entries
	 */
	protected void showCatalogDialog(final IProperty setting,
			final Catalog catalog) {
		final List<CatalogEntry> entries = catalog.getEntries();
		Dialog dialog = new DiskBrowseDialog(getShell(), machine, entries, catalog);
		dialog.open();

		
	}

	private boolean isDiskImage() {
		return setting.getName().contains("Image");
	}
	/**
	 * @param combo 
	 * @param absolutePath
	 */
	protected void switchPath(Combo combo, String path) {
		if (path == null)
			return;
		setting.setString(path);
		for (String p : combo.getItems())
			if (p.equals(path))
				return;
		
		// only store history for real places
		if (new File(path).exists()) {
			combo.add(path);
			setHistory(getHistoryName(), combo.getItems());
		}
	}

	private String getHistoryName() {
		return isDiskImage() ? "Images" : "Directories";
	}
	
}