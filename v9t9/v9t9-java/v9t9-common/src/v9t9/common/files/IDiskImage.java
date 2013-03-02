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


public interface IDiskImage {
	void readImageHeader() throws IOException;
	boolean isDiskImageOpen();
	void openDiskImage() throws IOException;
	void closeDiskImage() throws IOException;
	void writeImageHeader() throws IOException;
	byte[] readCurrentTrackData() throws IOException;
	long getTrackDiskOffset();
	void growImageForContent() throws IOException;
	

	String getDiskType();
	int getHeaderSize();
	
	void readSector(int sector, byte[] rwBuffer, int start, int buflen) throws IOException;
}