/**
 * 
 */
package v9t9.emulator.clients.builtin.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;

/**
 * @author ejs
 *
 */
public interface V9t9Render extends Library {
	V9t9Render INSTANCE = (V9t9Render) Native.loadLibrary("v9t9render", V9t9Render.class);
	
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

	void addNoise(byte[] data, int offset, int destWidth, int destHeight, int destrowstride,
			int width, int height);
			
	void addNoiseRGBA(int [] buffer, int offset, int destWidth, int destHeight, int destrowstride,
			int width, int height);
	
	public static class AnalogTVData extends Structure implements Structure.ByReference {
		public byte[] image = null;
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
