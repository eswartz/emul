package v9t9.gui.client.swt.imageimport;

import static v9t9.common.hardware.VdpV9938Consts.*;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.LinkedHashSet;

import v9t9.common.hardware.IVdpChip;
import v9t9.common.hardware.IVdpTMS9918A;
import v9t9.common.hardware.VdpV9938Consts;
import v9t9.common.memory.ByteMemoryAccess;
import v9t9.common.video.ColorMapUtils;
import v9t9.common.video.IVdpCanvasRenderer;
import v9t9.common.video.VdpFormat;
import v9t9.video.ImageDataCanvas;
import v9t9.video.imageimport.ImageImport;
import v9t9.video.imageimport.ImageImportData;
import v9t9.video.imageimport.ImageImportOptions;

public abstract class ImageImportHandler implements IImageImportHandler {

	private ImageImportOptions imageImportOptions;
	private Collection<String> urlHistory = new LinkedHashSet<String>();

	public ImageImportHandler() {
		super();
	}

	abstract protected IVdpCanvasRenderer getCanvasRenderer() ;
	abstract protected IVdpChip getVdpHandler() ;

	abstract protected ImageDataCanvas getCanvas();


	@Override
	public ImageImport createImageImport() {
		return new ImageImport(getCanvas(), getImageImportOptions());
	}
	
	@Override
	public ImageImportOptions getImageImportOptions() {
		if (imageImportOptions == null) {
			imageImportOptions = new ImageImportOptions();
			resetOptions();
		}
		return imageImportOptions;
	}
	
	@Override
	public void resetOptions() {
		imageImportOptions.resetOptions(getCanvas(), getVdpHandler());
	}
	
	public void importImage(BufferedImage image, boolean scaleSmooth) {
		ImageImport importer = createImageImport();
		ImageImportOptions imageImportOptions = getImageImportOptions();
		imageImportOptions.updateFrom(image);
		imageImportOptions.setScaleSmooth(scaleSmooth);
		importImageAndDisplay(importer);
	}

	/**
	 * @param importer
	 */
	public void importImageAndDisplay(ImageImport importer) {
		synchronized (getCanvasRenderer()) {
			synchronized (getCanvas()) {
				ImageImportData data = importer.importImage();
				importImageToCanvas(data);
			}
		}
	}
	
	/**
	 * @param data
	 */
	public void importImageToCanvas(ImageImportData data) {
		setPalette(data);
		setVideoMemory(data);
		getCanvas().markDirty();
		
	}

	/**
	 * @param data
	 */
	private void setPalette(ImageImportData data) {
		byte[][] thePalette = data.getThePalette();
		IVdpChip vdp = getVdpHandler();
		VdpFormat format = getCanvas().getFormat();
		
		int ncols = format.getNumColors();
		if (ncols < 256) {
			for (int c = 0; c < ncols; c++) {
				vdp.setRegister(VdpV9938Consts.REG_PAL0 + c, ColorMapUtils.rgb8ToRgbRBXG(thePalette[c]));
			}
		}
				
	}

	public Collection<String> getHistory() {
		return urlHistory;
	}
	

	/**
	 * @param data 
	 * @param converted 
	 * 
	 */
	private void setVideoMemory(ImageImportData data) {
		IVdpChip vdp = getVdpHandler();		
		VdpFormat format = getCanvas().getFormat();
		
		if (vdp instanceof IVdpTMS9918A) {
			IVdpTMS9918A vdp99 = (IVdpTMS9918A) vdp;
			if (format == VdpFormat.COLOR16_8x1) {
				setVideoMemoryBitmapMode(data, vdp99);
			} 
			else if (format == VdpFormat.COLOR16_8x8) {
				setVideoMemoryGraphicsMode(data, vdp99);
			}
			else if (format == VdpFormat.COLOR16_1x1) {
				setVideoMemoryV9938BitmapMode(data, vdp99);
			}
			else if (format == VdpFormat.COLOR256_1x1) {
				setVideoMemoryV9938BitmapMode(data, vdp99);
			}
			else if (format == VdpFormat.COLOR4_1x1) {
				setVideoMemoryV9938BitmapMode(data, vdp99);
			}
			else if (format== VdpFormat.COLOR16_4x4) {
				setVideoMemoryMulticolorMode(data, vdp99);
			}
		}
	}

	/**
	 * @param data 
	 * @param vdp99
	 */
	private void setVideoMemoryMulticolorMode(ImageImportData data, IVdpTMS9918A vdp99) {
		ByteMemoryAccess patt = vdp99.getByteReadMemoryAccess(vdp99.getPatternTableBase());
		
		for (int y = 0; y < 48; y++) {
			for (int x = 0; x < 64; x += 2) {
				
				byte f = data.getPixel(x, y);
				byte b = data.getPixel(x + 1, y);
				
				int poffs = ((y >> 3) << 8) + (y & 7) + ((x >> 1) << 3);  
				//System.out.println("("+y+","+x+") = "+ poffs);
				vdp99.writeAbsoluteVdpMemory(patt.offset + poffs, (byte) ((f << 4) | b));
			}
		}
	}

	protected interface IBitmapModeImportHandler {
		byte createImageDataByte(int x, int row);

		/**
		 * @return
		 */
		int getRowStride();

		/**
		 * @return
		 */
		int getColumnStride();
	}
	

	abstract class BaseGraphicsModeXImportHandler implements IBitmapModeImportHandler {
		protected final ImageImportData data;
		
		public BaseGraphicsModeXImportHandler(ImageImportData data) {
			this.data = data;
		}
	}
	
	class GraphicsMode4ImportHandler extends BaseGraphicsModeXImportHandler {
		public GraphicsMode4ImportHandler(ImageImportData data) {
			super(data);
		}

		@Override
		public byte createImageDataByte(int x, int row) {
			
			byte f = data.getPixel(x, row);
			byte b = data.getPixel(x + 1, row);
			return (byte) ((f << 4) | b);
		}

		@Override
		public int getRowStride() {
			return 128;
		}

		@Override
		public int getColumnStride() {
			return 2;
		}
	}

	class GraphicsMode5ImportHandler extends BaseGraphicsModeXImportHandler {
		public GraphicsMode5ImportHandler(ImageImportData data) {
			super(data);
		}

		@Override
		public byte createImageDataByte(int x, int y) {
			byte p = 0;
			for (int xo = 0; xo < 4; xo++) {
				byte c = data.getPixel(x + xo, y);
				p |= c << ((3 - xo) * 2);
			}
			return p;
		}

		@Override
		public int getRowStride() {
			return 128;
		}

		@Override
		public int getColumnStride() {
			return 4;
		}
	}

	class GraphicsMode6ImportHandler extends BaseGraphicsModeXImportHandler {
		public GraphicsMode6ImportHandler(ImageImportData data) {
			super(data);
		}

		@Override
		public byte createImageDataByte(int x, int y) {
			byte f = data.getPixel(x, y);
			byte b = data.getPixel(x + 1, y);
			
			return (byte) ((f << 4) | b);
		}

		@Override
		public int getRowStride() {
			return 256;
		}

		@Override
		public int getColumnStride() {
			return 2;
		}
	}

	class GraphicsMode7ImportHandler extends BaseGraphicsModeXImportHandler {
		public GraphicsMode7ImportHandler(ImageImportData data) {
			super(data);
		}

		@Override
		public byte createImageDataByte(int x, int y) {
			
			return data.getPixel(x, y);
		}

		@Override
		public int getRowStride() {
			return 256;
		}

		@Override
		public int getColumnStride() {
			return 1;
		}
		
	}
	/**
	 * @param data 
	 * @param vdp
	 */
	private void setVideoMemoryV9938BitmapMode(ImageImportData data, IVdpTMS9918A vdp) {
		IBitmapModeImportHandler handler;
		int mx;
		switch (vdp.getModeNumber()) {
		case MODE_GRAPHICS4:
			handler = new GraphicsMode4ImportHandler(data);
			mx = 256;
			break;
		case MODE_GRAPHICS5:
			handler = new GraphicsMode5ImportHandler(data);
			mx = 512;
			break;
		case MODE_GRAPHICS6:
			handler = new GraphicsMode6ImportHandler(data);
			mx = 512;
			break;
		case MODE_GRAPHICS7:
			handler = new GraphicsMode7ImportHandler(data);
			mx = 256;
			break;
		default:
			throw new IllegalStateException();	
		}
		
		int ystep = vdp.isInterlacedEvenOdd() ? 2 : 1;
		int my =  (vdp.getRegister(9) & 0x80) != 0 ? 212 : 192;
		int graphicsPageSize = vdp.getGraphicsPageSize();
		
		int colstride = handler.getColumnStride();
		int rowstride = handler.getRowStride();
		
		for (int eo = 0; eo < ystep; eo++) {
			ByteMemoryAccess patt = vdp.getByteReadMemoryAccess(vdp.getPatternTableBase()
					^ (eo != 0 ? graphicsPageSize : 0));
			for (int y = 0; y < my; y++) {
				int row = y * ystep + eo;
				for (int x = 0; x < mx; x += colstride) {
					
					byte byt = handler.createImageDataByte(x, row);
					
					int poffs = y * rowstride + (x / colstride); 
					vdp.writeAbsoluteVdpMemory(patt.offset + poffs, byt);
				}
			}
		}
		
	}
	/**
	 * @param data 
	 * @param vdp
	 */
	private void setVideoMemoryGraphicsMode(ImageImportData data, IVdpTMS9918A vdp) {
		ByteMemoryAccess screen = vdp.getByteReadMemoryAccess(vdp.getScreenTableBase());
		ByteMemoryAccess patt = vdp.getByteReadMemoryAccess(vdp.getPatternTableBase());
		ByteMemoryAccess color = vdp.getByteReadMemoryAccess(vdp.getColorTableBase());
		
		// assume char 255 is not used
		for (int i = 0; i < 768; i++)
			vdp.writeAbsoluteVdpMemory(screen.offset + i, (byte) 0xff);

		for (int i = 0; i < 8; i++)
			vdp.writeAbsoluteVdpMemory(patt.offset + 255*8 + i, (byte) 0x0);

		byte b = 0;
		
		byte cb = (byte) vdp.getRegister(7);
		cb = (byte) ((cb & 0xf) | 0x10);
		
		b = (byte) ((cb >> 0) & 0xf);

		for (int i = 0; i < 32; i++)
			vdp.writeAbsoluteVdpMemory(color.offset + i, cb);

		int width = data.getScaledImage().getWidth();
		int height = data.getScaledImage().getHeight();
		
		int yoffs = ((192 - height) / 2) & ~0x7;
		int xoffs = ((256 - width) / 2) & ~0x7;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x+= 8) {
				int ch = ((y >> 3) * ((width + 7) >> 3)) + (x >> 3);
				if (ch > 0xff)
					throw new IllegalStateException();
				int choffs = (((y + yoffs) >> 3) << 5) + ((x + xoffs) >> 3);
				
				vdp.writeAbsoluteVdpMemory(screen.offset + choffs, (byte) ch);
				
				int poffs = (ch << 3) + (y & 7);
				
				byte p = 0;
				
				for (int xo = 0; xo < 8; xo++) {
					byte c = data.getPixel(x + xo + xoffs, y + yoffs);
					if (c != b) {
						p |= 0x80 >> xo;
					}
				}

				vdp.writeAbsoluteVdpMemory(patt.offset + poffs, p);
			}
		}
		
	}

	/**
	 * @param data 
	 * @param vdp
	 */
	private void setVideoMemoryBitmapMode(ImageImportData data, IVdpTMS9918A vdp) {
		boolean isMono = vdp.isBitmapMonoMode();
		
		ByteMemoryAccess screen = vdp.getByteReadMemoryAccess(vdp.getScreenTableBase());
		ByteMemoryAccess patt = vdp.getByteReadMemoryAccess(vdp.getPatternTableBase());
		ByteMemoryAccess color = vdp.getByteReadMemoryAccess(vdp.getColorTableBase());
		
		byte f = 0, b = 0;
		
		if (isMono) {
			f = (byte) ((vdp.getRegister(7) >> 4) & 0xf);
			b = (byte) ((vdp.getRegister(7) >> 0) & 0xf);
		}

		for (int y = 0; y < 192; y++) {
			for (int x = 0; x < 256; x += 8) {
				
				int choffs = ((y >> 6) << 8) + ((y & 0x3f) >> 3) * 32 + (x >> 3);
				int ch = choffs & 0xff;
				
				if ((y & 7) == 0) {
					vdp.writeAbsoluteVdpMemory(screen.offset + choffs, (byte) ch);
				}

				int poffs = (y >> 6) * 0x800 + (ch << 3) + (y & 7);
				
				byte p = 0;
				
				if (!isMono) {
					// in color mode, by convention keep the foreground color
					// as the lesser color.
					f = data.getPixel(x, y);
					p = (byte) 0x80;
				
					boolean gotBG = false;
					for (int xo = 1; xo < 8; xo++) {
						byte c = data.getPixel(x + xo, y);
						if (c == f) {
							p |= 0x80 >> xo;
						} else {
							if (!gotBG) {
								if (c < f) {
									b = f;
									f = c;
									p ^= (0xff << (8 - xo));
									p |= 0x80 >> xo;
								} else {
									b = c;
								}
								gotBG = true;
							}
						}
					}
					
					vdp.writeAbsoluteVdpMemory(color.offset + poffs, (byte) ((f << 4) | (b)));
				} else {
					// in mono mode, mapper has matched with fg and bg from vr7
					for (int xo = 0; xo < 8; xo++) {
						byte c = data.getPixel(x + xo, y);
						if (c == f) {
							p |= 0x80 >> xo;
						}
					}
				}

				vdp.writeAbsoluteVdpMemory(patt.offset + poffs, p);
			}
		}
	}

}