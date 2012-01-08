/**
 * 
 */
package v9t9.gui.client.swt.shells;

import java.net.URI;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.settings.ISettingSection;

import v9t9.common.InternetDefinitions;
import v9t9.common.files.DataFiles;
import v9t9.common.files.IPathFileLocator;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.settings.Settings;
import v9t9.gui.client.swt.BrowserUtils;
import v9t9.gui.client.swt.PathSelector;
import v9t9.gui.client.swt.StyledTextHelper;
import v9t9.gui.client.swt.SwtWindow;

/**
 * This tool shell shows up when a new configuration has
 * been detected and there are some ROM loading failures.
 * @author ejs
 *
 */
public class NewSetupDialog extends Composite {
	public static final String NEW_SETUP_TOOL_ID = "new.setup";

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

	public NewSetupDialog(Shell shell, IMachine machine, SwtWindow window,
			MemoryEntryInfo[] requiredRoms, MemoryEntryInfo[] optionalRoms) {
		super(shell, SWT.NONE);
		
		this.window = window;
		this.requiredRoms = requiredRoms;
		this.optionalRoms = optionalRoms;
		
		shell.setText("New Setup");
		
		this.machine = machine;
		
		pauseProperty = Settings.get(machine, IMachine.settingPauseMachine);
		wasPaused = pauseProperty.getBoolean();
		pauseProperty.setBoolean(true);
		
		GridLayoutFactory.fillDefaults().applyTo(this);
		
		scanForRoms();

		createInfoSection();

		createPathSelector(Settings.get(machine, DataFiles.settingBootRomsPath));
		//createPathSelector(Settings.get(machine, DataFiles.settingUserRomsPath));
		
		updateRomAvailability();
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				pauseProperty.setBoolean(wasPaused);
			}
		});
	}


	/**
	 * 
	 */
	private void createInfoSection() {
		infoLabel = new StyledText(this, SWT.BORDER | SWT.WRAP);
		styledTextHelper = new StyledTextHelper(infoLabel);
				
		GridDataFactory.fillDefaults().grab(true, false).indent(8, 8).applyTo(infoLabel);
		setupInfoLabel();
	}

	
	private void setupInfoLabel() {
		// clear
		infoLabel.replaceTextRange(0, infoLabel.getCharCount(), "");
		
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
		
		final StyleRange urlStyle = styledTextHelper.pushStyle(urlSt, SWT.ITALIC);

		infoLabel.append(InternetDefinitions.sV9t9WikiURL);

		styledTextHelper.popStyle();
		
		infoLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if (isOverLink(urlStyle, e)) {
					BrowserUtils.openURL(InternetDefinitions.sV9t9WikiURL);
				}
			}


		});

		infoLabel.addMouseMoveListener(new MouseMoveListener() {
			Cursor cursor = infoLabel.getCursor();
			
			@Override
			public void mouseMove(MouseEvent e) {
				if (isOverLink(urlStyle, e)) {
					infoLabel.setCursor(getDisplay().getSystemCursor(SWT.CURSOR_HAND));
				} else {
					infoLabel.setCursor(cursor);
				}
			}
			
		});
		infoLabel.append(" for information on ROMs.");
		
		styledTextHelper.popStyle();
	}

	/**
	 * @param optionalRoms2
	 * @return
	 */
	private StyleRange[] emitRomsAndStyles(MemoryEntryInfo[] infos) {
		StyleRange[] ranges = new StyleRange[infos.length];
		
		for (int i = 0; i < infos.length; i++) {
			infoLabel.append("\n\t");
			
			infoLabel.append("\u2022 ");
			
			
			StyleRange range = styledTextHelper.pushStyle(new TextStyle(), SWT.NONE);
			infoLabel.append(filenameFor(infos[i]));
			styledTextHelper.popStyle();
			ranges[i] = range;
			
			String descr = infos[i].getString(MemoryEntryInfo.DESCRIPTION);
			if (descr != null)
				infoLabel.append(" (" + descr + ")  ");
			
			if (isRomAvailable(infos[i])) {
				infoLabel.append("\u2714");
			} else {
				infoLabel.append("\u2715");
			}
			
			infoLabel.append(" ");
			
		}
		return ranges;
	}


	/**
	 * @param memoryEntryInfo
	 * @return
	 */
	private String filenameFor(MemoryEntryInfo info) {
		if (info.getFilename() != null)
			return info.getFilename();
		if (info.getFilenameProperty() != null)
			return Settings.get(machine, info.getFilenameProperty()).getString();
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
				style.borderStyle = SWT.BORDER_SOLID;
				style.borderColor = getDisplay().getSystemColor(SWT.COLOR_GREEN);
				style.font = JFaceResources.getFontRegistry().getBold(JFaceResources.TEXT_FONT);
			} else {
				style.borderStyle = SWT.BORDER_DASH;
				style.borderColor = getDisplay().getSystemColor(SWT.COLOR_RED);
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
			info.getProperties().put(MemoryEntryInfo.FILENAME, machine.getPathFileLocator().splitFileName(uri).second);
		} else {
			info.getProperties().remove(MemoryEntryInfo.FILENAME);
		}
	}
	
	/**
	 * @param info
	 * @return
	 */
	private boolean isRomAvailable(MemoryEntryInfo info) {
		
		URI uri = findRom(info);
		return uri != null;
	}


	/**
	 * @param info
	 * @return
	 */
	private URI findRom(MemoryEntryInfo info) {
		
		IPathFileLocator locator = machine.getPathFileLocator();
		URI uri = null;
		
		if (info.getFileMD5() != null) {
			uri = locator.findFileByMD5(info.getFileMD5());
		} 
		
		if (uri == null)
			uri = locator.findFile(info.getFilename());
		
		return uri;
			
	}


	private void createPathSelector(final IProperty property) {
		PathSelector pathSelector = new PathSelector(this, window, "ROM directory", property);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(pathSelector);

		final IPropertyListener pathChangeListener = new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (!isDisposed()) {
							scanForRoms();
							setupInfoLabel();
							updateRomAvailability();
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
				return new NewSetupDialog(shell, machine, window, 
						machine.getMemoryModel().getRequiredRomMemoryEntries(),
						machine.getMemoryModel().getOptionalRomMemoryEntries()
						);
			}
			public Behavior getBehavior() {
				return behavior;
			}
		};
	}
		

}
