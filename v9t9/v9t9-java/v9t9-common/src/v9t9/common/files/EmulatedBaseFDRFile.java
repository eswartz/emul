/*
  EmulatedBaseFDRFile.java

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
public abstract class EmulatedBaseFDRFile implements EmulatedFile {

	protected FDR fdr;

	public EmulatedBaseFDRFile(FDR fdr) {
		this.fdr = fdr;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.files.EmulatedFile#getName()
	 */
	@Override
	public String getFileName() {
		return fdr.getFileName();
	}
	
	public int getFileSize() {
	    return fdr.getFileSize();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.NativeFile#setFileSize(int)
	 */
	@Override
	public void setFileSize(int size) throws IOException {
		fdr.setFileSize(size);	
	}

	public FDR getFDR() {
		return fdr;
	}

	public int getFlags() {
		return fdr.getFlags();
	}

	public int getSectorsUsed() {
		return fdr.secsused;
	}

	public int getByteOffset() {
		return fdr.getByteOffset();
	}

	public int getNumberRecords() {
		return fdr.getNumberRecords();
	}

	public int getRecordLength() {
		return fdr.getRecordLength();
	}

	public int getRecordsPerSector() {
		return fdr.getRecordsPerSector();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IFDRInfo#getContentSectors()
	 */
	@Override
	public int[] getContentSectors() {
		return fdr.getContentSectors();
	}
}