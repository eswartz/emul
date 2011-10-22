/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

/**
 * An image, which is allowed to be transparent and drawn on a bar.
 * @author ejs
 *
 */
public abstract class ImageIconCanvas extends Canvas {

	public interface IImageBar {
		Composite getComposite();
		void drawBackground(GC gc, ImageIconCanvas imageButton, Point offset, Point size);
		IFocusRestorer getFocusRestorer();
		boolean isHorizontal();
		void redrawAll();
	}
	protected final Rectangle bounds;
	protected IImageBar parentDrawer;
	protected final ImageProvider imageProvider;
	
	public ImageIconCanvas(IImageBar parentDrawer, int style, 
			ImageProvider imageProvider, int iconIndex, String tooltip) {
		super(parentDrawer.getComposite(),  style /*| SWT.NO_BACKGROUND*/);
		this.imageProvider = imageProvider;
		
		this.parentDrawer = parentDrawer;
		
		GridData data;
		if (imageProvider != null) {
			this.bounds = imageProvider.imageIndexToBounds(iconIndex);
			data = new GridData(bounds.width, bounds.height);
			//data.minimumHeight = 8;	// the minimums above override this
			//data.minimumWidth = 8;	// the minimums above override this
			data.grabExcessHorizontalSpace = false;
			data.grabExcessVerticalSpace = false;
		} else {
			bounds = null;
			data = GridDataFactory.fillDefaults().grab(true, false).create();
		}
		setLayoutData(data);
		setLayout(new FillLayout());
		
		if (tooltip != null)
			setToolTipText(tooltip);
		
		addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				doPaint(e);
			}
			
		});
	}


	protected abstract void doPaint(PaintEvent e);
	
}