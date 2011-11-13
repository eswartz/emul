/**
 * 
 */
package v9t9.emulator.clients.builtin.video.image;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import org.ejs.coffee.core.properties.FieldProperty;
import org.ejs.coffee.core.properties.IPropertySource;
import org.ejs.coffee.core.properties.PropertySource;

import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.clients.builtin.video.VdpColorManager;
import v9t9.emulator.clients.builtin.video.VdpCanvas.Format;
import v9t9.emulator.clients.builtin.video.tms9918a.BitmapModeRedrawHandler;
import v9t9.emulator.clients.builtin.video.v9938.VdpV9938;
import v9t9.engine.VdpHandler;

/**
 * @author ejs
 *
 */
public class ImageImportOptions {

	public enum Dither {
		NONE("None"),
		ORDERED("Ordered"),
		FS("Floyd-Steinberg");
		
		private final String label;

		/**
		 * 
		 */
		private Dither(String label) {
			this.label = label;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return label;
		}
	}
	
	
	private boolean scaleSmooth = true;
	private boolean keepAspect = true;
	private boolean asGreyScale;
	private boolean optimizePalette = true;
	private boolean ditherMono;
	private Dither ditherType = Dither.NONE;
	
	private byte[][] origPalette;
	private BufferedImage image;
	
	private FieldProperty scaleSmoothProperty;
	private FieldProperty keepAspectProperty;
	private FieldProperty asGreyScaleProperty;
	private FieldProperty optimizePaletteProperty;
	private FieldProperty ditheringProperty;
	private FieldProperty ditherMonoProperty;
	
	/**
	 * 
	 */
	public ImageImportOptions() {
		scaleSmoothProperty = new FieldProperty(this, "scaleSmooth", "Smooth Scaling");
		keepAspectProperty = new FieldProperty(this, "keepAspect", "Keep Aspect Ratio");
		asGreyScaleProperty = new FieldProperty(this, "asGreyScale", "Convert To Greyscale");
		optimizePaletteProperty = new FieldProperty(this, "optimizePalette", "Optimize Palette");
		ditheringProperty = new FieldProperty(this, "ditherType", "Dithering");
		ditherMonoProperty = new FieldProperty(this, "ditherMono", "Dither Monochrome");
	}
	/**
	 * @return
	 */
	public IPropertySource createPropertySource() {
		PropertySource ps = new PropertySource();
		ps.addProperty(scaleSmoothProperty);
		ps.addProperty(keepAspectProperty);
		ps.addProperty(asGreyScaleProperty);
		ps.addProperty(optimizePaletteProperty);
		ps.addProperty(ditheringProperty);
		ps.addProperty(ditherMonoProperty);
		return ps;
	}
	
	public boolean isScaleSmooth() {
		return scaleSmooth;
	}
	public void setScaleSmooth(boolean scaleSmooth) {
		this.scaleSmooth = scaleSmooth;
	}
	public boolean isKeepAspect() {
		return keepAspect;
	}
	public void setKeepAspect(boolean keepAspect) {
		this.keepAspect = keepAspect;
	}
	public boolean isAsGreyScale() {
		return asGreyScale;
	}
	public void setAsGreyScale(boolean asGreyScale) {
		this.asGreyScale = asGreyScale;
	}
	public boolean isOptimizePalette() {
		return optimizePalette;
	}
	public void setOptimizePalette(boolean optimizePalette) {
		this.optimizePalette = optimizePalette;
	}
	public Dither getDitherType() {
		return ditherType;
	}
	public void setDitherType(Dither dither) {
		this.ditherType = dither;
	}
	
	public boolean isDitherMono() {
		return ditherMono;
	}
	public void setDitherMono(boolean ditherMono) {
		this.ditherMono = ditherMono;
	}
	public void setImage(BufferedImage image) {
		this.image = image;
	}
	public BufferedImage getImage() {
		return image;
	}

	public void setOrigPalette(byte[][] thePalette) {
		byte[][] newP = new byte[thePalette.length][];
		for (int i = 0; i < thePalette.length; i++) {
			newP[i] = Arrays.copyOf(thePalette[i], 3);
		}
		this.origPalette = newP;
	}
	public byte[][] getOrigPalette() {
		return origPalette;
	}
	/**
	 * Use this when the image has been dragged/dropped.
	 */
	public void updateFrom(BufferedImage image) {
		setImage(image);
	}
	
	/**
	 * Call to reset options to the presumed best ones for the
	 * current video mode.
	 */
	public void resetOptions(VdpCanvas canvas, VdpHandler vdp) {
		boolean canSetPalette;
		
		Format format = canvas.getFormat();
		
		if (vdp instanceof VdpV9938) {
			// hack: graphics mode 2 allows setting the palette too, 
			// but for comparison shopping, pretend we can't.
			if (format == Format.COLOR16_8x1 && (vdp.readVdpReg(0) & 0x6) == 0x2) {
				canSetPalette = false;
			} else {
				canSetPalette = format != Format.COLOR256_1x1;
			}
		} else {
			canSetPalette = false;
		}
		
		///////
		
		boolean isLowColor = false;
		
		if (format == Format.COLOR16_8x1) {
			if (!canvas.getColorMgr().isGreyscale())
				isLowColor = true;
		}
		
		setScaleSmooth(!isLowColor);
		
		////
		
		setOptimizePalette(canSetPalette);
		
		/////
		boolean isMonoMode = (vdp.getVdpModeRedrawHandler() instanceof BitmapModeRedrawHandler &&
				((BitmapModeRedrawHandler) vdp.getVdpModeRedrawHandler()).isMono());
		
		setDitherMono(isMonoMode);
		setDitherType(format == Format.COLOR16_8x1 && !canSetPalette ? Dither.ORDERED : Dither.FS);
		
		if (!canSetPalette)
			setOrigPalette(VdpColorManager.getStandardPalette(vdp));
		else
			setOrigPalette(canvas.getColorMgr().getPalette());
	}
}
