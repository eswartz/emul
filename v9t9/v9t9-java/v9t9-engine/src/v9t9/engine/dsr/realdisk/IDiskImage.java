/**
 * Mar 1, 2011
 */
package v9t9.engine.dsr.realdisk;

import java.io.IOException;
import java.util.List;


public interface IDiskImage {
	void readImageHeader() throws IOException;
	void openDiskImage() throws IOException;
	void closeDiskImage() throws IOException;
	void writeImageHeader() throws IOException;
	byte[] readCurrentTrackData() throws IOException;
	long getTrackDiskOffset();
	void growImageForContent() throws IOException;
	

	String getDiskType();
	
	List<IdMarker> getTrackMarkers();

	void writeTrackData(byte[] rwBuffer, int i,
			int buflen, FDCStatus status);

	void writeSectorData(byte[] rwBuffer, int start,
			int buflen, IdMarker marker, FDCStatus status);

	int getHeaderSize();
}