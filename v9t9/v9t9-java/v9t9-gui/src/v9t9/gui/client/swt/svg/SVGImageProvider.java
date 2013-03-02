/*
  SVGImageProvider.java

  (c) 2010-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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
						try {
							Thread.sleep(100);
						} catch (InterruptedException e1) {
							return;
						}
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
		final ImageData scaledImageData;
		if (!svgLoader.isValid()) {
			svgFailed = true;
			return;
		}
		
		try {
			//int min = iconMap.values().iterator().next().getBounds().width;
			Point scaledSize = new Point(size.x, size.y);
			Point svgSize = svgLoader.getSize();
			scaledSize.y = size.y * svgSize.y / svgSize.x;
			
			long start = System.currentTimeMillis();
			
			scaledImageData = ImageUtils.convertAwtImageData(svgLoader.getImageData(scaledSize));
			long end = System.currentTimeMillis();
			if (DEBUG)
				System.out.println("Loaded " + svgLoader.getURI() + " @ " + scaledSize + ": " + (end - start) + " ms");
			svgFailed = false;
			
			final Composite composite = imageCanvas.getComposite();
			
			if (composite != null && !composite.isDisposed() && scaledImageData != null) {
				Runnable runnable = new Runnable() {
					public void run() {
						if (!composite.isDisposed()) {
							scaledImage = new Image(composite.getDisplay(), scaledImageData);
							if (DEBUG)
								System.out.println("Got image " + scaledImage.getBounds());
							imageCanvas.redrawAll();
						}
					}
				};
				if (svgLoader.isSlow())
					composite.getDisplay().asyncExec(runnable);
				else
					composite.getDisplay().syncExec(runnable);
			}
		} catch (SVGException e) {
			e.printStackTrace();
			svgFailed = true;
		}
	}
}
