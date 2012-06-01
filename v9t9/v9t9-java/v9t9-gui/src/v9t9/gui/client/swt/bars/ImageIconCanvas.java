/**
 * 
 */
package v9t9.gui.client.swt.bars;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;

/**
 * An image, which is allowed to be transparent and drawn on a bar.
 * @author ejs
 *
 */
public abstract class ImageIconCanvas extends ImageBarChild {


	protected ImageIconInfo imageIconInfo;
	protected Rectangle bounds;
	protected final ImageProvider imageProvider;
	public ImageIconCanvas(IImageBar parentDrawer, int style, 
			ImageProvider imageProvider, int iconIndex, String tooltip) {
		super(parentDrawer,  style /*| SWT.NO_BACKGROUND*/);
		
		this.imageProvider = imageProvider;
		
		if (imageProvider != null)
			imageIconInfo = new ImageIconInfo(imageProvider);
		else
			imageIconInfo = null;
		
		setLayout(new FillLayout());
		
		if (tooltip != null)
			setToolTipText(tooltip);
		
		setIconIndex(iconIndex);
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
		super.doPaint(e);
		drawImage(e);
	}

	/**
	 * @param e
	 */
	protected void drawImage(PaintEvent e) {
		Rectangle drawRect = getBounds();
		try {
			imageProvider.drawImage(e.gc, drawRect, bounds);
		} catch (IllegalArgumentException e2) {
			e2.printStackTrace();
		}
	}
}