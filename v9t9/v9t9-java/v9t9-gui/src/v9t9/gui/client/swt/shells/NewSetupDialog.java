/**
 * 
 */
package v9t9.gui.client.swt.shells;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import ejs.base.properties.IProperty;
import ejs.base.settings.ISettingSection;

import v9t9.common.InternetDefinitions;
import v9t9.common.files.DataFiles;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import v9t9.gui.client.swt.BrowserUtils;
import v9t9.gui.client.swt.SwtWindow;

/**
 * This tool shell shows up when a new configuration has
 * been detected and there are some ROM loading failures.
 * @author ejs
 *
 */
public class NewSetupDialog extends Composite {
	public static final String NEW_SETUP_TOOL_ID = "new.setup";

	private ISettingSection dialogSettings;

	private SwtWindow window;

	private IMachine machine;

	private final IProperty[] requiredRomNames;

	private IProperty pauseProperty;

	private boolean wasPaused;

	private StyledText infoLabel;

	private StyleRange[] reqdRomNameStyleRanges;

	private final IProperty[] optionalRomNames;

	private StyleRange[] optionalRomNameStyleRanges; 

	public NewSetupDialog(Shell shell, IMachine machine, SwtWindow window,
			IProperty[] requiredRomNames, IProperty[] optionalRomNames) {
		super(shell, SWT.NONE);
		
		setSize(500, 400);
		
		this.window = window;
		this.requiredRomNames = requiredRomNames;
		this.optionalRomNames = optionalRomNames;
		
		shell.setText("New Setup");
		
		this.machine = machine;
		
		pauseProperty = Settings.get(machine, IMachine.settingPauseMachine);
		wasPaused = pauseProperty.getBoolean();
		pauseProperty.setBoolean(true);
		
		GridLayoutFactory.fillDefaults().applyTo(this);
		
		createInfoSection();

		createPathSelector(Settings.get(machine, DataFiles.settingBootRomsPath));
		createPathSelector(Settings.get(machine, DataFiles.settingUserRomsPath));
		
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
		GridDataFactory.fillDefaults().grab(true, false).indent(8, 8).applyTo(infoLabel);
		setupInfoLabel();
	}

	
	private void setupInfoLabel() {
		// clear
		infoLabel.replaceTextRange(0, infoLabel.getCharCount(), "");
		
		infoLabel.append(
			"Welcome to V9t9!\n\n"+
				"In order to emulate the TI-99/4A, you need to configure V9t9 so it can "+
				"find the necessary ROMs.\n\n"+
				"V9t9 can detect ROMs by contents or by filename (if you have customizations).\n\n"+
				"These ROMs are required:\t");

		reqdRomNameStyleRanges = new StyleRange[requiredRomNames.length];
		
		for (int i = 0; i < requiredRomNames.length; i++) {
			StyleRange range = new StyleRange();
			range.start = infoLabel.getCharCount();
			infoLabel.append(requiredRomNames[i].getString());
			range.length = infoLabel.getCharCount() - range.start;
			infoLabel.append(" ");
			reqdRomNameStyleRanges[i] = range;
			infoLabel.setStyleRange(range);
		}
		
		infoLabel.append(
				"\n\nThese ROMs are optional:\t");
				
		optionalRomNameStyleRanges = new StyleRange[optionalRomNames.length];

		for (int i = 0; i < optionalRomNames.length; i++) {
			StyleRange range = new StyleRange();
			range.start = infoLabel.getCharCount();
			infoLabel.append(optionalRomNames[i].getString());
			range.length = infoLabel.getCharCount() - range.start;
			infoLabel.append(" ");
			optionalRomNameStyleRanges[i] = range;
			infoLabel.setStyleRange(range);
		}
		
		int italicOffset = infoLabel.getCharCount();
		infoLabel.append("\n\n"+
			"Note: copyrighted ROMs are not distributed with V9t9 itself.  "+
			"Please see ");
		
		int italicRangeLength = infoLabel.getCharCount() - italicOffset;
		StyleRange italicStyle;
		
		italicStyle = new StyleRange(italicOffset, italicRangeLength, 
				null, null, SWT.ITALIC);
		infoLabel.setStyleRange(italicStyle);
		
		int urlOffset = infoLabel.getCharCount();
		
		infoLabel.append(InternetDefinitions.sV9t9WikiURL);
		int urlLength = infoLabel.getCharCount() - urlOffset;
		
		// SWT.UNDERLINE_LINK does not appear to work as promised :p
		StyleRange urlStyle = new StyleRange(urlOffset, urlLength, 
				getDisplay().getSystemColor(SWT.COLOR_BLUE), null);
		urlStyle.font = JFaceResources.getFontRegistry().getItalic(JFaceResources.TEXT_FONT);
		
		infoLabel.setStyleRange(urlStyle);
		infoLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				BrowserUtils.openURL(InternetDefinitions.sV9t9WikiURL);
			}
		});

		italicOffset = infoLabel.getCharCount();
		
		infoLabel.append(" for information on ROMs.");
		
		italicRangeLength = infoLabel.getCharCount() - italicOffset;
		
		italicStyle = new StyleRange(italicOffset, italicRangeLength, 
				null, null, SWT.ITALIC);
		infoLabel.setStyleRange(italicStyle);
	}

	protected void updateRomAvailability() {
		updateRomLabels(requiredRomNames, reqdRomNameStyleRanges);
		updateRomLabels(optionalRomNames, optionalRomNameStyleRanges);
		
	}
	
	private void updateRomLabels(IProperty[] properties, StyleRange[] styleRanges) {
		for (int i = 0; i < properties.length; i++) {
			StyleRange style = styleRanges[i];
			if (isRomAvailable(properties[i].getString())) {
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
	 * @param string
	 * @return
	 */
	private boolean isRomAvailable(String string) {
		return machine.getPathFileLocator().findFile(string) != null;
	}


	private void createPathSelector(IProperty property) {
		//PathSelector pathSelector = new PathSelector(this, property);
		//GridDataFactory.fillDefaults().grab(true, true).applyTo(pathSelector);
	}

	public static IToolShellFactory getToolShellFactory(final IMachine machine,
			final SwtWindow window) {
		 return new IToolShellFactory() {
			Behavior behavior = new Behavior();
			{
				behavior.boundsPref = "NewConfigBounds";
				behavior.centering = Centering.CENTER;
				behavior.centerOverControl = window.getShell();
				behavior.dismissOnClickOutside = false;
			}
			public Control createContents(Shell shell) {
				return new NewSetupDialog(shell, machine, window, 
						machine.getMemoryModel().getRequiredRomProperties(),
						machine.getMemoryModel().getOptionalRomProperties()
						);
			}
			public Behavior getBehavior() {
				return behavior;
			}
		};
	}
		

}
