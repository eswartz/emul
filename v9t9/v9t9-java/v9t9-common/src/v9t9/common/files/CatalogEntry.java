/*
  CatalogEntry.java

  (c) 2011-2012 Edward Swartz

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
public class CatalogEntry {
	public static final int TYPE_DIS_FIX = 1;
	public static final int TYPE_DIS_VAR = 2;
	public static final int TYPE_INT_FIX = 3;
	public static final int TYPE_INT_VAR = 4;
	public static final int TYPE_PROGRAM = 5;
	static byte drcTrans[][] = new byte[][] { 
		{0, TYPE_DIS_FIX}, 
		{FDR.ff_program, TYPE_PROGRAM},
		{FDR.ff_internal, TYPE_INT_FIX}, 
		{(byte) FDR.ff_variable, TYPE_DIS_VAR},
		{(byte) (FDR.ff_variable + FDR.ff_internal), TYPE_INT_VAR}
	};


	public final int indexSector;
	public final String fileName;
	public final int secs;
	/** symbolic name, from {@link FDR#getType(int)} */
	public final String type;
	public final int recordLength;
	/** one of TYPE_ */
	public final int typeCode;
	public final boolean isProtected;


	private IEmulatedFile file;

	/**
	 * @param fileName
	 * @param sz
	 * @param type
	 * @param recordLength
	 */
	public CatalogEntry(int indexSector, String fileName, IEmulatedFile file) {
		this.indexSector = indexSector;
		this.fileName = fileName;
		this.file = file;
		this.secs = file.getSectorsUsed() + 1;
		int flags = file.getFlags();
		this.isProtected = (flags & FDR.ff_protected) != 0;
		
		this.type = FDR.getType(flags);
		
		int idx;
		int code = 0;
		for (idx = 0; idx < drcTrans.length; idx++)
			if (drcTrans[idx][0] ==
				(flags & (FDR.ff_internal | FDR.ff_program | FDR.ff_variable))) {
				code = drcTrans[idx][1];
				break;
			}
		// no match == program
		if (idx >= drcTrans.length) {
			code = 1;
		}
		this.typeCode = code;
		
		this.recordLength = file.getRecordLength();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return fileName + " " + type + " " + recordLength;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + (isProtected ? 1231 : 1237);
		result = prime * result + recordLength;
		result = prime * result + secs;
		result = prime * result + typeCode;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CatalogEntry other = (CatalogEntry) obj;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (isProtected != other.isProtected)
			return false;
		if (recordLength != other.recordLength)
			return false;
		if (secs != other.secs)
			return false;
		if (typeCode != other.typeCode)
			return false;
		return true;
	}

	/**
	 * @return the file
	 */
	public IEmulatedFile getFile() {
		return file;
	}
	
}
