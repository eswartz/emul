/**
 * 
 */
package v9t9.tests.video;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import v9t9.emulator.clients.builtin.video.ColorMapUtils;

/**
 * Test how many distinct colors we can wring out of an RGB
 * palette if greyscale mode is enabled.
 * @author ejs
 *
 */
public class TestGreyscaleTheory {

	public static void main(String[] args) {
		Set<Integer> vals = new TreeSet<Integer>();
		
		for (int g = 0; g < 256; g += 0x20) {
			for (int r = 0; r < 256; r += 0x20) {
				for (int b = 0; b < 256; b += 0x20) {
					byte[] rgb = new byte[] { (byte) (r * 0xff / 0xe0), 
							(byte) (g * 0xff / 0xe0), 
							(byte) (b * 0xff / 0xe0) };
					byte[] greys = ColorMapUtils.rgbToGrey(rgb);
					int color = ColorMapUtils.rgb8ToPixel(greys);
					vals.add(color);
				}
			}
		}
		
		System.out.println("# colors: " + vals.size());
		
		Map<Integer, Integer> greyToRgbMap = new TreeMap<Integer, Integer>();
		
		for (int g = 0; g < 256; g += 0x20) {
			for (int r = 0; r < 256; r += 0x20) {
				for (int b = 0; b < 256; b += 0x20) {
					byte[] rgb = new byte[] { (byte) (r * 0xff / 0xe0), 
							(byte) (g * 0xff / 0xe0), 
							(byte) (b * 0xff / 0xe0) };
					byte[] greys = ColorMapUtils.rgbToGrey(rgb);
					int lum = greys[0] & 0xff;
					if (!greyToRgbMap.containsKey(lum)) {
						int color = ColorMapUtils.rgb8ToPixel(rgb);
						greyToRgbMap.put(lum, color);
					}
				}
			}
		}
		for (Map.Entry<Integer, Integer> ent : greyToRgbMap.entrySet()) {
			System.out.printf("%d:\t%02x %02x %02x\n",
					ent.getKey(), 
					(ent.getValue() & 0xff0000) >> 16,
					(ent.getValue() & 0x00ff00) >> 8,
					(ent.getValue() & 0x0000ff) >> 0);
		}
	}

}
