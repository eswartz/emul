/**
 * 
 */
package v9t9.gui.client.swt;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import v9t9.base.properties.IProperty;
import v9t9.base.properties.IPropertyListener;
import v9t9.common.client.IVideoRenderer;
import v9t9.common.events.IEventNotifier;
import v9t9.engine.hardware.IVdpChip;
import v9t9.gui.image.ImageImport;
import v9t9.gui.video.ImageDataCanvas;

/**
 * @author ejs
 *
 */
public class SwtImageImportSupport extends ImageImportHandler {
	protected IVideoRenderer videoRenderer;
	protected IEventNotifier eventNotifier;
	private Control imageDndControl;
	private IPropertyListener importPropertyListener;
	public SwtImageImportSupport(IEventNotifier eventNotifier, IVideoRenderer videoRenderer) {
		if (eventNotifier == null || videoRenderer == null)
			throw new NullPointerException();
		
		this.eventNotifier = eventNotifier;
		this.videoRenderer = videoRenderer;
	}
	/**
	 * @param shell2
	 * @return
	 */
	public ImageImportOptionsDialog createImageImportDialog(Shell shell) {
		return new ImageImportOptionsDialog(shell, SWT.NONE, 
				this, importPropertyListener);
	}

	public void setImageImportDnDControl(Control control) {
		this.imageDndControl = control;
		if (getVideoRenderer() != null) {
			importPropertyListener = new IPropertyListener() {

				@Override
				public void propertyChanged(IProperty property) {
					if (property == null || !property.isHidden()) {
						// in case, e.g., mode changed
						ImageImport importer = createImageImport();
						try {
							importer.importImage();
						} catch (Throwable t) {
							t.printStackTrace();
						}
					}
				}
			};

			/*imageDragDropHandler =*/ new SwtDragDropHandler(imageDndControl, 
					(ISwtVideoRenderer) getVideoRenderer(), 
					getEventNotifier(),
					this);
		}
	}
	public void addImageImportDnDControl(Control control) {
		if (getVideoRenderer() == null)
			throw new IllegalStateException();
		
		new SwtDragDropHandler(control, 
				(ISwtVideoRenderer) getVideoRenderer(), 
				getEventNotifier(),
				this);
	}
	

	/**
	 * @return
	 */
	protected IEventNotifier getEventNotifier() {
		return eventNotifier;
	}

	/**
	 * @return
	 */
	protected IVideoRenderer getVideoRenderer() {
		return videoRenderer;
	}

	@Override
	protected ImageDataCanvas getCanvas() {
		ISwtVideoRenderer renderer = (ISwtVideoRenderer) getVideoRenderer();
		final ImageDataCanvas canvas = (ImageDataCanvas) renderer.getCanvas();
		return canvas;
	}
	@Override
	protected IVdpChip getVdpHandler() {
		ISwtVideoRenderer renderer = (ISwtVideoRenderer) getVideoRenderer();
		final IVdpChip vdp = renderer.getVdpHandler();
		return vdp;
	}
}
