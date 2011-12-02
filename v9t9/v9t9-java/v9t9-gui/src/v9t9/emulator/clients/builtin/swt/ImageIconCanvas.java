/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;

/**
 * An image, which is allowed to be transparent and drawn on a bar.
 * @author ejs
 *
 */
public abstract class ImageIconCanvas extends Canvas {

	protected IImageBar parentDrawer;
	protected ImageIconInfo imageIconInfo;
	protected Rectangle bounds;
	protected final ImageProvider imageProvider;
	
	public ImageIconCanvas(IImageBar parentDrawer, int style, 
			ImageProvider imageProvider, int iconIndex, String tooltip) {
		super(parentDrawer.getComposite(),  style /*| SWT.NO_BACKGROUND*/);
		this.imageProvider = imageProvider;
		
		if (imageProvider != null)
			imageIconInfo = new ImageIconInfo(imageProvider);
		else
			imageIconInfo = null;
		
		this.parentDrawer = parentDrawer;

		setLayout(new FillLayout());
		
		if (tooltip != null)
			setToolTipText(tooltip);
		
		setIconIndex(iconIndex);
		
		addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				doPaint(e);
			}
			
		});
	}


	/**
	 * @param iconIndex
	 */
	public void setIconIndex(int iconIndex) {
		
		GridData data;
		if (imageIconInfo != null) {
			imageIconInfo.setIconIndex(iconIndex);
			bounds = imageIconInfo.getBounds();
			data = new GridData(bounds.width, bounds.height);
			data.grabExcessHorizontalSpace = false;
			data.grabExcessVerticalSpace = false;
		} else {
			bounds = null;
			data = GridDataFactory.fillDefaults().grab(true, false).create();
		}
		setLayoutData(data);
	}

	protected void doPaint(PaintEvent e) {
		Rectangle drawRect = getBounds();
		drawRect.x = drawRect.y = 0;
		if (parentDrawer != null)
			parentDrawer.drawBackground(e.gc);
		try {
			imageProvider.drawImage(e.gc, drawRect, bounds);
		} catch (IllegalArgumentException e2) {
			e2.printStackTrace();
		}
	}
	
}