/*
  SVGSalamanderLoader.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video.svg;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.net.URI;
import java.net.URL;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.kitfox.svg.SVGCache;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;

/**
 * This class manages a reference to a loaded SVG document
 * and provides SWT Images from it
 * @author eswartz
 *
 */
public class SVGSalamanderLoader implements ISVGLoader {
	private static boolean DEBUG = false;
	
    private URI uri;
	private BufferedImage image;
	private SVGDiagram diagram;
	private Thread loaderThread;

    public SVGSalamanderLoader(final URL url) {
    	loaderThread = new Thread() {
    		/* (non-Javadoc)
    		 * @see java.lang.Thread#run()
    		 */
    		@Override
    		public void run() {
    			if (DEBUG) System.err.println("*** start");
    			try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
    			if (DEBUG) System.err.println("*** loading");
    			SVGUniverse universe = SVGCache.getSVGUniverse();
    			URI uri = universe.loadSVG(url);
    			SVGDiagram diagram = universe.getDiagram(uri);
				synchronized (SVGSalamanderLoader.this) {
					SVGSalamanderLoader.this.uri = uri;
					SVGSalamanderLoader.this.diagram = diagram;
					if (DEBUG) System.err.println("*** done");
					SVGSalamanderLoader.this.notifyAll();
    			}
    		}
    	};
    	loaderThread.start();
		
        /*
		this.icon = new SVGIcon();
		icon.setSvgURI(url.toURI());
		icon.setAntiAlias(true);
		icon.setInterpolation(SVGIcon.INTERP_BICUBIC);
		*/
    }
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final SVGSalamanderLoader other = (SVGSalamanderLoader) obj;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.ISVGLoader#isValid()
	 */
	@Override
	public synchronized boolean isValid() {
		return loaderThread.isAlive() || diagram != null;
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.ISVGLoader#isSlow()
	 */
	@Override
	public synchronized boolean isSlow() {
		return diagram == null;
	}
    /* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.ISVGLoader#getImageData(org.eclipse.swt.graphics.Point)
	 */
	@Override
	public BufferedImage getImageData(Point size) throws SVGException {
		synchronized (this) {
			if (diagram == null)
				throw new SVGException("Failed to read SVG image from " + uri, null);
		}
			
		try {
			java.awt.Rectangle r = getNativeRect();
			return load(new Rectangle(r.x, r.y, r.width, r.height), size);
		} catch (Exception e) {
			throw new SVGException("Failed to read SVG image from " + uri, e);
		}
	}

	/**
	 * @return
	 */
	protected java.awt.Rectangle getNativeRect() {
		synchronized (this) {
			if (diagram == null)
				return null;
		}
		Rectangle2D r = diagram.getViewRect();
		return new java.awt.Rectangle((int) r.getMinX(), (int) r.getMinY(), 
				(int) Math.round(r.getMaxX() - r.getMinX()), 
				(int) Math.round(r.getMaxY() - r.getMinY()));
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.ISVGLoader#getImageData(org.eclipse.swt.graphics.Rectangle, org.eclipse.swt.graphics.Point)
	 */
	@Override
	public BufferedImage getImageData(Rectangle aoi, Point size) throws SVGException {
		try {
			return load(aoi, size);
		} catch (Exception e) {
			throw new SVGException("Failed to load " + uri, e);
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.ISVGLoader#getSize()
	 */
	@Override
	public Point getSize() {
		try {
			java.awt.Rectangle r = getNativeRect();
			if (r == null)
				return null;
			return new Point(r.width, r.height);
		} catch (Exception e) {
			return null;
		}
	}

    private synchronized BufferedImage load(Rectangle aoi, Point size) {

    	if (size.x == 0 || size.y == 0)
    		return null;
    	
    	java.awt.Rectangle r = getNativeRect();
    	
    	Rectangle scaledAoi = new Rectangle(aoi.x * size.x / r.width,
    			aoi.y * size.y / r.height,
    			aoi.width * size.x / r.width, 
    			aoi.height * size.y / r.height);
    	
    	if (image == null || image.getWidth() != size.x) {
	    	image = new BufferedImage(size.x, 
	    			size.y, 
	    			BufferedImage.TYPE_4BYTE_ABGR);
	    	
	    	diagram.setDeviceViewport(new java.awt.Rectangle(aoi.x, aoi.y, aoi.width, aoi.height));
	    	
	    	Graphics2D g = image.createGraphics();
	        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

	        g.setClip(0, 0, r.width, r.height);
	    	g.scale((double) size.x / r.width,
	    			(double) size.y / r.height);
	    	//icon.setPreferredSize(new Dimension(size.x, size.y));
	    	//icon.setScaleToFit(true);
	    	try {
	    		//System.out.println("rendering...");
				diagram.render(g);
			} catch (com.kitfox.svg.SVGException e) {
				e.printStackTrace();
				return null;
			}
	    	g.dispose();
	    	
    	}

    	BufferedImage sub;
    	
    	if (scaledAoi.width != image.getWidth() || scaledAoi.height != image.getHeight()) {
	        ColorModel cm = image.getColorModel();
	        WritableRaster wr = image.getRaster().createCompatibleWritableRaster(
	        		scaledAoi.width, scaledAoi.height);
	        wr.setRect(-scaledAoi.x, -scaledAoi.y, image.getRaster());
	
	        sub = new BufferedImage(cm, wr, cm.isAlphaPremultiplied(), null);
	        //data = imageData = ImageUtils.convertAwtImageData(sub);
    	} else {
    		sub = image;
    		//data = imageData;
    	}
    	
        return sub;
    }


	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.ISVGLoader#getURI()
	 */
	@Override
	public String getURI() {
		return uri.toString();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.svg.ISVGLoader#isLoaded()
	 */
	@Override
	public synchronized boolean isLoaded() {
		return !loaderThread.isAlive();
	}

}