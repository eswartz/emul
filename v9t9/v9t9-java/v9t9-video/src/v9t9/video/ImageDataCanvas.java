/*
  ImageDataCanvas.java

  (c) 2008-2012 Edward Swartz

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

import java.nio.Buffer;

import org.eclipse.swt.graphics.ImageData;
import v9t9.common.video.BitmapVdpCanvas;


public abstract class ImageDataCanvas extends BitmapVdpCanvas {

	protected ImageData imageData;
	protected int bytesPerLine;

	protected int pixSize;
	
	public ImageDataCanvas() {
		super();
	}

	public ImageData getImageData() {
		return imageData;
	}

	@Override
	final public int getLineStride() {
		return bytesPerLine;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.video.ICanvas#getPixelStride()
	 */
	@Override
	final public int getPixelStride() {
		return pixSize;
	}


	@Override
	public void doChangeSize() {
		createImageData();
	}

	/** Create imageData and set bytesPerLine */
	abstract protected void createImageData();

	public int getDisplayAdjustOffset() {
		int displayAdjust = getYOffset() * getLineStride() + getXOffset() * getPixelStride();
		return displayAdjust;
	}


	/**
	 * @param buffer
	 * @return 
	 */
	public Buffer copy(Buffer buffer) {
		return copyBytes(buffer, imageData.data, imageData.bytesPerLine, imageData.bytesPerLine / imageData.width);
	}
	
	
}