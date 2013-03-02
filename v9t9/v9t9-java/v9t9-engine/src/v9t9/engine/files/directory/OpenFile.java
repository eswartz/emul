/*
  OpenFile.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.files.directory;

import java.io.IOException;
import java.util.Arrays;

import v9t9.common.files.FDR;
import v9t9.common.files.IFDROwner;
import v9t9.common.files.InvalidFDRException;
import v9t9.common.files.NativeFile;
import v9t9.common.memory.ByteMemoryAccess;
import v9t9.engine.dsr.DsrException;
import v9t9.engine.dsr.PabConstants;

/** Information about an open file. */
public class OpenFile {
	final String devName;
	final String fileName;
	final byte[] sector = new byte[256];
//	private final File file;
	
	private NativeFile nativefile;

	/** number of sector in sector buffer */
	int currentSecNum = -1;
	
	int position;
	int secnum;
	int byteoffs;
	
	boolean modified;
	
	public OpenFile(NativeFile file, String devName, String fileName) throws DsrException {
		this.nativefile = file;
		this.devName = devName;
		this.fileName = fileName;
		
		if (nativefile != null) {
			try {
				nativefile.validate();
			} catch (InvalidFDRException e) {
				throw new DsrException(PabConstants.e_badfiletype, e, "File header (FDR) does not match file");
			}
		}
		seekToPosition(0);
	}
	
	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}
	
	public void close() throws DsrException {
		flush();
	}
	
	public void flush() throws DsrException {
		if (modified) {
			try {
				if (position > nativefile.getFileSize()) {
					nativefile.setFileSize(position);
				}
				nativefile.flush();
				nativefile.writeContents(sector, 0, secnum * 256, sector.length);
			} catch (IOException e) {
				throw new DsrException(PabConstants.e_hardwarefailure, e);
			}
			modified = false;
		}
	}
	
	/**
	 * @return the nativefile
	 */
	public NativeFile getNativeFile() {
		return nativefile;
	}
	
	/**
	 * @param nativefile the nativefile to set
	 */
	public void setNativeFile(NativeFile nativefile) {
		this.nativefile = nativefile;
	}

	public void seekToPosition(int pos) throws DsrException {
		int newsecnum = pos >> 8;
		if (newsecnum != secnum)
			flush();
		position = pos;
		secnum = pos >> 8;
		byteoffs = pos & 0xff;
		ensureSector();
	}

	public void seekToEOF() throws DsrException {
		seekToPosition(nativefile.getFileSize());
	}

	protected void nextSector() throws DsrException {
		seekToPosition((secnum + 1) * 256);
	}
	
	public boolean isVariable() {
		return (nativefile.getFlags() & FDR.ff_variable) != 0;
//		return nativefile instanceof IFDROwner ? (((IFDROwner) nativefile).getFDR().getFlags() & FDR.ff_variable) != 0 : true;
	}

	public int getRecordLength() {
		int len = nativefile instanceof IFDROwner ? ((IFDROwner) nativefile).getFDR().getRecordLength() : 80;
		if (len == 0)
			len = 256;
		return len;
	}
	public int getNumberRecords() {
		int num = nativefile instanceof IFDROwner ? ((IFDROwner) nativefile).getFDR().getNumberRecords() : 0;
		return num;
	}
	protected void ensureSector() throws DsrException {
		if (currentSecNum != secnum) {
			currentSecNum = secnum;
			try {
				if (nativefile != null) {
					int read = nativefile.readContents(sector, 0, secnum * 256, sector.length);
					// if short, clear sector (when seeking past EOF to write data, don't repeat other records)
					if (read < sector.length)
						Arrays.fill(sector, Math.max(0, read), sector.length, (byte) 0);
				}
			} catch (IOException e) {
				throw new DsrException(PabConstants.e_hardwarefailure, e);
			}
		}
	}
	
	/**
	 * Read one record (fixed or variable)
	 * @param bufaddr VDP address
	 * @param reclen buffer size 
	 */
	public int readRecord(ByteMemoryAccess access, int reclen) throws DsrException {
		int size;
		if (isVariable()) {
			size = 0;
			while (position < nativefile.getFileSize()) {
				size = sector[byteoffs++] & 0xff;
				position++;
				if (size == 0xff) {
					nextSector();
				} else {
					break;
				}
			}
		} else {
			size = getRecordLength();
			while (position < nativefile.getFileSize()) {
				if (byteoffs + size > 256) {
					nextSector();
				} else {
					break;
				}
			}
		}
		
		if (position >= nativefile.getFileSize())
			throw new DsrException(PabConstants.e_endoffile, "End of file: " + nativefile);
		
		System.arraycopy(sector, byteoffs, access.memory, access.offset, size);
		position += size;
		byteoffs += size;
		
		return size;
	}

	/**
	 * Read one record (fixed or variable)
	 * @param bufaddr VDP address
	 * @param reclen buffer size 
	 */
	public int writeRecord(ByteMemoryAccess access, int reclen) throws DsrException {
		int size;
		if (isVariable()) {
			size = Math.min(reclen, getRecordLength());
			if (reclen + 1 + byteoffs > 255) {
				// just be sure
				sector[byteoffs] = (byte) 0xff;
				nextSector();
			}
			sector[byteoffs++] = (byte) size;
			position++;
		} else {
			size = getRecordLength();
			if (size + byteoffs > 256) {
				nextSector();
			}
		}
		
		System.arraycopy(access.memory, access.offset, sector, byteoffs, size);
		byteoffs += size;
		position += size;
		
		if (isVariable()) {
			sector[byteoffs] = (byte) 0xff;
		}
		
		modified = true;
		
		return size;
	}

	/**
	 * Seek to a given record number
	 * @param recnum
	 * @throws DsrException 
	 */
	public void seekToRecord(int recnum) throws DsrException {
		if (!isVariable()) {
			int reclen = getRecordLength();
			int numrecs = 256 / reclen;
			int secpos = (recnum / numrecs) * 256;
			int pos = secpos + reclen * (recnum % numrecs);
			seekToPosition(pos);
		} else {
			seekToPosition(recnum);
		}
	}

	/**
	 * @return
	 */
	public boolean isProgram() {
		return nativefile != null && (nativefile.getFlags() & FDR.ff_program) != 0;
	}

	/**
	 * @return
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * @return
	 */
	public boolean isProtected() {
		return nativefile != null && (nativefile.getFlags() & FDR.ff_protected) != 0;
	}
}