/**
 * 
 */
package v9t9.emulator.hardware.dsrs.realdisk;

import java.io.File;

/**
 * @author ejs
 *
 */
public class DiskImageFactory {

	/**
	 * @param name
	 * @param file
	 * @return
	 */
	public static BaseDiskImage createDiskImage(String name, File file) {
		if (file.exists()) {
			if (V9t9TrackDiskImage.isTrackImage(file))
				return new V9t9TrackDiskImage(name, file);
			
			if (RawTrackDiskImage.isTrackImage(file))
				return new RawTrackDiskImage(name, file);
		}
		if (file.getName().toLowerCase().endsWith(".trk"))
			return new V9t9TrackDiskImage(name, file);
		else
			return new SectorDiskImage(name, file);
	}

}
