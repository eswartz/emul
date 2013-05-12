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
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
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
import org.eclipse.swt.graphics.Color;
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
import org.ejs.gui.common.FontUtils;

import v9t9.common.InternetDefinitions;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.files.DataFiles;
import v9t9.common.files.IPathFileLocator;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.memory.StoredMemoryEntryInfo;
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
	
	/*
	public static IToolShellFactory getToolShellFactory(final IMachine machine,
			final SwtWindow window) {
		 return new IToolShellFactory() {
			Behavior behavior = new Behavior();
			{
				behavior.boundsPref = "NewConfigBounds";
				behavior.defaultBounds = new Rectangle(0, 0, 700, 600);
				behavior.centering = Centering.CENTER;
				behavior.centerOverControl = window.getShell();
				behavior.dismissOnClickOutside = false;
			}
			public Control createContents(Shell shell) {
				return new ROMSetupDialog(shell, machine, window, 
						machine.getMemoryModel().getRequiredRomMemoryEntries(),
						machine.getMemoryModel().getOptionalRomMemoryEntries()
						);
			}
			public Behavior getBehavior() {
				return behavior;
			}
		};
	}
	*/
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

	private StyledText infoLabel;

	private StyleRange[] reqdRomNameStyleRanges;


	private StyleRange[] optionalRomNameStyleRanges;

	private StyledTextHelper styledTextHelper;

	private Font winUnicodeFont;

	private Set<URI> detectedROMs;
	private Set<URI> origDetectedROMs;

	private IdentityHashMap<StyleRange, LinkInfo> linkMap;

	private IProperty bootRomsPath;

	private ISettingsHandler settings;

	private boolean allRequiredRomsFound;

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
		
		scanForRoms();

		SashForm sash = new SashForm(composite, SWT.VERTICAL | SWT.SMOOTH);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(sash);
		
		createInfoSection(sash);

		bootRomsPath = Settings.get(machine, DataFiles.settingBootRomsPath);
		createPathSelector(sash, bootRomsPath);
		//createPathSelector(Settings.get(machine, DataFiles.settingUserRomsPath));
		
		sash.setWeights(new int[] { 75, 25 });
		
		//updateRomAvailability();
		
		origDetectedROMs = new HashSet<URI>(detectedROMs);
		
		composite.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (origDetectedROMs.isEmpty()) {
					// ... some kind of "raw raw" setup?
				}
				
				if (!origDetectedROMs.equals(detectedROMs)) {
					//machine.getMemoryModel().initMemory(machine);
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

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		
		refreshAll();
	}

	/**
	 * @param parent 
	 * 
	 */
	private void createInfoSection(final Composite parent) {
		infoLabel = new StyledText(parent, SWT.BORDER | SWT.WRAP | SWT.READ_ONLY | SWT.V_SCROLL);
		styledTextHelper = new StyledTextHelper(infoLabel);
		linkMap = new IdentityHashMap<StyleRange, LinkInfo>();
				
		GridDataFactory.fillDefaults().grab(true, false).indent(8, 8).applyTo(infoLabel);
		infoLabel.setMargins(6, 6, 6, 6);
		
		setupInfoLabel();
		

		infoLabel.addMouseListener(new MouseAdapter() {
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
		
		
		infoLabel.addMouseMoveListener(new MouseMoveListener() {
			Cursor cursor = infoLabel.getCursor();
			
			@Override
			public void mouseMove(MouseEvent e) {
				boolean any = false;
				for (Map.Entry<StyleRange, LinkInfo> entry : linkMap.entrySet()) {
					if (isOverLink(entry.getKey(), e)) {
						infoLabel.setCursor(getDisplay().getSystemCursor(SWT.CURSOR_HAND));
						any = true;
						break;
					}
				}
				if (!any) {
					infoLabel.setCursor(cursor);
				}
			}
			
		});
	}

	
	protected Display getDisplay() {
		return getShell().getDisplay();
	}

	private void setupInfoLabel() {
		// clear
		infoLabel.replaceTextRange(0, infoLabel.getCharCount(), "");
		linkMap.clear();
		
		styledTextHelper.pushStyle(new TextStyle(JFaceResources.getHeaderFont(), 
				getDisplay().getSystemColor(SWT.COLOR_BLUE),
				null), SWT.BOLD);
		
		infoLabel.append(
			"Welcome to V9t9!\n\n");
		
		styledTextHelper.popStyle();
		
		infoLabel.append(
				"In order to emulate the " + machine.getModel().getName() + ", you need to configure V9t9 so it can "+
				"find the necessary ROMs for the system and for any modules you want to use.\n\n"+
				"Add new Search Locations (below), and V9t9 will detect ROMs and modules by content. " +
				(ADVANCED ? "If you have custom ROMs, though, you can select your own file by clicking the links.\n" : "\n"));

		if (requiredRoms.length > 0) {
			infoLabel.append("\nThese ROMs are required:\t");
			reqdRomNameStyleRanges = emitRomsAndStyles(requiredRoms);
		}
		
		if (optionalRoms.length > 0) {
			infoLabel.append(
					"\n\nThese ROMs are optional:\t");
					
			optionalRomNameStyleRanges = emitRomsAndStyles(optionalRoms);
		
			styledTextHelper.pushStyle(new TextStyle(), SWT.ITALIC);
	
			infoLabel.append("\n\n"+
				"Note: copyrighted ROMs are not distributed with V9t9 itself.\n\n"+
				"Please see ");
			
			// SWT.UNDERLINE_LINK does not appear to work as promised :p
			TextStyle urlSt = new TextStyle(
					JFaceResources.getFontRegistry().getItalic(JFaceResources.TEXT_FONT),
					getDisplay().getSystemColor(SWT.COLOR_BLUE), null);
	
			urlSt.underline = true;
			urlSt.underlineColor = getDisplay().getSystemColor(SWT.COLOR_BLUE);
	
			final StyleRange urlStyle = styledTextHelper.pushStyle(urlSt, SWT.ITALIC);
	
			infoLabel.append(InternetDefinitions.sV9t9RomsURL);
	
			styledTextHelper.popStyle();
			
			registerLink(urlStyle, 
				new ILinkHandler() {
					public void linkClicked() {
						BrowserUtils.openURL(InternetDefinitions.sV9t9RomsURL);
					}
			});
			
			infoLabel.append(" for information on ROMs.\n\n");
			infoLabel.append("Or, exit this dialog and use the 'Demo' button to see how V9t9 behaves.");
			
			styledTextHelper.popStyle();
		}
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


	/**
	 * @param optionalRoms2
	 * @return
	 */
	private StyleRange[] emitRomsAndStyles(final MemoryEntryInfo[] infos) {
		StyleRange[] ranges = new StyleRange[infos.length];
		
		for (int i = 0; i < infos.length; i++) {
			final MemoryEntryInfo info = infos[i];
			
			infoLabel.append("\n\t");
			
			infoLabel.append("\u2022 ");
			
			
			styledTextHelper.pushStyle(new TextStyle(
					JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT), null, null), 
					SWT.NONE);
			String descr = info.getDescription();
			if (descr != null)
				infoLabel.append(descr);
			else
				infoLabel.append(info.getName());

			styledTextHelper.popStyle();

			if (ADVANCED) {
				infoLabel.append(" (");
	
				if (info.isDefaultFilename(settings)) {
					infoLabel.append("default filename: ");
				} else {
					infoLabel.append("user-specified filename: ");
				}
				
				StyleRange range = styledTextHelper.pushStyle(new TextStyle(), SWT.NONE);
				
				range.foreground = getDisplay().getSystemColor(SWT.COLOR_BLUE);
				range.underline = true;
				range.underlineColor = getDisplay().getSystemColor(SWT.COLOR_BLUE);
	
				if (info.getFilenameProperty() != null) {
					registerLink(range, new ILinkHandler() {
						
						@Override
						public void linkClicked() {
							editRomFilename(info);
						}
					});
				}
				
				infoLabel.append(info.getResolvedFilename(settings));
				
				
				styledTextHelper.popStyle();
				ranges[i] = range;
	
				infoLabel.append(") ");
			} else {
				StyleRange range = styledTextHelper.pushStyle(new TextStyle(), SWT.NONE);
				styledTextHelper.popStyle();
				ranges[i] = range;
				infoLabel.append(" ");
			}

			StoredMemoryEntryInfo storedInfo = null;
			try {
				storedInfo = StoredMemoryEntryInfo.createStoredMemoryEntryInfo(
						machine.getRomPathFileLocator(), settings, 
						machine.getMemory(), info);
			} catch (IOException e) {
			}
			

			int index;
			Color color;
			if (storedInfo != null) {
				if (info.matches(storedInfo)) {
					index = 0;
					color = getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN);
				} else {
					index = 2;
					color = getDisplay().getSystemColor(SWT.COLOR_YELLOW);
				}
			} else {
				index = 1;
				color = getDisplay().getSystemColor(SWT.COLOR_RED);
			}
			
			
			String[] marks;
			TextStyle unicodeStyle = new TextStyle();
			
			if (SWT.getPlatform().toLowerCase().contains("win")) {
				if (winUnicodeFont == null) {
					//FontData[] data = getDisplay().getFontList("Wingdings", true);
					//if (data.length > 0) {
						float height = FontUtils.measureText(getDisplay(), getShell().getFont(), "!").y;
						//data[0].setHeight((int) (height * 0.75f));
						winUnicodeFont = new Font(getDisplay(), "Wingdings", (int) (height * 0.9f), SWT.BOLD);
					//}
				}
				unicodeStyle.font = winUnicodeFont;
				marks = new String[] { "\u00FC", "\u00FB", "\u004C" };
			} else {
				marks = new String[] { "\u2714", "\u2718", "\u2639"  };
			}


			unicodeStyle.foreground = color;
			
			styledTextHelper.pushStyle(unicodeStyle, SWT.NONE);
			infoLabel.append(marks[index]);	// icon
			styledTextHelper.popStyle();
			
			infoLabel.append("\n\t\t");
			
			if (storedInfo != null) {
				infoLabel.append(" (found at ");
				
				styledTextHelper.pushStyle(new TextStyle(JFaceResources.getTextFont(), null, null), SWT.NONE);
				try {
					infoLabel.append(new File(storedInfo.uri).toString());
				} catch (IllegalArgumentException e) {
					infoLabel.append(storedInfo.uri.toString());
				}
				styledTextHelper.popStyle();
				infoLabel.append(")");
				
				if (index == 2) {
					TextStyle warningStyle = new TextStyle(JFaceResources.getFontRegistry().getItalic(
							JFaceResources.DEFAULT_FONT), null, null);
					styledTextHelper.pushStyle(warningStyle, SWT.NONE);
					
					infoLabel.append("\n\t\t(checksum does not match: double-check this entry)");
					styledTextHelper.popStyle();
					
				}
			} else {
				if (ADVANCED) {
					infoLabel.append(" (need size: " + (info.getSize() < 0 ? " up to " + -info.getSize() : info.getSize())
							+ "; MD5 sum = " + info.getFileMD5() + ")");
				} else {
					infoLabel.append(" (not found)");
				}
			}
			//infoLabel.append(filenameFor(info));
			
		}
		return ranges;
	}


	/**
	 * 
	 */
	private void refreshAll() {
		BusyIndicator.showWhile(getDisplay(), new Runnable() {
			public void run() {
				infoLabel.setRedraw(false);
				scanForRoms();
				setupInfoLabel();
				updateRomAvailability();
				infoLabel.setRedraw(true);
				
				Button ok = getButton(OK);
				if (ok != null) {
					ok.setEnabled(allRequiredRomsFound);
				}
			}
		});
	}

	private boolean isOverLink(final StyleRange urlStyle, MouseEvent e) {
		try {
			int offs = infoLabel.getOffsetAtLocation(new Point(e.x, e.y));
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
	
	protected void scanForRoms() {
		detectedROMs = new HashSet<URI>();
		scanForRoms(requiredRoms);
		scanForRoms(optionalRoms);
	}

	protected void updateRomAvailability() {
		allRequiredRomsFound = updateRomLabels(requiredRoms, reqdRomNameStyleRanges);
		updateRomLabels(optionalRoms, optionalRomNameStyleRanges);
		
	}
	

	private void scanForRoms(MemoryEntryInfo[] infos) {
		for (int i = 0; i < infos.length; i++) {
			scanForRom(infos[i]);
		}
	}

	
	private boolean updateRomLabels(MemoryEntryInfo[] infos, StyleRange[] styleRanges) {
		boolean allFound = true;
		for (int i = 0; i < infos.length; i++) {
			StyleRange style = styleRanges[i];
			
			if (isRomAvailable(infos[i])) {
				if (!infos[i].isDefaultFilename(settings))
					style.font = JFaceResources.getFontRegistry().getBold(JFaceResources.TEXT_FONT);
				else
					style.font = JFaceResources.getTextFont();
			} else {
				style.font = JFaceResources.getFontRegistry().getItalic(JFaceResources.TEXT_FONT);
				allFound = false;
			}
			
			infoLabel.setStyleRange(style);
		}
		return allFound;
	}


	/**
	 * @param info
	 * @return
	 */
	private void scanForRom(MemoryEntryInfo info) {
		
		URI uri = findRom(info);
		
		if (uri != null) {
			detectedROMs.add(uri);
		}
	}
	
	/**
	 * @param info
	 * @return
	 */
	private boolean isRomAvailable(MemoryEntryInfo info) {
		
		URI uri = findRom(info);
		if (uri != null)
			detectedROMs.add(uri);
		
		return uri != null;
	}


	/**
	 * @param info
	 * @return
	 */
	private URI findRom(MemoryEntryInfo info) {
		
		IPathFileLocator locator = machine.getRomPathFileLocator();
		URI uri = locator.findFile(settings, info); 
		return uri;
			
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
