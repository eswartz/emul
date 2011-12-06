/**
 * 
 */
package v9t9.gui.client.swt;

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
	private final double quantum;
	private final double maxforquantum;
	private boolean fullScreen;

	public FixedAspectLayout(int w, int h, double zoomx, double zoomy, double quantum, double max) {
		this.w = w;
		this.h = h;
		this.quantum = quantum;
		this.maxforquantum = max;
		this.zoomx = zoomx;
		this.zoomy = zoomy;
		this.aspect = (double) w / h;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("Layout for %d x %d is zoom %g x %g; fullscreen=%s", w, h, zoomx, zoomy, fullScreen);
	}

	
	
	public boolean isFullScreen() {
		return fullScreen;
	}

	public void setFullScreen(boolean fullScreen) {
		this.fullScreen = fullScreen;
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
	
	public Point computeSize(Composite composite) {
		return computeSize(composite, SWT.DEFAULT, SWT.DEFAULT, false);
	}
	@Override
	protected Point computeSize(Composite composite, int wHint, int hHint,
			boolean flushCache) {
		Rectangle bounds = composite.getParent().getClientArea();
		//System.out.println("bounds: " +bounds);
		
		int neww, newh;
		if (fullScreen) {		// don't check current shell's fullscreen state -- may be async!
			neww = bounds.width;
			newh = bounds.height;
		}
		else {
			if (wHint == SWT.DEFAULT) {
				neww = fixup(bounds.width, w);
			} else {
				neww = fixup(wHint, w);
			}
			if (hHint == SWT.DEFAULT) {
				newh = fixup(bounds.height, h);
			} else {
				newh = fixup(hHint, h);
			}
		}
		
		if (neww < newh * aspect) {
			newh = (int) (neww / aspect);
		}
		else if (neww > newh * aspect) {
			neww = (int) (newh * aspect);
		}
		
		Point desired = new Point(neww, newh);
		
		//System.out.println("desired at " + desired);
		
		return desired;
	}

	private int fixup(int max, int base) {
		// get the hint close to a multiple of base
		int q = base;
		if (max / base <= maxforquantum)
			q = (int) (base * quantum);
		int val = q >> 1;
		if (val * 2 > max)
			return val;
		
		return (max / q) * q;
	}
	
	@Override
	protected void layout(Composite composite, boolean flushCache) {
		Rectangle area = composite.getBounds();
		//System.out.println("layout at " + area);
		zoomx = (double) area.width / w;
		if (zoomx < 1)
			zoomx = 0.5;
		else
			zoomx = (int) (Math.round(zoomx / quantum) * quantum);
		zoomy = (double) area.height / h;
		if (zoomy < 1)
			zoomy = 0.5;
		else
			zoomy = (int) (Math.round(zoomy / quantum) * quantum);
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