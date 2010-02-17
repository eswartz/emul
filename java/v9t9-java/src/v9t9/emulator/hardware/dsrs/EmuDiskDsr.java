/**
 * 
 */
package v9t9.emulator.hardware.dsrs;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.ejs.coffee.core.utils.HexUtils;

import v9t9.emulator.clients.builtin.video.tms9918a.VdpTMS9918A;
import v9t9.emulator.hardware.dsrs.EmuDiskDsr.EmuDiskPabHandler.PabInfoBlock;
import v9t9.emulator.runtime.Executor;
import v9t9.engine.files.FDR;
import v9t9.engine.files.IFDRFlags;
import v9t9.engine.files.InvalidFDRException;
import v9t9.engine.files.NativeFDRFile;
import v9t9.engine.files.NativeFile;
import v9t9.engine.files.NativeFileFactory;
import v9t9.engine.files.NativeTextFile;
import v9t9.engine.files.V9t9FDR;
import v9t9.engine.memory.ByteMemoryAccess;
import v9t9.engine.memory.DiskMemoryEntry;
import v9t9.engine.memory.MemoryDomain;

/**
 * This is a device which allows accessing files on the local filesystem.
 * @author ejs
 *
 */
public class EmuDiskDsr implements DsrHandler {

	/* emudisk.dsr */
	/* this first group doubles as device codes */
	public static final int D_DSK = 0; 	// standard file operation on DSK.XXXX.[YYYY]
	public static final int D_DSK1 = 1;	// standard file operation on DSK1.[YYYY]
	public static final int D_DSK2 = 2;	// ...
	public static final int D_DSK3 = 3;	// ...
	public static final int D_DSK4 = 4;	// ...
	public static final int D_DSK5 = 5;	// ...

	public static final int MAXDRIVE = 5;
	public static final int D_INIT = 6;		// initialize disk DSR
	public static final int D_DSKSUB = 7;	// subroutines

	public static final int D_SECRW = 7;	// sector read/write    (10)
	public static final int D_FMTDISK = 8;	// format disk          (11)
	public static final int D_PROT = 9;		// file protection      (12)
	public static final int D_RENAME = 10;	// rename file          (13)
	public static final int D_DINPUT = 11;	// direct input file    (14)
	public static final int D_DOUTPUT = 12;	// direct output file   (15)
	public static final int D_16 = 13;		// set the VDP end of buffer (like call files) (16)

	public static final int D_FILES = 14;	
			
	/*	Error codes for subroutines */
	public static final byte es_okay = 0;
	public static final byte es_outofspace = 0x4;
	public static final byte es_cantopenfile = 0x1;
	public static final byte es_filenotfound = 0x1;
	public static final byte es_badfuncerr = 0x7;
	public static final byte es_fileexists = 0x7;
	public static final byte es_badvalerr = 0x1;
	public static final byte es_hardware = 0x6;
	
	private DiskMemoryEntry memoryEntry;
	private short vdpnamebuffer;
	private final IFileMapper mapper;
	
	public EmuDiskDsr(IFileMapper mapper) {
		this.mapper = mapper;
	}
	
	public String getName() {
		return "Emulated Disk DSR";
				
	}
	public short getCruBase() {
		return 0x1000;
	}
	public void activate(MemoryDomain console) throws IOException {
		if (memoryEntry == null)
			this.memoryEntry = DiskMemoryEntry.newWordMemoryFromFile(
					0x4000, 0x2000, "File Stream DSR ROM", console,
					"emudisk.bin", 0, false);
		
		console.mapEntry(memoryEntry);
	}
	
	public void deactivate(MemoryDomain console) {
		console.unmapEntry(memoryEntry);
	}

	public boolean handleDSR(MemoryTransfer xfer, short code) {
		switch (code) {
			// PAB file operation on DSKx 
		case D_DSK1:
		case D_DSK2:
		case D_DSK3:
		case D_DSK4:
		case D_DSK5:
		{
			EmuDiskPabHandler handler = new EmuDiskPabHandler(getCruBase(), xfer, mapper);
			info(handler.toString());
			try {
				handler.run();
			} catch (DsrException e) {
				handler.error(e);
			}
			handler.store();
	
			//  return, indicating that the DSR handled the operation 
			return true;
		}
			/* init disk dsr */
		case D_INIT:
		{
			/*
			int         x;
	
			for (x = 0; x < MAXFILES; x++) {
				memset((void *)&files[x], 0, sizeof(files[0]));
			}
	
			// Set up timer stuff for catalogs 
			for (x = 0; x < MAXDRIVE; x++) {
				DskCatFlag[x] = 1;
				if (!DskCatTag[x])
					DskCatTag[x] = TM_UniqueTag();
			}
			 */
	
			EmuDiskPabHandler.getPabInfoBlock(getCruBase()).reset();
			
			// also steal some RAM for the name compare buffer,
			//  so dependent programs can function 
			vdpnamebuffer = (short) (xfer.readParamWord(0x70) - 9);
			xfer.writeParamWord(0x70, (short) (vdpnamebuffer - 1));
			return false;  // does not bump return
		}
	
			/* ???? */
		/*
		case D_16:
		{
			console.writeByte(rambase+0x50, 0);	// no error 
			bumpReturnAddress(cpu);
			break;
		}*/
	
			/* call files(x) */
		case D_FILES:
			//xfer.writeParamWord(0x2C, (short) (xfer.readParamWord(0x2C) + 12));
			//xfer.writeParamByte(0x42, (byte) 0);
			//xfer.writeParamByte(0x50, (byte) 0);
			PabInfoBlock block = EmuDiskPabHandler.getPabInfoBlock(getCruBase());
			
			int cnt = xfer.readParamWord(0x4c);
			if (block.openFiles.size() > cnt) {
				xfer.writeParamByte(0x50, (byte) es_badfuncerr);
			} else if (cnt < 1 || cnt >= 16) { 
				xfer.writeParamByte(0x50, (byte) es_badvalerr);
			} else {
				xfer.writeParamByte(0x50, (byte) 0);
				block.maxOpenFiles = cnt;
			}
			return true;
	
		//case D_SECRW:
		//case D_FMTDISK:
		//case D_PROT:
		//case D_RENAME:
		case D_DINPUT:
		case D_DOUTPUT:
		{
			DirectDiskHandler handler = new DirectDiskHandler(xfer, mapper, code);
	
			if (handler.getDevice() <= MAXDRIVE) {
				try {
					handler.run();
				} catch (DsrException e) {
					handler.error(e);
				}
				return true;
			} else {
				// unhandled device
				return false;
			}
		}
	
		default:
			info("EmuDiskDSR: ignoring code = " + code);
			return false;
		}
	}

	/**
	 * @param string
	 */
	private static void info(String string) {
		if (Executor.settingDumpFullInstructions.getBoolean())
			Executor.getDumpfull().println(string);
		System.out.println(string);
		
	}

	/**
	 * This maps DSR device+filenames back and forth to disk ones
	 * @author ejs
	 *
	 */
	public interface IFileMapper {
		/**
		 * Get the candidate file for the given device.filename
		 * @param deviceFilename name like DSK1.FOO
		 * @return File (directory or file possibly not existing)
		 */
		File getLocalDottedFile(String deviceFilename);
		
		/**
		 * Get the candidate file for the given filename
		 * @param device
		 * @param filename
		 * @return File (directory or file possibly not existing)
		 */
		File getLocalFile(String device, String filename);
		
		/**
		 * Get the local filename for the given DSR filename.
		 * @param fileName the DSR filename (without device)
		 * @return the local filename 
		 */
		String getLocalFileName(String fileName);
		
		/**
		 * Get the root device file (e.g. for a file or filepath) 
		 */
		File getLocalRoot(File file);
		
		/**
		 * Get the DSR filename for the given filename
		 * @param filename the file segment (or dotted path)
		 * @return DSR-formatted filename
		 */
		String getDsrFileName(String filename);
		
		/**
		 * Get the device matching the given directory (exactly;
		 * use {@link #getLocalRoot(File)} if needed)
		 * @return device name or <code>null</code>
		 */
		String getDsrDeviceName(File dir);
		
	}
	

	/** Information about an open file. */
	public static class OpenFile {
		final String devName;
		final String fileName;
		final byte[] sector = new byte[256];
		private final File file;
		
		private NativeFile nativefile;

		/** number of sector in sector buffer */
		int currentSecNum = -1;
		
		int position;
		int secnum;
		int byteoffs;
		
		boolean modified;
		
		public OpenFile(File file, String devName, String fileName) throws DsrException {
			this.file = file;
			if (file.exists()) {
				try {
					this.nativefile = NativeFileFactory.createNativeFile(file);
				} catch (IOException e) {
					this.nativefile = new NativeTextFile(file);
				}
			}
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
		
		public void create(int fdrflags, int reclen) throws DsrException {
			FDR fdr = createNewFDR(fileName);
			fdr.setFlags(fdrflags);
			fdr.setRecordLength(reclen);
			
			if ((fdrflags & IFDRFlags.ff_variable) != 0)
				fdr.setRecordsPerSector(255 / (reclen + 1));
			else
				fdr.setRecordsPerSector(256 / reclen);
			
			nativefile = new NativeFDRFile(file, fdr);
			try {
				nativefile.flush();
			} catch (IOException e) {
				throw new DsrException(PabConstants.e_outofspace, e, "Failed to create: " + file);
			}
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
			return nativefile instanceof NativeFDRFile ? (((NativeFDRFile) nativefile).getFDR().getFlags() & IFDRFlags.ff_variable) != 0 : true;
		}

		public int getRecordLength() {
			int len = nativefile instanceof NativeFDRFile ? ((NativeFDRFile) nativefile).getFDR().getRecordLength() : 80;
			if (len == 0)
				len = 256;
			return len;
		}
		public int getNumberRecords() {
			int num = nativefile instanceof NativeFDRFile ? ((NativeFDRFile) nativefile).getFDR().getNumberRecords() : 0;
			return num;
		}
		protected void ensureSector() throws DsrException {
			if (currentSecNum != secnum) {
				currentSecNum = secnum;
				try {
					if (nativefile != null)
						nativefile.readContents(sector, 0, secnum * 256, sector.length);
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
				throw new DsrException(PabConstants.e_endoffile, "End of file: " + file);
			
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
			}
		}

		/**
		 * @return
		 */
		public boolean isProgram() {
			return nativefile != null && (nativefile.getFDRFlags() & IFDRFlags.ff_program) != 0;
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
			return nativefile != null && (nativefile.getFDRFlags() & IFDRFlags.ff_protected) != 0;
		}
	}
	
	public static class EmuDiskPabHandler extends PabHandler {

		public static class PabInfoBlock {
			Map<Short, OpenFile> openFiles = new HashMap<Short, OpenFile>();
			int maxOpenFiles;
			
			public PabInfoBlock() {
				reset();
			}
			
			/**
			 * 
			 */
			public void reset() {
				maxOpenFiles = 3;
				for (OpenFile file : openFiles.values())
					try {
						file.close();
					} catch (DsrException e) {
						e.printStackTrace();
					}
				openFiles.clear();
			}

			protected OpenFile allocOpenFile(short pabaddr, File file, String devName, String fileName) throws DsrException {
				OpenFile pabfile = openFiles.get(pabaddr);
				if (pabfile != null) {
					pabfile.close();
				} else {
					if (openFiles.size() >= maxOpenFiles)
						throw new DsrException(PabConstants.e_outofspace, null, "Too many open files");
				}
				pabfile = new OpenFile(file, devName, fileName);
				openFiles.put(pabaddr, pabfile);
				return pabfile;
			}

			public OpenFile findOpenFile(short pabaddr) {
				return openFiles.get(pabaddr);
			}

			/**
			 * @param pabaddr
			 */
			public void removeOpenFile(short pabaddr) {
				openFiles.remove(pabaddr);
			}


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

		private final IFileMapper mapper;
		private PabInfoBlock block;

		public EmuDiskPabHandler(short cruaddr, MemoryTransfer xfer, IFileMapper mapper) {
			super(xfer);
			this.block = getPabInfoBlock(cruaddr);
			this.mapper = mapper;
		}

		public EmuDiskPabHandler(short cruaddr, MemoryTransfer xfer, IFileMapper mapper, PabStruct pab) {
			super(xfer, pab);
			this.block = getPabInfoBlock(cruaddr);
			this.mapper = mapper;
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
			else if (pab.opcode == PabConstants.op_read || pab.opcode == PabConstants.op_write) {
				builder.append(", " + pab.charcount + " #" + pab.recnum);
			}
			return builder.toString();
		}
		/**
		 * 
		 */
		public void run() throws DsrException {


			if (pab.opcode > 9) {
				throw new DsrException(PabConstants.e_illegal, "Bad opcode: " + pab.opcode);
			} 
			
			if (fname.indexOf('.') >= 0) {
				throw new DsrException(PabConstants.e_badfiletype, "Bad filename: " + fname);
			}
			
			File file = mapper.getLocalFile(devname, fname);
			if (file == null)
				throw new DsrException(PabConstants.e_baddevice, null, "Cannot map " + devname + " to host");
			
			switch (pab.opcode) {
			case PabConstants.op_open:
				DSKOpen(file);
				break;
			case PabConstants.op_close:
				DSKClose(file);
				break;
			case PabConstants.op_read:
				DSKRead(file);
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
				// TODO
				throw new DsrException(PabConstants.e_illegal, "Directory read not implemented");
			}
			
			OpenFile openFile = block.allocOpenFile(pab.pabaddr, file, devname, fname);
			
			int fdrflags = 0;
			if ((pab.pflags & PabConstants.fp_internal) != 0)
				fdrflags |= IFDRFlags.ff_internal;
			if ((pab.pflags & PabConstants.fp_variable) != 0)
				fdrflags |= IFDRFlags.ff_variable;

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
				int mask = IFDRFlags.ff_variable + IFDRFlags.ff_program;
				int extFdrFlags = (fdrFile.getFDR().getFlags() & IFDRFlags.ff_internal + IFDRFlags.ff_variable + IFDRFlags.ff_program);
				if ((extFdrFlags & mask) != (fdrflags & mask)) {
					throw new DsrException(PabConstants.e_badopenmode, "Open file mode does not match FDR: " + 
							HexUtils.toHex2(extFdrFlags) + " vs. " + HexUtils.toHex2(fdrflags));
				}
				if (fdrFile.getFDR().getRecordLength() != pab.preclen) {
					throw new DsrException(PabConstants.e_badopenmode, "Open file record size does not match FDR: " + fdrFile.getFDR().getRecordLength() + " vs " + pab.preclen);
				}
			}
			
			if (pab.getOpenMode() == PabConstants.m_output && pab.recnum != 0) {
				// TODO: generate this many records (A514C)
			}
			if (pab.getOpenMode() == PabConstants.m_append) {
				openFile.seekToEOF();
			}
		}

		private void DSKClose(File file) throws DsrException {
			OpenFile openFile = block.findOpenFile(pab.pabaddr);
			if (openFile == null)
				throw new DsrException(PabConstants.e_badfiletype, "File not open: " + file);
			
			openFile.close();
			block.removeOpenFile(pab.pabaddr);
			
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
				pab.recnum &= 0x7fff;
				openFile.seekToRecord(pab.recnum);
				pab.recnum++;
			}
			
			ByteMemoryAccess access = xfer.getVdpMemory(pab.bufaddr);
			
			try {
				pab.charcount = openFile.readRecord(access, pab.preclen);
				xfer.dirtyVdpMemory(pab.bufaddr, pab.charcount);
				
				if (true) {
					dump(pab.bufaddr, pab.charcount);
				}
			} catch (DsrException e) {
				if (e.getErrorCode() == PabConstants.e_endoffile) {
					DSKClose(file);
				}
				throw e;
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
			info("Read: " + builder + "\n | " + hexbuilder);
			
		}

		private void DSKRestore(File file) throws DsrException {
			if (!pab.isReading())
				throw new DsrException(PabConstants.e_illegal, "File not open for reading: " + file);
				
			OpenFile openFile = block.findOpenFile(pab.pabaddr);
			if (openFile == null)
				throw new DsrException(PabConstants.e_badfiletype, "File not open: " + file);
			
			pab.recnum &= 0x7fff;
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
				pab.recnum &= 0x7fff;
				openFile.seekToRecord(pab.recnum);
				pab.recnum++;
			}
			
			ByteMemoryAccess access = xfer.getVdpMemory(pab.bufaddr);
			openFile.writeRecord(access, pab.charcount);
		}
		
		private void DSKLoad(File file) throws DsrException {
			
			if (file.isDirectory()) {
				throw new DsrException(PabConstants.e_illegal, null, "Can't load catalog as binary: " + file);
			}
			
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

			if (file.isDirectory()) {
				throw new DsrException(PabConstants.e_illegal, null, "Can't delete catalog");
			}
			
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
		
			if (!openFile.getNativeFile().getFile().delete())
				throw new DsrException(PabConstants.e_hardwarefailure, null, "File not deleted: " + file);
		}


		private void DSKSave(File file) throws DsrException {

			if (file.isDirectory()) {
				throw new DsrException(PabConstants.e_illegal, null, "Can't save catalog as binary");
			}
			
			OpenFile openFile = new OpenFile(file, devname, fname);
			if (openFile.getNativeFile() != null) {
				if (openFile.isProtected()) {
					throw new DsrException(PabConstants.e_readonly, null, "File is protected: " + file);
				}
				
				file.delete();
			}
			
			// make a FDR file for it
			FDR fdr = createNewFDR(fname);
			fdr.setFlags(FDR.ff_program);
			
			// and a native file
			openFile.setNativeFile(new NativeFDRFile(file, fdr));
			openFile.flush();
			
			ByteMemoryAccess access = xfer.getVdpMemory(pab.bufaddr);
			try {
				int wrote = openFile.getNativeFile().writeContents(access.memory, access.offset, 
						0, pab.recnum);
				
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
					int fdrflags = nativeFile.getFDRFlags();
					if ((fdrflags & IFDRFlags.ff_internal) != 0)
						status |= PabConstants.st_internal;
					if ((fdrflags & IFDRFlags.ff_program) != 0)
						status |= PabConstants.st_program;
					if ((fdrflags & IFDRFlags.ff_variable) != 0)
						status |= PabConstants.st_variable;
					if ((fdrflags & IFDRFlags.ff_protected) != 0)
						status |= PabConstants.st_protected;
				}
				
			}
			
			pab.scrnoffs = status;
		}
		
	}
	
	public static class DirectDiskHandler {
		private final MemoryTransfer xfer;
		private byte dev;
		private byte opt;
		private short addr1;
		private short addr2;
		private final short code;
		private final IFileMapper mapper;
		private String devname;

		public DirectDiskHandler(MemoryTransfer xfer, IFileMapper mapper, short code) {
			this.xfer = xfer;
			this.mapper = mapper;
			this.code = code;
			
			dev = xfer.readParamByte(0x4c);
			devname = "DSK" + (char)(dev + '0');
			
			opt = xfer.readParamByte(0x4d);
			addr1 = xfer.readParamWord(0x4e);
			addr2 = xfer.readParamWord(0x50);
			
			// no error
			xfer.writeParamByte(0x50, (byte) 0);
		}
		
		public byte getDevice() {
			return dev;
		}
		
		public void run() throws DsrException {
			switch (code) {
			case D_DINPUT:
				directDiskInput();
				break;
			case D_DOUTPUT:
				directDiskOutput();
				break;
			default:
				throw new DsrException(es_badfuncerr, "Unhandled function: " + code);
			}
		}
		

		public void error(DsrException e) {
			xfer.writeParamByte(0x50, (byte) e.getErrorCode());
			if (e != null)
				info(e.getMessage());
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
				file = NativeFileFactory.createNativeFile(mapper.getLocalFile(devname, filename));
			} catch (IOException e) {
				throw new DsrException(es_filenotfound, e);
			}
			
			parms = (parms >> 8);

			if (secs == 0) {
				// read FDR info
				if (file instanceof NativeFDRFile) {
					NativeFDRFile fdrFile = (NativeFDRFile) file;
					FDR fdr = fdrFile.getFDR();
					xfer.writeParamWord(parms + 2, (short) fdr.getSectorsUsed());
					xfer.writeParamByte(parms + 4, (byte) fdr.getFlags());
					xfer.writeParamByte(parms + 5, (byte) fdr.getRecordsPerSector());
					xfer.writeParamByte(parms + 6, (byte) fdr.getByteOffset());
					xfer.writeParamByte(parms + 7, (byte) fdr.getRecordLength());
					xfer.writeParamWord(parms + 8, (short) fdr.getNumberRecords());
				} else {
					int size = file.getFileSize();
					xfer.writeParamWord(parms + 2, (short) ((size + 255) / 256));
					xfer.writeParamByte(parms + 6, (byte) (size % 256));
				}

			} else {
				// read sectors
				short   vaddr = xfer.readParamWord(parms);
				short	secnum = xfer.readParamWord(parms + 2);

				info("reading "+secs+" sectors from sector #"+secnum+
						" in " + file + ", storing to >"+HexUtils.toHex4(vaddr));

				ByteMemoryAccess access = xfer.getVdpMemory(vaddr);
				try {
					int read = file.readContents(access.memory, access.offset, secnum * 256, secs * 256);
					xfer.dirtyVdpMemory(vaddr, read);
					// error will be set if sector read failed
					xfer.writeParamByte(0x4D, (byte) ((read + 255) >> 8));
				} catch (IOException e) {
					throw new DsrException(es_hardware, e);
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
					file = NativeFileFactory.createNativeFile(localFile);
				} catch (IOException e) {
					try {
						localFile.createNewFile();
					} catch (IOException e1) {
						throw new DsrException(es_hardware, e1, "Failed to create file: " + localFile);
					}
				}
				
				if (file instanceof NativeFDRFile) {
					NativeFDRFile fdrFile = (NativeFDRFile) file;
					FDR fdr = fdrFile.getFDR();
					fdr.setSectorsUsed(xfer.readParamWord(parms + 2) & 0xffff);
					fdr.setFlags(xfer.readParamByte(parms + 4) & 0xff);
					fdr.setRecordsPerSector(xfer.readParamByte(parms + 5) & 0xff);
					fdr.setByteOffset(xfer.readParamByte(parms + 6) & 0xff);
					fdr.setRecordLength(xfer.readParamByte(parms + 7) & 0xff);
					fdr.setNumberRecords(xfer.readParamWord(parms + 8) & 0xffff);
					try {
						fdr.writeFDR(file.getFile());
					} catch (IOException e) {
						throw new DsrException(es_hardware, e, "Failed to write FDR: " + file.getFile());
					}
				}
				
				// change real file
				int byteoffs = xfer.readParamByte(parms + 6);
				int numsecs = xfer.readParamWord(parms + 2);
				if (byteoffs != 0)
					numsecs++;
				int size = numsecs * 256 + byteoffs;
				int oldsize = file.getFileSize();
				try {
					file.setFileSize(size);
				} catch (IOException e) {
					throw new DsrException(oldsize < size ? es_outofspace : es_hardware, e, "Failed to resize file: " + file.getFile());
				}
				int flags = xfer.readParamByte(parms + 4);
				if (!file.getFile().setWritable((flags & IFDRFlags.ff_protected) == 0)) {
					throw new DsrException(es_hardware, "Error updating file protection status");
				}

			} else {
				// write sectors
				try {
					file = NativeFileFactory.createNativeFile(localFile);
				} catch (IOException e) {
					throw new DsrException(es_filenotfound, "File not found: " + localFile);
				}
				
				short   vaddr = xfer.readParamWord(parms);
				short	secnum = xfer.readParamWord(parms + 2);

				info("writing "+secs+" sectors to sector #"+secnum+
						" in " + file + ", reading from >"+HexUtils.toHex4(vaddr));

				ByteMemoryAccess access = xfer.getVdpMemory(vaddr);
				try {
					//String contents = new String(access.memory, access.offset, secs * 256);
					//System.out.println(contents);
					int wrote = file.writeContents(access.memory, access.offset, secnum * 256, secs * 256);
					// error will be set if sector write failed
					xfer.writeParamByte(0x4D, (byte) ((wrote + 255) >> 8));
				} catch (IOException e) {
					throw new DsrException(es_outofspace, e);
				}
			}
		}

	}

	/**
	 * @return
	 * @throws DsrException 
	 */
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
