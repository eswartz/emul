/**
 * 
 */
package v9t9.gui.client.awt;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import v9t9.gui.jna.V9t9Render;

/**
 * @author ejs
 *
 */
public class NoiseEffect implements IAwtMonitorEffect {

	private BufferedImage noisySurface;

	/**
	 * @param awtVideoRenderer
	 */
	public NoiseEffect() {
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.client.IMonitorEffect#getLabel()
	 */
	@Override
	public String getLabel() {
		return "Noisy";
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.IMonitorEffect#applyEffect(int, int, java.awt.image.BufferedImage, java.awt.Rectangle, java.awt.Rectangle)
	 */
	@Override
	public BufferedImage applyEffect(int destWidth, int destHeight,
			BufferedImage surface, Rectangle logRect, Rectangle physRect) {
		if (physRect.width / logRect.width <= 1) {
			return null;
		}
			
		if (noisySurface == null || noisySurface.getWidth() != destWidth || noisySurface.getHeight() != destHeight) {
			noisySurface = new BufferedImage(destWidth, destHeight, BufferedImage.TYPE_INT_BGR);
		}

		DataBufferInt buffer = (DataBufferInt) surface.getRaster().getDataBuffer();
		int[] data = buffer.getData();

		// modify the original area
		//if (logRect.x > 0) { logRect.x--; logRect.width++; }
		//if (logRect.y > 0) { logRect.y--; logRect.height++; }
		//if (logRect.y + logRect.height + 2 <= logMax.height) logRect.height++;

		//logRect = physicalToLogical(new Rectangle(x, y, width, height));
		//Rectangle physRect = logicalToPhysical(logRect);
		
		DataBufferInt noisyBuffer = (DataBufferInt) noisySurface.getRaster().getDataBuffer();
		int[] noisyData = noisyBuffer.getData();
		
		try {
			V9t9Render.INSTANCE.addNoiseRGBA/*Monitor*/(noisyData, data,
					destWidth * 4 * physRect.y + 4 * physRect.x,
					Math.min(noisyData.length, data.length) * 4,
					physRect.width, physRect.height, destWidth * 4,
					logRect.width, logRect.height, destHeight);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		return noisySurface;
	}

}
