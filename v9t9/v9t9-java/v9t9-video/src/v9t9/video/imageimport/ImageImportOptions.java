/*
  ImageImportOptions.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video.imageimport;


import org.ejs.gui.images.ColorOctree;

import v9t9.common.hardware.IVdpChip;
import v9t9.common.hardware.IVdpTMS9918A;
import v9t9.common.video.IVdpCanvas;
import v9t9.common.video.VdpFormat;
import ejs.base.properties.FieldProperty;
import ejs.base.properties.PropertySource;
import ejs.base.properties.Range;

/**
 * @author ejs
 *
 */
public class ImageImportOptions {

	public enum Dither {
		NONE("None"),
		ORDERED("Ordered"),
		//ORDERED2("Ordered2"),
		FS("Floyd-Steinberg");
		
		private final String label;

		private Dither(String label) {
			this.label = label;
		}
		
		@Override
		public String toString() {
			return label;
		}
	}

	public enum Palette {
		STANDARD("Standard"),
		CURRENT("Current"),
		OPTIMIZED("Optimized");
		
		private final String label;

		private Palette(String label) {
			this.label = label;
		}
		
		@Override
		public String toString() {
			return label;
		}
	}
	
	protected VdpFormat format = VdpFormat.COLOR16_8x1;
	protected boolean asGreyScale;
	protected Palette paletteOption = Palette.OPTIMIZED;
	protected boolean ditherMono;
	protected boolean isMonoMode;
	protected Dither ditherType = Dither.NONE;
	@Range(minimum=-100, maximum=100f)
	protected float gamma = 0f;

	protected ColorOctree octree;
	
	private FieldProperty paletteOptionProperty;
	private FieldProperty ditheringProperty;
	private FieldProperty ditherMonoProperty;
	private FieldProperty gammaProperty;

	protected IVdpCanvas canvas;
	protected IVdpChip vdp;
	private boolean canSetPalette;
	
	/**
	 * @param iVdpChip 
	 * @param canvas 
	 * 
	 */
	public ImageImportOptions(IVdpCanvas canvas, IVdpChip iVdpChip) {
		this.canvas = canvas;
		this.vdp = iVdpChip;
		if (canvas.getFormat() != null)
			format = canvas.getFormat();
		paletteOptionProperty = new FieldProperty(this, "paletteOption", "Palette Selection");
		ditheringProperty = new FieldProperty(this, "ditherType", "Dithering");
		ditherMonoProperty = new FieldProperty(this, "ditherMono", "Dither Monochrome");
		gammaProperty = new FieldProperty(this, "gamma", "Gamma % Delta");

	}

	public void addToPropertySource(PropertySource ps) {
		ps.addProperty(paletteOptionProperty);
		ps.addProperty(ditheringProperty);
		ps.addProperty(ditherMonoProperty);
		ps.addProperty(gammaProperty);
	}
	
	public VdpFormat getFormat() {
		return format;
	}
	public void setFormat(VdpFormat format) {
		this.format = format;
	}
	public boolean isAsGreyScale() {
		return asGreyScale;
	}
	public void setAsGreyScale(boolean asGreyScale) {
		this.asGreyScale = asGreyScale;
	}
	public Palette getPalette() {
		return paletteOption;
	}
	public void setPalette(Palette option) {
		this.paletteOption = option;
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
	
	/**
	 * Call to reset options to the presumed best ones for the
	 * current video mode.
	 */
	public void resetOptions() {
		VdpFormat format = canvas.getFormat();
		
		canSetPalette = format.canSetPalette();
		
		////
		
		setPalette(canSetPalette ? Palette.OPTIMIZED : Palette.STANDARD);
		
		/////
		isMonoMode = vdp instanceof IVdpTMS9918A && ((IVdpTMS9918A) vdp).isBitmapMonoMode();
		
		setDitherMono(isMonoMode);
		setDitherType(format.getLayout() == VdpFormat.Layout.BITMAP_2_PER_8 ? Dither.ORDERED : Dither.FS);
		
		octree = null;
		
		gamma = 0f;
	}
	
	/**
	 * @return the isMonoMode
	 */
	public boolean isMonoMode() {
		return isMonoMode;
	}
	
	/**
	 * @return the canSetPalette
	 */
	public boolean canSetPalette() {
		return canSetPalette;
	}
	
	/**
	 * @return the octree
	 */
	public ColorOctree getOctree() {
		if (octree == null)
			octree = new ColorOctree(3, true);
		return octree;
	}
	/**
	 * @param octree the octree to set
	 */
	public void setOctree(ColorOctree octree) {
		this.octree = octree;
	}

	
	public float getGamma() {
		return gamma;
	}
	public void setGamma(float gamma) {
		this.gamma = gamma;
	}
	
	
}