/*
  ISwtSprite.java

  (c) 2012 Edward Swartz

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
package v9t9.gui.client.swt;

import org.eclipse.swt.graphics.ImageData;

/**
 * @author ejs
 *
 */
public interface ISwtSprite {

	/** get image for sprite */
	ImageData getImageData();
	/** position, 0=center,  -1=left edge, 1=right edge */
	float getXPos();
	/** position, 0=center,  -1=top edge, 1=bottom edge */
	float getYPos();
	/** size, proportion of width */
	float getXSize();
	/** size, proportion of height */
	float getYSize();
}
