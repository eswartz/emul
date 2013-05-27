/**
 * 
 */
package v9t9.gui.client.swt.wizards;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import v9t9.common.InternetDefinitions;
import v9t9.common.machine.IMachine;
import v9t9.gui.client.swt.BrowserUtils;
import v9t9.gui.client.swt.StyledTextHelper;

/**
 * @author ejs
 *
 */
public class SetupIntroPage extends WizardPage {

	private static final boolean ADVANCED = false;
	private IMachine machine;

	/**
	 * @param pageName
	 */
	protected SetupIntroPage(IMachine machine) {
		super("intro");
		this.machine = machine;
		setTitle("Introduction");
//		setDescription("This appears to be the first launch of V9t9 on your system.");
	}
	
	private IdentityHashMap<StyleRange, LinkInfo> linkMap;
	private StyledText label;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayoutFactory.fillDefaults().margins(6, 6).applyTo(composite);
		
		linkMap = new IdentityHashMap<StyleRange, LinkInfo>();

		label = createInfoSection(composite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(label);
		
		setupHeaderLabel();
		setupFooterLabel();

		setControl(composite);

	}
	
	private void setupHeaderLabel() {
		// clear
		label.replaceTextRange(0, label.getCharCount(), "");
		linkMap.clear();
		
		StyledTextHelper styledTextHelper = new StyledTextHelper(label);

		styledTextHelper.pushStyle(new TextStyle(JFaceResources.getHeaderFont(), 
				getDisplay().getSystemColor(SWT.COLOR_BLUE),
				null), SWT.BOLD);
		
		label.append(
			"Welcome to V9t9!\n\n");
		
		styledTextHelper.popStyle();
		
		label.append(
				"In order to emulate the " + machine.getModel().getName() + ", you need to configure V9t9 so it can "+
				"find the necessary ROMs for the system and for any modules you want to use.\n\n"+
				"Add new Search Locations (on the next page), and V9t9 will detect ROMs and modules by content. " +
				(ADVANCED ? "If you have custom ROMs, though, you can select your own file by clicking the links.\n" : "\n"));

	}
	private void setupFooterLabel() {
		StyledTextHelper styledTextHelper = new StyledTextHelper(label);

		styledTextHelper.pushStyle(new TextStyle(), SWT.ITALIC);

		label.append("\n"+
			"Note: copyrighted ROMs are not distributed with V9t9 itself.\n\n"+
			"Please see ");
		
		// SWT.UNDERLINE_LINK does not appear to work as promised :p
		TextStyle urlSt = new TextStyle(
				JFaceResources.getFontRegistry().getItalic(JFaceResources.TEXT_FONT),
				getDisplay().getSystemColor(SWT.COLOR_BLUE), null);

		urlSt.underline = true;
		urlSt.underlineColor = getDisplay().getSystemColor(SWT.COLOR_BLUE);

		final StyleRange urlStyle = styledTextHelper.pushStyle(urlSt, SWT.ITALIC);

		label.append(InternetDefinitions.sV9t9RomsURL);

		styledTextHelper.popStyle();
		
		registerLink(urlStyle, 
			new ILinkHandler() {
				public void linkClicked() {
					BrowserUtils.openURL(InternetDefinitions.sV9t9RomsURL);
				}
		});
		
		label.append(" for information on ROMs.\n\n");
		label.append("Or, exit this wizard and use the 'Demo' button to see how V9t9 behaves.");
		
		styledTextHelper.popStyle();
	}
	
	/**
	 * @param parent 
	 * @return 
	 * 
	 */
	private StyledText createInfoSection(final Composite parent) {
		final StyledText text = new StyledText(parent, SWT.BORDER | SWT.WRAP | SWT.READ_ONLY | SWT.V_SCROLL);
				
		text.setCaret(null);
		
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
	private boolean isOverLink(final StyleRange urlStyle, MouseEvent e) {
		try {
			int offs = label.getOffsetAtLocation(new Point(e.x, e.y));
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
	
}
