/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import com.trolltech.qt.core.QRect;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.gui.QBrush;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QImage;
import com.trolltech.qt.gui.QPaintEvent;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QWidget;



/**
 * Render video into an SWT window
 * @author ejs
 *
 */
public class QtVideoRenderer implements VideoRenderer {

	private final QtCanvas vdpCanvas;

	private boolean isBlank;

	private QWidget window;
	private QWidget canvas;

	private int zoom;

	private QColor bg;
	public QtVideoRenderer(QtCanvas canvas) {
		zoom = 1;
		
		// the canvas collects update regions in a big rect
		window = new QWidget() {
			@Override
			protected void paintEvent(QPaintEvent e) {
				QtVideoRenderer.this.repaint(e.rect());
			}
		};
		window.resize(256, 192);
		window.move(800, 800);
		
		this.canvas = window;
		
		this.vdpCanvas = canvas;
		window.show();
		
	}

	/** Force a redraw and repaint of the entire canvas */
	public void redraw() {
		window.update();
	}
	
	public void sync() {
		window.repaint();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.VideoRenderer#resize(int, int)
	 */
	public void resize(int width, int height) {
		QSize size = new QSize(vdpCanvas.getWidth() * zoom, vdpCanvas.getHeight() * zoom);
		canvas.resize(size);
		
		//QRect trim = shell.computeTrim(0, 0, size.x, size.y);
		window.resize(size);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.VideoRenderer#setForegroundAndBackground(int, int)
	 */
	public void setForegroundAndBackground(int bg, int fg) {
		if (this.bg != null)
			this.bg.dispose();
		byte[] rgb = vdpCanvas.getRGB(bg);
		this.bg = new QColor(rgb[0], rgb[1], rgb[2]);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.VideoRenderer#setBlank(int)
	 */
	public void setBlank(boolean blank) {
		isBlank = blank;
		redraw();
	}

	public void updateList(RedrawBlock[] blocks, int count) {
		//final Region region = new Region(shell.getDisplay());
		QRect redrawRect_ = null;
		int nblocks = 0;
		for (int idx = 0; idx < count; idx++) {
			final RedrawBlock block = blocks[idx];
			//region.add(new QRect(block.c, block.r, block.w, block.h));
			QRect blockRect = new QRect(block.c, block.r, block.w, block.h);
			if (redrawRect_ == null)
				redrawRect_ = blockRect;
			else
				redrawRect_ = redrawRect_.united(blockRect);
			nblocks++;
		}
		
		// queue redraw
		if (redrawRect_ != null) {
			//final QRect redrawRect = region.getBounds();
			final QRect redrawRect = redrawRect_;
			//region.dispose();
			
			//if (nblocks > 0) 
			//	System.out.println("Redrew " + nblocks + " blocks to " + redrawRect);
			
			canvas.repaint(redrawRect.x() * zoom, redrawRect.y() * zoom, 
					redrawRect.width() * zoom, redrawRect.height() * zoom);
		}
	}

	protected void repaint(QRect updateRect) {
		QPainter painter = new QPainter(window);
		QImage image = vdpCanvas.getImage(); 
		if (!isBlank && image != null) {
			QRect destRect = updateRect;
			
			destRect = destRect.intersected(new QRect(0, 0, 
					vdpCanvas.getWidth() * zoom, vdpCanvas.getHeight() * zoom));
			QRect imageRect = new QRect(destRect.x() / zoom, destRect.y() / zoom, 
					destRect.width() / zoom, destRect.height() / zoom);
			imageRect = vdpCanvas.mapVisible(imageRect);
			
			painter.drawImage(destRect, image, imageRect);
		} else if (bg != null) {
			QBrush brush = new QBrush(bg);
			painter.fillRect(updateRect, brush);
		}
		
	}

	public VdpCanvas getCanvas() {
		return vdpCanvas;
	}

	public void setZoom(int zoom) {
		setZoom(zoom, zoom);
	}

	public void setZoom(int zoom, int zoomy) {
		this.zoom = zoom;
		redraw();
	}

	public void yield() {
		
	}
}
