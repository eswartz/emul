/**
 * 
 */
package v9t9.gui.client.swt.bars;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

/**
 * Generator for linear gradient drawing
 * @author ejs
 *
 */
public class Gradient {

	private ImageData gradientLineData;
	private Image gradientLine;
	private final int[] colors;
	private final float[] weights;
	private final boolean horizontal;
	
	/**
	 * 
	 * @param horizontal true: colors change left-right; false: colors change top-bottom
	 * @param colors
	 * @param weights
	 */
	public Gradient(boolean horizontal, int[] colors, float[] weights) {
		if (colors == null || weights == null || 
				colors.length < 2 || colors.length != weights.length + 1)
			throw new IllegalArgumentException();
		
		this.horizontal = horizontal;
		this.colors = colors;
		this.weights = weights;
	}
	
	public void draw(GC gc, int x, int y, int width, int height) {
		ensure(gc.getDevice(), horizontal ? width : height);
		
		gc.drawImage(gradientLine, 
				0, 0, 
				horizontal ? gradientLineData.width : 1, 
				horizontal ? 1 : gradientLineData.height, 
				x, y, width, height);		
	}

	/**
	 * @param device 
	 * @param sz
	 */
	private void ensure(Device device, int sz) {
		if (gradientLineData == null || gradientLineData.width < sz) {
			PaletteData palette = new PaletteData (0xFF0000, 0xFF00, 0xFF);
			gradientLineData = new ImageData(horizontal ? sz : 1, horizontal ? 1 : sz,
					device.getDepth(), palette);
			
			int xd = horizontal ? 1 : 0;
			int yd = horizontal ? 0 : 1;
			
			int x = 0;
			int y = 0;
			
			int idx = -1;
			
			// go through each consecutive pair of colors,
			// each of which contributes sz * weight pixels
			RGB prev = null, limit = null;
			int from = 0, next = 0;
			int range = 0;
			
			for (int i = 0; i < sz; i++) {
				if (idx < 0) {
					limit = device.getSystemColor(colors[++idx]).getRGB();
				}
				if (i >= next) {
					prev = limit;
					range = Math.round(weights[idx] * sz);
					if (range + i > sz)
						range = sz - i;
					limit = device.getSystemColor(colors[++idx]).getRGB();
					
					from = i;
					next = from + range;
				}
				
				RGB cur = new RGB(
						(int) (prev.red + (limit.red - prev.red) * (i - from) / (float) range),
						(int) (prev.green + (limit.green - prev.green) * (i - from)  / (float) range),
						(int) (prev.blue + (limit.blue - prev.blue) * (i - from)  / (float) range)
						);
				
				gradientLineData.setPixel(x, y, palette.getPixel(cur));
				x += xd;
				y += yd;
			}
			
			if (gradientLine != null) {
				gradientLine.dispose();
				gradientLine = null;
			}
			
			gradientLine = new Image(device, gradientLineData);
		}
	}
}
