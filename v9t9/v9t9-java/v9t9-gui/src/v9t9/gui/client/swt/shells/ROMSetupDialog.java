/**
 * 
 */
package v9t9.gui.client.swt.shells;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.settings.ISettingSection;

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
import v9t9.gui.common.FontUtils;

/**
 * This tool shell shows up when a new configuration has
 * been detected and there are some ROM loading failures.
 * @author ejs
 *
 */
public class ROMSetupDialog extends Composite {
	public static final String ROM_SETUP_TOOL_ID = "rom.setup";

	/*private*/ ISettingSection dialogSettings;

	private SwtWindow window;

	private IMachine machine;

	private final MemoryEntryInfo[] requiredRoms;
	private final MemoryEntryInfo[] optionalRoms;

	private IProperty pauseProperty;

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

	public ROMSetupDialog(Shell shell, IMachine machine_, SwtWindow window,
			MemoryEntryInfo[] requiredRoms, MemoryEntryInfo[] optionalRoms) {
		super(shell, SWT.NONE);
		
		this.window = window;
		this.requiredRoms = requiredRoms;
		this.optionalRoms = optionalRoms;
		
		shell.setText("ROM Setup");
		
		this.machine = machine_;
		this.settings = Settings.getSettings(machine);
		
		pauseProperty = Settings.get(machine, IMachine.settingPauseMachine);
		wasPaused = pauseProperty.getBoolean();
		pauseProperty.setBoolean(true);
		
		GridLayoutFactory.fillDefaults().applyTo(this);
		
		scanForRoms();

		SashForm sash = new SashForm(this, SWT.VERTICAL | SWT.SMOOTH);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(sash);
		
		createInfoSection(sash);

		bootRomsPath = Settings.get(machine, DataFiles.settingBootRomsPath);
		createPathSelector(sash, bootRomsPath);
		//createPathSelector(Settings.get(machine, DataFiles.settingUserRomsPath));
		
		sash.setWeights(new int[] { 75, 25 });
		
		updateRomAvailability();
		
		origDetectedROMs = new HashSet<URI>(detectedROMs);
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (!origDetectedROMs.equals(detectedROMs)) {
					//machine.getMemoryModel().initMemory(machine);
					machine.getMemoryModel().loadMemory(machine.getClient().getEventNotifier());
					machine.reset();
				}
				pauseProperty.setBoolean(wasPaused);
			}
		});
		

		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (winUnicodeFont != null)
					winUnicodeFont.dispose();
			}
		});
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
		
		setupInfoLabel();
		
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

	
	private void setupInfoLabel() {
		// clear
		infoLabel.replaceTextRange(0, infoLabel.getCharCount(), "");
		for (Map.Entry<StyleRange, LinkInfo> entry : linkMap.entrySet()) {
			infoLabel.removeMouseListener(entry.getValue().mouseListener);
		}
		linkMap.clear();
		
		styledTextHelper.pushStyle(new TextStyle(JFaceResources.getHeaderFont(), 
				getDisplay().getSystemColor(SWT.COLOR_BLUE),
				null), SWT.BOLD);
		
		infoLabel.append(
			"Welcome to V9t9!\n\n");
		
		styledTextHelper.popStyle();
		
		infoLabel.append(
				"In order to emulate the TI-99/4A, you need to configure V9t9 so it can "+
				"find the necessary ROMs.\n\n"+
				"If you point V9t9 to the appropriate locations (below), it can detect ROMs by content. " +
				"If you have customizations, though, it will match only by filename.\n\n"+
				"These ROMs are required:\t");

		reqdRomNameStyleRanges = emitRomsAndStyles(requiredRoms);
		
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

		infoLabel.append(InternetDefinitions.sV9t9WikiURL);

		styledTextHelper.popStyle();
		
		registerLink(urlStyle, 
			new ILinkHandler() {
				public void linkClicked() {
					BrowserUtils.openURL(InternetDefinitions.sV9t9WikiURL);
				}
		});
		
		infoLabel.append(" for information on ROMs.");
		
		styledTextHelper.popStyle();
	}

	private interface ILinkHandler {
		void linkClicked();

	}
	static class LinkInfo {
		MouseListener mouseListener;
	}
	
	/**
	 * @param styleRange
	 * @param handler
	 */
	private void registerLink(final StyleRange styleRange, final ILinkHandler handler) {
		LinkInfo info = new LinkInfo();
		linkMap.put(styleRange, info);
		
		info.mouseListener = new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if (isOverLink(styleRange, e)) {
					handler.linkClicked();
				}
			}
		};
		
		
		infoLabel.addMouseListener(info.mouseListener);
	}


	/**
	 * @param optionalRoms2
	 * @return
	 */
	private StyleRange[] emitRomsAndStyles(final MemoryEntryInfo[] infos) {
		StyleRange[] ranges = new StyleRange[infos.length];
		
		for (int i = 0; i < infos.length; i++) {
			infoLabel.append("\n\t");
			
			infoLabel.append("\u2022 ");
			
			
			StyleRange range = styledTextHelper.pushStyle(new TextStyle(), SWT.NONE);
			
			range.foreground = getDisplay().getSystemColor(SWT.COLOR_BLUE);
			range.underline = true;
			range.underlineColor = getDisplay().getSystemColor(SWT.COLOR_BLUE);

			final MemoryEntryInfo info = infos[i];
			if (info.getFilenameProperty() != null) {
				registerLink(range, new ILinkHandler() {
					
					@Override
					public void linkClicked() {
						editRomFilename(info);
					}
				});
			}
			
			infoLabel.append(filenameFor(info));
			styledTextHelper.popStyle();
			ranges[i] = range;
			
			String descr = info.getDescription();
			if (descr != null)
				infoLabel.append(" (" + descr + ")  ");
			
			String[] marks;
			TextStyle unicodeStyle = new TextStyle();
			
			if (SWT.getPlatform().toLowerCase().contains("win")) {
				if (winUnicodeFont == null) {
					FontData[] data = getDisplay().getFontList("Wingdings", true);
					if (data.length > 0) {
						float height = FontUtils.measureText(getDisplay(), getFont(), "!").y;
						data[0].setHeight((int) (height * 0.75f));
						winUnicodeFont = new Font(getDisplay(), data[0]);
					}
				}
				unicodeStyle.font = winUnicodeFont;
				marks = new String[] { "\u00FC", "\u00FB" };
			} else {
				marks = new String[] { "\u2714", "\u2715" };
			}

			if (isRomAvailable(info)) {
				unicodeStyle.foreground = getDisplay().getSystemColor(SWT.COLOR_GREEN);
				styledTextHelper.pushStyle(unicodeStyle, SWT.NONE);
				infoLabel.append(marks[0]);	// checkmark
				styledTextHelper.popStyle();
			} else {
				unicodeStyle.foreground = getDisplay().getSystemColor(SWT.COLOR_RED);
				styledTextHelper.pushStyle(unicodeStyle, SWT.NONE);
				infoLabel.append(marks[1]);	// x-mark
				styledTextHelper.popStyle();
			}
			
			infoLabel.append(" ");
			
		}
		return ranges;
	}


	/**
	 * 
	 */
	private void refreshAll() {
		scanForRoms();
		setupInfoLabel();
		updateRomAvailability();
		
	}


	/**
	 * @param memoryEntryInfo
	 * @return
	 */
	private String filenameFor(MemoryEntryInfo info) {
		String filename = info.getResolvedFilename(settings);
		if (filename != null)
			return filename;
		return "<MD5:" + info.getFileMD5() + ">";
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
		updateRomLabels(requiredRoms, reqdRomNameStyleRanges);
		updateRomLabels(optionalRoms, optionalRomNameStyleRanges);
		
	}
	

	private void scanForRoms(MemoryEntryInfo[] infos) {
		for (int i = 0; i < infos.length; i++) {
			scanForRom(infos[i]);
		}
	}

	
	private void updateRomLabels(MemoryEntryInfo[] infos, StyleRange[] styleRanges) {
		for (int i = 0; i < infos.length; i++) {
			StyleRange style = styleRanges[i];
			
			if (isRomAvailable(infos[i])) {
				//style.borderStyle = SWT.BORDER_SOLID;
				//style.borderColor = getDisplay().getSystemColor(SWT.COLOR_GREEN);
				if (!infos[i].isDefaultFilename(settings))
					style.font = JFaceResources.getFontRegistry().getBold(JFaceResources.TEXT_FONT);
				else
					style.font = JFaceResources.getTextFont();
			} else {
				//style.borderStyle = SWT.BORDER_DASH;
				//style.borderColor = getDisplay().getSystemColor(SWT.COLOR_RED);
				style.font = JFaceResources.getFontRegistry().getItalic(JFaceResources.TEXT_FONT);
				
			}
			infoLabel.setStyleRange(style);
		}
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
		
		IPathFileLocator locator = machine.getPathFileLocator();
		URI uri = locator.findFile(settings, info); 
		return uri;
			
	}


	private void createPathSelector(Composite parent, final IProperty property) {
		PathSelector pathSelector = new PathSelector(parent, window, "ROM directory", property);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(pathSelector);

		final IPropertyListener pathChangeListener = new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (!isDisposed()) {
							refreshAll();
						}
					}
				});
			}
		};
		property.addListener(pathChangeListener);
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				property.removeListener(pathChangeListener);
			}
		});
	}

	public static IToolShellFactory getToolShellFactory(final IMachine machine,
			final SwtWindow window) {
		 return new IToolShellFactory() {
			Behavior behavior = new Behavior();
			{
				behavior.boundsPref = "NewConfigBounds";
				behavior.defaultBounds = new Rectangle(0, 0, 500, 600);
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
							new String[] { "bin|raw binary" });
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
