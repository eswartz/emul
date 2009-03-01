/**
 * 
 */
package v9t9.emulator.hardware.dsrs;

import java.io.IOException;

import v9t9.emulator.Machine;
import v9t9.emulator.runtime.Cpu;
import v9t9.engine.VdpHandler;
import v9t9.engine.files.DataFiles;
import v9t9.engine.files.FDR;
import v9t9.engine.files.NativeFDRFile;
import v9t9.engine.files.NativeFile;
import v9t9.engine.files.NativeFileFactory;
import v9t9.engine.memory.ByteMemoryAccess;
import v9t9.engine.memory.DiskMemoryEntry;
import v9t9.engine.memory.MemoryDomain;
import v9t9.utils.Utils;

/**
 * This is a device which allows accessing files on the local filesystem.
 * @author ejs
 *
 */
public class EmuDiskDSR implements DsrHandler {

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
			
	public static final int  PABREC_SIZE = 10;
	
	/*	Error codes for subroutines */
	public static final byte es_okay = 0;
	public static final byte es_outofspace = 0x4;
	public static final byte es_cantopenfile = 0x1;
	public static final byte es_filenotfound = 0x1;
	public static final byte es_badfuncerr = 0x7;
	public static final byte es_fileexists = 0x7;
	public static final byte es_badvalerr = 0x1;
	public static final byte es_hardware = 0x6;
	
	private short rambase;
	
	private DiskMemoryEntry memoryEntry;
	private MemoryDomain console;
	private final DSRManager manager;
	private short vdpnamebuffer;
	private VdpHandler vdpHandler;
	
	public EmuDiskDSR(Machine machine) {
		this.manager = machine.getDSRManager();
		this.console = machine.getConsole();
		this.vdpHandler = machine.getVdp();
	}
	
	public String getName() {
		return "Emulated Disk DSR";
				
	}
	public short getCruBase() {
		return 0x1000;
	}
	public void activate() throws IOException {
		if (memoryEntry == null)
			this.memoryEntry = DiskMemoryEntry.newWordMemoryFromFile(
					0x4000, 0x2000, "File Stream DSR ROM", console,
					"emudisk.bin", 0, false);
		
		manager.activate(this);
		console.mapEntry(memoryEntry);
	}
	
	public void deactivate() {
		console.unmapEntry(memoryEntry);
		manager.deactivate(this);
	}

	public boolean handleDSR(Cpu cpu, short code) {
		rambase = (short) (cpu.getWP() - 0xe0);
		
		switch (code) {
			// PAB file operation on DSKx 
		case D_DSK1:
		case D_DSK2:
		case D_DSK3:
		case D_DSK4:
		case D_DSK5:
		{
			short         pabaddr = (short) (console.readWord(rambase+0x56) - 
									console.readWord(rambase+0x54) -
									PABREC_SIZE);
			byte opcode;
			short fnptr;
	
			fnptr = (short) (pabaddr+9);
	
			opcode = readVdpByte(fnptr-9);
	
			//illegal opcode? 
			if (opcode > 9)
				setPabError(fnptr, 0x3);
			else {
				/*
				static void (*opcodehandlers[]) (byte dev, byte *fn) = {
					DSKOpen, DSKClose, DSKRead, DSKWrite,
					DSKSeek, DSKLoad, DSKSave, DSKDelete,
					DSKScratch, DSKStatus
				};*/
	
				if (opcode == 5) {
					DSKLoad((byte) code, fnptr);
				} else {
					System.out.println("[not] doing operation "+opcode+" on DSK"+code);
					//opcodehandlers[opcode] (code, fnptr);
					setPabError(fnptr, 1);
				}
			}
	
			//  return, indicating that the DSR handled the operation 
			bumpReturnAddress(cpu);
			break;
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
	
			// also steal some RAM for the name compare buffer,
			//  so dependent programs can function 
			vdpnamebuffer = (short) (console.readWord(rambase+0x70) - 9);
			console.writeWord(rambase+0x70, (short) (vdpnamebuffer - 1));
			break;
		}
	
			/* ???? */
		/*
		case D_16:
		{
			console.writeByte(rambase+0x50, 0);	// no error 
			bumpReturnAddress(cpu);
			break;
		}*/
	
			/* call files(x) 
			   just ignore it */
		/*
		case D_FILES:
			console.writeWord(rambase+0x2C, console.readWord(rambase+0x2C) + 12);
			console.writeByte(rambase+0x42, 0);
			console.writeByte(rambase+0x50, 0);
			bumpReturnAddress(cpu);
			break;
	*/
	
		//case D_SECRW:
		//case D_FMTDISK:
		//case D_PROT:
		//case D_RENAME:
		case D_DINPUT:
		case D_DOUTPUT:
		{
			//static void (*handlers[]) (byte dev, byte opt, short addr1, short addr2) = {
			//	DskSectorRW, DskFormatDisk, DskProtect,
		//		DskRename, DskDirectInput, DskDirectOutput
			//};
			byte          dev;
			byte          opt;
			short         addr1, addr2;
	
			dev = console.flatReadByte(rambase+0x4c);
			opt = console.flatReadByte(rambase+0x4d);
			addr1 = console.readWord(rambase+0x4e);
			addr2 = console.readWord(rambase+0x50);
	
			System.out.println("doing operation "+code+" on DSK"+dev+" ["+opt+", "
					+Utils.toHex4(addr1)+",  "+ Utils.toHex4(addr2)+"]\n");
	
			if (dev <= MAXDRIVE) {
				if (code == D_DINPUT) {
					directDiskInput(dev, opt, addr1, addr2);
				} else if (code == D_DOUTPUT) {
					directDiskOutput(dev, opt, addr1, addr2);
				} else {
					console.writeByte(rambase + 0x50, es_badfuncerr);
				}
				
				bumpReturnAddress(cpu);
			}
			break;
		}
	
		default:
			System.out.println("EmuDiskDSR: ignoring code = " + code);
			
			return true;
		}
		
		return true;
	}

	private void setPabError(int fnptr, int code) {
		byte current = readVdpByte(fnptr - 8);
		current = (byte) ((current & ~0xe0) | (code << 5));
		writeVdpByte(fnptr - 8, current);
	}

	private void bumpReturnAddress(Cpu cpu) {
		console.writeWord(cpu.getWP() + 11*2, (short) (console.readWord(cpu.getWP() + 11*2) + 2));
	}
	
	/*	Error codes (byte 1, pflags)*/
	final static int
		m_error	= 0x7 << 5,

		e_baddevice = 0x0,
		e_readonly = 1 << 5,
		e_badopenmode = 2 << 5,
		e_illegal = 3 << 5,
		e_outofspace = 4 << 5,
		e_endoffile	= 5 << 5,
		e_hardwarefailure = 6 << 5,
		e_badfiletype = 7 << 5
	;
	
	class PabOpException extends IOException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5048051203516224874L;

		public PabOpException() {
			super();
			// TODO Auto-generated constructor stub
		}

		public PabOpException(String message) {
			super(message);
			// TODO Auto-generated constructor stub
		}

		
	}

	
	class pabrec
	{
		byte	opcode;		/* 0 file operation (f_xxx) */
		byte	pflags;		/* 1 file access code (fp_xxx) + open mode (m_xxx) [IN]*/
							/* error code [OUT] */
		short	addr;		/* 2 VDP record address */
		byte	preclen;	/* 4 file record length */
		byte	charcount;	/* 5 characters used in record */
		short	recnum;		/* 6 current record (seek position) */
		byte	scrnoffs;	/* 8 screen offset (for CSx) */
		byte	namelen;	/* 9 length of filename following */
		String	name;		/* filename */
	}

	class PabHandler {
		private byte len;
		private short fnaddr;
		private short pabaddr;
		private String fname;
		private byte dev;
		private NativeFile file;
		private pabrec pabrec;

		public PabHandler(byte dev, short fn) throws PabOpException {
			/*  rambase+0x56 holds a pointer into VDP RAM to the end
			   of the device name (RS232|, DSK|., DSK1|.ed) */
		
			//pf.fnptr = fn;
			//fiad_tifile_clear(&pf->tf);
		
			this.dev = dev;
			len = readVdpByte(fn);					/* length of device+filename */
			fnaddr = (short) (console.readWord(rambase+0x56) + 1);	/* addr of filename (skip period) */
			//module_logger(&emuDiskDSR, _L | L_2, _("getfilespec_pab:  fnaddr++ at rambase+0x56 = %20.20s       \n"),
			len -= console.readWord(rambase+0x54) + 1;	/* minus length of device + period */
			//module_logger(&emuDiskDSR, _L | L_2, _("getfilespec_pab:  length of device rambase+0x54 = %04X\n"),
			//	 memory_read_word(rambase+0x54));
			fname = readFilename(fnaddr, len);
			pabaddr = (short) (fn - 9);
		
			if (fname.indexOf('.') >= 0) {
				setPabError(fn, e_badfiletype);
				throw new PabOpException();
			}
			
			try {
				this.file = NativeFileFactory.createNativeFile(DataFiles.resolveFile(fname));
			} catch (IOException e) {
				throw (PabOpException) new PabOpException().initCause(e);
			}
			
			pabrec = new pabrec();
		}

		public void fetch(boolean opening) {

			pabrec.opcode = readVdpByte(pabaddr);
			pabrec.addr = readVdpShort(pabaddr + 2);
			pabrec.charcount = readVdpByte(pabaddr + 5);
			pabrec.recnum = readVdpShort(pabaddr + 6);
			pabrec.scrnoffs = readVdpByte(pabaddr + 8);
			pabrec.namelen = readVdpByte(pabaddr + 9);

			/* 
			   TI BASIC appears to trash these bytes after
			   opening the file.  We stubbornly continue to
			   use them, however, so don't reread from the
			   real PAB after opening. 
			*/
			if (opening)
			{
				pabrec.pflags = readVdpByte(pabaddr + 1);
				pabrec.preclen = readVdpByte(pabaddr + 4);

				//module_logger(&emuDiskDSR, _L | L_3, _("PAB contents: flags=>%02X, reclen=%d, addr=>%04X, charcount=%d, recnum=%d\n"),
				//	   pf->pab.pflags, pf->pab.preclen,
				//	   pf->pab.addr, pf->pab.charcount, pf->pab.recnum);
			}
			else
			{
				//module_logger(&emuDiskDSR, _L | L_3, _("PAB contents: addr=>%04X, charcount=%d, recnum=%d\n"),
				//	   pf->pab.addr, pf->pab.charcount, pf->pab.recnum);

			}
		}

		/**
		 * Compare that the PAB and file match
		 * @param file2
		 * @return
		 */
		public boolean compareToFile() {
			// TODO
			return true;
		}

		public void setError(int code) {
			byte current = readVdpByte(pabaddr + 1);
			current = (byte) ((current & ~0xe0) | (code << 5));
			writeVdpByte(pabaddr + 1, current);			
		}
	}
	


	private void DSKLoad(byte dev, short fnptr) {
		PabHandler pab;
		try {
			pab = new PabHandler(dev, fnptr);

		} catch (PabOpException e) {
			e.printStackTrace();
			return;
		}
		
		/* read PAB */
		pab.fetch(true /*opening*/);

		if (!pab.compareToFile())
			return;
			
		pab.setError(0);

		ByteMemoryAccess access = getVdpMemory(pab.pabrec.addr);
		try {
			int read = pab.file.readContents(access.memory, access.offset, 
					0, pab.pabrec.recnum);
			dirtyVdpMemory(pab.pabrec.addr, read);
			
			if (read >= 0) {
				// no error or EOF (which is okay for DSKLoad)

				// EJS 050221: nope, this isn't documented behavior
				// and it breaks TI Artist! when loading printer files.
				//pab->recnum = len;
			} else {
				// failure
				pab.setError(e_hardwarefailure);
			}

		} catch (IOException e) {
			pab.setError(es_hardware);
		}
		
		/* save changed PAB info */
		//pab_store_to_vdp(&pf);

		/* close file */
		//pab_close_file(&pf);
		
	}


	private void directDiskInput(byte dev, byte secs, short fname, int parms) {
		if (dev != 1) {
			setSubError(e_hardwarefailure, null);
			return;
		}
			
		String filename = readBareFilename(fname);
		NativeFile file = null;
		
		setSubError(0, null);
		try {
			file = NativeFileFactory.createNativeFile(DataFiles.resolveFile(filename));
		} catch (IOException e) {
			setSubError(es_filenotfound, e);
			return;
		}
		
		parms = rambase+0x00 + (parms >> 8);

		if (secs == 0) {
			// read FDR info
			if (file instanceof NativeFDRFile) {
				NativeFDRFile fdrFile = (NativeFDRFile) file;
				FDR fdr = fdrFile.getFDR();
				console.writeWord(parms + 2, (short) fdr.getSectorsUsed());
				console.writeByte(parms + 4, (byte) fdr.getFlags());
				console.writeByte(parms + 5, (byte) fdr.getRecordsPerSector());
				console.writeByte(parms + 6, (byte) fdr.getByteOffset());
				console.writeByte(parms + 7, (byte) fdr.getRecordLength());
				console.writeWord(parms + 8, HOST2TI((short) fdr.getNumberRecords()));
			} else {
				console.writeWord(parms + 2, (short) ((file.getFileSize() + 255) / 256));
			}

		} else {
			// read sectors
			short   vaddr = console.readWord(parms);
			short	secnum = console.readWord(parms + 2);

			System.out.println("reading "+secs+" sectors from sector #"+secnum+
					" in " + file + ", storing to >"+Utils.toHex4(vaddr));

			ByteMemoryAccess access = getVdpMemory(vaddr);
			try {
				int read = file.readContents(access.memory, access.offset, secnum * 256, secs * 256);
				dirtyVdpMemory(vaddr, read);
				// error will be set if sector read failed
				console.writeByte(rambase+0x4D, (byte) ((read + 255) >> 8));
			} catch (IOException e) {
				setSubError(es_hardware, e);
			}
		}
	}

	private void directDiskOutput(byte dev, byte secs, short fname, int parms) {
		if (dev != 1) {
			setSubError(e_hardwarefailure, null);
			return;
		}
		
		String filename = readBareFilename(fname);
		NativeFile file = null;
		
		setSubError(0, null);
		try {
			file = NativeFileFactory.createNativeFile(DataFiles.resolveFile(filename));
			
		} catch (IOException e) {
			// TODO
			setSubError(es_filenotfound, e);
			return;
			/*
			File rawFile = new File(filename);
			try {
				new FileOutputStream(rawFile).close();
		
				file = NativeFileFactory.createNativeFile(new File(filename));
			} catch (IOException e2) {
				setSubError(es_outofspace, e2);
				return;
			}
			*/
		}
		
		parms = rambase+0x00 + (parms >> 8);

		if (secs == 0) {
			// write FDR info
			// TODO
			/*
			if (file instanceof NativeFDRFile) {
				NativeFDRFile fdrFile = (NativeFDRFile) file;
				FDR fdr = fdrFile.getFDR();
				console.writeWord(parms + 2, fdr.getSectorsUsed());
				console.writeByte(parms + 4, fdr.getFlags());
				console.writeByte(parms + 5, fdr.getRecordsPerSector());
				console.writeByte(parms + 6, fdr.getByteOffset());
				console.writeByte(parms + 7, fdr.getRecordLength());
				console.writeWord(parms + 8, HOST2TI(fdr.getNumberRecords()));
			} else {
				console.writeWord(parms + 2, (short) ((file.getFileSize() + 255) / 256));
			}
			*/
			setSubError(es_badfuncerr, null);

		} else {
			// write sectors
			short   vaddr = console.readWord(parms);
			short	secnum = console.readWord(parms + 2);

			System.out.println("writing "+secs+" sectors to sector #"+secnum+
					" in " + file + ", reading from >"+Utils.toHex4(vaddr));

			ByteMemoryAccess access = getVdpMemory(vaddr);
			try {
				String contents = new String(access.memory, access.offset, secs * 256);
				System.out.println(contents);
				int wrote = file.writeContents(access.memory, access.offset, secnum * 256, secs * 256);
				// error will be set if sector write failed
				console.writeByte(rambase+0x4D, (byte) ((wrote + 255) >> 8));
			} catch (IOException e) {
				setSubError(es_outofspace, e);
			}
		}
	}

	/**
	 * Record a write to classic VDP memory
	 * @param vaddr address in 0-0x3FFF range
	 * @param read
	 */
	private void dirtyVdpMemory(short vaddr, int read) {
		int base = vdpHandler.getVdpMmio().getBankAddr();
		while (read-- > 0) {
			vdpHandler.touchAbsoluteVdpMemory(
					base + vaddr, 
					(byte) vdpHandler.readAbsoluteVdpMemory(base + vaddr));
			vaddr++;
		}
	}

	/**
	 * Get memory read/write access to classic VDP memory
	 * @param vaddr address in 0-0x3FFF range
	 * @return access to memory (need {@link #dirtyVdpMemory(short, int)} to notice)
	 */
	private ByteMemoryAccess getVdpMemory(short vaddr) {
		return vdpHandler.getByteReadMemoryAccess(
				vdpHandler.getVdpMmio().getBankAddr() + vaddr);
	}
	
	/**
	 * Read byte in classic VDP memory
	 * @param vaddr address in 0-0x3FFF range
	 * @return byte
	 */
	private byte readVdpByte(int vaddr) {
		int base = vdpHandler.getVdpMmio().getBankAddr();
		return vdpHandler.readAbsoluteVdpMemory(base + vaddr);
	}
	
	/**
	 * Read word in classic VDP memory
	 * @param vaddr address in 0-0x3FFF range
	 * @return byte
	 */
	private short readVdpShort(int vaddr) {
		int base = vdpHandler.getVdpMmio().getBankAddr();
		return (short) ((vdpHandler.readAbsoluteVdpMemory(base + vaddr) << 8)
			| (vdpHandler.readAbsoluteVdpMemory(base + vaddr + 1) & 0xff));
	}
	
	/**
	 * Write byte in classic VDP memory
	 * @param vaddr address in 0-0x3FFF range
	 * @return byte
	 */
	private void writeVdpByte(int vaddr, byte byt) {
		int base = vdpHandler.getVdpMmio().getBankAddr();
		vdpHandler.writeAbsoluteVdpMemory(base + vaddr, byt);
	}

	private short HOST2TI(short word) {
		return (short) (((word & 0xff) << 8) | ((word >> 8) & 0xff));
	}

	private void setSubError(int err, Exception e) {
		console.writeByte(rambase + 0x50, (byte) err);
		if (e != null)
			System.err.println(e.getMessage());
	}

	private String readBareFilename(short addr) {
		StringBuilder builder = new StringBuilder();
		int endAddr = addr;
		while (endAddr < addr + 10) {
			byte ch = readVdpByte(endAddr);
			if (ch == ' ')
				break;
			builder.append((char) ch);
			endAddr++;
		}
		return builder.toString();
	}
	
	private String readFilename(short addr, int len) {
		StringBuilder builder = new StringBuilder();
		int endAddr = addr;
		while (len-- > 0) {
			byte ch = readVdpByte(endAddr);
			builder.append((char) ch);
			endAddr++;
		}
		return builder.toString();
	}
	
}
