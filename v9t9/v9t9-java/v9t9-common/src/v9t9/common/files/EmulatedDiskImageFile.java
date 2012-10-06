/**
 * 
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
