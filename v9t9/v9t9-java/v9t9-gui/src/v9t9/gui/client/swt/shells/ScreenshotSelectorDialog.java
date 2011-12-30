package v9t9.gui.client.swt.shells;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import v9t9.common.client.IVideoRenderer;
import v9t9.common.video.IVdpCanvas;
import v9t9.gui.client.swt.ISwtVideoRenderer;
import v9t9.gui.client.swt.imageimport.ImageClipDecorator;
import v9t9.gui.client.swt.imageimport.ImageLabel;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.settings.DialogSettingsWrapper;
import ejs.base.settings.ISettingSection;
import ejs.base.settings.SettingProperty;

/**
 * Not used -- UI is too complex 
 * @author ejs
 *
 */
final class ScreenshotSelectorDialog extends Dialog {
	private static final String SECTION_SCREEN_SHOTS = "ScreenShots";
	private ImageLabel imageLabel;
	private IProperty clipProperty = new SettingProperty("clip", new java.awt.Rectangle());
	private Image screenshot;
	private ImageLabel renderedImageLabel;
	private Image renderedImage;
	private final IVideoRenderer videoRenderer;
	private final ISettingSection dialogSettings;

	private ScreenshotSelectorDialog(Shell parentShell, ISettingSection dialogSettings, IVideoRenderer videoRenderer) {
		super(parentShell);
		this.dialogSettings = dialogSettings;
		this.videoRenderer = videoRenderer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#setShellStyle(int)
	 */
	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(newShellStyle | SWT.RESIZE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
	 */
	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return new DialogSettingsWrapper(dialogSettings.findOrAddSection(SECTION_SCREEN_SHOTS));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(400, 300);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		
		Composite sideBySide = new Composite(composite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(sideBySide);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(sideBySide);
		
		imageLabel = new ImageLabel(sideBySide, SWT.BORDER);
		IVdpCanvas canvas = videoRenderer.getCanvas();
		GridDataFactory.fillDefaults().grab(true, true)
					.hint(canvas.getVisibleWidth(), canvas.getVisibleHeight())
					.applyTo(imageLabel);

		Label sep = new Label(sideBySide, SWT.VERTICAL | SWT.SHADOW_IN);
		GridDataFactory.fillDefaults().grab(false, true).applyTo(sep);
		
		renderedImageLabel = new ImageLabel(sideBySide, SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER)
			.grab(false, false)
			.minSize(32, 32).hint(32, 32)
			.applyTo(renderedImageLabel);
		
		renderedImage = new Image(composite.getDisplay(), 32, 32);
		renderedImageLabel.setImage(renderedImage);
		
		screenshot = new Image(composite.getDisplay(), ((ISwtVideoRenderer) videoRenderer).getScreenshotImageData());
		imageLabel.setImage(screenshot);
		
		updateRenderedImage();
		
		new ImageClipDecorator(imageLabel, clipProperty, new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				updateRenderedImage();						
			}
		}, new ImageClipDecorator.IBoundsUpdater() {
			public Rectangle update(Rectangle rect) {
				int sz = Math.max(rect.width, rect.height);
				// make power-of-two
				while ((sz & (sz - 1)) != 0)
					sz &= (sz - 1);
				rect.width = rect.height = sz;
				
				return rect;
			}
		});
		
		composite.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				screenshot.dispose();
			}
		});
		
		Label message = new Label(composite, SWT.NONE | SWT.WRAP);
		message.setText("Select a portion of the screenshot to use as the module icon.");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(message);
		
		return composite;
	}

	private void updateRenderedImage() {
		java.awt.Rectangle rect = (java.awt.Rectangle) clipProperty.getValue();
		if (rect == null || rect.isEmpty()) {
			Rectangle bounds = screenshot.getBounds();
			rect = new java.awt.Rectangle(0, 0, bounds.width, bounds.height);
		}
		
		GC gc = new GC(renderedImage);
		Rectangle rbounds = renderedImage.getBounds();
		gc.drawImage(screenshot, rect.x, rect.y, rect.width, rect.height, 0, 0, rbounds.width, rbounds.height);
		gc.dispose();
		
		renderedImageLabel.redraw();
	}
}