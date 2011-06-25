/**
 * Mar 11, 2011
 */
package v9t9.emulator.clients.builtin.swt;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.ejs.coffee.core.properties.IProperty;
import org.ejs.coffee.core.properties.IPropertyListener;

/**
 * @author ejs
 *
 */
public class ImageDeviceIndicator extends ImageIconCanvas {

	private Rectangle overlayBounds;
	private final IDeviceIndicatorProvider provider;
	private IPropertyListener listener;

	public ImageDeviceIndicator(IImageBar parentDrawer, int style,
			ImageProvider imageProvider, IDeviceIndicatorProvider provider) {
		super(parentDrawer, style, imageProvider, provider.getBaseIconIndex(), provider.getToolTip());
		setOverlayBounds(imageProvider.imageIndexToBounds(provider.getActiveIconIndex()));
		this.provider = provider;
		listener = new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (!isDisposed())
							redraw();
					}
				});
			}
		};
		provider.getActiveProperty().addListener(listener);
		

		getParent().addControlListener(new ControlListener() {

			public void controlMoved(ControlEvent e) {
			}

			public void controlResized(ControlEvent e) {
				Rectangle bounds = getParent().getClientArea();
				int height = bounds.height;
				Rectangle metrics = new Rectangle(bounds.x + (bounds.width - height), bounds.y,
						height, height);
				//System.out.println(metrics);
				setBounds(metrics);				
			}
			
		});
	}

	public void dispose() {
		provider.getActiveProperty().removeListener(listener);
		
		super.dispose();
	}

	public void setOverlayBounds(Rectangle overlayBounds) {
		this.overlayBounds = overlayBounds; 
	}

	protected void doPaint(PaintEvent e) {
		//Rectangle bounds = getClientArea();
		//Point size = new Point(bounds.width, bounds.height);
		Point size = getSize();
		this.parentDrawer.drawBackground(e.gc, this, new Point(0, 0), size);
		int offset = 0;
		try {
			//System.out.println(bounds);
			imageProvider.drawImage(e.gc, size, bounds, offset, offset);
			if (overlayBounds != null && provider.getActiveProperty().getBoolean()) {
				imageProvider.drawImage(e.gc, size, overlayBounds, offset, offset);
			}
		} catch (IllegalArgumentException e2) {
			e2.printStackTrace();
		}
	}

}
