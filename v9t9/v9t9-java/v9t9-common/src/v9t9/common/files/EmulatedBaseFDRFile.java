/**
 * 
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