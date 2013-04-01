/*
  IDiskImage.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.files;

import java.io.IOException;
import java.util.List;

import ejs.base.properties.IPersistable;


public interface IDiskImage extends IPersistable {
	/**
	 * Get the name of the property associated with this image (also for diags)
	 * @return
	 */
	String getName();
	
	void readImageHeader() throws IOException;
	boolean isDiskImageOpen();
	void openDiskImage() throws IOException;
	void openDiskImage(boolean readOnly) throws IOException;

	void closeDiskImage() throws IOException;
	void writeImageHeader() throws IOException;
	byte[] readCurrentTrackData() throws IOException;
	long getTrackDiskOffset();
	void growImageForContent() throws IOException;
	

	String getDiskType();
	int getHeaderSize();
	
	void readSector(int sector, byte[] rwBuffer, int start, int buflen) throws IOException;

	void writeTrackData(byte[] rwBuffer, int start,
			int buflen);

	void writeSectorData(byte[] rwBuffer, int start,
			int buflen, IdMarker marker);


	/**
	 * Tell if the disk appears formatted
	 * @return
	 */
	boolean isFormatted();

	boolean isReadOnly();
	
	/**
	 * 
	 */
	IDiskHeader getHeader();
	
	Catalog readCatalog(String devname) throws IOException;

	/**
	 * 
	 */
	void commitTrack() throws IOException;

	/**
	 * @return
	 */
	List<IdMarker> getTrackMarkers();

	/**
	 * @param seektrack
	 * @param seekside
	 * @throws IOException 
	 */
	boolean seekToCurrentTrack(int seektrack, int seekside) throws IOException;

	/**
	 * @return
	 */
	int getTrackSize();

	/**
	 * @param currentMarker
	 * @param rwBuffer
	 * @param i
	 * @param buflen
	 */
	void readSectorData(IdMarker currentMarker, byte[] rwBuffer, int i,
			int buflen);

	/**
	 * @param i
	 */
	void setMotorTimeout(long millis);

	/**
	 * @return
	 */
	boolean isMotorRunning();

	/**
	 * @param rwBuffer
	 * @param i
	 * @param buflen
	 * @throws IOException 
	 */
	void readTrackData(byte[] rwBuffer, int i, int buflen) throws IOException;

}