/**
 * 
 */
package v9t9.emulator.hardware.dsrs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.ejs.coffee.core.utils.HexUtils;
import org.ejs.coffee.core.utils.Setting;

import v9t9.emulator.EmulatorSettings;
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
 * This is a device handler which allows accessing files on the local filesystem.
 * Each directory is a disk.  The DSR instructions in ROM are "enhanced instructions"
 * that forward to the DSR manager and trigger this code.
 * @author ejs
 *
 */
public class EmuDiskDsr implements DsrHandler {
	public static final Setting emuDiskDsrEnabled = new Setting("EmuDiskDSREnabled", Boolean.TRUE);
	
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
		
    	String diskRootPath = EmulatorSettings.getInstance().getBaseConfigurationPath() + "disks";
    	File diskRootDir = new File(diskRootPath);
    	File dskdefault = new File(diskRootDir, "default");
    	dskdefault.mkdirs();
    	
    	for (String dev : new String[] { "DSK1", "DSK2", "DSK3", "DSK4", "DSK5" }) {
    		DiskDirectoryMapper.INSTANCE.registerDiskPath(dev, dskdefault); 
    	}
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
		if (!emuDiskDsrEnabled.getBoolean())
			return false;
		
		switch (code) {
		// PAB file operation on DSKx 
		case D_DSK:
			// find disk
		case D_DSK1:
		case D_DSK2:
		case D_DSK3:
		case D_DSK4:
		case D_DSK5:
		{
			EmuDiskPabHandler handler = new EmuDiskPabHandler(getCruBase(), xfer, mapper);
			
			if (handler.devname.equals("DSK1")
					|| handler.devname.equals("DSK2")
					|| (handler.devname.equals("DSK") && handler.mapper.getLocalFile(handler.devname, handler.fname) == null)) {
				if (DiskImageDsr.diskImageDsrEnabled.getBoolean())
					return false;
			}
			
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
			EmuDiskPabHandler.getPabInfoBlock(getCruBase()).reset();
			DirectDiskHandler.getDiskInfoBlock(getCruBase()).reset();
			
			// also steal some RAM for the name compare buffer,
			//  so dependent programs can function 
			vdpnamebuffer = (short) (xfer.readParamWord(0x70) - 9);
			xfer.writeParamWord(0x70, (short) (vdpnamebuffer - 1));
			
			// ???
			xfer.writeParamWord(0x6c, (short) 0x404);
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
			PabInfoBlock block = EmuDiskPabHandler.getPabInfoBlock(getCruBase());
			
			int cnt = xfer.readParamWord(0x4c);
			if (block.openFiles.size() > cnt) {
				xfer.writeParamWord(0x50, (short) -1);
			} else if (cnt < 1 || cnt >= 16) { 
				xfer.writeParamWord(0x50, (short) -1);
			} else {
				xfer.writeParamWord(0x50, (short) 0);
				block.maxOpenFileCount = cnt;
			}
			return true;
	
		//case D_FMTDISK:
		//case D_PROT:
		//case D_RENAME:
		case D_DINPUT:
		case D_DOUTPUT:
		case D_SECRW:
		{
			DirectDiskHandler handler = new DirectDiskHandler(getCruBase(), xfer, mapper, code);
	
			if (handler.dev <= 2 && DiskImageDsr.diskImageDsrEnabled.getBoolean())
				return false;
			
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
		 * Get all the registered settings (String)
		 */
		Setting[] getSettings();
		
		void saveState(IDialogSettings section);
		void loadState(IDialogSettings section);
		
		/**
		 * Get the candidate file for the given device.filename
		 * @param deviceFilename name like DSK1.FOO
		 * @return File (directory or file possibly not existing)
		 */
		File getLocalDottedFile(String deviceFilename);
		
		/**
		 * Get the candidate file for the given filename
		 * @param device
		 * @param filename name, or null
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
		 * Get the root device with this DSR name (e.g. FOO in DSK.FOO) 
		 */
		String getDeviceNamed(String name);
		
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
				fdr.setRecordsPerSector(Math.min(255, 256 / reclen));
			
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
					if (nativefile != null) {
						int read = nativefile.readContents(sector, 0, secnum * 256, sector.length);
						// if short, clear sector (when seeking past EOF to write data, don't repeat other records)
						if (read <= sector.length)
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
			} else {
				seekToPosition(recnum);
			}
		}

		/**
		 * @return
		 */
		public boolean isProgram() {
			return nativefile != null && (nativefile.getFlags() & IFDRFlags.ff_program) != 0;
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
			return nativefile != null && (nativefile.getFlags() & IFDRFlags.ff_protected) != 0;
		}
	}

	static private byte drcTrans[][] = new byte[][] { 
		{0, 1}, {FDR.ff_program, 5},
		{FDR.ff_internal, 3}, {(byte) FDR.ff_variable, 2},
		{(byte) (FDR.ff_variable + FDR.ff_internal), 4}
	};

	private static class DirectoryInfo {

		protected File[] entries;
		protected final IFileMapper mapper;
		protected File dir;

		public DirectoryInfo(File file, IFileMapper mapper) {
			this.mapper = mapper;
			
			this.dir = file;
			this.entries = file != null ? file.listFiles() : new File[0];
			Arrays.sort(entries);
		}

	}
	
	private static class FileLikeDirectoryInfo extends DirectoryInfo {

		private int index;
		private long totalSectors;
		private long freeSectors;
		private int lastEntry;

		public FileLikeDirectoryInfo(File file, IFileMapper mapper) {
			super(file, mapper);
			lastEntry = Math.min(128, entries.length);
			totalSectors = (file.getTotalSpace() + 255) / 256;
			freeSectors = (file.getFreeSpace() + 255) / 256;
		}

		public void setIndex(int index) {
			this.index = index;
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

			if (index >= lastEntry) {
				// make an empty record
				access.memory[offset++] = (byte) 0;
				offset = writeFloat(access, offset, 0);
				offset = writeFloat(access, offset, 0);
				offset = writeFloat(access, offset, 0);
				index++;
				return offset - access.offset;
			}
			
			// Get file info
			File file = entries[index - 1];
			
			NativeFile nativefile;
			try {
				nativefile = NativeFileFactory.createNativeFile(file);
			} catch (IOException e) {
				nativefile = new NativeTextFile(file);
			}
			
			// first field is the string representing the
			// file or volume name
			offset = writeName(access, offset, mapper.getDsrFileName(file.getName()));

			// second field is file type
			int flags = nativefile instanceof NativeFDRFile ? ((NativeFDRFile) nativefile).getFlags() : FDR.ff_variable;
			{
				int         idx;

				for (idx = 0; idx < drcTrans.length; idx++)
					if (drcTrans[idx][0] ==
						(flags & (FDR.ff_internal | FDR.ff_program | FDR.ff_variable))) {
						offset = writeFloat(access, offset, drcTrans[idx][1]);
						break;
					}
				// no match == program
				if (idx >= drcTrans.length) {
					offset = writeFloat(access, offset, 1);
				}
			}

			// third field is file size, one sector for fdr
			offset = writeFloat(access, offset, 1 + (nativefile.getFileSize() + 255) / 256);

			// fourth field is record size
			offset = writeFloat(access, offset, nativefile instanceof NativeFDRFile ? ((NativeFDRFile) nativefile).getFDR().getRecordLength() : 80);

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
	public static class EmuDiskPabHandler extends PabHandler {

		public static class PabInfoBlock {
			Map<Short, OpenFile> openFiles = new HashMap<Short, OpenFile>();
			Map<Short, FileLikeDirectoryInfo> openDirectories = new HashMap<Short, FileLikeDirectoryInfo>();
			int maxOpenFileCount;
			int openFileCount;
			
			public PabInfoBlock() {
				reset();
			}
			
			/**
			 * 
			 */
			public void reset() {
				maxOpenFileCount = 3;
				openFileCount = 0;
				for (OpenFile file : openFiles.values())
					try {
						file.close();
					} catch (DsrException e) {
						e.printStackTrace();
					}
				openFiles.clear();
				openDirectories.clear();
			}

			protected OpenFile allocOpenFile(short pabaddr, File file, String devName, String fileName) throws DsrException {
				OpenFile pabfile = openFiles.get(pabaddr);
				if (pabfile != null) {
					pabfile.close();
				} else {
					if (openFileCount >= maxOpenFileCount)
						throw new DsrException(PabConstants.e_outofspace, null, "Too many open files");
					openFileCount++;
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
				openFileCount--;
			}

			/**
			 * @param pabaddr
			 * @param file
			 * @param dskName 
			 * @throws DsrException 
			 */
			public void openDirectory(short pabaddr, File file, IFileMapper mapper) throws DsrException {
				if (openFileCount >= maxOpenFileCount) {
					throw new DsrException(PabConstants.e_outofspace, null, "Too many open files");
				}
				FileLikeDirectoryInfo info = new FileLikeDirectoryInfo(file, mapper);
				openDirectories.put(pabaddr, info);
				openFileCount++;
			}

			/**
			 * @param pabaddr
			 */
			public void closeDirectory(short pabaddr) {
				openDirectories.remove(pabaddr);
				openFileCount--;
			}

			/**
			 * @param pabaddr
			 * @return
			 */
			public FileLikeDirectoryInfo getDirectory(short pabaddr) {
				return openDirectories.get(pabaddr);
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
					DSKClose(file);
				else
					DSKCloseCatalog(file);
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
			info("Read: " + builder + "\n | " + hexbuilder);
			
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

		private void DSKClose(File file) throws DsrException {
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
			
			ByteMemoryAccess access = xfer.getVdpMemory(pab.bufaddr);
			pab.charcount = openFile.readRecord(access, pab.preclen);
			xfer.dirtyVdpMemory(pab.bufaddr, pab.charcount);
			
			if (false) {
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
		
		private void DSKCloseCatalog(File file) throws DsrException {
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
			
			if (true) {
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
			FDR fdr = createNewFDR(fname);
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
	
	static class DiskFileRange {
		int fdrSector;
		int start;
		int len;
		NativeFile file;
	}
	
	static class DiskLikeDirectoryInfo extends DirectoryInfo {

		Map<File, DiskFileRange> sectorRanges = new HashMap<File, DiskFileRange>();
		private String devname;
		private int lastSector;
		
		public DiskLikeDirectoryInfo(File dir, IFileMapper mapper, String devname) {
			super(dir, mapper);
			this.devname = devname;
			
			int sec = 2;
			for (File entry : entries) {
				if (sec >= 65536)
					break;
				
				DiskFileRange range = new DiskFileRange();
				try {
					range.file = NativeFileFactory.createNativeFile(entry);
					range.fdrSector = sec++;
					range.start = sec;
					range.len = range.file.getSectorsUsed();
					sec += range.len;
					sectorRanges.put(entry, range);
				} catch (IOException e) {
				}
			}
			this.lastSector = sec;
		}
		
		/**
		 * @return the lastSector
		 */
		public int getLastSector() {
			return lastSector;
		}

		public void synthesizeFDRSector(ByteMemoryAccess access, DiskFileRange range) throws DsrException {
			int offset = access.offset;
			
			Arrays.fill(access.memory, offset, offset + 256, (byte) 0);
			
			if (range == null) {
				return;
			}
			
			String name = mapper.getDsrFileName(range.file.getFile().getName());
			for (int i = 0; i < 10; i++) {
				if (i < name.length())
					access.memory[offset++] = (byte) name.charAt(i);
				else
					access.memory[offset++] = (byte) 0x20;
			}
			
			// reserved
			access.memory[offset++] = (byte) 0x0;
			access.memory[offset++] = (byte) 0x0;
			
			// file type
			access.memory[offset++] = (byte) range.file.getFlags();
			access.memory[offset++] = (byte) range.file.getRecordsPerSector();
			
			int numsecs = (int) range.file.getSectorsUsed();
			access.memory[offset++] = (byte) (numsecs / 256);
			access.memory[offset++] = (byte) (numsecs % 256);
			
			access.memory[offset++] = (byte) range.file.getByteOffset();
			access.memory[offset++] = (byte) range.file.getRecordLength();
			
			int numrecs = range.file.getNumberRecords();
			access.memory[offset++] = (byte) (numrecs % 256);
			access.memory[offset++] = (byte) (numrecs / 256);
			
			while (offset< 0x1C) {
				access.memory[offset++] = (byte) 0;
			}
			
			// sectors per track
			access.memory[offset++] = (byte) 18;
			
			int left = range.len;
			int ofs = range.start;
			while (left > 0) {
				// >UM >SN >OF == >NUM >OFS
				int num = Math.min(0xfff, left);
				access.memory[offset++] = (byte) (num & 0xff);
				access.memory[offset++] = (byte) (((num >> 8) & 0x0f) | ((ofs & 0xf) << 4));
				access.memory[offset++] = (byte) ((ofs >> 4) & 0xff);
				left -= num;
			}
			
		}

		public void synthesizeVolumeSector(ByteMemoryAccess access) throws DsrException {
			int offset = access.offset;
			
			File localFile = mapper.getLocalFile(devname, null);
			if (localFile == null)
				throw new DsrException(es_hardware, "No directory for " + devname);
			
			String diskname = mapper.getDsrFileName(localFile.getName());
			for (int i = 0; i < 10; i++) {
				if (i < diskname.length())
					access.memory[offset++] = (byte) diskname.charAt(i);
				else
					access.memory[offset++] = (byte) 0x20;
			}
			
			// # sectors
			long numsecs = Math.min(65535, (localFile.getTotalSpace() / 256));
			access.memory[offset++] = (byte) (numsecs / 256);
			access.memory[offset++] = (byte) (numsecs % 256);
			
			// sectors per track
			access.memory[offset++] = (byte) 18;
			
			// DSR mark
			access.memory[offset++] = 'D';
			access.memory[offset++] = 'S';
			access.memory[offset++] = 'K';
			
			// protection
			access.memory[offset++] = ' ';
			
			// # tracks/side
			access.memory[offset++] = 40;

			// # sides
			access.memory[offset++] = 2;
			
			// density
			access.memory[offset++] = 2;
		
			// reserved
			while (offset < access.offset + 0x38) {
				access.memory[offset++] = 0;
			}
			
			// bitmap
			while (offset < access.offset + 0xec) {
				access.memory[offset++] = (byte) 0xaa;
			}
			
			// reserved
			while (offset < access.offset + 0x100) {
				access.memory[offset++] = (byte) 0xff;
			}
		}
		
		public void synthesizeIndexSector(ByteMemoryAccess access) {
			int offset = access.offset;
			
			int cnt = 128;
			for (Map.Entry<File, DiskFileRange> entry : sectorRanges.entrySet()) {
				int fdrSector = entry.getValue().fdrSector;
				access.memory[offset++] = (byte) (fdrSector / 256);
				access.memory[offset++] = (byte) (fdrSector % 256);
				if (cnt-- == 0)
					break;
			}
			while (cnt-- > 0) {
				access.memory[offset++] = (byte) 0;
				access.memory[offset++] = (byte) 0;
			}
		}

		public void synthesizeSector(ByteMemoryAccess access, int secnum) throws IOException {
			for (Map.Entry<File, DiskFileRange> entry : sectorRanges.entrySet()) {
				DiskFileRange range = entry.getValue();
				if (secnum == range.fdrSector) {
					synthesizeFDRSector(access, range);
					return;
				} else if (secnum >= range.start && secnum < range.start + range.len) {
					range.file.readContents(access.memory, access.offset, 
							(secnum - range.start) * 256, 256);
					return;
				}
			}
			Arrays.fill(access.memory, access.offset, access.offset + 256, (byte) 0xe5);
		}
	}
	public static class DirectDiskHandler {
		static class DirectDiskInfo {
			Map<File, DiskLikeDirectoryInfo> dirInfos = new HashMap<File, DiskLikeDirectoryInfo>();

			public void reset() {
				dirInfos.clear();
			}
			
		}
		private final MemoryTransfer xfer;
		private byte dev;
		private byte opt;
		private short addr1;
		private short addr2;
		private final short code;
		private String devname;
		private final IFileMapper mapper;

		private static Map<Short, DirectDiskInfo> diskInfoBlocks = new HashMap<Short, DirectDiskInfo>();
		private final short cru;
		
		public static DirectDiskInfo getDiskInfoBlock(short cru) {
			DirectDiskInfo block = diskInfoBlocks.get(cru);
			if (block == null) {
				block = new DirectDiskInfo();
				diskInfoBlocks.put(cru, block);
			}
			return block;
		}
		
		public DirectDiskHandler(short cru, MemoryTransfer xfer, IFileMapper mapper, short code) {
			this.cru = cru;
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
			case D_SECRW:
				directSectorReadWrite();
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
				xfer.writeParamByte(0x50, (byte) 0);

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
					xfer.writeParamByte(0x50, (byte) 0);
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
				}
				if (file == null || file.getFileSize() == 0) {
					file = new NativeFDRFile(localFile, new V9t9FDR());
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
					if (fdr instanceof V9t9FDR) {
						try {
							((V9t9FDR)fdr).setFileName(filename);
						} catch (IOException e) {
						}
					}
					try {
						fdr.writeFDR(file.getFile());
						xfer.writeParamByte(0x50, (byte) 0);
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
				int oldsize = file != null ? file.getFileSize() : 0;
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
					xfer.writeParamByte(0x50, (byte) 0);
				} catch (IOException e) {
					throw new DsrException(es_outofspace, e);
				}
			}
		}
		
		private void directSectorReadWrite() throws DsrException {
			boolean write = opt == 0;
			
			if (write)
				throw new DsrException(es_hardware, "Not implemented");
			
			short addr = addr1;
			int secnum = addr2;
			
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
						throw new DsrException(es_hardware, e);
					}
				}	
			} else {
				Arrays.fill(access.memory, access.offset, access.offset + 256, (byte) 0xe5);
			}
			
			xfer.dirtyVdpMemory(addr, 256);
			xfer.writeParamByte(0x50, (byte) 0);
		}

		DiskLikeDirectoryInfo getDirectory() {
			DirectDiskInfo diskInfo = getDiskInfoBlock(cru);
			
			File dir = mapper.getLocalFile(devname, null);
			DiskLikeDirectoryInfo info = diskInfo.dirInfos.get(dir);
			if (info == null) {
				info = new DiskLikeDirectoryInfo(dir, mapper, devname);
				diskInfo.dirInfos.put(dir, info);
			}
			
			return info;
		}

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
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler#getSettings()
	 */
	public Setting[] getSettings() {
		List<Setting> settings = new ArrayList<Setting>();
		settings.add(emuDiskDsrEnabled);
		settings.addAll(Arrays.asList(mapper.getSettings()));
		return (Setting[]) settings.toArray(new Setting[settings.size()]);
	}
	public void saveState(IDialogSettings section) {
		emuDiskDsrEnabled.saveState(section);
		mapper.saveState(section.addNewSection("Mappings"));
	}
	
	public void loadState(IDialogSettings section) {
		if (section == null) return;
		emuDiskDsrEnabled.loadState(section);
		mapper.loadState(section.getSection("Mappings"));
	}
}
