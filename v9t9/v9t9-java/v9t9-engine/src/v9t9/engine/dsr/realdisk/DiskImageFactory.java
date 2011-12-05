/**
 * 
 */
package v9t9.engine.dsr.realdisk;

import java.io.File;

import v9t9.common.client.ISettingsHandler;


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
	public static BaseDiskImage createDiskImage(ISettingsHandler settings, String name, File file) {
		if (file.exists()) {
			if (V9t9TrackDiskImage.isTrackImage(file))
				return new V9t9TrackDiskImage(name, file, settings);
			
			if (RawTrackDiskImage.isTrackImage(file))
				return new RawTrackDiskImage(name, file, settings);
		}
		if (file.getName().toLowerCase().endsWith(".trk"))
			return new V9t9TrackDiskImage(name, file, settings);
		else
			return new SectorDiskImage(name, file, settings);
	}

}
