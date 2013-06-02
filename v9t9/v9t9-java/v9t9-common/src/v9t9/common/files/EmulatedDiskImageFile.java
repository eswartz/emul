/*
  EmulatedDiskImageFile.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.files;

import java.io.IOException;

/**
 * @author ejs
 *
 */
public class EmulatedDiskImageFile extends EmulatedBaseFDRFile implements IEmulatedFile {

	private IDiskImage diskImage;
	private int indexSector;

	public EmulatedDiskImageFile(IDiskImage diskImage, int indexSector, FDR fdr, String name) {
		super(diskImage, fdr);
		if (false == fdr instanceof DiskImageFDR)
			throw new IllegalArgumentException();
		this.diskImage = diskImage;
		this.indexSector = indexSector;
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
			if (fileSecs.length <= secOffs)
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
	public int writeContents(final byte[] contents, final int startContentOffset, 
			final int startOffset, final int length) throws IOException {

		int[] secs = getContentSectors();
		if (startOffset + length > secs.length * 256) {
			throw new IOException("writing past end of file (only " + secs.length + " sectors allocated)");
		}
		
		int left = length;
		int offset = startOffset;
		while (left > 0) {
			int toWrite = Math.min(256 - offset % 256, left);
			int sec = secs[offset / 256];
			if (sec < 0) {
				throw new IOException("corrupt FDR: not enough sectors allocated at position " + offset);
			}
			writeContentToSector(sec, 
					contents, startContentOffset + offset % 256, 
					offset % 256, toWrite);
			left -= toWrite;
			offset += toWrite;
		}
		return length;
	}

	/**
	 * @throws IOException 
	 */
	protected void writeContentToSector(int sec, final byte[] contents, final int contentOffset, 
			final int offset, final int length) throws IOException {
		diskImage.updateSector(sec, new IDiskImage.SectorUpdater() {

			@Override
			public boolean updateSector(byte[] content) {
				System.arraycopy(contents, contentOffset, content, offset, length); 
				return true;
			}
			
		});
		
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
		diskImage.closeDiskImage();
	}

	/**
	 * @return
	 */
	public int getIndexSector() {
		return indexSector;
	}


}
