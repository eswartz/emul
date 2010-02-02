/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

/**
 * This layout ensures that the aspect ratio remains consistent with the provided
 * width and height, allowing for zoom.
 * @author ejs
 *
 */
public class FixedAspectLayout extends Layout {

	private int w;
	private int h;
	private double zoomx;
	private double zoomy;
	private double aspect;

	public FixedAspectLayout(int w, int h, double zoomx, double zoomy) {
		this.w = w;
		this.h = h;
		this.zoomx = 1.0;
		this.zoomy = 1.0;
		this.aspect = (double) w / h;
	}
	
	public int getHeight() {
		return h;
	}
	
	public int getWidth() {
		return w;
	}

	public void setSize(int w, int h) {
		this.w = w;
		this.h = h;
		this.aspect = (double) w / h;
	}
	
	public void setAspect(double aspect) {
		this.aspect = aspect;
	}
	
	public double getZoomX() {
		return zoomx;
	}
	
	public double getZoomY() {
		return zoomy;
	}
	
	@Override
	protected Point computeSize(Composite composite, int wHint, int hHint,
			boolean flushCache) {
		Rectangle area = composite.getClientArea();
		Rectangle bounds = composite.getParent().getClientArea();
		System.out.println("cursize: " + area + " vs " +bounds);
		
		int neww, newh;
		if (wHint == SWT.DEFAULT) {
			neww = fixup(area.width, bounds.width, w);
		} else {
			neww = fixup(wHint, wHint, w);
		}
		if (hHint == SWT.DEFAULT) {
			newh = fixup(area.height, bounds.height, h);
		} else {
			newh = fixup(hHint, hHint, h);
		}
		
		if (neww < newh * aspect) {
			newh = (int) (newh / aspect);
		}
		else if (neww > newh * aspect) {
			neww = (int) (newh * aspect);
		}
		
		Point desired = new Point(neww, newh);
		
		System.out.println("desired at " + desired);
		
		return desired;
	}

	private int fixup(int hint, int max, int base) {
		// get the hint close to a multiple of base
		int val = base >> 1;
		if (val * 2 > max)
			return val;
		
		return (max / base) * base;
	}
	
	@Override
	protected void layout(Composite composite, boolean flushCache) {
		Rectangle area = composite.getClientArea();
		System.out.println("layout at " + area);
		zoomx = (double) area.width / w;
		if (zoomx < 1)
			zoomx = 0.5;
		else
			zoomx = (int) Math.round(zoomx);
		zoomy = (double) area.height / h;
		if (zoomy < 1)
			zoomy = 0.5;
		else
			zoomy = (int)  Math.round(zoomy);
	}

	/**
	 * @param zoom
	 */
	public void setZoomX(double zoom) {
		this.zoomx = zoom;
	}
	public void setZoomY(double zoom) {
		this.zoomy = zoom;
	}
	
}