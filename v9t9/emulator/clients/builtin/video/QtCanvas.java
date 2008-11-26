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
	public QtCanvas() {
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#setSize(int, int)
	 */
	@Override
	public void doChangeSize() {
		if (image != null)
			image.dispose();
		image = new QImage(new QSize(width, height), Format.Format_RGB32);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#clear()
	 */
	@Override
	public void clear() {
		image.fill(getPixel(clearColor));
	}

	protected int getPixel(int color) {
		byte[] rgb = getRGB(color);
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
	protected void drawEightMagnifiedSpritePixels(int offs, byte mem, byte fg, short bitmask) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void drawEightSpritePixels(int offs, byte mem, byte fg, byte bitmask) {
		// TODO Auto-generated method stub
		
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
		return new QRect(rect.x(), rect.y(), rect.width(), rect.height());
	}

}
