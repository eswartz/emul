/**
 * 
 */
package v9t9.tests.video;

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.PackedColorModel;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;

import org.eclipse.swt.graphics.ImageData;

import v9t9.emulator.clients.builtin.video.ImageDataCanvas24Bit;
import v9t9.jni.v9t9render.utils.V9t9RenderUtils;

/**
 * YIKES this is slow
 * 
 * @author ejs
 * 
 */
public class TestAWTEmulatorWindow {
	static {
		System.loadLibrary("v9t9renderutils");
	}
	public static void main(String[] args) {
		TestAWTEmulatorWindow window = new TestAWTEmulatorWindow();
		window.run();
	}

	private GraphicsDevice device;
	private ImageDataCanvas24Bit vdpCanvas;
	private int ctr;
	private BufferedImage image;

	public void run() {
		vdpCanvas = new ImageDataCanvas24Bit();
		ctr = 0;
		
		GraphicsEnvironment env = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		device = env.getDefaultScreenDevice();
		GraphicsConfiguration gc = device.getDefaultConfiguration();
		Frame frame = new Frame(gc);
		frame.setIgnoreRepaint(true);
		frame.setSize(256 * 5, 192 * 5);
		//device.setFullScreenWindow(frame);
		frame.setVisible(true);
		Rectangle bounds = gc.getBounds();
		//makeBigImage(frame, bounds);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.createBufferStrategy(1);
		BufferStrategy bufferStrategy = frame.getBufferStrategy();

		Graphics g = bufferStrategy.getDrawGraphics();
		do {
			Image screenImage = createScreenImage(bounds);
			if (!bufferStrategy.contentsLost()) {
				g.drawImage(screenImage, 0, 0, 
						frame);
				bufferStrategy.show();
			}
			System.out.println(System.currentTimeMillis());
			/*
			Graphics g = frame.getGraphics();
			g.drawImage(screenImage, 0, 0, frame);
			g.dispose();*/
		} while (true);
		//g.dispose();

	}

	protected Image createScreenImage(Rectangle bounds) {
		ctr++;
		vdpCanvas.clear(new byte[] { (byte) (ctr*11),(byte) (ctr*15),(byte) (ctr*7) });
		
		if (image == null) {
			image = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_RGB);
		}
		DataBufferInt buffer = (DataBufferInt) image.getRaster().getDataBuffer();
		V9t9RenderUtils.scaleImageToRGBA(
				buffer.getData(),
				vdpCanvas.getImageData().data,
				vdpCanvas.getWidth(), vdpCanvas.getHeight(), vdpCanvas.getLineStride(),
				bounds.width, bounds.height,
				0, 0, bounds.width, bounds.height);
		V9t9RenderUtils.addNoiseRGBA(buffer.getData(), 
				bounds.width, bounds.height, bounds.width * 4,
				vdpCanvas.getWidth(), vdpCanvas.getHeight());
		
		return image;
	}
}
