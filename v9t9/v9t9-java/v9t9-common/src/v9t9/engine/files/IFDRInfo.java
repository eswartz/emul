/**
 * 
 */
package v9t9.engine.files;


/**
 * @author ejs
 *
 */
public interface IFDRInfo {
    /** filetype flags */
    public int getFlags();
	
    /** # records per sector, 
    256/reclen for FIXED,
    255/(reclen+1) for VAR,
    0 for program
    */
    public int getRecordsPerSector();
    
    /** Get # sectors in file */
    public int getSectorsUsed();
    
    /** Get last byte used in file 
    (0 = no last empty sector) */
    public int getByteOffset();
    
    /** Get record length, 0 for program */
    public int getRecordLength();
    
    /** Get # records for FIXED file,
    # sectors for VARIABLE file,
    0 for program */
    public int getNumberRecords();
    
    public int getFileSize();
    
}
