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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;

import v9t9.common.events.NotifyEvent.Level;
import v9t9.common.events.NotifyException;
import v9t9.common.files.Catalog;
import v9t9.common.files.IDiskDriveSetting;
import v9t9.common.files.IEmulatedDisk;
import v9t9.common.files.IEmulatedFileHandler;
import v9t9.common.files.IFileExecutor;
import v9t9.gui.EmulatorGuiData;
import ejs.base.properties.IProperty;

class DiskEntry extends DiskSettingEntry {
	private Combo combo;
	private Label icon;
	private Image doesNotExistImage;
	private Image blankImage;
	private Button runButton;
	private Button catalogButton;
	
	public DiskEntry(IDeviceSelectorDialog dialog, final Composite parent, IProperty setting_) {
		super(dialog, parent, setting_, SWT.NONE);
		doesNotExistImage = EmulatorGuiData.loadImage(getDisplay(), "icons/error.png");
//		blankImage = new Image(getDisplay(), doesNotExistImage.getBounds());
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.DiskSelector.SettingEntry#createControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createControls(final Composite parent) {
		GridLayoutFactory.fillDefaults().numColumns(7).applyTo(this);
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (doesNotExistImage != null) 
					doesNotExistImage.dispose();
				if (blankImage != null) 
					blankImage.dispose();
				doesNotExistImage = null;
				blankImage = null;
			}
		});
		
		Label label = new Label(parent, SWT.NONE);
		label.setText(setting.getLabel() + ": ");
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(label);
		
		label.setToolTipText(setting.getDescription());

		icon = new Label(parent, SWT.NONE);
		icon.setImage(blankImage);
		GridDataFactory.fillDefaults().minSize(16, 16).hint(16, 16).applyTo(icon);

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
				commitDisk();
			}
		});
		combo.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				commitDisk();
			}
		});
		combo.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == '\r') {
					commitDisk();
					e.doit = false;
				}
			}
		});
		
		
		final Button browse = new Button(parent, SWT.PUSH);
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
		
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				commitDisk();
			}
		});
		

	}
	

	/**
	 * 
	 */
	protected void commitDisk() {
		File dir = new File(combo.getText());
		String path = dir.getAbsolutePath();
		switchPath(combo, path);
		
		updateSetting();
	}

	/**
	 * @param setting
	 * @return
	 * @throws IOException 
	 */
	protected Catalog readCatalog(IEmulatedFileHandler fileHandler, IProperty setting) throws IOException {
		Catalog catalog;
		
		IEmulatedDisk disk;
		
		if (!isDiskImage()) {
			disk = fileHandler.getFilesInDirectoryMapper().createDiskDirectory(new File(setting.getString()));
		} else {
			disk = fileHandler.getDiskImageMapper().createDiskImage(new File(setting.getString()));
		}
		catalog = disk.readCatalog();
		return catalog;
	}

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
	
	private boolean isDiskImage() {
		return setting.getName().contains("Image");
	}
	/**
	 * @param combo 
	 * @param absolutePath
	 */
	protected void switchPath(Combo combo, String path) {
		if (isDiskImage()) {
			// always set for disks
			setting.setString(path);
		} else {
			if (path == null)
				return;
			setting.setString(path);
		}

		validatePath();
	}

	/**
	 * 
	 */
	private void validatePath() {
		String path = setting.getString();
		File file = new File(path);
		boolean exists = isDiskImage() ? file.isFile() : file.isDirectory();
		if (exists) {
			icon.setImage(blankImage);
			if (catalogButton != null)
				catalogButton.setEnabled(true);
			if (runButton != null)
				runButton.setEnabled(true);
			icon.setToolTipText("");

			if (combo.indexOf(path) < 0) {
				// only store history for real places
				combo.add(path);
				setHistory(getHistoryName(), combo.getItems());
			}
			
		} else {
			icon.setImage(doesNotExistImage);
			if (catalogButton != null)
				catalogButton.setEnabled(false);
			if (runButton != null)
				runButton.setEnabled(false);
			icon.setToolTipText(isDiskImage() ? "The disk image file does not exist or is a directory"
					: "The disk directory does not exist or is a file");
		}
	}

	private String getHistoryName() {
		return isDiskImage() ? "Images" : "Directories";
	}
	
}