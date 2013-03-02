/*
  IFDRInfo.java

  (c) 2010-2012 Edward Swartz

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
