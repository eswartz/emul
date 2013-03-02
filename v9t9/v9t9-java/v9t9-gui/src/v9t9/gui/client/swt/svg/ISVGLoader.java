/*
  ISVGLoader.java

  (c) 2011-2012 Edward Swartz

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

import java.awt.image.BufferedImage;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;


/**
 * @author ejs
 *
 */
public interface ISVGLoader {
	boolean isSlow();

	/**
	 * Transcode and create an image from the SVG.
	 * @param size the size to scale to, or null
	 * @return new ImageData
	 */
	BufferedImage getImageData(Point size) throws SVGException;

	/**
	 * Transcode and create an image from the SVG.
	 * @param aoi area of interest
	 * @param size the size to scale to, or null
	 * @return new ImageData
	 */
	BufferedImage getImageData(Rectangle aoi, Point size) throws SVGException;

	Point getSize();

	/**
	 * @return
	 */
	String getURI();

	/**
	 * @return
	 */
	boolean isValid();

}