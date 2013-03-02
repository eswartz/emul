/*
  BitmapCanvasShortGreyscale.java

  (c) 2012-2013 Edward Swartz

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
package v9t9.video;


/**
 * Render video content into a short array
 * @author ejs
 *
 */
public class BitmapCanvasShortGreyscale extends BitmapCanvasShort {
	/* (non-Javadoc)
	 * @see v9t9.video.IGLDataCanvas#getImageType()
	 */
	@Override
	public int getImageType() {
		return GL_UNSIGNED_SHORT_5_6_5;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.video.IGLDataCanvas#getImageFormat()
	 */
	@Override
	public int getImageFormat() {
		return GL_RGB;
	}

	/* (non-Javadoc)
	 * @see v9t9.video.IGLDataCanvas#getInternalFormat()
	 */
	@Override
	public int getInternalFormat() {
		return GL_LUMINANCE12_ALPHA4;
	}

}
