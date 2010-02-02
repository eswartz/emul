/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.engine.files;

import java.io.IOException;


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
    public int getFlags() {
        return flags;
    }
    /**
	 * @param flags the flags to set
	 */
	public void setFlags(int flags) {
		this.flags = (byte) flags;
	}
	
    /** # records per sector, 
    256/reclen for FIXED,
    255/(reclen+1) for VAR,
    0 for program
    */
    public int getRecordsPerSector() {
        return recspersec;
    }
    /**
     * Set # records per sector,
     * 256/reclen for FIXED,
     * 255/(reclen+1) for VAR,
     * 0 for program
	 * @param recspersec the recspersec to set
	 */
	public void setRecordsPerSector(int recspersec) {
		this.recspersec = (byte) recspersec;
	}
    
    /** Get # sectors in file */
    public int getSectorsUsed() {
        return secsused;
    }
    /**
     * Set # sectors in file
	 * @param secsused the secsused to set
	 */
	public void setSectorsUsed(int secsused) {
		this.secsused = (short) secsused;
	}
    
    /** Get last byte used in file 
    (0 = no last empty sector) */
    public int getByteOffset() {
        return byteoffs & 0xff;
    }
    /**
     * Set the last byte used in file
     * (0 = no last empty sector)
	 * @param byteoffs the byteoffs to set
	 */
	public void setByteOffset(int byteoffs) {
		this.byteoffs = (byte) byteoffs;
	}
    
    /** Get record length, 0 for program */
    public int getRecordLength() {
        return reclen;
    }
    
    /**
     * Set the record length, 0 for program
	 * @param reclen the reclen to set
	 */
	public void setRecordLength(int reclen) {
		this.reclen = (byte) reclen;
	}
    
    /** Get # records for FIXED file,
    # sectors for VARIABLE file,
    0 for program */
    public int getNumberRecords() {
        return numrecs;
    }
    
    /**
     * Set the # of records for FIXED file,
     * # sectors for VARIABLE file,
     * 0 for program
	 * @param numrecs the numrecs to set
	 */
	public void setNumberRecords(int numrecs) {
		this.numrecs = (short) numrecs;
	}

    /**
     * Get the filename
     */
    public String getFileName() {
        return filename;
    }
    
    /**
     * Set the filename
     */
    public void setFileName(String name) throws IOException {
    	if (name.length() > 10)
    		throw new IOException("Name too long: " + name);
    	this.filename = name;
    }

    public int getFileSize() {
        int full = secsused * 256;
        if (byteoffs != 0) {
			full = full - 256 + (byteoffs & 0xff);
		}
        return full;
    }
    
    public void setFileSize(int size) throws IOException {
    	if (size >= 256 * 65536)
    		throw new IOException("File size too big: " + size);
    	
    	byteoffs = (byte) (size & 0xff);
    	secsused = (short) ((size + 255) / 256);
    }
    
    
}
