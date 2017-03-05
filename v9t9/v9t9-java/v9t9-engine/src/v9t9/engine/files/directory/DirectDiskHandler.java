/*
  DirectDiskHandler.java

  (c) 2010-2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.files.directory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import v9t9.common.dsr.IMemoryTransfer;
import v9t9.common.events.IEventNotifier;
import v9t9.common.events.NotifyEvent.Level;
import v9t9.common.files.DiskLikeDirectoryInfo;
import v9t9.common.files.DsrException;
import v9t9.common.files.EmulatedBaseFDRFile;
import v9t9.common.files.FDR;
import v9t9.common.files.IFDROwner;
import v9t9.common.files.IFilesInDirectoryMapper;
import v9t9.common.files.NativeFDRFile;
import v9t9.common.files.NativeFile;
import v9t9.common.files.NativeFileFactory;
import v9t9.common.files.PabConstants;
import v9t9.common.files.V9t9FDR;
import v9t9.common.memory.ByteMemoryAccess;
import v9t9.engine.Dumper;
import ejs.base.utils.HexUtils;

public class DirectDiskHandler {
	public static class DirectDiskInfo {
		Map<File, DiskLikeDirectoryInfo> dirInfos = new HashMap<File, DiskLikeDirectoryInfo>();

		public void reset() {
			dirInfos.clear();
		}
		
	}
	private static Map<Short, DirectDiskHandler.DirectDiskInfo> diskInfoBlocks = new HashMap<Short, DirectDiskHandler.DirectDiskInfo>();
	
	private final short cru;
	private final IMemoryTransfer xfer;
	/** the command code */
	private final short code;
	private final String devname;
	private final IFilesInDirectoryMapper mapper;
	/** device from >834C -- e.g. DSK# */
	byte dev;
	/** option from >834D -- usually flag or # of sectors */
	private byte opt;
	/** option from >834E -- usually filename */
	private short addr1;
	/** option from >8350 -- usually filename #2 */
	private short addr2;

	private final Dumper dumper;

	private IEventNotifier eventNotifier;

	
	public static DirectDiskHandler.DirectDiskInfo getDiskInfoBlock(short cru) {
		DirectDiskHandler.DirectDiskInfo block = diskInfoBlocks.get(cru);
		if (block == null) {
			block = new DirectDiskInfo();
			diskInfoBlocks.put(cru, block);
		}
		return block;
	}
	
	public DirectDiskHandler(IEventNotifier eventNotifier, Dumper dumper, 
			short cru, IMemoryTransfer xfer, IFilesInDirectoryMapper mapper, short code) {
		this.eventNotifier = eventNotifier;
		this.dumper = dumper;
		this.cru = cru;
		this.xfer = xfer;
		this.mapper = mapper;
		this.code = code;
		
		dev = xfer.readParamByte(0x4c);
		devname = "DSK" + (char)(dev + '0');
		
		opt = xfer.readParamByte(0x4d);
		addr1 = xfer.readParamWord(0x4e);
		addr2 = xfer.readParamWord(0x50);
	}

	public byte getDevice() {
		return dev;
	}
	
	public void run() throws DsrException {
		switch (code) {
		case EmuDiskConsts.D_DINPUT:
			directDiskInput();
			break;
		case EmuDiskConsts.D_DOUTPUT:
			directDiskOutput();
			break;
		case EmuDiskConsts.D_SECRW:
			directSectorReadWrite();
			break;
		case EmuDiskConsts.D_PROT:
			directChangeProtection();
			break;
		case EmuDiskConsts.D_RENAME:
			directRenameFile();
			break;
			
		default:
			eventNotifier.notifyEvent(this, Level.ERROR, "Unknown disk directory DSR function: " + code);
			throw new DsrException(PabConstants.es_badfuncerr, "Unhandled function: " + code);
		}
	}
	

	public void error(DsrException e) {
		xfer.writeParamByte(0x50, (byte) e.getErrorCode());
		if (e != null)
			dumper.info(e.getMessage());
	}


	protected String readBareFilename(short addr) {
		StringBuilder builder = new StringBuilder();
		int endAddr = addr;
		while (endAddr < addr + 10) {
			byte ch = xfer.readVdpByte(endAddr);
			if (ch == ' ')
				break;
			builder.append((char) ch);
			endAddr++;
		}
		return builder.toString();
	}
	

	private void directDiskInput() throws DsrException {
		byte secs = opt;
		short fname = addr1;
		int parms = addr2;
		
		String filename = readBareFilename(fname);
		NativeFile file = null;
		
		try {
			file = NativeFileFactory.INSTANCE.createNativeFile(mapper.getLocalFile(devname, filename));
		} catch (IOException e) {
			throw new DsrException(PabConstants.es_filenotfound, e);
		} catch (Throwable t) {
			throw new DsrException(PabConstants.es_filenotfound, t);
		}
		
		parms = (parms >> 8);

		if (secs == 0) {
			// read FDR info
			if (file instanceof EmulatedBaseFDRFile) {
				EmulatedBaseFDRFile fdrFile = (EmulatedBaseFDRFile) file;
				FDR fdr = fdrFile.getFDR();
				xfer.writeParamWord(parms + 2, (short) fdr.getSectorsUsed());
				xfer.writeParamByte(parms + 4, (byte) fdr.getFlags());
				xfer.writeParamByte(parms + 5, (byte) fdr.getRecordsPerSector());
				xfer.writeParamByte(parms + 6, (byte) fdr.getByteOffset());
				xfer.writeParamByte(parms + 7, (byte) fdr.getRecordLength());
				xfer.writeParamWord(parms + 8, swpb((short) fdr.getNumberRecords()));
			} else {
				int size = file.getFileSize();
				xfer.writeParamWord(parms + 2, (short) ((size + 255) / 256));
				xfer.writeParamByte(parms + 6, (byte) (size % 256));
			}
			xfer.writeParamByte(0x50, (byte) 0);

		} else {
			// read sectors
			short   vaddr = xfer.readParamWord(parms);
			short	secnum = xfer.readParamWord(parms + 2);

			dumper.info("reading "+secs+" sectors from sector #"+secnum+
					" in " + file + ", storing to >"+HexUtils.toHex4(vaddr));

			ByteMemoryAccess access = xfer.getVdpMemory(vaddr);
			try {
				int read = file.readContents(access.memory, access.offset, secnum * 256, secs * 256);
				xfer.dirtyVdpMemory(vaddr, read);
				// error will be set if sector read failed
				xfer.writeParamByte(0x4D, (byte) ((read + 255) >> 8));
				xfer.writeParamByte(0x50, (byte) 0);
			} catch (IOException e) {
				throw new DsrException(PabConstants.es_hardware, e);
			}
		}
	}


	private void directDiskOutput() throws DsrException {
		byte secs = opt;
		short fname = addr1;
		int parms = addr2;
		
		String filename = readBareFilename(fname);
		NativeFile file = null;
		
		File localFile = mapper.getLocalFile(devname, filename);
		
		parms = (parms >> 8);

		if (secs == 0) {
			// write FDR info (or create file)
			try {
				file = NativeFileFactory.INSTANCE.createNativeFile(localFile);
			} catch (IOException e) {
			}
			if (file == null || file.getFileSize() == 0) {
				file = new NativeFDRFile(localFile, new V9t9FDR());
			}
			
			if (file instanceof EmulatedBaseFDRFile) {
				EmulatedBaseFDRFile fdrFile = (EmulatedBaseFDRFile) file;
				FDR fdr = fdrFile.getFDR();
				fdr.setSectorsUsed(xfer.readParamWord(parms + 2) & 0xffff);
				fdr.setFlags(xfer.readParamByte(parms + 4) & FDR.FF_VALID_FLAGS);
				fdr.setRecordsPerSector(xfer.readParamByte(parms + 5) & 0xff);
				fdr.setByteOffset(xfer.readParamByte(parms + 6) & 0xff);
				fdr.setRecordLength(xfer.readParamByte(parms + 7) & 0xff);
				fdr.setNumberRecords(swpb(xfer.readParamWord(parms + 8)));
				try {
					fdr.setFileName(filename);
				} catch (IOException e) {
					throw new DsrException(PabConstants.es_badvalerr, e);
				}
				try {
					fdr.writeFDR(file.getFile());
					xfer.writeParamByte(0x50, (byte) 0);
				} catch (IOException e) {
					throw new DsrException(PabConstants.es_hardware, e, "Failed to write FDR: " + file.getFile());
				}
			}
			
			// change real file
			int byteoffs = xfer.readParamByte(parms + 6);
			int numsecs = xfer.readParamWord(parms + 2);
			if (byteoffs != 0)
				numsecs++;
			int size = numsecs * 256 + byteoffs;
			int oldsize = file != null ? file.getFileSize() : 0;
			try {
				file.setFileSize(size);
			} catch (IOException e) {
				throw new DsrException(oldsize < size ? PabConstants.es_outofspace : PabConstants.es_hardware, e, "Failed to resize file: " + file.getFile());
			}
			int flags = xfer.readParamByte(parms + 4);
			
			// try to make real file match virtual protected bit, 
			// but ignore error if fails -- filesystem may not allow it (VFAT on Linux)
			file.getFile().setWritable((flags & FDR.ff_protected) != 0);

		} else {
			// write sectors
			try {
				file = NativeFileFactory.INSTANCE.createNativeFile(localFile);
			} catch (IOException e) {
				throw new DsrException(PabConstants.es_filenotfound, "File not found: " + localFile);
			}
			
			short   vaddr = xfer.readParamWord(parms);
			short	secnum = xfer.readParamWord(parms + 2);

			dumper.info("writing "+secs+" sectors to sector #"+secnum+
					" in " + file + ", reading from >"+HexUtils.toHex4(vaddr));

			ByteMemoryAccess access = xfer.getVdpMemory(vaddr);
			try {
				//String contents = new String(access.memory, access.offset, secs * 256);
				//System.out.println(contents);
				int wrote = file.writeContents(access.memory, access.offset, secnum * 256, secs * 256);
				// error will be set if sector write failed
				xfer.writeParamByte(0x4D, (byte) ((wrote + 255) >> 8));
				xfer.writeParamByte(0x50, (byte) 0);
			} catch (IOException e) {
				throw new DsrException(PabConstants.es_outofspace, e);
			}
		}
	}
	
	/**
	 * @param i
	 * @return
	 */
	private short swpb(short x) {
		return (short) (((x & 0xff) << 8) | ((x >> 8) & 0xff));
	}

	private void directSectorReadWrite() throws DsrException {
		boolean write = opt == 0;
		
		// we only pretend to read sectors, e.g. to handle catalogs
		if (write) {
			eventNotifier.notifyEvent(this, Level.ERROR, "The program is trying to write sectors in a disk directory -- this isn't supported");
			throw new DsrException(PabConstants.es_hardware, "Write sector not implemented");
		}
		
		short addr = addr1;
		int secnum = addr2 & 0xffff;
		
		ByteMemoryAccess access = xfer.getVdpMemory(addr);
		DiskLikeDirectoryInfo info = getDirectory();
		
		if (secnum < info.lastSector) {
			if (secnum == 0) {
				info.synthesizeVolumeSector(access);
			}
			else if (secnum == 1) {
				info.synthesizeIndexSector(access);
			}
			else {
				try {
					info.synthesizeSector(access, secnum);
				} catch (IOException e) {
					throw new DsrException(PabConstants.es_hardware, e);
				}
			}	
		} else {
			Arrays.fill(access.memory, access.offset, access.offset + 256, (byte) 0xe5);
		}
		
		xfer.dirtyVdpMemory(addr, 256);
		xfer.writeParamByte(0x50, (byte) 0);
	}

	private DiskLikeDirectoryInfo getDirectory() {
		DirectDiskHandler.DirectDiskInfo diskInfo = getDiskInfoBlock(cru);
		
		File dir = mapper.getLocalFile(devname, null);
		DiskLikeDirectoryInfo info = diskInfo.dirInfos.get(dir);
		if (info == null) {
			info = new DiskLikeDirectoryInfo(dir, mapper, devname);
			diskInfo.dirInfos.put(dir, info);
		}
		
		return info;
	}


	private void directChangeProtection() throws DsrException {
		boolean protect = opt != 0;
		short fname = addr1;
		
		String filename = readBareFilename(fname);
		NativeFile file = null;
		
		try {
			file = NativeFileFactory.INSTANCE.createNativeFile(mapper.getLocalFile(devname, filename));
		} catch (IOException e) {
			throw new DsrException(PabConstants.es_filenotfound, e);
		}
		
		dumper.info("Changing protection for " + file + " to " 
				+ (protect ? "enabled" : "disabled"));
		
		if (file instanceof IFDROwner) {
			IFDROwner fdrFile = (IFDROwner) file;
			
			FDR fdr = fdrFile.getFDR();
			if (protect)
				fdr.setFlags(fdr.getFlags() | FDR.ff_protected);
			else
				fdr.setFlags(fdr.getFlags() &~ FDR.ff_protected);
			
			try {
				fdr.writeFDR(file.getFile());
				xfer.writeParamByte(0x50, (byte) 0);
			} catch (IOException e) {
				throw new DsrException(PabConstants.es_hardware, e, "Failed to write FDR: " + file.getFile());
			}
		}
		
		// change real file (ignore error: may be on DOS filesystem in Unix)
		file.getFile().setWritable(!protect);
	}

	private void directRenameFile() throws DsrException {
		short tname = addr1;
		short fname = addr2;
		
		String fromFilename = readBareFilename(fname);
		String toFilename = readBareFilename(tname);
		NativeFile file = null;
		
		try {
			file = NativeFileFactory.INSTANCE.createNativeFile(mapper.getLocalFile(devname, fromFilename));
		} catch (IOException e) {
			throw new DsrException(PabConstants.es_filenotfound, e);
		}
		
		dumper.info("Renaming " + file + " to " + toFilename); 

		// check real file first
		File toFile = new File(file.getFile().getParentFile(),
				mapper.getLocalFileName(toFilename));
		if (toFile.exists()) {
			throw new DsrException(PabConstants.es_fileexists, 
					"Cannot rename " + fromFilename + "; " + toFilename + " already exists");
		}
		
		// early check
		if (!file.getFile().exists()) {
			throw new DsrException(PabConstants.es_filenotfound, "File is does not exist when renaming: " + file.getFile());
		}
		if (!file.getFile().canWrite()) {
			throw new DsrException(PabConstants.es_hardware, "File is protected when renaming: " + file.getFile());
		}
		
		if (file instanceof IFDROwner) {
			IFDROwner fdrFile = (IFDROwner) file;
			
			FDR fdr = fdrFile.getFDR();
			if ((fdr.getFlags() & FDR.ff_protected) != 0) {
				throw new DsrException(PabConstants.es_hardware, "Cannot rename; file is protected: " + file.getFile());
			}
			
			try {
				fdr.setFileName(toFilename);
			} catch (IOException e) {
				throw new DsrException(PabConstants.es_badvalerr, e);
			}
			
			try {
				fdr.writeFDR(file.getFile());
				xfer.writeParamByte(0x50, (byte) 0);
			} catch (IOException e) {
				throw new DsrException(PabConstants.es_hardware, e, "Failed to write FDR: " + file.getFile());
			}
		}
		
		// change real file
		if (!file.getFile().renameTo(toFile)) {
			throw new DsrException(PabConstants.es_hardware, "Failed to rename file: " + file.getFile());
		}
	}
}