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
	private Color bg;

	// global zoom
	private int zoom = 2;
	// zoom based on the resolution
	private int zoomx = 2, zoomy = 2;
	private Image image;
	private Rectangle updateRect;
	private boolean isBlank;
	private boolean wasBlank;
	private boolean isDirty;
	
	public SwtVideoRenderer(Display display, VdpCanvas canvas) {
		shell = new Shell(display);
		
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		shell.setLayout(layout);
		shell.setBounds(800,800,0,0);
		
		this.canvas = new Canvas(shell, SWT.NO_BACKGROUND);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		this.canvas.setLayoutData(gridData);
		this.canvas.setLayout(new FillLayout());

		this.vdpCanvas = (ImageDataCanvas) canvas;
		this.vdpCanvas.setListener(this);		
		this.updateRect = new Rectangle(0, 0, 0, 0);
		
		// the canvas collects update regions in a big rect
		this.canvas.addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				if (updateRect.isEmpty()) {
					updateRect.x = e.x;
					updateRect.y = e.y;
					updateRect.width = e.width;
					updateRect.height = e.height;
				}
				else {
					updateRect.add(new Rectangle(e.x, e.y, e.width, e.height));
				}
				if (e.count == 0) {
					//System.out.println(updateRect);
					repaint(e.gc, updateRect);
					updateRect.width = updateRect.height = 0;
				}
			}
			
		});
		shell.open();
	}

	protected Rectangle logicalToPhysical(Rectangle logical) {
		return logicalToPhysical(logical.x, logical.y, logical.width, logical.height);
	}
	
	protected Rectangle logicalToPhysical(int x, int y, int w, int h) {
		return new Rectangle(x * zoomx, y * zoomy, w * zoomx, h * zoomy);
	}
	
	protected Rectangle physicalToLogical(Rectangle physical) {
		return new Rectangle(physical.x / zoomx, physical.y / zoomy, physical.width / zoomx, physical.height / zoomy);
	}

	public void redraw() {
		if (!isDirty)
			return;
		
		boolean becameBlank = vdpCanvas.isBlank() && !isBlank;
		isBlank = vdpCanvas.isBlank();
		
		Rectangle redrawRect_ = vdpCanvas.getDirtyRect();
		if (becameBlank)
			redrawRect_ = new Rectangle(0, 0, vdpCanvas.width, vdpCanvas.height);
			
		if (redrawRect_ != null) {
			final Rectangle redrawRect = redrawRect_;
			
			Display.getDefault().asyncExec(new Runnable() {
	
				public void run() {
					if (canvas.isDisposed())
						return;
					
					// update size if needed
					updateZoomFactor();
					
					Point curSize = canvas.getSize();
					
					Rectangle targetRect = logicalToPhysical(0, 0, vdpCanvas.getWidth(), vdpCanvas.getHeight());
					
					if (curSize.x != targetRect.width
							|| curSize.y != targetRect.height) {
						Point size = new Point(targetRect.width, targetRect.height);
						canvas.setSize(size);
						
						Rectangle trim = shell.computeTrim(0, 0, size.x, size.y);
						shell.setSize(trim.width, trim.height);
					}
					
					Rectangle redrawPhys = logicalToPhysical(redrawRect);
					canvas.redraw(redrawPhys.x, redrawPhys.y, 
							redrawPhys.width, redrawPhys.height, false);
					
					isDirty = false;
				}
				
			});
			
		}
	}
	
	protected void updateZoomFactor() {
		if (vdpCanvas.getWidth() > 256) {
			zoomx = zoom / 2;
		} else {
			zoomx = zoom;
		}
		if (vdpCanvas.getHeight() > 212) {
			zoomy = zoom / 2;
		} else {
			zoomy = zoom;
		}
	}

	public void sync() {
		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				if (canvas.isDisposed())
					return;
				canvas.update();
			}
		});
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
					if (canvas.isDisposed())
						return;
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
			
			//System.out.println(updateRect);
			synchronized (vdpCanvas) {
				image = new Image(shell.getDisplay(), imageData);
			}
			
			Rectangle destRect = updateRect;
			
			destRect = destRect.intersection(logicalToPhysical(0, 0, vdpCanvas.getWidth(), vdpCanvas.getHeight()));
			
			Rectangle imageRect = physicalToLogical(destRect);
			imageRect = vdpCanvas.mapVisible(imageRect);
			
			gc.drawImage(image, 
					imageRect.x, imageRect.y, 
					imageRect.width, imageRect.height, 
					destRect.x, destRect.y, 
					destRect.width, destRect.height);
			wasBlank = false;
		} else {
			if (wasBlank)
				return;
			bg = allocColor(bg, vdpCanvas.getClearRGB());
			gc.setBackground(bg);
			gc.fillRectangle(updateRect);
			wasBlank = true;
		}
		vdpCanvas.clearDirty();
		
	}

	public VdpCanvas getCanvas() {
		return vdpCanvas;
	}

	public void setZoom(int zoom) {
		this.zoom = zoom;
		isDirty = true;
		redraw();
	}

	public void canvasDirtied(VdpCanvas canvas) {
		//redraw();
		//System.out.println("!");
		isDirty = true;
	}

	private Color allocColor(Color color, byte[] rgb) {
		if (color != null)
			color.dispose();
		if (rgb == null)
			rgb = vdpCanvas.getRGB(0);
		
		return new Color(shell.getDisplay(), rgb[0] & 0xff, rgb[1] & 0xff, rgb[2] & 0xff);
	}
}
