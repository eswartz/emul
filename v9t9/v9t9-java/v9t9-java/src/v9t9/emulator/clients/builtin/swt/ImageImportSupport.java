/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.awt.image.BufferedImage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.ejs.coffee.core.properties.IProperty;
import org.ejs.coffee.core.properties.IPropertyListener;

import v9t9.emulator.clients.builtin.video.ImageDataCanvas;
import v9t9.emulator.clients.builtin.video.VideoRenderer;
import v9t9.emulator.clients.builtin.video.image.ImageImport;
import v9t9.emulator.clients.builtin.video.image.ImageImportOptions;
import v9t9.emulator.common.IEventNotifier;
import v9t9.engine.VdpHandler;

/**
 * @author ejs
 *
 */
public class ImageImportSupport implements IImageImportHandler {

	private ImageImportOptions imageImportOptions;
	private IPropertyListener importPropertyListener;
	private Control imageDndControl;
	private VideoRenderer videoRenderer;
	private IEventNotifier eventNotifier;
	
	public ImageImportSupport(IEventNotifier eventNotifier, VideoRenderer videoRenderer) {
		if (eventNotifier == null || videoRenderer == null)
			throw new NullPointerException();
		this.eventNotifier = eventNotifier;
		this.videoRenderer = videoRenderer;
	}
	/**
	 * @param shell2
	 * @return
	 */
	public ImageImportDialog createImageImportDialog(Shell shell) {
		return new ImageImportDialog(shell, SWT.NONE, imageImportOptions, importPropertyListener);
	}

	public void setImageImportDnDControl(Control control) {
		this.imageDndControl = control;
		if (getVideoRenderer() != null) {
			final ISwtVideoRenderer renderer = (ISwtVideoRenderer) getVideoRenderer();
			imageImportOptions = new ImageImportOptions();
			importPropertyListener = new IPropertyListener() {

				@Override
				public void propertyChanged(IProperty property) {
					if (property == null || !property.isHidden()) {
						// in case, e.g., mode changed
						final ImageDataCanvas canvas = (ImageDataCanvas) renderer.getCanvas();
						final VdpHandler vdp = renderer.getVdpHandler();
						
						ImageImport importer = new ImageImport(canvas, vdp);
						importer.importImage(imageImportOptions);
					}
				}
			};

			/*imageDragDropHandler =*/ new SwtDragDropHandler(imageDndControl, 
					(ISwtVideoRenderer) getVideoRenderer(), 
					getEventNotifier(),
					this);
		}
	}
	/**
	 * @return
	 */
	private IEventNotifier getEventNotifier() {
		return eventNotifier;
	}

	/**
	 * @return
	 */
	private VideoRenderer getVideoRenderer() {
		return videoRenderer;
	}

	public void addImageImportDnDControl(Control control) {
		if (getVideoRenderer() == null)
			throw new IllegalStateException();
		
		new SwtDragDropHandler(control, 
				(ISwtVideoRenderer) getVideoRenderer(), 
				getEventNotifier(),
				this);
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.IImageImportHandler#importImage(java.awt.image.BufferedImage, boolean)
	 */
	@Override
	public void importImage(BufferedImage image, boolean isLowColor) {
		ISwtVideoRenderer renderer = (ISwtVideoRenderer) getVideoRenderer();
		final ImageDataCanvas canvas = (ImageDataCanvas) renderer.getCanvas();
		final VdpHandler vdp = renderer.getVdpHandler();
		
		ImageImport importer = new ImageImport(canvas, vdp);
		
		imageImportOptions.updateFrom(canvas, vdp, image, isLowColor);
		
		importer.importImage(imageImportOptions);
		
	}
}
