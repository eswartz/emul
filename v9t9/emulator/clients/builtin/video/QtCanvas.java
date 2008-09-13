/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import com.trolltech.qt.core.QRect;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.gui.QImage;
import com.trolltech.qt.gui.QImage.Format;

/**
 * @author ejs
 *
 */
public class QtCanvas extends VdpCanvas {

	private QImage image;
	private int width;
	private int height;
	public QtCanvas() {
		image = null;
		setSize(256, 192);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#setSize(int, int)
	 */
	@Override
	public void setSize(int x, int y) {
		if (image == null || width != x || height != y) {
			if (image != null)
				image.dispose();
			image = new QImage(new QSize(x + X_PADDING, y), Format.Format_RGB32);
			width = x;
			height = y;
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#clear()
	 */
	@Override
	public void clear() {
		image.fill(getPixel(clearColor));
	}

	protected int getPixel(int color) {
		byte[] rgb = getColorRGB(color);
		return ((rgb[0] & 0xff) << 24) | ((rgb[1] & 0xff) << 16) | ((rgb[2] & 0xff)); 
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#getBitmapOffset(int, int)
	 */
	@Override
	public int getBitmapOffset(int x, int y) {
		return (y << 16) + x;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#setColorAtOffset(int, byte)
	 */
	@Override
	public void setColorAtOffset(int offset, byte color) {
		image.setPixel(offset & 0xffff, offset >> 16, getPixel(color));
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#drawEightPixels(int, byte, byte, byte)
	 */
	@Override
	protected void drawEightPixels(int offs, byte mem, byte fg, byte bg) {
		int fgRGB = getPixel(fg);
		int bgRGB = getPixel(bg);
		for (int i = 0; i < 8; i++) {
			int rgb = (mem & 0x80) != 0 ? fgRGB : bgRGB;
			image.setPixel(offs & 0xffff, offs >> 16, rgb);
			mem <<= 1;
			offs++;
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#drawSixPixels(int, byte, byte, byte)
	 */
	@Override
	protected void drawSixPixels(int offs, byte mem, byte fg, byte bg) {
		// TODO Auto-generated method stub

	}
	
	@Override
	protected void drawEightMagnifiedSpritePixels(int offs, byte mem, byte fg) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void drawEightSpritePixels(int offs, byte mem, byte fg) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#getWidth()
	 */
	@Override
	public int getWidth() {
		return width;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#getHeight()
	 */
	@Override
	public int getHeight() {
		return height;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#getLineStride()
	 */
	@Override
	public int getLineStride() {
		return 0x10000;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#getPixelStride()
	 */
	@Override
	public int getPixelStride() {
		return 1;
	}

	public QImage getImage() {
		return image;
	}

	public QRect mapVisible(QRect rect) {
		return new QRect(rect.x() + X_PADDING, rect.y(), rect.width(), rect.height());
	}

}
