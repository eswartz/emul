/*
  ROMSetupDialog.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.files.DataFiles;
import v9t9.common.files.IPathFileLocator;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.modules.IModule;
import v9t9.common.settings.SettingSchema;
import v9t9.common.settings.Settings;
import v9t9.gui.client.swt.PathSelector;
import v9t9.gui.client.swt.SwtWindow;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.settings.ISettingSection;
import ejs.base.utils.ListenerList;

/**
 * Allow selecting the paths for searching ROMs
 * @author ejs
 *
 */
public class PathSetupComposite extends Composite {
	/*private*/ ISettingSection dialogSettings;

	private IMachine machine;

	private final MemoryEntryInfo[] requiredRoms;
	private final MemoryEntryInfo[] optionalRoms;

	private Font winUnicodeFont;

	private IProperty bootRomsPath;

	private ISettingsHandler settings;

	private boolean allRequiredRomsFound;

	private TreeViewer viewer;

	private ROMSetupTreeContentProvider romTreeContentProvider;

	private SwtWindow window;
	
	public interface IPathSetupListener {
		void allRequiredRomsFound(boolean found);
	}
	private ListenerList<IPathSetupListener> listeners = new ListenerList<PathSetupComposite.IPathSetupListener>();
	
	public PathSetupComposite(Composite parent, IMachine machine_, SwtWindow window) {
		super(parent, SWT.NONE);
		this.window = window;
		this.machine = machine_;
		this.settings = Settings.getSettings(machine);
		
		this.requiredRoms = machine.getMemoryModel().getRequiredRomMemoryEntries();
		this.optionalRoms = machine.getMemoryModel().getOptionalRomMemoryEntries();

		GridLayoutFactory.fillDefaults().margins(6, 6).applyTo(this);
		SashForm sash = new SashForm(this, SWT.VERTICAL);
		
		GridLayoutFactory.fillDefaults().applyTo(sash);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(sash);
		
		createROMTable(sash);
		
		
		bootRomsPath = Settings.get(machine, DataFiles.settingBootRomsPath);
		createPathSelector(sash, bootRomsPath);

		sash.setWeights(new int[] { 66, 33 } );
		
		this.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (winUnicodeFont != null)
					winUnicodeFont.dispose();
			}
		});
		
	}

	/**
	 * 
	 */
	private void createROMTable(Composite parent) {
		viewer = new TreeViewer(parent);
		
		viewer.setAutoExpandLevel(2);
		
		Tree tree = viewer.getTree();
		
		GridDataFactory.fillDefaults().grab(true, true).minSize(-1, 96).applyTo(tree);
		
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		
		TreeViewerColumn nameColumn = new TreeViewerColumn(viewer, SWT.LEFT);
		nameColumn.getColumn().setText("Name");
		
		TreeViewerColumn fileColumn = new TreeViewerColumn(viewer, SWT.LEFT);  
		fileColumn.getColumn().setText("File(s)");
		
		TreeViewerColumn dirColumn = new TreeViewerColumn(viewer, SWT.LEFT);  
		dirColumn.getColumn().setText("Path(s)");
		
		romTreeContentProvider = new ROMSetupTreeContentProvider(requiredRoms, optionalRoms, machine);
		viewer.setContentProvider(romTreeContentProvider);
		viewer.setLabelProvider(new ROMSetupLabelProvider(machine, romTreeContentProvider));
		viewer.setInput(new Object());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			refreshAll();
			for (TreeColumn col : viewer.getTree().getColumns())
				col.pack();
		}

	}

	private void refreshAll() {
		BusyIndicator.showWhile(getDisplay(), new Runnable() {
			public void run() {
				romTreeContentProvider.refresh();
				viewer.refresh();

				allRequiredRomsFound = scanForRoms(requiredRoms);
				fireListeners();
				
			}
		});
	}
	
	private boolean scanForRoms(MemoryEntryInfo[] infos) {
		for (MemoryEntryInfo info : infos) {
			IPathFileLocator locator = machine.getRomPathFileLocator();
			URI uri = locator.findFile(settings, info); 
			if (uri == null) {
				return false;
			}
		}
		return true;
	}


	private void createPathSelector(Composite parent, final IProperty property) {
		PathSelector pathSelector = new PathSelector(parent, machine.getRomPathFileLocator(),
				window, "ROM directory", property);
		GridDataFactory.fillDefaults().grab(true, true).minSize(-1, 64).applyTo(pathSelector);

		final IPropertyListener pathChangeListener = new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (!getShell().isDisposed()) {
							refreshAll();
						}
					}
				});
			}
		};
		property.addListener(pathChangeListener);
		
		getShell().addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				property.removeListener(pathChangeListener);
			}
		});
	}

	static class RomEntryEditor extends Dialog {
		private final MemoryEntryInfo info;
		private Text text;
		protected String filename;
		private final SwtWindow window;
		private final ISettingsHandler settings;

		/**
		 * @param parentShell
		 */
		protected RomEntryEditor(Shell parentShell, SwtWindow window, ISettingsHandler settings, MemoryEntryInfo info) {
			super(parentShell);
			this.window = window;
			this.settings = settings;
			this.info = info;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
		 */
		@Override
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);
			
			Label label = new Label(composite, SWT.WRAP);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(label);
			label.setText("Enter the name of the ROM to always use for " + info.getDescription() +
					"\n(or leave blank to search by content)");
			
			Composite textAndButton = new Composite(composite, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(textAndButton);
			GridLayoutFactory.fillDefaults().numColumns(2).applyTo(textAndButton);
			
			text = new Text(textAndButton, SWT.BORDER);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(text);
			
			text.addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent e) {
					filename = text.getText();
				}
			});
			text.setText(info.getResolvedFilename(settings));
			
			Button browseButton = new Button(textAndButton, SWT.PUSH);
			browseButton.setText("Browse...");
			browseButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String newPath = window.openFileSelectionDialog(
							"Select ROM", null, text.getText(), false,
							new String[] { "*.bin|raw binary (*.bin)", "|all files" });
					if (newPath != null) {
						text.setText(newPath);
					}
				}
			});
			
			return composite;
		}

		/**
		 * @return
		 */
		public String getFilename() {
			return filename;
		}
	}

	/**
	 * @param memoryEntryInfo
	 */
	protected void editRomFilename(MemoryEntryInfo memoryEntryInfo) {
		RomEntryEditor dialog = new RomEntryEditor(getShell(), window, settings, memoryEntryInfo);
		
		int ret = dialog.open();
		
		if (ret == Dialog.OK) {
			String name = dialog.getFilename();
			
			int idx = name.lastIndexOf(File.separatorChar);
			if (idx >= 0) {
				String path = name.substring(0, idx);
				name = name.substring(idx + 1);
				
				File dir = new File(path);
				boolean found = false;
				for (Object ent : bootRomsPath.getList()) {
					if (new File((String) ent).equals(dir)) {
						found = true;
						break;
					}
				}
				if (!found) {
					bootRomsPath.getList().add(path);
					bootRomsPath.firePropertyChange();
				}
			}
			
			SettingSchema schema = memoryEntryInfo.getFilenameProperty();
			IProperty property = Settings.get(machine, schema);
			if (name == null || name.length() == 0) {
				property.setValue(schema.getDefaultValue());
			} else {
				property.setString(name);
			}

			refreshAll();
		}
	}

	/**
	 * @return
	 */
	public boolean allRequiredRomsFound() {
		return allRequiredRomsFound;
	}

	/**
	 * 
	 */
	public void refresh() {
		refreshAll();
		for (TreeColumn col : viewer.getTree().getColumns())
			col.pack();
		
	}

	public void addListener(IPathSetupListener listener) {
		listeners.add(listener);
	}
	public void removeListener(IPathSetupListener listener) {
		listeners.remove(listener);
	}
	protected void fireListeners() {
		for (IPathSetupListener listener : listeners)
			listener.allRequiredRomsFound(allRequiredRomsFound);
	}

	public List<IModule> getDetectedModules() {
		return romTreeContentProvider != null ?
				 romTreeContentProvider.getDetectedModules() : Collections.<IModule>emptyList();
	}
	

}
