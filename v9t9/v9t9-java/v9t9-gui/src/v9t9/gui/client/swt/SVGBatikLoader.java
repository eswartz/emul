package v9t9.gui.client.swt;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class manages a reference to a loaded SVG document
 * and provides SWT Images from it
 * @author eswartz
 *
 */
public class SVGBatikLoader implements ISVGLoader {

	/** Pattern to match sizes which convert roughly to pixels */
	private static Pattern sizePattern = Pattern.compile("(\\d+)(pt|px)?"); //$NON-NLS-1$

    private SWTImageTranscoder trans;
    private Document document;
    private String uri;

	private TranscoderInput transcoderInput;

    public SVGBatikLoader(URL url) {
    	try {
			this.uri = url.toURI().toString();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
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
		final SVGBatikLoader other = (SVGBatikLoader) obj;
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
		return uri != null;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.ISVGLoader#isSlow()
	 */
	@Override
	public boolean isSlow() {
		return true;
	}
	
    /**
     * Create an SVG document from an absolute file. 
     * @return SVG XML DOM
     */
    private Document getSvgDomFromFile(String uri) throws Exception {
        Document mydoc = null;
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
        mydoc = f.createDocument(uri);
        return mydoc;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.ISVGLoader#getImageData(org.eclipse.swt.graphics.Point)
	 */
	@Override
	public ImageData getImageData(Point size) throws SVGException {
		try {
			if (document == null) {
				document = getSvgDomFromFile(uri);
			}
			TranscoderInput transcoderInput = new TranscoderInput(document);
			transcoderInput.setURI(uri);
			return load(transcoderInput, size);
		} catch (Exception e) {
			if (e instanceof TranscoderException) {
				TranscoderException tce = (TranscoderException) e;
				if (tce.getException() != null)
					e = tce.getException();
			}
			throw new SVGException("Failed to read SVG image from " + uri, e);
		}
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.ISVGLoader#getImageData(org.eclipse.swt.graphics.Rectangle, org.eclipse.swt.graphics.Point)
	 */
	@Override
	public ImageData getImageData(Rectangle aoi, Point size) throws SVGException {
		try {
			if (document == null) {
				document = getSvgDomFromFile(uri);
			}
			if (transcoderInput == null) { 
				transcoderInput = new TranscoderInput(document);
				transcoderInput.setURI(uri);
			}
			return load(transcoderInput, aoi, size);
		} catch (Exception e) {
			if (e instanceof TranscoderException) {
				TranscoderException tce = (TranscoderException) e;
				if (tce.getException() != null)
					e = tce.getException();
			}
			throw new SVGException("Failed to load " + uri, e);
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.ISVGLoader#getSize()
	 */
	@Override
	public Point getSize() {
		try {
			if (document == null) {
				document = getSvgDomFromFile(uri);
			}
			if (transcoderInput == null) { 
				transcoderInput = new TranscoderInput(document);
				transcoderInput.setURI(uri);
			}
			Document doc = transcoderInput.getDocument();
	        if (doc == null)
	            return null;

	        return readDefaultSize(doc);
		} catch (Exception e) {
			return null;
		}
	}

    private ImageData load(TranscoderInput input, Point size)
            throws TranscoderException {

    	//System.out.println("loading svg " + uri + " at " + size);
    	
        // create the transcoder output
        TranscoderOutput output = null;

        // create a transcoder
        trans = new SWTImageTranscoder();

        // be sure to execute script code every time
        trans.addTranscodingHint(ImageTranscoder.KEY_EXECUTE_ONLOAD,
                new Boolean(true));

        //trans.addTranscodingHint(SWTImageTranscoder.KEY_USE_TRANSPARENT_PIXEL,
        //        new Boolean(true));

        Document doc = input.getDocument();
        if (doc == null)
            return null;

        Point defaultSize = readDefaultSize(doc);
        
        if (size == null || (size.x < 0 && size.y < 0)) {
            // Provide a hard cap on the size of ImageData provided for the default size (bug 5363).
        	// Let explicit requests for the larger sizes succeed and fail.
            if ((long)defaultSize.x * defaultSize.y > 2048 * 2048) {
            	// 2048*2048*32bpp = 16 megs!
            	defaultSize = ImageUtils.scaleSizeToSize(defaultSize, new Point(2048, 2048));
            }
        	size = defaultSize;
        } else if (size.x < 0 && size.y > 0) {
        	// aspect with y set
        	size = new Point(defaultSize.x * size.y / defaultSize.y, size.y);
        } else if (size.x > 0 && size.y < 0) {
        	// aspect with x set
        	size = new Point(size.x, defaultSize.y * size.x / defaultSize.x);
        } else  {
        	// non-aspect 
        }
        
        //System.out.println("dims: "+theWidth+","+theHeight);
        if (size != null && size.x > 0) {
            trans.addTranscodingHint(ImageTranscoder.KEY_WIDTH, new Float(
            		size.x));
            trans.addTranscodingHint(ImageTranscoder.KEY_MAX_WIDTH, new Float(
            		size.x));
        }
        if (size != null && size.y > 0) {
            trans.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, new Float(
                size.y));
            trans.addTranscodingHint(ImageTranscoder.KEY_MAX_HEIGHT, new Float(
                    size.y));
        }

        // save the image
        trans.transcode(input, output);

        ImageData imageData = trans.getImageData();
        
        // if the size is not what we expected, force it
        // (we may desire a different aspect ratio)
        /*
        Display display = Display.getDefault();
        ImageDump dump = new ImageDump(new Shell(display), new Image(display, imageData));
        dump.open();
        while (display.readAndDispatch()) {
        	
        }
        */
        
        if (imageData != null && size.x > 0 && size.y > 0) {
        	if (imageData.width != size.x || imageData.height != size.y) {
        		imageData = ImageUtils.scaleImageData(imageData, size, false, false);
        	}
        }
        
        return imageData;
    }

    private ImageData load(TranscoderInput input, Rectangle aoi, Point size)
			throws TranscoderException {

    	//System.out.println("rendering svg " + aoi + " at " + size);

		// create the transcoder output
		TranscoderOutput output = null;

		// create a transcoder
		trans = new SWTImageTranscoder();

		// be sure to execute script code every time
		trans.addTranscodingHint(ImageTranscoder.KEY_EXECUTE_ONLOAD,
				new Boolean(true));

		trans.addTranscodingHint(ImageTranscoder.KEY_AOI,
				new java.awt.Rectangle(aoi.x, aoi.y, aoi.width, aoi.height));

		Document doc = input.getDocument();
		if (doc == null)
			return null;

		trans.addTranscodingHint(ImageTranscoder.KEY_WIDTH, new Float(
				size.x));
		trans.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, new Float(
				size.y));
		/*
		trans.addTranscodingHint(ImageTranscoder.KEY_MAX_WIDTH, new Float(
				size.x));
		trans.addTranscodingHint(ImageTranscoder.KEY_MAX_HEIGHT, new Float(
				size.y));
*/
		// save the image
		trans.transcode(input, output);

		ImageData imageData = trans.getImageData();

		// if the size is not what we expected, force it
		// (we may desire a different aspect ratio)
		/*
		 * Display display = Display.getDefault(); ImageDump dump = new
		 * ImageDump(new Shell(display), new Image(display, imageData));
		 * dump.open(); while (display.readAndDispatch()) {
		 * 
		 * }
		 */

		if (imageData != null && size.x > 0 && size.y > 0) {
			if (imageData.width != size.x || imageData.height != size.y) {
				imageData = ImageUtils.scaleImageData(imageData, size, false,
						false);
			}
		}

		return imageData;
	}
    
    /**
     * Read the size of the image as specified in the SVG.
     * @param doc
     * @return Point where x or y is -1 for the default size.
     */
	private Point readDefaultSize(Document doc) {
		Point size = new Point(-1, -1);
        Element root = doc.getDocumentElement();
        if (root != null) {
        	// the width and height should be trusted first (bug 5363)
        	//
            // This may be an absolute value, assumed to be pixels,
            // or a percentage or a value with units.  Only accept
            // it if it parses cleanly as an int, meaning pixels.
            String width = root.getAttributeNS(null, "width"); //$NON-NLS-1$
            String height = root.getAttributeNS(null, "height"); //$NON-NLS-1$
            if (width != null) {
            	Matcher matcher = sizePattern.matcher(width);
            	if (matcher.matches())
            		size.x = Integer.parseInt(matcher.group(1));
            }
            if (height != null) {
            	Matcher matcher = sizePattern.matcher(height);
            	if (matcher.matches())
            		size.y = Integer.parseInt(matcher.group(1));
            }
        	
            // try the viewBox
            if (size.x < 0 || size.y < 0) {
	            String viewBox = root.getAttributeNS(null, "viewBox"); //$NON-NLS-1$
	            if (viewBox != null) {
	                String number = "(-?\\d+(\\.\\d+)?)"; //$NON-NLS-1$
	                Pattern pattern = Pattern.compile(
	                        "\\s*" + number +  //$NON-NLS-1$
	                        ",?\\s+" + number + //$NON-NLS-1$
	                        ",?\\s+" + number +  //$NON-NLS-1$
	                        ",?\\s+" + number + //$NON-NLS-1$
	                        "\\s*"); //$NON-NLS-1$
	                Matcher matcher = pattern.matcher(viewBox);
	                if (matcher.matches()) {
	                    size.x = (int)Math.round(Double.parseDouble(matcher.group(5)));
	                    size.y = (int)Math.round(Double.parseDouble(matcher.group(7)));
	                }
	            }

            }
        }
		return size;
	}



	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.ISVGLoader#getURI()
	 */
	@Override
	public String getURI() {
		return uri;
	}

}