/*
  ImageImportHandler.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.imageimport;

import java.util.Collection;
import java.util.LinkedHashSet;

import v9t9.common.hardware.IVdpChip;
import v9t9.common.video.IVdpCanvas;
import v9t9.common.video.IVdpCanvasRenderer;
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
		return new ImageImport(getCanvas());
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
		
		ImageImportData[] datas = importImage(importer);
		displayImages(datas);
	}

	public Collection<String> getHistory() {
		return urlHistory;
	}
	

	/**
	 * @param importer
	 * @return converted frame(s)
	 */
	protected ImageImportData[] importImage(ImageImport importer) {
		
		imageImportOptions.setFormat(getCanvas().getFormat());
		int targWidth = getCanvas().getVisibleWidth();
		int targHeight = getCanvas().getVisibleHeight();
		
		return importer.importImage(imageImportOptions, targWidth, targHeight);
	

	}

	public void displayImages(ImageImportData[] datas) {

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