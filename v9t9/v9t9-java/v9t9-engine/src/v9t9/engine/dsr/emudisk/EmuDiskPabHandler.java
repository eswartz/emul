/**
 * 
 */
package v9t9.engine.dsr.emudisk;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


import v9t9.base.utils.HexUtils;
import v9t9.common.cpu.ICpu;
import v9t9.common.memory.ByteMemoryAccess;
import v9t9.engine.cpu.Executor;
import v9t9.engine.dsr.DsrException;
import v9t9.engine.dsr.IMemoryTransfer;
import v9t9.engine.dsr.PabConstants;
import v9t9.engine.dsr.PabHandler;
import v9t9.engine.dsr.PabStruct;
import v9t9.engine.files.FDR;
import v9t9.engine.files.NativeFDRFile;
import v9t9.engine.files.NativeFile;
import v9t9.engine.files.NativeFileFactory;
import v9t9.engine.files.NativeTextFile;
import v9t9.engine.files.V9t9FDR;

public class EmuDiskPabHandler extends PabHandler {

	private static final boolean DEBUG = false;


	public static void info(String string) {
		if (ICpu.settingDumpFullInstructions.getBoolean())
			Executor.getDumpfull().println(string);
		if (DEBUG)
			System.out.println(string);
		
	}
	
	private static Map<Short, PabInfoBlock> pabInfoBlocks = new HashMap<Short, PabInfoBlock>();
	
	public static PabInfoBlock getPabInfoBlock(short cru) {
		PabInfoBlock block = pabInfoBlocks.get(cru);
		if (block == null) {
			block = new PabInfoBlock();
			pabInfoBlocks.put(cru, block);
		}
		return block;
	}

	IFileMapper mapper;
	private PabInfoBlock block;

	public EmuDiskPabHandler(short cruaddr, IMemoryTransfer xfer, IFileMapper mapper, short vdpNameCompareBuffer) {
		super(xfer);
		init(cruaddr, mapper, vdpNameCompareBuffer);
	}
	public EmuDiskPabHandler(short cruaddr, IMemoryTransfer xfer, IFileMapper mapper, PabStruct pab, short vdpNameCompareBuffer) {
		super(xfer, pab);
		init(cruaddr, mapper, vdpNameCompareBuffer);
	}

	private void init(short cruaddr, IFileMapper mapper,
			short vdpNameCompareBuffer) {
		this.block = getPabInfoBlock(cruaddr);
		this.mapper = mapper;

		// convert to absolute path
		File localFile = mapper.getLocalFile(devname, fname);
		File dir = localFile != null ? mapper.getLocalRoot(localFile) : null;
		String devName = dir != null ? mapper.getDsrDeviceName(dir) : null;
		
		if (devName != null) {
			devname = devName;
			fname = fname.substring(fname.lastIndexOf('.') + 1);
		}
		
		// copy filename to VDP
		int drive = devname.charAt(devname.length() - 1) - '0';
		xfer.writeVdpByte(vdpNameCompareBuffer, (byte) drive);
		for (int i = 0; i < fname.length(); i++)
			xfer.writeVdpByte(vdpNameCompareBuffer + i + 1, (byte) fname.charAt(i));
		for (int i = fname.length(); i < 10; i++)
			xfer.writeVdpByte(vdpNameCompareBuffer + i + 1, (byte) ' ');
			
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(pab.path + ": ");
		
		switch (pab.opcode) {
		case PabConstants.op_close:
			builder.append("CLOSE");
			break;
		case PabConstants.op_open:
			builder.append("OPEN");
			break;
		case PabConstants.op_read:
			builder.append("READ");
			break;
		case PabConstants.op_write:
			builder.append("WRITE");
			break;
		case PabConstants.op_restore:
			builder.append("RESTORE");
			break;
		case PabConstants.op_load:
			builder.append("LOAD");
			break;
		case PabConstants.op_save:
			builder.append("SAVE");
			break;
		case PabConstants.op_delete:
			builder.append("DELETE");
			break;
		case PabConstants.op_scratch:
			builder.append("SCRATCH");
			break;
		case PabConstants.op_status:
			builder.append("STATUS");
			break;
		}
		builder.append(" @>" + HexUtils.toHex4(pab.bufaddr));
		if (pab.opcode == PabConstants.op_save || pab.opcode == PabConstants.op_load)
			builder.append(", " + pab.recnum);
		else if (pab.opcode == PabConstants.op_read || pab.opcode == PabConstants.op_write)
			builder.append(", " + pab.charcount + " #" + pab.recnum);
		else if (pab.opcode == PabConstants.op_open) 
			builder.append(", " + pab.preclen + ", >" + HexUtils.toHex2(pab.pflags));
		return builder.toString();
	}
	/**
	 * 
	 */
	public void run() throws DsrException {

		if (pab.opcode > 9) {
			throw new DsrException(PabConstants.e_illegal, "Bad opcode: " + pab.opcode);
		} 
		
		File file = mapper.getLocalFile(devname, fname);
		if (file == null)
			throw new DsrException(PabConstants.e_baddevice, null, "Cannot map " + devname + " to host");
		
		boolean isCatalog = file.isDirectory();
		if (isCatalog && pab.opcode > PabConstants.op_read) {
			throw new DsrException(PabConstants.e_illegal, null, "Unsupported catalog opcode: " + pab.opcode);
		}

		switch (pab.opcode) {
		case PabConstants.op_open:
			if (!isCatalog)
				DSKOpen(file);
			else
				DSKOpenCatalog(file);
			break;
		case PabConstants.op_close:
			if (!isCatalog)
				DSKClose();
			else
				DSKCloseCatalog();
			break;
		case PabConstants.op_read:
			if (!isCatalog)
				DSKRead(file);
			else
				DSKReadCatalog(file);
			break;
		case PabConstants.op_write:
			DSKWrite(file);
			break;
		case PabConstants.op_restore:
			DSKRestore(file);
			break;
		case PabConstants.op_load:
			DSKLoad(file);
			break;
		case PabConstants.op_save:
			DSKSave(file);
			break;
		case PabConstants.op_delete:
			DSKDelete(file);
			break;
		case PabConstants.op_scratch:
			throw new DsrException(PabConstants.e_illegal, "Scratch record not implemented on "+devname);
		case PabConstants.op_status:
			DSKStatus(file);
			break;
		default:
			throw new DsrException(PabConstants.e_illegal, "[not] doing operation "+pab.opcode+" on "+devname);
		}
	
	}

	/**
	 * @param bufaddr
	 * @param charcount
	 */
	private void dump(int bufaddr, int charcount) {
		StringBuilder builder = new StringBuilder();
		StringBuilder hexbuilder = new StringBuilder();
		for (int i = 0; i < charcount; i++) {
			byte b = xfer.readVdpByte(bufaddr + i);
			builder.append(b >= 32 && b < 127 ? (char)b : '.');
			hexbuilder.append(HexUtils.toHex2(b));
			if (i % 4 == 1)
				hexbuilder.append('.');
			else if (i % 4 == 3)
				hexbuilder.append(' ');
		}
		
		EmuDiskPabHandler.info("Read: " + builder + "\n | " + hexbuilder);
		
	}

	private void DSKOpen(File file) throws DsrException {
		
		// clear error
		pab.pflags &= ~PabConstants.e_pab_mask;
		
		// sanity checks 
		if (pab.preclen == 255 && (pab.pflags & PabConstants.fp_variable) != 0) {
			throw new DsrException(PabConstants.e_badopenmode, "Cannot have variable record size of 255: " + file);
		}
		if  (pab.getOpenMode() == PabConstants.m_append && (pab.pflags & PabConstants.fp_variable) == 0) {
			throw new DsrException(PabConstants.e_badopenmode, "Cannot append to FIXED file");
		}
		
		if (file.isDirectory()) {
			// should not get here (should be in DSKOpenCatalog)
			throw new DsrException(PabConstants.e_illegal, "Directory read not implemented");
		}
		
		OpenFile openFile = block.allocOpenFile(pab.pabaddr, file, devname, fname);
		
		int fdrflags = 0;
		if ((pab.pflags & PabConstants.fp_internal) != 0)
			fdrflags |= FDR.ff_internal;
		if ((pab.pflags & PabConstants.fp_variable) != 0)
			fdrflags |= FDR.ff_variable;

		// make sure our native file works
		if (pab.getOpenMode() != PabConstants.m_input) {
			if (openFile.getNativeFile() != null) {
				if (openFile.isProtected()) {
					throw new DsrException(PabConstants.e_readonly, null, "File is protected: " + file);
				}
				
				// can only use text files as DIS/VAR
				if (openFile.getNativeFile() instanceof NativeTextFile && !pab.isDisVar()) {
					if (!pab.isOverwriting()) {
						throw new DsrException(PabConstants.e_badfiletype, "Can only open text file as DISPLAY/VARIABLE: " + file);
					}
				}
				
				if (pab.isOverwriting()) {
					file.delete();
					openFile.setNativeFile(null);
				}
			}
			
			// (make a new file
			if (openFile.getNativeFile() == null) {
				if (pab.preclen == 0)
					pab.preclen = 80;
				openFile.create(fdrflags, pab.preclen);
			}
		} else {
			// input mode
			if (openFile.getNativeFile() == null) {
				throw new DsrException(PabConstants.e_badfiletype, "File not found: " + file);
			}
			
			// can only use text files as DIS/VAR
			if (openFile.getNativeFile() instanceof NativeTextFile && !pab.isDisVar()) {
				throw new DsrException(PabConstants.e_badfiletype, "Can only open text file as DISPLAY/VARIABLE: " + file);
			}
		}

		// update default
		if (pab.preclen == 0) {
			pab.preclen = openFile.getRecordLength();
		}

		// validate flags for existing file
		if (openFile.getNativeFile() instanceof NativeFDRFile) {
			NativeFDRFile fdrFile = (NativeFDRFile) openFile.getNativeFile();
			int mask = FDR.ff_variable + FDR.ff_program;
			int extFdrFlags = (fdrFile.getFDR().getFlags() & FDR.ff_internal + FDR.ff_variable + FDR.ff_program);
			if ((extFdrFlags & mask) != (fdrflags & mask)) {
				throw new DsrException(PabConstants.e_badopenmode, "Open file mode does not match FDR: " + 
						HexUtils.toHex2(extFdrFlags) + " vs. " + HexUtils.toHex2(fdrflags));
			}
			if (fdrFile.getFDR().getRecordLength() != pab.preclen) {
				throw new DsrException(PabConstants.e_badopenmode, "Open file record size does not match FDR: " + fdrFile.getFDR().getRecordLength() + " vs " + pab.preclen);
			}
		}
		
		if (pab.getOpenMode() == PabConstants.m_output
				&& !pab.isVariable()
				&& pab.recnum != 0) {
			// initialize with allocated records
			if (pab.recnum > 0x7fff) {
				throw new DsrException(PabConstants.e_badopenmode, "Too many records: " + pab.recnum);
			}
			
			// instead of just setting the file size, 
			// simulate behavior of allocating sectors
			byte[] zeroes = new byte[pab.preclen];
			ByteMemoryAccess access = new ByteMemoryAccess(zeroes, 0);
			for (int i = 0; i < pab.recnum; i++) {
				openFile.writeRecord(access, pab.preclen);
			}
		}
		
		pab.recnum = 0;
		openFile.seekToPosition(0);
		
		if (pab.getOpenMode() == PabConstants.m_append) {
			openFile.seekToEOF();
		}
	}

	private void DSKClose() throws DsrException {
		OpenFile openFile = block.findOpenFile(pab.pabaddr);
		// no error closing closed file
		if (openFile != null) {
			openFile.close();
			block.removeOpenFile(pab.pabaddr);
		}
		
	}
	private void DSKRead(File file) throws DsrException {
		if (!pab.isReading())
			throw new DsrException(PabConstants.e_illegal, "File not open for reading: " + file);
			
		OpenFile openFile = block.findOpenFile(pab.pabaddr);
		if (openFile == null)
			throw new DsrException(PabConstants.e_badfiletype, "File not open: " + file);
		
		if (pab.preclen == 0)
			pab.preclen = openFile.getRecordLength();
		
		if (!pab.isVariable()) {
			// always use record number
			if (pab.recnum > 0x7fff) {
				throw new DsrException(PabConstants.e_badopenmode, "Too many records: " + pab.recnum);
			}
			openFile.seekToRecord(pab.recnum);
			pab.recnum++;
		}
		
		//System.out.printf("%s @ %08x\n", openFile.getNativeFile().getFile(), openFile.getPosition());
		
		ByteMemoryAccess access = xfer.getVdpMemory(pab.bufaddr);
		pab.charcount = openFile.readRecord(access, pab.preclen);
		xfer.dirtyVdpMemory(pab.bufaddr, pab.charcount);
		
		if (DEBUG) {
			dump(pab.bufaddr, pab.charcount);
		}
	}
	
	private void DSKOpenCatalog(File file) throws DsrException {
		
		// clear error
		pab.pflags &= ~PabConstants.e_pab_mask;
		
		// sanity checks 
		if ((pab.preclen != 0 && pab.preclen != 38) 
				|| (pab.pflags & PabConstants.fp_internal + PabConstants.fp_variable) != PabConstants.fp_internal
				|| (pab.getOpenMode() != PabConstants.m_input)) {
			throw new DsrException(PabConstants.e_badopenmode, "Bad directory open mode: " + HexUtils.toHex2(pab.pflags) + " reclen " + pab.preclen);
		}
		
		if (pab.preclen == 0)
			pab.preclen = 38;
		
		DirectoryInfo info = block.getDirectory(pab.pabaddr);
		if (info != null)
			throw new DsrException(PabConstants.e_badfiletype, "Directory already open: " + file);
		
		block.openDirectory(pab.pabaddr, file, mapper);
		
		pab.recnum = 0;
	}
	
	private void DSKCloseCatalog() throws DsrException {
		block.closeDirectory(pab.pabaddr);
	}
	
	private void DSKReadCatalog(File file) throws DsrException {
		FileLikeDirectoryInfo info = block.getDirectory(pab.pabaddr);
		if (info == null)
			throw new DsrException(PabConstants.e_badfiletype, "Directory not open: " + file);
		
		
		ByteMemoryAccess access = xfer.getVdpMemory(pab.bufaddr);
		
		if (pab.isRelative())
			info.setIndex(pab.recnum);

		/*pab.charcount =*/ info.readRecord(access);
		pab.charcount = pab.preclen = 38;
		
		xfer.dirtyVdpMemory(pab.bufaddr, pab.charcount);
		
		pab.recnum++;
		
		if (DEBUG) {
			dump(pab.bufaddr, pab.charcount);
		}
	}
	

	private void DSKRestore(File file) throws DsrException {
		if (!pab.isReading())
			throw new DsrException(PabConstants.e_illegal, "File not open for reading: " + file);
			
		OpenFile openFile = block.findOpenFile(pab.pabaddr);
		if (openFile == null)
			throw new DsrException(PabConstants.e_badfiletype, "File not open: " + file);

		if (pab.recnum > 0x7fff) {
			throw new DsrException(PabConstants.e_badopenmode, "Too many records: " + pab.recnum);
		}

		if (!pab.isRelative())
			pab.recnum = 0;
		
		openFile.seekToRecord(pab.recnum);
	}
	
	private void DSKWrite(File file) throws DsrException {
		if (!pab.isWriting())
			throw new DsrException(PabConstants.e_illegal, "File not open for reading: " + file);
		
		OpenFile openFile = block.findOpenFile(pab.pabaddr);
		if (openFile == null)
			throw new DsrException(PabConstants.e_badfiletype, "File not open: " + file);
		
		if (pab.preclen == 0)
			pab.preclen = openFile.getRecordLength();
		

		if (!pab.isVariable()) {
			if (pab.recnum > 0x7fff) {
				throw new DsrException(PabConstants.e_badopenmode, "Too many records: " + pab.recnum);
			}
			openFile.seekToRecord(pab.recnum);
			pab.recnum++;
		}
		
		ByteMemoryAccess access = xfer.getVdpMemory(pab.bufaddr);
		openFile.writeRecord(access, pab.charcount);
	}
	
	private void DSKLoad(File file) throws DsrException {
		
		OpenFile openFile = new OpenFile(file, devname, fname);
		if (openFile.getNativeFile() == null)
			throw new DsrException(PabConstants.e_badfiletype, "File not found: " + file);
		
		if (!openFile.isProgram()) {
			throw new DsrException(PabConstants.e_badfiletype, "Cannot load a non-PROGRAM file: " + file);
		}
		
		ByteMemoryAccess access = xfer.getVdpMemory(pab.bufaddr);
		try {
			int read = openFile.getNativeFile().readContents(access.memory, access.offset, 
					0, pab.recnum);
			xfer.dirtyVdpMemory(pab.bufaddr, read);
			
			if (DEBUG)
				dump(pab.bufaddr, Math.min(read, 32));
			
			if (read >= 0) {
				// no error or EOF (which is okay for DSKLoad)

				// EJS 050221: nope, this isn't documented behavior
				// and it breaks TI Artist! when loading printer files.
				//pab->recnum = len;
			} else {
				// failure
				throw new DsrException(PabConstants.e_hardwarefailure, "Read error: " + read);
			}

		} catch (IOException e) {
			throw new DsrException(PabConstants.e_badfiletype, e);
		}
	}

	private void DSKDelete(File file) throws DsrException {

		OpenFile openFile = block.findOpenFile(pab.pabaddr);
		if (openFile != null) {
			openFile.close();
			block.removeOpenFile(pab.pabaddr);
		} else {
			openFile = new OpenFile(file, devname, fname);
		}
		if (openFile.isProtected()) {
			throw new DsrException(PabConstants.e_readonly, null, "File is protected: " + file);
		}
	
		if (openFile.getNativeFile() != null && !openFile.getNativeFile().getFile().delete())
			throw new DsrException(PabConstants.e_hardwarefailure, null, "File not deleted: " + file);
	}


	private void DSKSave(File file) throws DsrException {

		OpenFile openFile = new OpenFile(file, devname, fname);
		if (openFile.getNativeFile() != null) {
			if (openFile.isProtected()) {
				throw new DsrException(PabConstants.e_readonly, null, "File is protected: " + file);
			}
			
			file.delete();
		}
		
		// make a FDR file for it
		FDR fdr = EmuDiskPabHandler.createNewFDR(fname);
		fdr.setFlags(FDR.ff_program);
		
		// and a native file
		openFile.setNativeFile(new NativeFDRFile(file, fdr));
		openFile.flush();
		
		ByteMemoryAccess access = xfer.getVdpMemory(pab.bufaddr);
		try {
			int towrite = pab.recnum;
			int wrote = 0;
			int addr = access.offset;
			while (towrite > 0) {
				int chunk = Math.min(access.memory.length - addr, towrite);
				wrote += openFile.getNativeFile().writeContents(access.memory, addr, 
					0, chunk);
				towrite -= chunk;
				addr -= access.memory.length;
			}
			
			// fill the sector
			int secfill = 256 - (wrote % 256);
			if (secfill < 256) {
				byte[] zeroes = new byte[secfill];
				openFile.getNativeFile().writeContents(zeroes, 0, wrote, secfill);
			}
			
			// write final FDR
			fdr.setFileSize(wrote);
			
			try {
				fdr.writeFDR(file);
			} catch (IOException e) {
				throw new DsrException(PabConstants.e_hardwarefailure, e);
			}
			
		} catch (IOException e) {
			throw new DsrException(PabConstants.e_outofspace, e);
		}
	}

	private void DSKStatus(File file) throws DsrException {
		
		int status = 0;

		if (file.isDirectory()) {
			status |= PabConstants.st_noexist;
		} else {
			OpenFile openFile = block.findOpenFile(pab.pabaddr);
			NativeFile nativeFile = null;
			
			if (openFile != null) {
				nativeFile = openFile.getNativeFile();
				int size = openFile.getNativeFile().getFileSize();
				if (openFile.getPosition() >= size) {
					status |= PabConstants.st_endoffile;
				}
				if (size >= 256 * 65536) {
					status |= PabConstants.st_endofspace;
				}
			} else {
				try {
					nativeFile = NativeFileFactory.createNativeFile(file);
				} catch (IOException e) {
					status |= PabConstants.st_noexist;
				}
			}
			
			if (nativeFile != null) {
				int fdrflags = nativeFile.getFlags();
				if ((fdrflags & FDR.ff_internal) != 0)
					status |= PabConstants.st_internal;
				if ((fdrflags & FDR.ff_program) != 0)
					status |= PabConstants.st_program;
				if ((fdrflags & FDR.ff_variable) != 0)
					status |= PabConstants.st_variable;
				if ((fdrflags & FDR.ff_protected) != 0)
					status |= PabConstants.st_protected;
			}
			
		}
		
		//System.out.printf("Status: %02X\n", status);
		pab.scrnoffs = status;
	}
	public static FDR createNewFDR(String dsrFile) throws DsrException {
		// make a FDR file for it
		V9t9FDR fdr = new V9t9FDR();
		try {
			fdr.setFileName(dsrFile);
		} catch (IOException e2) {
			throw new DsrException(PabConstants.e_badfiletype, e2);
		}
		return fdr;
	}
	
}