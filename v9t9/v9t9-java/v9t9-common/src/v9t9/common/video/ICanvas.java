/*
  ICanvas.java

  (c) 2011-2013 Edward Swartz

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
public interface ICanvas {

	void setSize(int x, int y);

	/** Get the full screen width (this includes any overscan and possibly extra pixels
	 * not intended to be seen). */
	int getWidth();

	/** Get the full screen width (this includes any overscan and possibly extra pixels
	 * not intended to be seen). */
	int getVisibleWidth();

	/** Get the nominal screen height. This does not count interlacing. */
	int getHeight();

	int getVisibleHeight();
	

	/** Get the delta for one pixel, in terms of the offset. 
	 * @see #getBitmapOffset(int, int) 
	 */ 
	int getPixelStride();
	/** Get the delta for one row, in terms of the offset. 
	 * @see #getBitmapOffset(int, int) 
	 */ 
	int getLineStride();


	int getBlockCount();
	
	void markDirty();

	void markDirty(RedrawBlock[] blocks, int count);

}
