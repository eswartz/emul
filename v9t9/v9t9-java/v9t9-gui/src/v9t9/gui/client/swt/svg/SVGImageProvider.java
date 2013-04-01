/*
  SVGImageProvider.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.svg;

import java.util.TreeMap;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import v9t9.gui.client.swt.bars.IImageCanvas;
import v9t9.gui.client.swt.bars.MultiImageSizeProvider;
import v9t9.gui.client.swt.imageimport.ImageUtils;
import ejs.base.utils.Pair;



/**
 * @author ejs
 *
 */
public class SVGImageProvider extends MultiImageSizeProvider {

	private static boolean DEBUG = false;
	
	private final ISVGLoader svgLoader;
	private Thread loadIconThread;
	
	private Point desiredSize;
	private Image scaledImage;
	private boolean svgFailed;
	private IImageCanvas imageCanvas;

	/**
	 * @param iconMap
	 */
	public SVGImageProvider(TreeMap<Integer, Image> iconMap, ISVGLoader svgIcon) {
		super(iconMap);
		this.svgLoader = svgIcon;
	}

	public void setImageCanvas(IImageCanvas imageCanvas) {
		this.imageCanvas = imageCanvas;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.MultiImageSizeProvider#getImage(org.eclipse.swt.graphics.Point)
	 */
	@Override
	public synchronized Pair<Double, Image> getImage(final int sx, final int sy) {
		boolean recreate = false;
		final Point size = new Point(sx, sy);
		if (scaledImage == null || !size.equals(desiredSize)) {
			if (loadIconThread == null || !svgLoader.isSlow()) {
				recreate = true;
			}
		}
		if (!svgFailed && recreate) {
			desiredSize = size;
			if (scaledImage != null)
				scaledImage.dispose();
			scaledImage = null;
			
			if (svgLoader.isSlow()) {
				loadIconThread = new Thread("Scaling icon") {

					public void run() {
						fetchImage(desiredSize);
						loadIconThread = null;
					}
					
				};
				
				loadIconThread.start();
			}
			else {
				fetchImage(size);
			}
		}
		if (scaledImage == null) {
			return super.getImage(sx, sy);
		}
		else {
			int min = iconMap.values().iterator().next().getBounds().width;
			double ratio = (double) scaledImage.getBounds().width / min;
			if (DEBUG) System.out.println("Using svg image " + scaledImage.getBounds() + " at " +ratio);
			return new Pair<Double, Image>(ratio, scaledImage);
		}
		
	}

	/**
	 * @param size
	 */
	protected void fetchImage(final Point size) {
		if (!svgLoader.isValid()) {
			svgFailed = true;
			return;
		}
		
		synchronized (this) {
			while (!svgLoader.isLoaded()) {
				try {
					wait(100);
				} catch (InterruptedException e) {
					svgFailed = true;
					return;
				}
			}
			
			//int min = iconMap.values().iterator().next().getBounds().width;
			
			final Composite composite = imageCanvas.getComposite();
			
			Point scaledSize = new Point(size.x, size.y);
			Point svgSize = svgLoader.getSize();
			scaledSize.y = size.y * svgSize.y / svgSize.x;
			
			long start = System.currentTimeMillis();
			final ImageData scaledImageData;
			try {
				scaledImageData = ImageUtils.convertAwtImageData(svgLoader.getImageData(scaledSize));
			} catch (SVGException e) {
				e.printStackTrace();
				svgFailed = true;
				return;
			}
			long end = System.currentTimeMillis();
			if (DEBUG)
				System.out.println("Loaded " + svgLoader.getURI() + " @ " + scaledSize + ": " + (end - start) + " ms");
			svgFailed = false;
			
			if (composite != null && !composite.isDisposed() && scaledImageData != null) {
				composite.getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (composite.isDisposed())
							return;
						scaledImage = new Image(composite.getDisplay(), scaledImageData);
						if (DEBUG)
							System.out.println("Got image " + scaledImage.getBounds());
						imageCanvas.redrawAll();
					}
				});
			}
			
		}
	}
}
