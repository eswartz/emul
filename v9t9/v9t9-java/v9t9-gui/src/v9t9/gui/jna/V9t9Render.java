/*
  V9t9Render.java

  (c) 2010-2011 Edward Swartz

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
package v9t9.gui.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;

/**
 * @author ejs
 *
 */
public interface V9t9Render extends Library {
	V9t9Render INSTANCE = (V9t9Render) Native.loadLibrary(
			"v9t9render" + (System.getProperty("os.arch").matches("x86_64|amd64") ? "64" : "32"), 
			V9t9Render.class);
	
	void        scaleImage(
			byte[] out,
	        byte[] in,  int offset,
	        int width, int height, int rowstride,
	        int destWidth, int destHeight, int destRowstride,
	        int upx, int upy, int upwidth, int upheight);

	void        scaleImageToRGBA(
			int[] out,
	        byte[] in, int offset,
	        int width, int height, int rowstride,
	        int destWidth, int destHeight, int destRowstride,
	        int upx, int upy, int upwidth, int upheight);
	void        scaleImageToRGBA(
			int[] out,
			Pointer in, int offset,
			int width, int height, int rowstride,
			int destWidth, int destHeight, int destRowstride,
			int upx, int upy, int upwidth, int upheight);

	void addNoise(byte[] data, int offset, int destWidth, int destHeight, int destrowstride,
			int width, int height);
			
	void addNoiseRGBA(int [] target, int[] src, int offset, int end,
			int destWidth, int destHeight, int destrowstride,
			int width, int height, int fullHeight);
	
	void addNoiseRGBAMonitor(int [] target, int[] src, int offset, int end,
			int destWidth, int destHeight, int destrowstride,
			int width, int height, int fullHeight);
	
	public static class AnalogTVData extends Structure implements Structure.ByReference {
		public Pointer image = null;
		public int width = 0, height = 0, bytes_per_line = 0;
	}
	public static class AnalogTV extends PointerType {
	}
	

	AnalogTV allocateAnalogTv(int width, int height);
	void freeAnalogTv(AnalogTV analog);
	AnalogTVData getAnalogTvData(AnalogTV analog);
	        
	void 		analogizeImageData(
			AnalogTV analog,
			byte[] in, int srcoffset, 
			int width, int height, int rowstride);


}
