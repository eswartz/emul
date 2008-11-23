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

import v9t9.emulator.clients.builtin.video.VdpCanvas.ICanvasListener;


/**
 * Render video into an SWT window
 * @author ejs
 *
 */
public class SwtVideoRenderer implements VideoRenderer, ICanvasListener {

	private Shell shell;
	private Canvas canvas;
	private final ImageDataCanvas vdpCanvas;
	private Color bg, fg;

	private int zoom = 2;
	private Image image;
	private Rectangle updateRect;
	private boolean isBlank;
	private boolean wasBlank;
	
	public SwtVideoRenderer(Display display, ImageDataCanvas canvas) {
		shell = new Shell(display);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		shell.setLayout(layout);
		shell.setBounds(800,800,0,0);
		
		this.canvas = new Canvas(shell, SWT.NO_BACKGROUND);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		this.canvas.setLayoutData(gridData);
		this.canvas.setLayout(new FillLayout());

		this.vdpCanvas = canvas;
		//this.vdpCanvas.setListener(this);
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
				// update size if needed
				Point curSize = canvas.getSize();
				if (curSize.x != vdpCanvas.getWidth() * zoom
						|| curSize.y != vdpCanvas.getHeight() * zoom) {
					Point size = new Point(vdpCanvas.getWidth() * zoom, vdpCanvas.getHeight() * zoom);
					canvas.setSize(size);
					
					Rectangle trim = shell.computeTrim(0, 0, size.x, size.y);
					shell.setSize(trim.width, trim.height);
				}
				
				final boolean becameBlank = vdpCanvas.isBlank() && !isBlank;
				isBlank = becameBlank;
				
				Rectangle redrawRect_ = vdpCanvas.getDirtyRect();
				if (becameBlank)
					redrawRect_ = new Rectangle(0, 0, vdpCanvas.width * zoom, vdpCanvas.height * zoom);
				if (redrawRect_ != null) {
					final Rectangle redrawRect = redrawRect_;
					
					Display.getDefault().asyncExec(new Runnable() {
			
						public void run() {
							canvas.redraw(redrawRect.x * zoom, redrawRect.y * zoom, 
									redrawRect.width * zoom, redrawRect.height * zoom, true);
							vdpCanvas.clearDirty();
						}
						
					});
					
				}
			}
		});
	}
	
	public void sync() {
		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				canvas.update();
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.VideoRenderer#resize(int, int)
	 */
	public void resize(int width, int height) {
	}

	public Shell getShell() {
		return shell;
	}

	/*
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
	}*/

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
			wasBlank = false;
		} else {
			if (wasBlank)
				return;
			bg = allocColor(bg, 0);
			gc.setBackground(bg);
			gc.fillRectangle(updateRect);
			wasBlank = true;
		}
		
	}

	public VdpCanvas getCanvas() {
		return vdpCanvas;
	}

	public void setZoom(int zoom) {
		this.zoom = zoom;
		redraw();
	}

	public void canvasDirtied(VdpCanvas canvas) {
		redraw();
	}

	private Color allocColor(Color color, int idx) {
		if (color != null)
			color.dispose();
		byte[] rgb;
		rgb = vdpCanvas.getColorRGB(idx);
		return new Color(shell.getDisplay(), rgb[0] & 0xff, rgb[1] & 0xff, rgb[2] & 0xff);
	}
}
