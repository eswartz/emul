/*
  IDiskImage.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.files;

import java.io.IOException;
import java.util.List;

import ejs.base.properties.IPersistable;


public interface IDiskImage extends IPersistable, IEmulatedDisk {
	public interface SectorUpdater {
		boolean updateSector(byte[] content) throws IOException;
	}

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
	
	void writeTrackData(byte[] rwBuffer, int start,
			int buflen);

	void writeSectorData(byte[] rwBuffer, int start,
			int buflen, IdMarker marker);


	boolean isReadOnly();
	
	/**
	 * 
	 */
	IDiskHeader getHeader();

	/**
	 * 
	 */
	void commitTrack() throws IOException;

	/**
	 * @return
	 */
	List<IdMarker> getTrackMarkers();

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
	 * @param rwBuffer
	 * @param i
	 * @param buflen
	 * @throws IOException 
	 */
	void readTrackData(byte[] rwBuffer, int i, int buflen) throws IOException;


	void setSide(int side) throws IOException;
	int getSide();

	int getTrack();
	void setTrack(int i) throws IOException;


	void updateSector(int sector, SectorUpdater sectorUpdater) throws IOException;
	
	/** stupid sector reader */
	IdMarker readSector(int sector, byte[] rwBuffer, int start, int buflen) throws IOException;

	/**
	 * @param start
	 * @return
	 * @throws IOException
	 */
	int allocateSector(int start) throws IOException;

}