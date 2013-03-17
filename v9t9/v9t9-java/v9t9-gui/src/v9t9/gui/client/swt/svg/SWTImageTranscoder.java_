/*
  SWTImageTranscoder.java

  (c) 2009-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.svg;

import java.awt.image.BufferedImage;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.keys.BooleanKey;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

import v9t9.gui.client.swt.imageimport.ImageUtils;

/**
 * This class transcodes the output from the transcoder 
 * from an AWT into an SWT-compatible image.
 * @author eswartz
 *
 */
public class SWTImageTranscoder extends ImageTranscoder {

    public static final TranscodingHints.Key KEY_USE_TRANSPARENT_PIXEL
    	= new BooleanKey();
    
    public SWTImageTranscoder() {
        super();
    }

    /* save rendered image */
    protected java.awt.image.BufferedImage img_;

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.batik.transcoder.image.ImageTranscoder#createImage(int,
     *      int)
     */
    public BufferedImage createImage(int width, int height) {
        BufferedImage img = new java.awt.image.BufferedImage(width, height,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        return img;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.batik.transcoder.image.ImageTranscoder#writeImage(java.awt.image.BufferedImage,
     *      org.apache.batik.transcoder.TranscoderOutput)
     * 
     * IGNORE output in this broken API
     */
    public void writeImage(BufferedImage img, TranscoderOutput output)
            throws TranscoderException {
        
        // TODO: try using the OutputStream 
        img_ = img;
    }

    /**
     * Get the image generated from the transcode operation.
     * This constructs a new, non-owned Image each time it is called. 
     * @param device
     * @return Image, owned by caller
     */
    public Image getImage(Device device) {
    	ImageData data = getImageData();
    	if (data == null)
    		return null;
    	return new Image(device, data);
    }

    public BufferedImage getBufferedImage() {
    	return img_;
    }
    /**
     * Get the image generated from the transcode operation.
     * This constructs a new, non-owned Image each time it is called. 
     * @param device
     * @return Image, owned by caller
     */
    public ImageData getImageData() {
        ImageData imageData = ImageUtils.convertAwtImageData(img_);
        boolean flattenAlpha = false;
        if (hints.containsKey(KEY_USE_TRANSPARENT_PIXEL)) {
            flattenAlpha = ((Boolean) hints
                    .get(KEY_USE_TRANSPARENT_PIXEL)).booleanValue();
        }
        if (flattenAlpha) {
            boolean blend = false;
            /*
            if (hints.containsKey(KEY_FORCE_TRANSPARENT_WHITE)) {
                blend = !((Boolean) hints
                        .get(KEY_FORCE_TRANSPARENT_WHITE)).booleanValue();
            }*/
            ImageData flattenedData = ImageUtils.flattenAlphaMaskedImageData(imageData, null, blend, true /*transparent*/);
            return flattenedData;
        } else {
            return imageData;
        }
    }
}