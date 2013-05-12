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
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import v9t9.common.InternetDefinitions;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.files.DataFiles;
import v9t9.common.files.IPathFileLocator;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.settings.SettingSchema;
import v9t9.common.settings.Settings;
import v9t9.gui.client.swt.BrowserUtils;
import v9t9.gui.client.swt.PathSelector;
import v9t9.gui.client.swt.StyledTextHelper;
import v9t9.gui.client.swt.SwtWindow;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.settings.DialogSettingsWrapper;
import ejs.base.settings.ISettingSection;

/**
 * This tool shell shows up when a new configuration has
 * been detected and there are some ROM loading failures.
 * @author ejs
 *
 */
public class ROMSetupDialog extends Dialog {
	public static final String ROM_SETUP_TOOL_ID = "rom.setup";

	/** User feedback shows this stuff is just confusing */ 
	private static boolean ADVANCED = false;
	
	public static ROMSetupDialog createDialog(Shell shell, final IMachine machine,
			final SwtWindow window) {
		 return new ROMSetupDialog(shell, machine, window, 
						machine.getMemoryModel().getRequiredRomMemoryEntries(),
						machine.getMemoryModel().getOptionalRomMemoryEntries()
						);
	}
	
	/*private*/ ISettingSection dialogSettings;

	private SwtWindow window;

	private IMachine machine;

	private final MemoryEntryInfo[] requiredRoms;
	private final MemoryEntryInfo[] optionalRoms;

	private boolean wasPaused;

	private StyledText headerLabel;
	private StyledText footerLabel;

	//private StyledTextHelper styledTextHelper;

	private Font winUnicodeFont;

	private IdentityHashMap<StyleRange, LinkInfo> linkMap;

	private IProperty bootRomsPath;

	private ISettingsHandler settings;

	private boolean allRequiredRomsFound;

	private TreeViewer viewer;

	private ROMSetupTreeContentProvider romTreeContentProvider;
	
	public ROMSetupDialog(Shell shell, IMachine machine_, SwtWindow window,
			MemoryEntryInfo[] requiredRoms, MemoryEntryInfo[] optionalRoms) {
		super(shell);
		
		this.window = window;
		this.requiredRoms = requiredRoms;
		this.optionalRoms = optionalRoms;
		
		this.machine = machine_;
		this.settings = Settings.getSettings(machine);
		
		wasPaused = machine.setPaused(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#getShellStyle()
	 */
	@Override
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		newShell.setText("ROM Setup");
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(700, 850);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
	 */
	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return new DialogSettingsWrapper(machine.getSettings().getUserSettings()
				.getHistorySettings().findOrAddSection("ROMSetup"));
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		
		GridLayoutFactory.fillDefaults().margins(6, 6).applyTo(composite);
		
		SashForm sash = new SashForm(composite, SWT.VERTICAL | SWT.SMOOTH);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(sash);
		
		linkMap = new IdentityHashMap<StyleRange, LinkInfo>();

		headerLabel = createInfoSection(sash);
		setupHeaderLabel();

		createROMTable(sash);
		
		bootRomsPath = Settings.get(machine, DataFiles.settingBootRomsPath);
		createPathSelector(sash, bootRomsPath);

		footerLabel = createInfoSection(sash);
		setupFooterLabel();

		sash.setWeights(new int[] { 20, 50, 25, 15 });
		
		composite.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (allRequiredRomsFound) {
					machine.getMemoryModel().loadMemory(machine.getEventNotifier());
					machine.reset();
				}
				
				machine.setPaused(wasPaused);
			}
		});
		

		composite.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (winUnicodeFont != null)
					winUnicodeFont.dispose();
			}
		});
		
		return composite;
	}

	/**
	 * 
	 */
	private void createROMTable(Composite parent) {
		viewer = new TreeViewer(parent);
		
		viewer.setAutoExpandLevel(2);
		
		Tree tree = viewer.getTree();
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tree);
		
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
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		
		refreshAll();
		for (TreeColumn col : viewer.getTree().getColumns())
			col.pack();
	}

	/**
	 * @param parent 
	 * @return 
	 * 
	 */
	private StyledText createInfoSection(final Composite parent) {
		final StyledText text = new StyledText(parent, SWT.BORDER | SWT.WRAP | SWT.READ_ONLY | SWT.V_SCROLL);
				
		GridDataFactory.fillDefaults().grab(true, false).indent(8, 8).applyTo(text);
		text.setMargins(6, 6, 6, 6);
		
		text.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				@SuppressWarnings("unchecked")
				Entry<StyleRange, LinkInfo>[] array = linkMap.entrySet().toArray(new Map.Entry[linkMap.size()]);
				for (Map.Entry<StyleRange, LinkInfo> entry : array) {
					if (isOverLink(entry.getKey(), e)) {
						entry.getValue().handler.linkClicked();
					}
				}
			}
		});
		
		
		text.addMouseMoveListener(new MouseMoveListener() {
			Cursor cursor = text.getCursor();
			
			@Override
			public void mouseMove(MouseEvent e) {
				boolean any = false;
				for (Map.Entry<StyleRange, LinkInfo> entry : linkMap.entrySet()) {
					if (isOverLink(entry.getKey(), e)) {
						text.setCursor(getDisplay().getSystemCursor(SWT.CURSOR_HAND));
						any = true;
						break;
					}
				}
				if (!any) {
					text.setCursor(cursor);
				}
			}
			
		});
		return text;
	}

	
	protected Display getDisplay() {
		return getShell().getDisplay();
	}

	private void setupHeaderLabel() {
		// clear
		headerLabel.replaceTextRange(0, headerLabel.getCharCount(), "");
		linkMap.clear();
		
		StyledTextHelper styledTextHelper = new StyledTextHelper(headerLabel);

		styledTextHelper.pushStyle(new TextStyle(JFaceResources.getHeaderFont(), 
				getDisplay().getSystemColor(SWT.COLOR_BLUE),
				null), SWT.BOLD);
		
		headerLabel.append(
			"Welcome to V9t9!\n\n");
		
		styledTextHelper.popStyle();
		
		headerLabel.append(
				"In order to emulate the " + machine.getModel().getName() + ", you need to configure V9t9 so it can "+
				"find the necessary ROMs for the system and for any modules you want to use.\n\n"+
				"Add new Search Locations (below), and V9t9 will detect ROMs and modules by content. " +
				(ADVANCED ? "If you have custom ROMs, though, you can select your own file by clicking the links.\n" : "\n"));

	}
	private void setupFooterLabel() {
		StyledTextHelper styledTextHelper = new StyledTextHelper(footerLabel);

		styledTextHelper.pushStyle(new TextStyle(), SWT.ITALIC);

		footerLabel.append(
			"Note: copyrighted ROMs are not distributed with V9t9 itself.\n\n"+
			"Please see ");
		
		// SWT.UNDERLINE_LINK does not appear to work as promised :p
		TextStyle urlSt = new TextStyle(
				JFaceResources.getFontRegistry().getItalic(JFaceResources.TEXT_FONT),
				getDisplay().getSystemColor(SWT.COLOR_BLUE), null);

		urlSt.underline = true;
		urlSt.underlineColor = getDisplay().getSystemColor(SWT.COLOR_BLUE);

		final StyleRange urlStyle = styledTextHelper.pushStyle(urlSt, SWT.ITALIC);

		footerLabel.append(InternetDefinitions.sV9t9RomsURL);

		styledTextHelper.popStyle();
		
		registerLink(urlStyle, 
			new ILinkHandler() {
				public void linkClicked() {
					BrowserUtils.openURL(InternetDefinitions.sV9t9RomsURL);
				}
		});
		
		footerLabel.append(" for information on ROMs.\n\n");
		footerLabel.append("Or, exit this dialog and use the 'Demo' button to see how V9t9 behaves.");
		
		styledTextHelper.popStyle();
	}

	private interface ILinkHandler {
		void linkClicked();

	}
	static class LinkInfo {
		ILinkHandler handler;
	}
	
	/**
	 * @param styleRange
	 * @param handler
	 */
	private void registerLink(final StyleRange styleRange, final ILinkHandler handler) {
		LinkInfo info = new LinkInfo();
		info.handler = handler;
		linkMap.put(styleRange, info);
	}

	private void refreshAll() {
		BusyIndicator.showWhile(getDisplay(), new Runnable() {
			public void run() {
				headerLabel.setRedraw(false);
				
				romTreeContentProvider.refresh();
				viewer.refresh();

				allRequiredRomsFound = scanForRoms(requiredRoms);
				
				linkMap.clear();

				setupHeaderLabel();
				
				headerLabel.setRedraw(true);
				
				Button ok = getButton(OK);
				if (ok != null) {
					ok.setEnabled(allRequiredRomsFound);
				}
			}
		});
	}

	private boolean isOverLink(final StyleRange urlStyle, MouseEvent e) {
		try {
			int offs = headerLabel.getOffsetAtLocation(new Point(e.x, e.y));
			if (offs >= urlStyle.start && offs < urlStyle.start + urlStyle.length) {
				return true;
			}
		} catch (IllegalArgumentException t) {
		} catch (SWTException t) {
			// stupid #getOffsetAtLocation throws an exception 
			// if the point is out of range...
			// wtf... ever heard of returning -1 like #indexOf() would?
		}
		return false;
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
		GridDataFactory.fillDefaults().grab(true, true).applyTo(pathSelector);

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


	

}
