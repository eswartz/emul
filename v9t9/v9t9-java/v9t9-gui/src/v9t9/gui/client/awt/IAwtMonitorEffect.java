/*
  IAwtMonitorEffect.java

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
package v9t9.gui.client.awt;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import v9t9.common.client.IMonitorEffect;

/**
 * @author ejs
 *
 */
public interface IAwtMonitorEffect extends IMonitorEffect {

	/**
	 * @param destWidth
	 * @param destHeight
	 * @param surface
	 * @param logRect
	 * @param physRect
	 * @return
	 */
	BufferedImage applyEffect(int destWidth, int destHeight,
			BufferedImage surface, Rectangle logRect, Rectangle physRect);

}
