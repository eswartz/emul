/*
  EmulatedDiskImageFile.java

  (c) 2012 Edward Swartz

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
package v9t9.common.files;

import java.io.IOException;

/**
 * @author ejs
 *
 */
public class EmulatedDiskImageFile extends EmulatedBaseFDRFile implements EmulatedFile {

	private IDiskImage diskImage;

	public EmulatedDiskImageFile(IDiskImage diskImage, FDR fdr, String name) {
		super(fdr);
		this.diskImage = diskImage;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.files.NativeFile#readContents(byte[], int, int, int)
	 */
	@Override
	public int readContents(byte[] contents, int contentOffset, int offset,
			int length) throws IOException {
		int secOffs = offset / 256;
		if (secOffs < 0)
			throw new IllegalArgumentException();
		
		int[] fileSecs = fdr.getContentSectors();
		
		int origOffset = contentOffset;
		
		boolean wasOpen = diskImage.isDiskImageOpen();
		if (!wasOpen)
			diskImage.openDiskImage();
		
		byte[] sector = new byte[256];
		int lastSector = -1;
		
		while (length > 0 ) {
			if (fileSecs.length < secOffs)
				break;
		
			int toRead = Math.min(256, length);
			if (lastSector != fileSecs[secOffs]) {
				//diskImage.readSector(fileSecs[secOffs], contents, contentOffset, toRead);
				diskImage.readSector(fileSecs[secOffs], sector, 0, 256);
				lastSector = fileSecs[secOffs];
			}
			System.arraycopy(sector, offset % 256, contents, contentOffset, toRead);
			
			secOffs++;
			
			contentOffset += toRead;
			length -= toRead;
		}
		
		if (!wasOpen)
			diskImage.closeDiskImage();
		
		return contentOffset - origOffset;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.files.NativeFile#writeContents(byte[], int, int, int)
	 */
	@Override
	public int writeContents(byte[] contents, int contentOffset, int offset,
			int length) throws IOException {
		throw new IOException("write operation not implemented");
	}

	/* (non-Javadoc)
	 * @see v9t9.common.files.NativeFile#validate()
	 */
	@Override
	public void validate() throws InvalidFDRException {
		fdr.validate();
	}

	/* (non-Javadoc)
	 * @see v9t9.common.files.NativeFile#flush()
	 */
	@Override
	public void flush() throws IOException {
		throw new IOException("write operation not implemented");
	}


}
