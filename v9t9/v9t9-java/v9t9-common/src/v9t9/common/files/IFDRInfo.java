/*
  IFDRInfo.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.files;


/**
 * @author ejs
 *
 */
public interface IFDRInfo {
	public String getFileName();
	
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
    
    /** Get the array of sectors which contain the content of the associated file. 
     * @return array of sector numbers, size {@link #getSectorsUsed()}
     */
    public int[] getContentSectors();
}
