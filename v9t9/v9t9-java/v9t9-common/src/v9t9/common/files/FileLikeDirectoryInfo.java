/*
  FileLikeDirectoryInfo.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.files;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import v9t9.common.memory.ByteMemoryAccess;

public class FileLikeDirectoryInfo extends DirectoryInfo {

	private int index;
	private long totalSectors;
	private long freeSectors;
	private int lastEntry;
	private DiskDirectory disk;

	public FileLikeDirectoryInfo(DiskDirectory disk, File file, IFilesInDirectoryMapper mapper) {
		super(file, mapper);
		this.disk = disk;
		lastEntry = Math.min(128, entries.length);
		totalSectors = (file.getTotalSpace() + 255) / 256;
		freeSectors = (file.getFreeSpace() + 255) / 256;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	protected CatalogEntry decodeFile(int index) throws DsrException {
		File file = entries[index];
		
		IEmulatedFile nativefile;
		try {
			nativefile = disk.getFile(file.getName());
		} catch (IOException e) {
			throw new DsrException(PabConstants.es_hardware, e);
		}
		
		
		// first field is the string representing the
		// file or volume name
		String name = mapper.getDsrFileName(file.getName());

//		// second field is file type
//		int flags = nativefile instanceof EmulatedBaseFDRFile ? ((NativeFDRFile) nativefile).getFlags() : FDR.ff_variable;
//
//		// third field is file size, one sector for fdr
//		int size = 1 + (nativefile.getFileSize() + 255) / 256;
//
//		// fourth field is record size
//		int reclen = nativefile instanceof EmulatedBaseFDRFile ? ((EmulatedBaseFDRFile) nativefile).getFDR().getRecordLength() : 80;

		return new CatalogEntry(index + 2, name, nativefile);
	}

	/**
	 * Read the full catalog, even if the client can only process 128 entries.
	 * @return
	 */
	public List<CatalogEntry> readCatalog() {
		List<CatalogEntry> list = new ArrayList<CatalogEntry>();
		for (int index = 1; index <= entries.length; index++) {
			try {
				list.add(decodeFile(index - 1));
			} catch (IOException e) { 
				// ignore
			}
		}
		return list;
	}

	public int readRecord(ByteMemoryAccess access) throws DsrException {
		int offset = access.offset;
		
		// volume record?
		if (index == 0) {

			/*  Get volume name from path. */
			offset = writeName(access, offset, mapper.getDsrFileName(dir.getName()));
			
			// zero field
			offset = writeFloat(access, offset, 0);

			// total space
			offset = writeFloat(access, offset, totalSectors);

			// free space
			offset = writeFloat(access, offset, freeSectors);

			index++;
			
			return offset - access.offset;
		}

		// read file record; restrict it to 127 entries
		// in case naive programs will die...
		if (index < 0 || index > 128)
			throw new DsrException(PabConstants.e_endoffile, "End of directory");

		if (index > lastEntry) {
			// make an empty record
			access.memory[offset++] = (byte) 0;
			offset = writeFloat(access, offset, 0);
			offset = writeFloat(access, offset, 0);
			offset = writeFloat(access, offset, 0);
			index++;
			return offset - access.offset;
		}
		
		// Get file info
		CatalogEntry entry = decodeFile(index - 1);
		
		// first field is the string representing the
		// file or volume name
		offset = writeName(access, offset, entry.fileName);

		offset = writeFloat(access, offset, entry.typeCode);

		// third field is file size, one sector for fdr
		offset = writeFloat(access, offset, entry.secs);

		// fourth field is record size
		offset = writeFloat(access, offset, entry.recordLength);

		index++;
		
		return offset - access.offset;
	}

	/**
	 * @param access
	 * @param offset
	 * @param dskName2
	 * @return
	 */
	private int writeName(ByteMemoryAccess access, int offset,
			String name) {
		int len = name.length();
		if (len > 10)
			len = 10;
		access.memory[offset++] = (byte) len;
		for (int i = 0; i < len; i++)
			access.memory[offset++] = (byte) name.charAt(i);

		return offset;
	}

	/**	Convert and push an integer into a TI floating point record:
			[8 bytes] [0x40+log num] 9*[sig figs, 0-99]
			Return pointer past end of float.
	 */
	private int writeFloat(ByteMemoryAccess access, int offset, long x) {
		access.memory[offset++] = (byte) 8; // bytes in length
		Arrays.fill(access.memory, offset, offset + 8, (byte) 0);
		if (x == 0)
			return offset + 8;

		long y = x;
		int places = 0;
		while (y > 0) {
			y /= 100;
			places++;
		}
		access.memory[offset] = (byte) (0x3F + places);
		while (places > 0) {
			if (places <= 9)
				access.memory[offset + places] = (byte) (x % 100);
			x /= 100;
			places--;
		}
		
		return offset + 8;
	}
	
}