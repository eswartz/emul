package v9t9.gui.client.swt.svg;

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

    private URI uri;
	private BufferedImage image;
	private SVGUniverse universe;
	private SVGDiagram diagram;

    public SVGSalamanderLoader(URL url) {
		universe = SVGCache.getSVGUniverse();
        diagram = null;

        uri = universe.loadSVG(url);
        diagram = universe.getDiagram(uri);
		
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
	public boolean isValid() {
		return diagram != null;
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.ISVGLoader#isSlow()
	 */
	@Override
	public boolean isSlow() {
		return false;
	}
    /* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.ISVGLoader#getImageData(org.eclipse.swt.graphics.Point)
	 */
	@Override
	public BufferedImage getImageData(Point size) throws SVGException {
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
			return new Point(r.width, r.height);
		} catch (Exception e) {
			return null;
		}
	}

    private BufferedImage load(Rectangle aoi, Point size) {

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

}