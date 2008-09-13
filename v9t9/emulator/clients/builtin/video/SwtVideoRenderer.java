/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;


/**
 * Render video into an SWT window
 * @author ejs
 *
 */
public class SwtVideoRenderer implements VideoRenderer {

	private Shell shell;
	private Canvas canvas;
	private final ImageDataCanvas vdpCanvas;
	private Color bg, fg;

	private int zoom = 2;
	private Image image;
	private Rectangle updateRect;
	private boolean isBlank;
	
	public SwtVideoRenderer(Display display, ImageDataCanvas canvas) {
		shell = new Shell(display);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		shell.setLayout(layout);
		shell.setBounds(800,800,0,0);
		
		this.canvas = new Canvas(shell, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		this.canvas.setLayoutData(gridData);
		this.canvas.setLayout(new FillLayout());

		this.vdpCanvas = canvas;
		
		this.updateRect = new Rectangle(0, 0, 0, 0);
		
		// the canvas collects update regions in a big rect
		this.canvas.addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				updateRect = updateRect.union(new Rectangle(e.x, e.y, e.width, e.height));
				if (e.count == 0) {
					repaint(e.gc, updateRect);
					updateRect = new Rectangle(0, 0, 0, 0);
				}
			}
			
		});
		shell.open();
	}

	/** Force a redraw and repaint of the entire canvas */
	public void redraw() {
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				canvas.redraw();
			}
		});
	}
	
	public void sync() {
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				canvas.update();
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.VideoRenderer#resize(int, int)
	 */
	public void resize(int width, int height) {
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				Point size = new Point(vdpCanvas.getWidth() * zoom, vdpCanvas.getHeight() * zoom);
				canvas.setSize(size);
				
				Rectangle trim = shell.computeTrim(0, 0, size.x, size.y);
				shell.setSize(trim.width, trim.height);
			}
			
		});
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.VideoRenderer#setForegroundAndBackground(int, int)
	 */
	public void setForegroundAndBackground(int bg, int fg) {
		this.bg = allocColor(this.bg, bg);
		this.fg = allocColor(this.fg, fg);
	}

	private Color allocColor(Color color, int idx) {
		if (color != null)
			color.dispose();
		byte[] rgb;
		rgb = vdpCanvas.getColorRGB(idx);
		return new Color(shell.getDisplay(), rgb[0] & 0xff, rgb[1] & 0xff, rgb[2] & 0xff);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.VideoRenderer#setBlank(int)
	 */
	public void setBlank(boolean blank) {
		isBlank = blank;
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				canvas.redraw();
			}
		});
	}

	public Shell getShell() {
		return shell;
	}

	public void updateList(RedrawBlock[] blocks, int count) {
		//final Region region = new Region(shell.getDisplay());
		Rectangle redrawRect_ = null;
		int nblocks = 0;
		for (int idx = 0; idx < count; idx++) {
			final RedrawBlock block = blocks[idx];
			//region.add(new Rectangle(block.c, block.r, block.w, block.h));
			Rectangle blockRect = new Rectangle(block.c, block.r, block.w, block.h);
			if (redrawRect_ == null)
				redrawRect_ = blockRect;
			else
				redrawRect_ = redrawRect_.union(blockRect);
			nblocks++;
		}
		
		// queue redraw
		if (redrawRect_ != null) {
			//final Rectangle redrawRect = region.getBounds();
			final Rectangle redrawRect = redrawRect_;
			//region.dispose();
			
			//if (nblocks > 0) 
			//	System.out.println("Redrew " + nblocks + " blocks to " + redrawRect);
			
			Display.getDefault().asyncExec(new Runnable() {
	
				public void run() {
					canvas.redraw(redrawRect.x * zoom, redrawRect.y * zoom, 
							redrawRect.width * zoom, redrawRect.height * zoom, true);
				}
				
			});
		}
	}

	protected void repaint(GC gc, Rectangle updateRect) {
		ImageData imageData = vdpCanvas.getImageData();
		if (!isBlank && imageData != null) {
			if (image != null && !image.isDisposed()) {
				image.dispose();
			}
			image = new Image(shell.getDisplay(), imageData);
			
			Rectangle destRect = updateRect;
			
			destRect = destRect.intersection(new Rectangle(0, 0, 
					vdpCanvas.getWidth() * zoom, vdpCanvas.getHeight() * zoom));
			Rectangle imageRect = new Rectangle(destRect.x / zoom, destRect.y / zoom, 
					destRect.width / zoom, destRect.height / zoom);
			imageRect = vdpCanvas.mapVisible(imageRect);
			
			gc.drawImage(image, imageRect.x, imageRect.y, imageRect.width, imageRect.height, 
					destRect.x, destRect.y, destRect.width, destRect.height);
		} else if (bg != null) {
			gc.setBackground(bg);
			gc.fillRectangle(updateRect);
		}
		
	}

	public VdpCanvas getCanvas() {
		return vdpCanvas;
	}

	public void setZoom(int zoom) {
		this.zoom = zoom;
		resize(vdpCanvas.getWidth(), vdpCanvas.getHeight());
	}


}
