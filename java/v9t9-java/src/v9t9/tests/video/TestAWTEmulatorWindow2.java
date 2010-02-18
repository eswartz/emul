/**
 * 
 */
package v9t9.tests.video;

import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

import v9t9.emulator.clients.builtin.video.ImageDataCanvas24Bit;
import v9t9.jni.v9t9render.utils.V9t9RenderUtils;

/**
 * @author ejs
 * 
 */
public class TestAWTEmulatorWindow2 {
	static {
		System.loadLibrary("v9t9renderutils");
	}
	public static void main(String[] args) {
		TestAWTEmulatorWindow2 window = new TestAWTEmulatorWindow2();
		window.run();
	}

	private GraphicsDevice device;
	private ImageDataCanvas24Bit vdpCanvas;
	private int ctr;

	public void run() {
		vdpCanvas = new ImageDataCanvas24Bit();
		ctr = 0;
		
		GraphicsEnvironment env = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		device = env.getDefaultScreenDevice();
		GraphicsConfiguration gc = device.getDefaultConfiguration();
		Frame frame = new Frame(gc);
		frame.setSize(512, 384);
		frame.setVisible(true);
		Rectangle bounds = gc.getBounds();
		//makeBigImage(frame, bounds);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				System.out.println(e);
			}

			public void keyReleased(KeyEvent e) {
				System.out.println(e);				
			}

			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});

	}

	protected Image createScreenImage(Rectangle bounds) {
		ctr++;
		vdpCanvas.clear(new byte[] { (byte) (ctr*11),(byte) (ctr*15),(byte) (ctr*7) });
		
		byte[] scaledData = new byte[bounds.width * bounds.height * 3];
		V9t9Render.scaleImage(scaledData, 
				vdpCanvas.getImageData().data, 0,
				vdpCanvas.getVisibleWidth(), vdpCanvas.getHeight(), vdpCanvas.getLineStride(),
				bounds.width, bounds.height, bounds.width * 4,
				0, 0, bounds.width, bounds.height);
		V9t9Render.addNoise(scaledData, 0,
				bounds.width, bounds.height, bounds.width * 3,
				vdpCanvas.getVisibleWidth(), vdpCanvas.getHeight());
		
		ComponentColorModel colorModel = new ComponentColorModel(
				ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB),
				false, false, ComponentColorModel.OPAQUE, 
				DataBuffer.TYPE_BYTE);
		DataBuffer buffer = new DataBufferByte(scaledData, scaledData.length);
		WritableRaster raster = WritableRaster.createWritableRaster(
				new ComponentSampleModel(DataBuffer.TYPE_BYTE, bounds.width, bounds.height, 3,
						bounds.width * 3, new int[] { 0, 1, 2 }),
						
				buffer, /*bounds.width, bounds.height, 24,*/ new Point(0, 0));
		return new BufferedImage(colorModel,
				raster, false, null);
				
	}
}
