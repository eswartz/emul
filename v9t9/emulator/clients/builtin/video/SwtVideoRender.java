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
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;


/**
 * @author ejs
 *
 */
public class SwtVideoRender implements VideoRenderer {

	private Shell shell;
	private Canvas canvas;
	private final VdpCanvas vdpCanvas;
	private Color bg, fg;

	private ImageData imageData; 
	private Image image;
	private Rectangle updateRect;
	private boolean isBlank;
	
	public SwtVideoRender(Display display, VdpCanvas vdpCanvas) {
		this.vdpCanvas = vdpCanvas;
		shell = new Shell(display);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		shell.setLayout(layout);
		
		this.canvas = new Canvas(shell, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		this.canvas.setLayoutData(gridData);
		this.canvas.setLayout(new FillLayout());
		
		this.updateRect = new Rectangle(0, 0, 0, 0);
		
		// the canvas collects update regions in a big rect
		this.canvas.addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				updateRect = updateRect.union(new Rectangle(e.x, e.y, e.width, e.height));
				if (e.count == 0) {
					repaint(e.gc);
					updateRect = new Rectangle(0, 0, 0, 0);
				}
			}
			
		});
		shell.open();
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.VideoRenderer#resize(int, int)
	 */
	public void resize(int width, int height) {
		if (imageData != null) {
			if (imageData.width == width && imageData.height == height) {
				return;
			}
		}
		
		PaletteData palette = new PaletteData(0xFF0000, 0xFF00, 0xFF);
		imageData = new ImageData(width, height, 24, palette);
		
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				canvas.setSize(new Point(imageData.width, imageData.height));
				
				Rectangle trim = shell.computeTrim(0, 0, imageData.width, imageData.height);
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
		for (int idx = 0; idx < count; idx++) {
			final RedrawBlock block = blocks[idx];
			
			// blit to ImageData
			if (imageData.depth == 24) {
				int offs = block.r * imageData.bytesPerLine + block.c * 3;
				for (int ro = 0; ro < block.h; ro++) {
					int r = block.r + ro;
					vdpCanvas.readBitmapRGB24(r, block.c, imageData.data, offs, block.w);
					offs += imageData.bytesPerLine;
				}
			} else {
				int offs = block.r * imageData.bytesPerLine + block.c * 4;
				for (int ro = 0; ro < block.h; ro++) {
					int r = block.r + ro;
					vdpCanvas.readBitmapARGB32(r, block.c, imageData.data, offs, block.w);
					offs += imageData.bytesPerLine;
				}

			}
			/*
			// queue redraw
			Display.getDefault().syncExec(new Runnable() {

				public void run() {
					canvas.redraw(block.r, block.c, block.w, block.h, true);
				}
				
			});
			*/
		}
		// queue redraw
		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				canvas.redraw();
			}
			
		});

	}

	protected void repaint(GC gc) {
		if (!isBlank && imageData != null) {
			if (image != null && !image.isDisposed()) {
				image.dispose();
			}
			image = new Image(shell.getDisplay(), imageData);
			
			// TODO: zoom
			int zoom = 1;
			Rectangle destRect = updateRect;
			destRect = new Rectangle(0, 0, 256, 192);
		
			
			destRect = destRect.intersection(new Rectangle(0, 0, imageData.width * zoom, imageData.height * zoom));
			Rectangle imageRect = new Rectangle(destRect.x / zoom, destRect.y / zoom, 
					destRect.width / zoom, destRect.height / zoom);
			gc.drawImage(image, imageRect.x, imageRect.y, imageRect.width, imageRect.height, 
					destRect.x, destRect.y, destRect.width, destRect.height);
		} else if (bg != null) {
			gc.setBackground(bg);
			gc.fillRectangle(updateRect);
		}
	}


}
