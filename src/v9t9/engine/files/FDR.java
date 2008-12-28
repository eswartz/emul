/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.engine.files;


/**
 * File descriptor record, as seen in the index sector for files 
 * in the TI Disk Controller.
 * @author ejs
 */
public abstract class FDR implements IFDRFlags {
    protected short numrecs;
    protected byte reclen;
    protected byte byteoffs;
    protected short secsused;
    protected byte recspersec;
    protected byte flags;
    protected int size;
    protected String filename;
    
    /** get size of FDR */
    public int getFDRSize() {
        return size;
    }
    
    /** filetype flags */
    public byte getFlags() {
        return flags;
    }
    /** # records per sector, 
    256/reclen for FIXED,
    255/(reclen+1) for VAR,
    0 for program
    */
    public byte getRecordsPerSector() {
        return recspersec;
    }
    /** Get # sectors in file */
    public short getSectorsUsed() {
        return secsused;
    }
    /** Get last byte used in file 
    (0 = no last empty sector) */
    public int getByteOffset() {
        return byteoffs & 0xff;
    }
    /** Get record length, 0 for program */
    public byte getRecordLength() {
        return reclen;
    }
    /** Get # records for FIXED file,
    # sectors for VARIABLE file,
    0 for program */
    public short getNumberRecords() {
        return numrecs;
    }

    /**
     * Get the filename
     */
    public String getFileName() {
        return filename;
    }

    public int getFileSize() {
        int full = secsused * 256;
        if (byteoffs != 0) {
			full = full - 256 + (byteoffs & 0xff);
		}
        return full;
    }
}
