/*
  ISpriteDrawingCanvas.java

  (c) 2011 Edward Swartz

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
package v9t9.common.video;

/**
 * @author ejs
 *
 */
public interface ISpriteDrawingCanvas {

	public void drawEightDoubleMagnifiedSpritePixels(int x, int y, byte mem, byte fg, short bitmask, boolean isLogicalOr);
	public void drawEightMagnifiedSpritePixels(int x, int y, byte mem, byte fg, short bitmask, boolean isLogicalOr);
	public void drawEightSpritePixels(int x, int y, byte mem, byte fg, byte bitmask, boolean isLogicalOr);
	/**
	 * @return
	 */
	public VdpColorManager getColorMgr();
	/**
	 * @return
	 */
	public int getHeight();
	/**
	 * @return
	 */
	public int getWidth();
	/**
	 * @return
	 */
	public int getVisibleWidth();


}
