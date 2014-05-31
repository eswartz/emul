/*
  ImageImportHandler.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.imageimport;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.ejs.gui.images.AwtImageUtils;

import v9t9.common.hardware.IVdpChip;
import v9t9.common.video.IVdpCanvas;
import v9t9.common.video.IVdpCanvasRenderer;
import v9t9.common.video.VdpFormat;
import v9t9.video.imageimport.ImageFrame;
import v9t9.video.imageimport.ImageImport;
import v9t9.video.imageimport.ImageImportData;
import v9t9.video.imageimport.ImageImportDialogOptions;

public abstract class ImageImportHandler implements IImageImportHandler {

	private ImageImportDialogOptions imageImportOptions;
	private Collection<String> urlHistory = new LinkedHashSet<String>();
	private RenderThread renderThread;

	public ImageImportHandler() {
		super();
	}

	abstract protected IVdpCanvasRenderer getCanvasRenderer() ;
	abstract protected IVdpChip getVdpHandler() ;

	abstract protected IVdpCanvas getCanvas();


	@Override
	public ImageImport createImageImport() {
		return new ImageImport(getCanvas(), getVdpHandler().getRegisterCount() > 10);
	}
	
	@Override
	public ImageImportDialogOptions getImageImportOptions() {
		if (imageImportOptions == null) {
			imageImportOptions = new ImageImportDialogOptions(getCanvas(), getVdpHandler());
			resetOptions();
		}
		return imageImportOptions;
	}
	
	@Override
	public void resetOptions() {
		imageImportOptions.resetOptions();
		stopRendering();
	}
	
	public void importImage(ImageFrame[] frames) {
		ImageImport importer = createImageImport();
		ImageImportDialogOptions imageImportOptions = getImageImportOptions();
		
		imageImportOptions.updateFrom(frames);
		imageImportOptions.setScaleSmooth(!frames[0].isLowColor);
		
		importImageAndDisplay(importer);
	}

	public Collection<String> getHistory() {
		return urlHistory;
	}
	

	/**
	 * @param importer
	 */
	protected void importImageAndDisplay(ImageImport importer) {
		Object hint = imageImportOptions.isScaleSmooth() ? RenderingHints.VALUE_INTERPOLATION_BILINEAR
				:  RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
	
		VdpFormat format = getCanvas().getFormat();
		boolean isBitmap = format == VdpFormat.COLOR16_8x1 || format == VdpFormat.COLOR16_8x1_9938;
		
		int targWidth = getCanvas().getVisibleWidth();
		int targHeight = getCanvas().getVisibleHeight();
	
		int screenWidth = targWidth;
		int screenHeight = targHeight;
		
		if (format == VdpFormat.COLOR16_4x4) {
			targWidth = screenWidth = 64;
			targHeight = screenHeight = 48;
		}
		
		int realWidth = imageImportOptions.getWidth();
		int realHeight = imageImportOptions.getHeight();
		float aspect = 1f;
		if (targWidth > 256)
			aspect *= 2.0f;
		if (targHeight > 212)
			aspect /= 2.0f;

		
		if (imageImportOptions.isKeepAspect()) {
			if (realWidth <= 0 || realHeight <= 0) {
				throw new IllegalArgumentException("image has zero or negative size");
			}
			if (realWidth != targWidth || realHeight != targHeight) {
				if (realWidth * targHeight * aspect > realHeight * targWidth) {
					targHeight = (int) (targWidth * realHeight / realWidth / aspect);
				} else {
					targWidth = (int) (targHeight * realWidth * aspect / realHeight);
					
					// make sure, for bitmap mode, that the size is a multiple of 8,
					// otherwise the import into video memory will destroy the picture
					if (isBitmap) {
						targWidth &= ~7;
						targHeight = (int) (targWidth * realHeight / realWidth / aspect);
					}
				}
			}
		}
		
		if (format == VdpFormat.COLOR16_8x8) {
			// make a maximum of 256 blocks  (256*64 = 16384)
			// Reduces total screen real estate down by sqrt(3)
			while ((targWidth & ~0x7) * 
					 (((int)(targWidth * realHeight / realWidth / aspect) + 7) & ~0x7) > 16384) {
				targWidth *= 0.99;
				targHeight *= 0.99;
			}
			targWidth &= ~0x7;
			targHeight = (int) (targWidth * realHeight / realWidth / aspect);
			
			screenWidth = targWidth;
			screenHeight = targHeight;
			//if (DEBUG) System.out.println("Graphics mode: " + targWidth*((targHeight+7)&~0x7));
		}

		ImageFrame[] frames = imageImportOptions.getImages();
		if (frames == null)
			return;
		
		importer.prepareConversion(imageImportOptions);
		
		for (ImageFrame frame : frames) {
			importer.addImage(imageImportOptions, frame.image);
		}

		importer.finishAddingImages();
		
		ImageImportData[] datas = new ImageImportData[frames.length];
		for (int i = 0; i < datas.length; i++) {
			// always scale even if same size since the option destroys the image
			BufferedImage scaled = AwtImageUtils.getScaledInstance(
					frames[i].image, targWidth, targHeight, 
					hint,
					false);
			
			ImageImportData data = importer.convertImage(imageImportOptions, scaled,
					screenWidth, screenHeight);
			data.delayMs = frames[i].delayMs;
			datas[i] = data;
		}

		VdpImageImporter vdpImporter = new VdpImageImporter(
				getVdpHandler(), getCanvas(), 
				getCanvasRenderer());
		
		synchronized (this) {
			stopRendering();
			renderThread = new RenderThread(vdpImporter, datas);
			renderThread.start();
		}
	}

	/**
	 * 
	 */
	public synchronized void stopRendering() {
		if (renderThread != null) {
			renderThread.cancel();
			try {
				renderThread.join();
			} catch (InterruptedException e) {
			}
			renderThread = null;
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.imageimport.IImageImportHandler#dispose()
	 */
	@Override
	public void dispose() {
		stopRendering();
	}

}