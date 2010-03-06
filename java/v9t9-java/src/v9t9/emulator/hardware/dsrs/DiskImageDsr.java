/**
 * 
 */
package v9t9.emulator.hardware.dsrs;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.ejs.coffee.core.utils.HexUtils;
import org.ejs.coffee.core.utils.ISettingListener;
import org.ejs.coffee.core.utils.Setting;

import v9t9.emulator.EmulatorSettings;
import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.IconSetting;
import v9t9.emulator.hardware.CruManager;
import v9t9.emulator.hardware.CruReader;
import v9t9.emulator.hardware.CruWriter;
import v9t9.emulator.hardware.V9t9;
import v9t9.emulator.hardware.memory.mmio.ConsoleMmioArea;
import v9t9.emulator.runtime.Executor;
import v9t9.engine.memory.DiskMemoryEntry;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;

/**
 * This is a device handler which allows accessing disks as flat sector files
 * or flat track files.
 * @author ejs
 *
 */
public class DiskImageDsr implements DsrHandler {
	public static final Setting diskImageDsrEnabled = new Setting("DiskImageDSREnabled", Boolean.TRUE);
	
	/** device (DSK1) to setting */
	private Map<String, Setting> diskSettingsMap = new LinkedHashMap<String, Setting>();
	private DiskMemoryEntry romMemoryEntry;
	
	//#define DSKbuffersize (256*18)	/* maximum track size */
	private static final int DSKtracksize_SD = (3210);
	private static final int DSKtracksize_DD = (6420);

	private static final int DSKbuffersize = (16384);		/* maximum track size */


	/*	Header for track (*.trk) files; also used internally for sector
		files, but not present in image: we guess the disk geometry from
		the size and sector 0 information. */

	private static final String TRACK_MAGIC			= "trak";
	private static final int TRACK_MAGIC_SIZE 	= 4;
	private static final int TRACK_VERSION		= 1;

	static class DSKheader
	{
		public static final short SIZE = 12;
		byte			magic[] = new byte[4];	
		byte			version;	/* disk version */
		byte			tracks;		/* tracks per side */
		byte			sides;		/* 1 or 2 */
		byte			unused;
		short			tracksize;	/* bytes per track */
		int			track0offs;	/* offset for track 0 data */
	};

	private static final int 
		R_RDSTAT = 0,
		R_RTADDR = 1,
		R_RSADDR = 2,
		R_RDDATA = 3,
		W_WTCMD = 4,
		W_WTADDR = 5,
		W_WSADDR = 6,
		W_WTDATA = 7
	;

	private static final int 
		FDC_restore			= 0x00,
		FDC_seek			= 0x10,
			fl_head_load	= 0x08,
			fl_verify_track	= 0x04,	/* match track register with sector ID */
			fl_step_rate	= 0x03,
		
		FDC_step			= 0x20,
		FDC_stepin			= 0x40,
		FDC_stepout			= 0x60,
			fl_update_track	= 0x10, /* for all step commands */	

			// +fl_head_load, fl_verify_track, fl_step_rate

		FDC_readsector		= 0x80,
		FDC_writesector		= 0xA0,
			fl_multiple		= 0x10,
			fl_length_coding= 0x08,	// sector length coding in FDC1771 
			fl_side_number	= 0x08,	/* which side to match */	// FDC179x
			fl_side_compare	= 0x02,
			fl_deleted_dam	= 0x01,

		FDC_readIDmarker= 0xC0,
		FDC_readtrack	= 0xE0,
		FDC_writetrack	= 0xF0,

			fl_15ms_delay	= 0x04,	/* common to readsector...writetrack */

		FDC_interrupt	= 0xD0
	;

	private static class DiskInfo {

		private Setting setting;
		File		spec;
		protected RandomAccessFile handle;
		
		boolean hold; /* holding for data? */
		byte lastbyte; /* last byte written to WDDATA when hold off */
		byte trackBuffer[] = new byte[DSKbuffersize]; /* track contents */
		byte rwBuffer[] = new byte[DSKbuffersize]; /* read/write contents */
		boolean dirty; /* is the buffer out of date with disk? */
		int buflen; /* max length of data expected for a write/read */
		int bufpos; /* offset into buffer */

		DSKheader hdr = new DSKheader(); /* info about disk */

		boolean fulltrk; /* full track information used? */
		boolean readonly; /* don't write! */

		int trackoffset; /* current track seek position into disk */

		int trackbyteoffset; /* offset into track, bytes */
		int idoffset; /* offset of ID field (0xfe) in track (for sectors) */
		int dataoffset; /* offset of data field (0xfb) in track (for sectors) */

		int command; /* command being executed (0xE0 mask) */
		byte flags; /* flags sent with command being executed (0x1F mask) */
		byte addr; /* last addr written */

		/* command-specified */
		byte seektrack; /* physically seeked track */
		byte trackReg; /* desired track */
		byte sideReg; /* current side */
		byte sectorReg; /* desired sector */
		short crc; /* current CRC */

		/* these are the logical values */
		byte trackid; /* current track */
		byte sideid; /* current side */
		byte sectorid; /* current sector */
		byte sizeid; /* current sector size (1=128, 2=256, etc.) */
		short crcid; /* expected CRC */

		boolean stepout; /* false: in, true: out */
		
		static enum StatusBit {
			// all steppings + force_interrupt
			NOT_READY(0x80),
			WRITE_PROTECT(0x40),
			HEAD_LOADED(0x20),
			SEEK_ERROR(0x10),
			CRC_ERROR(0x08),
			TRACK_0(0x04),
			INDEX_PULSE(0x02),
			BUSY(0x01),
			
			// read/write
			REC_NOT_FOUND(0x10),
			LOST_DATA(0x04),
			DRQ_PIN(0x02),
			
			MARK_TYPE_40(0x40)
			;
			
			private int val;

			StatusBit(int val) {
				this.val = val;
			}
		}
		static class Status {
			private Map<StatusBit, Boolean> values = new HashMap<StatusBit, Boolean>();
			public byte markType;
			
			public static final StatusBit[] COMMON_STATUS = {
				StatusBit.NOT_READY, StatusBit.WRITE_PROTECT, StatusBit.HEAD_LOADED, StatusBit.SEEK_ERROR, 
				StatusBit.CRC_ERROR, StatusBit.TRACK_0, StatusBit.INDEX_PULSE, StatusBit.BUSY
			};
			
			public static final StatusBit[] RW_STATUS = {
				StatusBit.NOT_READY, StatusBit.REC_NOT_FOUND,
				StatusBit.CRC_ERROR, StatusBit.LOST_DATA, StatusBit.DRQ_PIN, StatusBit.BUSY
			};
			
			public boolean is(StatusBit bit) {
				return values.containsKey(bit) && values.get(bit);
			}
			public void set(StatusBit bit) {
				values.put(bit, Boolean.TRUE);
			}
			public void reset(StatusBit bit) {
				values.put(bit, Boolean.FALSE);
			}
			public void clear() {
				values.clear();
			}
			
			/**
			 * @param status
			 * @return
			 */
			public byte calculate(int command, StringBuilder status) {
				StatusBit[] bits = COMMON_STATUS;
				switch (command) {
				case FDC_readIDmarker:
				case FDC_readsector:
				case FDC_readtrack:
				case FDC_writesector:
				case FDC_writetrack:
					bits = RW_STATUS;
					break;
				}
				
				byte val = 0;
				for (StatusBit bit : bits) {
					if (is(bit)) {
						if (status.length() > 0)
							status.append(',');
						status.append(bit);
						val |= bit.val;
					}
				}
				return val;
			}
		}
		
		protected Status status = new Status();
		
		protected boolean motorRunning;
		protected long motorTimeout;

		public DiskInfo(Setting setting) {
			this.setting = setting;
			spec = new File(setting.getString());
			
			setting.addListener(new ISettingListener() {
				
				public void changed(Setting setting, Object oldValue) {
					try {
						FDCflush();
						FDCclosedisk();
					} catch (IOException e) {
					}
					spec = new File(setting.getString());
					
				}
			});
		}
		
		private void FDCsetfilesize() throws IOException
		{
			long      sz, len;

			if (spec == null || handle == null) 
				return;

			len = spec.length();

			sz = hdr.tracksize * hdr.tracks * hdr.sides;

			/* never shrink */
			if (sz > len) {
				handle.setLength(sz);
			}
		}

		private void FDCwriteheader() throws IOException {
			if (handle == null || readonly) {
				return;
			}

			if (fulltrk) {
				/* byteswap header for export */
				handle.seek(0);
				handle.write(hdr.magic);
				handle.write(hdr.version);
				handle.write(hdr.tracks);
				handle.write(hdr.sides);
				handle.write(0); // unused
				handle.write(hdr.tracksize >> 8);
				handle.write(hdr.tracksize & 0xff);
				handle.write(hdr.track0offs >> 8);
				handle.write(hdr.track0offs & 0xff);
			}

			/* maintain invariants */
			FDCsetfilesize(); 
		}
		
		private void FDCreadheader() throws IOException
		{
			long sz;
			byte sector[] = new byte[256];

			if (handle == null)
				return;
			
			readonly = !spec.canWrite();

			if (fulltrk) {
				handle.seek(0);
				
				/* byteswap imported header */
				handle.read(hdr.magic);
				hdr.version = handle.readByte();
				hdr.tracks = handle.readByte();
				hdr.sides = handle.readByte();
				handle.readByte(); // unused
				hdr.tracksize =  (short) (((handle.read() & 0xff) << 8) | (handle.read() & 0xff));
				hdr.track0offs = (((handle.read() & 0xff) << 8) | (handle.read() & 0xff));

				/* verify */
				if (!Arrays.equals(TRACK_MAGIC.getBytes(), hdr.magic)) {
					throw new IOException(MessageFormat.format("DOAD server:  disk image ''{0}'' has unknown type (got {1}, expected {2})",
							spec,
							new String(hdr.magic),
							TRACK_MAGIC));
				}	

				if (hdr.version < 1 || hdr.version > TRACK_VERSION) {
					throw new IOException(MessageFormat.format("DOAD server:  disk image ''{0}'' has too new version ({1} > {2})\n",
								  spec, hdr.version, TRACK_VERSION));
				}
			} else {

				/* no header: guess */
				sz = handle.length();
				if (sz < 256)
					throw new IOException("Disk size for '" + spec + "' is too small to be a disk file");

				/* read sector 0 */
				handle.seek(0);
				handle.read(sector);

				Arrays.fill(hdr.magic, (byte) 0);
				hdr.version = 0;
				hdr.tracks = sector[17];
				hdr.sides = sector[18];
				hdr.tracksize = (short) (sector[12] * 256L);
				hdr.track0offs = 0;
				if (hdr.tracks <= 0 || hdr.sides <= 0 || hdr.tracksize <= 0)
				{
					hdr.sides = 1;
					hdr.tracksize = 256*9;
					int tracks = (int) (sz / hdr.tracksize);
					hdr.tracks = (byte) tracks;
					if (tracks >= 80) {
						tracks /= 2;
						hdr.tracks = (byte) tracks;
						hdr.sides++;
						if (tracks >= 80) {
							tracks /= 2;
							hdr.tracks = (byte) tracks;
							hdr.tracksize <<= 1;
							if (tracks > 40) {
								hdr.tracks = 40;
							}
						}
					}
				}
			}

			if (hdr.tracksize > DSKbuffersize) {
				throw new IOException(MessageFormat.format("Disk image has too large track size ({0} > {1})",
							  hdr.tracksize, DSKbuffersize));
			}
		}
		
		private void
		FDCcreatedisk(boolean fulltrk) throws IOException
		{
			info("DOAD server:  creating new disk image at {0} ({1})", setting.getName(), spec);

			/* defaults */
			this.fulltrk = fulltrk;
			System.arraycopy(TRACK_MAGIC.getBytes(), 0, hdr.magic, 0, TRACK_MAGIC_SIZE);
			hdr.version = TRACK_VERSION;
			hdr.tracks = 40;
			hdr.sides = 1;
			hdr.tracksize = (short) (fulltrk ? DSKtracksize_SD : 256*9);
			hdr.track0offs = DSKheader.SIZE;

			/* create file */
			handle = null;
			handle = new RandomAccessFile(spec, "rw");
			
			FDCwriteheader();
		}
		/**
		 * 
		 */
		public void FDCopendisk() throws IOException {
			String name;
		
			//module_logger(&realDiskDSR, _L|L_1, _("FDCopendisk\n"));
			if (handle != null)
				FDCclosedisk();
		
			//DSK.status = fdc_BADRECORD;
			//DSK.trackid = DSK.sectorid = DSK.track = DSK.sector = 0;
			resetStatus();
		
			/* disk file */
			name = setting.getString();
		
			/* get disk type from extension */
			fulltrk = name.toLowerCase().endsWith(".trk");

			if (spec.exists()) {
				try {
					handle = new RandomAccessFile(spec, "rw");
					readonly = false;
				} catch (IOException e) {
					handle = new RandomAccessFile(spec, "r");
					readonly = true;
				}
			} else {
				readonly = false;
				FDCcreatedisk(fulltrk);
				FDCclosedisk();
				return;
			}
		
			/* get disk info */
			try {
				FDCreadheader();
			} catch (IOException e) {
				try {
					FDCcreatedisk(fulltrk);
				} catch (IOException e2) {
					FDCclosedisk();
					throw e2;
				}
				FDCreadheader();
			}
		
			dirty = true;
			FDCseektotrack();
			FDCreadtrackdata();
		
			info("Opened {0} disk ''{1}'' {2},\n#tracks={3}, tracksize={4}, sides={5}",
						  fulltrk ? "track-image" : "sector-image",
						  spec,
				 setting.getName(), hdr.tracks, hdr.tracksize, hdr.sides);
			//module_logger(&realDiskDSR, _L|L_2, _("DSK.handle=%d\n"), DSK.handle);
			
		}

		/**
		 * 
		 */
		private void resetStatus() {
			status.clear();
		}

		/**
		 * 
		 */
		private void FDCreadtrackdata() throws IOException {
			if (!dirty || handle == null) return;

			info("Reading data from track {0}", 
						  seektrack);

			/* read track */
//			status &= ~fdc_LOSTDATA;

			handle.seek(getTrackDiskOffset());
			try {
				handle.read(trackBuffer, 0, hdr.tracksize);
			} catch (IndexOutOfBoundsException e) {
				throw (IOException) new IOException().initCause(e);
			}
			
			dirty = false;
		}

		/**
		 * @return
		 */
		private long getTrackDiskOffset() {
			long offset = trackoffset;
			if (sideReg != 0) {
				// goes in reverse order on side 2
				offset = hdr.track0offs + (hdr.tracks * 2 - seektrack) * hdr.tracksize;
			}
			return offset;
		}

		/**
		 * 
		 */
		private boolean FDCseektotrack() {
			int         offs;

			if (!dirty) return true;

			trackoffset = 0;
			trackbyteoffset = 0;
			idoffset = dataoffset = 0;
			dirty = true;

			if (handle == null) 
				return false;

			if (seektrack >= hdr.tracks || sideReg >= hdr.sides) {
				info("Cannot seek past end of disk Tr{0}({1}) Sd{2}({3})", 
							  seektrack, hdr.tracks, sideReg, hdr.sides);
				return false;
			}

			offs = seektrack & 0xff;
			offs *= hdr.tracksize & 0xffff;
			offs += hdr.track0offs;

			// side is handled dynamically
			
			trackoffset = offs;
			trackbyteoffset = 0;
			idoffset = dataoffset = 0;
			
			long diskOffs = getTrackDiskOffset(); 
			info("Seeking to Tr{0} Sd{1} = byte >{2} of file", seektrack, sideReg, Long.toHexString(diskOffs));

			try {
				handle.seek(diskOffs);
			} catch (IOException e) {
				return false;
			}
			
			return true;
		}
		/**
		 * @param b
		 * @throws IOException 
		 */
		public void FDChold(boolean onoff) throws IOException {
			if (onoff) {
				//info("FDChold on");
				/* about to read or write */
				status.set(StatusBit.DRQ_PIN);
			} else {
				//info("FDChold off");
				if (hold 
					&& (command == FDC_writesector || command == FDC_writetrack)) 
				{
					FDCflush();
				}
			}
			
		}

		/**
		 * 
		 */
		public void FDCclosedisk() throws IOException {
			if (handle != null) {
				FDCflush();
				buflen = bufpos = 0;
				handle.close();
				handle = null;
			}			
		}

		/**
		 * 
		 */
		public void FDCflush() throws IOException {
			if (!hold) return;

			if (handle != null && buflen != 0 && dirty) {
				//status &= ~fdc_LOSTDATA;

				if (readonly) {
					status.set(StatusBit.WRITE_PROTECT);

					throw new IOException(MessageFormat.format("DOAD server:  disk image ''{0}'' is write-protected",
								  spec));
				}


				if (fulltrk) {
					if (command == FDC_writesector) {
						int ptr, end;

						// write new ID field
						int offs = idoffset;
						if (trackBuffer[offs] != (byte) 0xfe)
							error("Inconsistent idoffset ({0})", idoffset);

						trackBuffer[offs+0] = (byte) 0xfe;
						trackBuffer[offs+1] = trackid;
						trackBuffer[offs+2] = sideid;
						trackBuffer[offs+3] = sectorid;
						trackBuffer[offs+4] = sizeid;
						trackBuffer[offs+5] = (byte) (crcid >> 8);
						trackBuffer[offs+6] = (byte) (crcid & 255);

						FDCwritedataat(trackBuffer, offs, offs, 7);

						// skip separator
						// write data with new CRC
						if (trackBuffer[dataoffset] != (byte) 0xfb)
							error("Inconsistent dataoffset ({0})", dataoffset);
						trackBuffer[dataoffset] = (byte) 0xfb;
						
						offs = dataoffset;
						
						
						ptr = offs + 1;
						end = ptr + (128 << sizeid);
						
						System.arraycopy(rwBuffer, 0, trackBuffer, ptr, end - ptr);

						crcid = (short) 0xffff;
						while (ptr < end)
							crcid = calc_crc(crcid, trackBuffer[ptr++]);
						trackBuffer[ptr++] = (byte) (crcid >> 8);
						trackBuffer[ptr++] = (byte) (crcid & 0xff);
						
						FDCwritedataat(trackBuffer, offs, offs, end - dataoffset);
					} else if (command == FDC_writetrack) {
						// write entire track

						FDCwritedataat(rwBuffer, 0, 0, hdr.tracksize);
					}
				}
				else {
					// simple disks only write data

					if (command == FDC_writesector) {
						FDCwritedataat(rwBuffer, dataoffset, 0, buflen);
					} else if (command == FDC_writetrack) {
						format_sector_track();
					}
					
					// update
					FDCreadtrackdata();
				}
			}

			
			dirty = false;
			
		}


		/**
		 * 
		 */
		private void format_sector_track() throws IOException {
			// interpret data
			byte[] buffer = rwBuffer;
			int is = 0;
			while (is < buflen) {
				while (is < buflen && buffer[is] != (byte) 0xfe) is++;
				if (is + 6 < buflen) {
					byte track, side, sector, size;
					int offs, sz;
					short crc = (short) 0xffff, crcid;

					// ID marker
					is++;

					crc = calc_crc(crc, 0xfe);
					track = buffer[is++]; crc = calc_crc(crc, track);
					side = buffer[is++]; crc = calc_crc(crc, side);
					sector = buffer[is++]; crc = calc_crc(crc, sector);
					size = buffer[is++]; crc = calc_crc(crc, size);
					crcid = (short) (buffer[is++]<<8); crcid += buffer[is++]&0xff;

					if (crcid == (short) 0xf7ff) crcid = crc;
					/*
					if (false && crcid != crc) {
						// only for MFM, apparently
						info("retrying for id marker (CRC=%04X != %04X)", crc, crcid);
						goto retry;
					}*/

					info("Formatting sector track:{0}, side:{1}, sector:{2}, size:{3}, crc={4}", track, side, sector, size, crc);

					sz = 128 << size;
					offs = sector * sz;
					if (offs >= hdr.tracksize) {
						error("Program is formatting track on ''{0}'' with non-ordinary sectors; " +
										"this does not work with sector-image disks", spec);
						offs = 0;
					}

				retry1:
					while (is < buflen && buffer[is++] != (byte) 0xfb) /**/;

					crc = (short) 0xffff;

					if (is + sz + 2 < buflen) {
						crc = calc_crc(crc, 0xfb);
						int cnt = 0;
						for (cnt=0; cnt < sz; cnt++) {
							crc = calc_crc(crc, buffer[cnt + is]);
						}
						crcid = (short) (buffer[cnt++]<<8); crcid += buffer[cnt++]&0xff;
						if (crcid == (short) 0xf7ff) crcid = crc;
						/*
						if (0 && crc != crcid) {
							module_logger(&realDiskDSR, _L|L_3,
										  _("retrying for sector data (CRC=%04X != %04X)\n"),
										  crc, crcid);
							goto retry1;
						}*/

						FDCwritedataat(buffer, offs, is, sz);
						is += sz + 2; // + crc
					} else {
						error("Lost sector data in format of sector-image disk ''{0}''", spec);
						break;
					}
				}
			}			
		}

		private void dump_buffer(byte[] buffer, int offs, int len)
		{
			StringBuilder builder = new StringBuilder();
			int x;
			if (len > 0)
				builder.append("Buffer contents:\n");
			for (x = offs; len-- > 0; x+=16, len-=16) {
				int         y;

				builder.append(HexUtils.toHex4(x));
				builder.append(' ');
				for (y = 0; y < 16; y++)
					builder.append(HexUtils.toHex2(buffer[x + y]) + " ");
				builder.append(' ');
				for (y = 0; y < 16; y++) {
					byte b = buffer[x+y];
					if (b >= 32 && b < 127)
						builder.append((char) b);
					else
						builder.append('.');
				}
				builder.append('\n');
			}
			info(builder.toString());

		}
		private void 
		FDCwritedataat(byte[] buffer, int diskoffset, int bufoffset, int size) throws IOException
		{
			int ret = size;
			long diskoffs = getTrackDiskOffset() + diskoffset;

			info("Writing {0} bytes of data on track {1}, trackoffset = {2}, offset = >{3}", 
					size, seektrack, trackoffset, Long.toHexString(diskoffs));

			// dump contents
			dump_buffer(buffer, bufoffset, size);

			status.reset(StatusBit.WRITE_PROTECT);
			status.reset(StatusBit.LOST_DATA);
			status.reset(StatusBit.CRC_ERROR);

			try {
				handle.seek(diskoffs);
			} catch (IOException e) {
				status.set(StatusBit.LOST_DATA);
				throw e;
			}
			try {
				handle.write(buffer, bufoffset, ret);
			} catch (IOException e) {
				status.set(StatusBit.LOST_DATA);
				status.set(StatusBit.CRC_ERROR);
				throw e;
			}
		}

		private static class CircularIter implements Iterator<Byte> {

			private final byte[] buffer;
			private final int end;
			private int ptr, cnt;
			private int start;

			public CircularIter(byte[] buffer, int size) {
				this.buffer = buffer;
				this.end = size;
				this.ptr = 0;
				this.cnt = size;
			}
			public void setPointers(int start, int ptr) {
				this.start = start;
				this.ptr = ptr % end;
			}
			public int getPointer() {
				return ptr;
			}
			public boolean hasNext() {
				return cnt > 0;
			}

			public Byte next() {
				if (cnt <= 0)
					throw new NoSuchElementException();
				Byte ret = buffer[ptr];
				if (ptr + 1 >= end) {
					ptr = start;
				} else {
					ptr++;
				}
				cnt--;
				return ret;
			}
			
			public Byte peek() {
				if (cnt <= 0)
					throw new NoSuchElementException();
				Byte ret = buffer[ptr];
				return ret;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
			/**
			 * @return
			 */
			public int remaining() {
				return cnt;
			}
			/**
			 * @param i
			 */
			public void setCount(int i) {
				this.cnt = i;
			}
			/**
			 * @return
			 */
			public int getStart() {
				return start;
			}
			
		}
		/*	Find a sector ID on the track */
		private boolean
		FDCfindIDmarker()
		{
			//info("FDC find ID marker");
			
			try {
				FDCreadtrackdata();
			} catch (IOException e) {
				error(e.getMessage());
				return false;
			}
			
			if (fulltrk) {
				boolean found = false;

				/* scan track for markers */
				CircularIter iter = new CircularIter(trackBuffer, hdr.tracksize);
				iter.setPointers(0, idoffset + 1);

				//info("FDCfindIDmarker: starting at {0}, traversing [{1}...{2}) (track offset: >{3})", iter.getPointer(), 
				//		0, iter.getSize(), Integer.toHexString(trackoffset));

				while (!found && iter.hasNext()) {
					// sync
					//while (cnt > 0 && *ptr == 0xff) NEXT();

					// scan for address field marker (account for broken disks)
					while (iter.hasNext()) {
						byte peek = iter.peek();
						if (peek == (byte) 0xfe) {
							break;
						}
						iter.next();
					}
					if (iter.remaining() < 7) break;

					// reset CRC
					idoffset = iter.getPointer() + iter.getStart();
					crc = (short) 0xffff;
					crc = calc_crc(crc, iter.next());

					// get ID
					trackid = iter.next();
					sideid = iter.next();
					sectorid = iter.next();
					sizeid = iter.next();
					crc = calc_crc(crc, trackid);
					crc = calc_crc(crc, sideid);
					crc = calc_crc(crc, sectorid);
					crc = calc_crc(crc, sizeid);

					crcid = (short) (iter.next()<<8); crcid |= iter.next() & 0xff;

					// this algorithm does NOT WORK
					if (false && crc != crcid)
					{
						info("FDCfindIDmarker: failed ID CRC check (>{0} != >{1})",
								HexUtils.toHex4(crcid), HexUtils.toHex4(crc));
						
						status.set(StatusBit.CRC_ERROR);
					}
					else // if ((command >= FDC_readsector && command != FDC_readIDmarker) || 
					{
						if (command != FDC_readIDmarker || sideid == sideReg)
						{
							// found one; look somewhere else next time
							trackbyteoffset = iter.getPointer() + iter.getStart();
							found = true;
							info("FDCfindIDmarker: T{0}, S{1}, s{2} at >{3}", trackid, sectorid, sideid,
										  Integer.toHexString(trackoffset + trackbyteoffset));
						}
					}
				}

				return found;
			}
			else {
				/* easy */
				trackid = trackReg;
				sectorid = sectorReg;
				sideid = sideReg;
				sizeid = 1;	//!!! hack
							  
				trackbyteoffset = (128<<sizeid) * sectorid;
				info("FDCfindIDmarker: T{0}, S{1}, s{2} at >{3}", trackid, sectorid, sideid, Integer.toHexString(trackoffset + trackbyteoffset));
				
				return true;
			}
		}

		/*	Match the current ID with the desired track/sector id */
		private boolean
		FDCmatchIDmarker() {
			info("FDC match ID marker: looking for T{0}, S{1}", trackReg, sectorReg);
			
			status.reset(StatusBit.REC_NOT_FOUND);
			status.reset(StatusBit.CRC_ERROR);
		
			// FDC179x mode
			//byte desiredSide = (byte) ((flags & fl_side_number) != 0 ? 1 : 0);
			
			if (fulltrk)
			{
				int tries;
				boolean found = false;
				for (tries = 0; !found && tries < hdr.tracksize / 18; tries++)
				{
					if (!FDCfindIDmarker())
						return false;
		
					if (trackid == trackReg
						//&& sideid == desiredSide
						&& sectorid == sectorReg
						//&& crcid == crc
						)
					{
						found = true;
					}
					else
					{
						//info("unmatching track T{0} S{1} s{2} z{3}", 
						//			  trackid, sectorid, sideid, sizeid);
					}
					//if (idoffset == origoffs) break;
				}
		
				if (!found) {
					error("FDCmatchIDmarker failed");
					status.set(StatusBit.REC_NOT_FOUND);
					return false;
				}
			}	/* else sector disk */
			else {
				/* easy */
		
				if (!FDCfindIDmarker())
					return false;
		
				/* simple disks cannot renumber sectors */
				if (!FDCseektotrack()
					|| trackReg >= hdr.tracks 
					|| sectorReg >= hdr.tracksize / 256) 
				{
					status.set(StatusBit.REC_NOT_FOUND);
					idoffset = 0;
			  
					error("FDCmatchIDmarker failed");
					return false;
				} 
				else 
				{
					trackid = trackReg;
					sectorid = sectorReg;
					sideid = sideReg;
					sizeid = 1;		// 256 bytes
		
					// ID marker lives virtually at end of track
					idoffset = 0;
					rwBuffer[idoffset+0] = (byte) 0xfe;
					rwBuffer[idoffset+1] = trackid;
					rwBuffer[idoffset+2] = sideid;
					rwBuffer[idoffset+3] = sectorid;
					rwBuffer[idoffset+4] = sizeid;
					rwBuffer[idoffset+5] = (byte) 0xf7;
					rwBuffer[idoffset+6] = (byte) 0xff;
				}
			}
			
			info("FDCmatchIDmarker succeeded: track {0}, sector {1}, side {2}, size {3} (sector #{4})",
					trackid, sectorid, sideid, sizeid, trackid * 9 + sectorid);
			return true;
		}

		/*	Scan forward from the ID field to the data field */
		private boolean
		FDCfindDataMarker() {
			if (fulltrk) {
				// search for data field

				CircularIter iter = new CircularIter(trackBuffer, hdr.tracksize);
				iter.setPointers(0, idoffset + 8);
				boolean found = false;

				/* scan forward for sector data */
				status.reset(StatusBit.REC_NOT_FOUND);
				status.reset(StatusBit.CRC_ERROR);

				iter.setCount(30);	/* 30 or 43 for MFM */

				try {
					while (!found && iter.hasNext()) {
						// sync 
						//while (cnt > 0 && *ptr == 0x00) NEXT();
	
						// scan for data field marker
						while (iter.hasNext() && iter.peek() != (byte) 0xfb) iter.next();
	
						dataoffset = iter.getPointer() + iter.getStart();
						trackbyteoffset = dataoffset;
						
						// reset CRC
						crc = (short) 0xffff;
						//crc = calc_crc(iter.next(), crc);
						found = true;
					}
				} catch (NoSuchElementException e) {
					
				}
				if (!found) {
					return false;
				}

			}
			else {
				// simple: physical offset of sector

				dataoffset = 256 * sectorid;
				trackbyteoffset = dataoffset;
			}
			return true;
		}

		/**
		 * @throws IOException 
		 * 
		 */
		public void FDCrestore() throws IOException {
			info("FDC restore");
			
			seektrack = trackReg = 0;
			dirty = true;
			FDCseektotrack();
			
			status.set(StatusBit.TRACK_0);
			status.reset(StatusBit.REC_NOT_FOUND);
			status.reset(StatusBit.CRC_ERROR);
			status.reset(StatusBit.SEEK_ERROR);
			
			if ((flags & fl_verify_track) != 0) {
				FDCfindIDmarker();
				if (trackid != 0)
					status.set(StatusBit.SEEK_ERROR);
			}			
		}

		/**
		 * @throws IOException 
		 * 
		 */
		public void FDCseek() throws IOException {
			info("FDC seek, T{0} s{1}", lastbyte, sideReg);
			
			/* current track written WTADDR, desired track written to WTDATA */
			//seektrack += lastbyte - track;
			seektrack = lastbyte;

			dirty = true;
			FDCseektotrack();

			status.reset(StatusBit.SEEK_ERROR);
			status.reset(StatusBit.TRACK_0);

			trackReg = seektrack;


			if ((flags & fl_verify_track) != 0)
			{
				int tries, origoffs = idoffset;

				for (tries = 0; tries < hdr.tracksize; tries++) {
					if (!FDCfindIDmarker()) 
						break;
					if (trackid == trackReg) 
						break;
					if (idoffset == origoffs)
						break;
				}
			
				if (trackid != trackReg) {
					status.set(StatusBit.SEEK_ERROR);
				   	error("FDC seek, record mismatch ({0} != {1}) {2}", trackid, trackReg, tries);
				}

			}
			
		}

		/**
		 * @throws IOException 
		 * 
		 */
		public void FDCstep() throws IOException {
			info("FDC step in, T{0} s{1}", seektrack, sideReg);

			seektrack+=stepout ? -1 : 1;
			if ((flags & fl_update_track) != 0)
				trackReg++;
			
			dirty = true;

			FDCseektotrack();
			status.reset(StatusBit.SEEK_ERROR);

			if ((flags & fl_verify_track) != 0)
			{
				int tries;

				for (tries = 0; tries < 18; tries++) {
					if (!FDCfindIDmarker()) break;
					if (trackid == trackReg) 
						break;
				}
			
				if (trackid != trackReg) {
					status.set(StatusBit.SEEK_ERROR);
					error("FDC seek, record mismatch ({0} != {1}) {2}", trackid, trackReg, tries);
				}

			}
			
		}

		/**
		 * @throws IOException 
		 * 
		 */
		public void FDCreadsector() throws IOException {
			info("FDC read sector, T{0} S{1} s{2}", trackReg, sectorReg, sideReg);
			
			status.reset(StatusBit.LOST_DATA);
			status.reset(StatusBit.DRQ_PIN);
			
			if (handle == null) {
				//status |= fdc_LOSTDATA;
				return;
			}

			FDCreadtrackdata();
			
			if (!FDCmatchIDmarker())
				return;
			
			if (!FDCfindDataMarker())
				return;

			buflen = 128 << sizeid;
			bufpos = 0;
			
			System.arraycopy(trackBuffer, dataoffset + (fulltrk ? 1 : 0), rwBuffer, 0, buflen);
			dump_buffer(rwBuffer, 0, 256);
			
			status.set(StatusBit.DRQ_PIN);
		}


		/**
		 * @throws IOException 
		 * 
		 */
		public void FDCwritesector() throws IOException {
			info("FDC write sector, T{0} S{1} s{2}", trackReg, sectorReg, sideReg);

			status.reset(StatusBit.LOST_DATA);
			status.reset(StatusBit.DRQ_PIN);
			
			if (handle == null) {
				return;
			}

			FDCreadtrackdata();

			if (!FDCmatchIDmarker()) 
				return;

			if (!FDCfindDataMarker())
				return;

			
			dirty = true;
			
			// not sure this is true
			// http://nouspikel.group.shef.ac.uk//ti99/disks.htm#Sector%20size%20code
			if (true || (flags & fl_length_coding) == 0)
				buflen = 128 << sizeid;
			else
				buflen = sizeid != 0 ? (sizeid & 0xff) * 16 : 4096;
				
			bufpos = 0;
			
			status.set(StatusBit.DRQ_PIN);
		}

		/**
		 * 
		 */
		public void FDCreadIDmarker() {
			/*
			 byte			track, side, sector, seclen;
			short			crc;
			 */

			/* since we can't tell what's a valid ID field,
			   always go to the beginning of the track to search. */

			//idoffset = 0;

			status.reset(StatusBit.LOST_DATA);
			
			if (!FDCfindIDmarker()) {
				status.set(StatusBit.REC_NOT_FOUND);
				return;
			}

			int ptr = 0;

			rwBuffer[ptr++] = trackid;
			rwBuffer[ptr++] = sideid;
			rwBuffer[ptr++] = sectorid;
			rwBuffer[ptr++] = sizeid;
			rwBuffer[ptr++] = (byte) (crcid >> 8);
			rwBuffer[ptr++] = (byte) (crcid & 0xff);

			buflen = 6;
			bufpos = 0;

			// the track is copied into the sector register (!)
			sectorReg = trackid;
			
			info("FDC read ID marker: track={0} sector={1} side={2} size={3}",
						  trackid, sectorid, sideid, sizeid);

			
		}

		/**
		 * @throws IOException 
		 * 
		 */
		public void FDCinterrupt() throws IOException {
			info("FDC interrupt");
			
			status.clear();
			
			FDCflush();

			buflen = bufpos = 0;			
		}

		/**
		 * 
		 */
		public void FDCwritetrack() {
			info("FDC write track, #{0}", seektrack);

			status.reset(StatusBit.LOST_DATA);
			
			dirty = true;
			buflen = fulltrk ? hdr.tracksize : DSKbuffersize;
			bufpos = 0;
			trackbyteoffset = 0;
		}

		/**
		 * @throws IOException 
		 * 
		 */
		public void FDCreadtrack() throws IOException {
			info("FDC read track, #{0}", seektrack);

			status.reset(StatusBit.LOST_DATA);
			
			FDCreadtrackdata();

			buflen = hdr.tracksize;
			bufpos = 0;
			trackbyteoffset = 0;
			
		}

		public void saveState(IDialogSettings section) {
			section.put("FilePath", spec.getAbsolutePath());
		}

		public void loadState(IDialogSettings section) {
			spec = getDefaultDiskImage(setting.getName());
			if (section == null)
				return;
			String path = section.get("FilePath");
			if (path != null)
				spec = new File(path);
		}

		/**
		 * @return
		 */
		public byte readByte() {
			byte ret = 0;
			
			if (bufpos == 0) {
				//if (log_level(LOG_REALDISK) > 2) {
				//	// dump contents
				//	dump_buffer(trackbyteoffset, buflen);
				//}
				//dump_buffer(trackbyteoffset, buflen);
			}

			if (hold && buflen != 0) {
				ret = rwBuffer[bufpos++];
				crc = calc_crc(crc, ret & 0xff);
				if (bufpos >= buflen) {
					status.reset(DiskInfo.StatusBit.DRQ_PIN);
				}
			} else {
				ret = lastbyte;
			}
			return ret;
		}

		/**
		 * @param val
		 */
		public void writeByte(byte val) {
			if (buflen != 0) {
				/* fill circular buffer */
				if (bufpos < buflen) {
					rwBuffer[bufpos++] = val;
					crc = calc_crc(crc, val);
				} else {
					status.reset(DiskInfo.StatusBit.DRQ_PIN);
					error("Tossing extra byte >{0}", Integer.toHexString(val & 0xff));
				}
			}	
		}

	}
	
	/** currently selected disk */
	private byte selectedDisk = 0;

	/** note: the side is global to all disks, though we propagate it to all DiskInfos */
	protected byte side;
	

	private String getDiskImageSetting(int num) {
		return "DSKImage" + num;
	}
	private Map<String, DiskInfo> disks = new LinkedHashMap<String, DiskInfo>();
	
	private DiskInfo getSelectedDisk() {
		if (selectedDisk == 0)
			return null;
		return getDiskInfo(selectedDisk);
	}
	
	
	private DiskInfo getDiskInfo(int num) {
		String name = getDiskImageSetting(num);
		DiskInfo info = disks.get(name);
		if (info == null) {
			info = new DiskInfo(diskSettingsMap.get(name));
			info.sideReg = side;
			disks.put(name, info);
		}
		return disks.get(name);
	}
	
	private CruWriter cruwRealDiskMotor = new CruWriter() {
		public int write(int addr, int data, int num) {
			//module_logger(&realDiskDSR, _L|L_1, _("CRU Motor %s\n"), data ? "on" : "off");
			DiskInfo info = getSelectedDisk();
			if (info != null) {
				// strobe the motor (this doesn't turn it off, which happens via timeout)
				if (data != 0) {
					if (!info.motorRunning) {
						info("{0}: motor on", info.setting.getName());
						info.motorTimeout = System.currentTimeMillis() /*+ 1000*/;
						info.motorRunning = true;
					} else {
						info.motorTimeout = System.currentTimeMillis() + 4230;
					}
				}
			}
			return 0;
		}
	};

	private MemoryEntry ioMemoryEntry;
	private DiskMMIOMemoryArea ioArea;	
	
	private CruWriter cruwRealDiskHold = new CruWriter() {
		public int write(int addr, int data, int num) {
			//module_logger(&realDiskDSR, _L|L_1, _("CRU hold %s\n"), data ? "on" : "off");
		
			DiskInfo info = getSelectedDisk();
			try {
				if (info != null)
					info.FDChold(data != 0);
			} catch (IOException e) {
				error(e.getMessage());
			}
		
			if (info != null)
				info.hold = data != 0;
			
			return 0;
		}
	};

	private CruWriter cruwRealDiskHeads = new CruWriter() {
		public int write(int addr, int data, int num) {
			//module_logger(&realDiskDSR, _L|L_1, _("CRU Heads %s\n"), data ? "on" : "off");
	
			// TODO
			//DiskInfo info = getSelectedDisk();
			return 0;
		}
	};
	
	private CruWriter cruwRealDiskSel = new CruWriter() {
		public int write(int addr, int data, int num) {
			byte newnum = (byte) (((addr - 0x1108) >> 1) + 1);
	
			//module_logger(&realDiskDSR, _L|L_1, _("CRU disk select, #%d\n"), newnum);
			
			if (data != 0) {
				DiskInfo oldInfo = getSelectedDisk();
				
				selectedDisk = newnum;
				
				DiskInfo info = getDiskInfo(newnum);
				
				if (oldInfo != info) {
					if (oldInfo != null) {
						try {
							info.FDCclosedisk();
						} catch (IOException e) {
							error(e.getMessage());
						}
					}
				}
				if (info != null && info.handle == null) {
					try {
						info.FDCopendisk();
					} catch (IOException e) {
						error(e.getMessage());
					}
				}

			} else {
				DiskInfo oldInfo = getSelectedDisk();
				
				if (selectedDisk == newnum) {
					selectedDisk = 0;
				
					if (oldInfo != null) {
						try {
							oldInfo.FDCclosedisk();
						} catch (IOException e) {
							error(e.getMessage());
						}
					}
				}
			}
			
			return 0;
		}
	};
	
	private CruWriter cruwRealDiskSide = new CruWriter() {
		public int write(int addr, int data, int num) {
			//module_logger(&realDiskDSR, _L|L_1, _("CRU disk side #%d\n"), data);
	
			// the side is global to all disks
			side = (byte) (data & 1);
			
			for (DiskInfo info : disks.values()) 
				info.sideReg = side;
			
	//		FDCseektotrack();
			return 0;
		}
	};
	
	private CruReader crurRealDiskPoll = new CruReader() {
		public int read(int addr, int data, int num) {
			byte newnum = (byte) (((addr - 0x1102) >> 1) + 1);
			return selectedDisk == newnum ? 1 : 0;
		}
	};

	private CruReader crurRealDiskMotor = new CruReader() {
		public int read(int addr, int data, int num) {
			DiskInfo info = getSelectedDisk();
			if (info != null)
				return info.motorRunning ? 1 : 0;
			else
				return 0;
		}
	};
	
	private CruReader crurRealDiskZero = new CruReader() {
		public int read(int addr, int data, int num) {
			return 0;
		}
	};
	
	private CruReader crurRealDiskOne = new CruReader() {
		public int read(int addr, int data, int num) {
			return 1;
		}
	};

	
	private CruReader crurRealDiskSide = new CruReader() {
		public int read(int addr, int data, int num) {
			return side;
		}
	};

	private TimerTask motorTickTask;

	private Timer motorTimer;
	
	private static File defaultDiskRootDir;

	
	
	public DiskImageDsr(Machine machine) {
		
    	String diskImageRootPath = EmulatorSettings.getInstance().getBaseConfigurationPath() + "disks";
    	defaultDiskRootDir = new File(diskImageRootPath);
    	defaultDiskRootDir.mkdirs();
    	
    	for (int drive = 1; drive <= 3; drive++) {
    		String name = getDiskImageSetting(drive);
			registerDiskImagePath(name, getDefaultDiskImage(name)); 
    	}
    	
    	CruManager cruManager = machine.getCruManager();
    	
		cruManager.add(0x1102, 1, cruwRealDiskMotor);
		cruManager.add(0x1104, 1, cruwRealDiskHold);
		cruManager.add(0x1106, 1, cruwRealDiskHeads);
		cruManager.add(0x1108, 1, cruwRealDiskSel);
		cruManager.add(0x110A, 1, cruwRealDiskSel);
		cruManager.add(0x110C, 1, cruwRealDiskSel);
		cruManager.add(0x110E, 1, cruwRealDiskSide);
		cruManager.add(0x1102, 1, crurRealDiskPoll);
		cruManager.add(0x1104, 1, crurRealDiskPoll);
		cruManager.add(0x1106, 1, crurRealDiskPoll);
		cruManager.add(0x1108, 1, crurRealDiskMotor);
		cruManager.add(0x110A, 1, crurRealDiskZero);
		cruManager.add(0x110C, 1, crurRealDiskOne);
		cruManager.add(0x110E, 1, crurRealDiskSide);
		
		// add motor timer
		motorTickTask = new TimerTask() {
			
			@Override
			public void run() {
				long now = System.currentTimeMillis();
				for (DiskInfo info : disks.values()) {
					if (info.motorTimeout != 0) {
						if (!info.motorRunning) {
							if (now >= info.motorTimeout) {
								info.motorTimeout = 0;
								info.motorRunning = true;
								info("{0}: motor on", info.setting.getName());
							}
						} else {
							if (now >= info.motorTimeout) {
								info.motorTimeout = 0;
								info.motorRunning = false;
								info("{0}: motor off", info.setting.getName());
							}
						}
					}
				}
			}
		};
		
		motorTimer = new Timer();
		
		motorTimer.scheduleAtFixedRate(motorTickTask, 0, 100);
	}


	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler#dispose()
	 */
	public void dispose() {
		motorTimer.cancel();		
	}
	
	private static File getDefaultDiskImage(String name) {
		return new File(defaultDiskRootDir, name + ".dsk");
	}
	

	public void registerDiskImagePath(String device, File dskfile) {
		IconSetting diskSetting = new IconSetting(device, dskfile.getAbsolutePath(),
				V9t9.getDataFile("icons/disk.png").getAbsolutePath());
		diskSetting.loadState(EmulatorSettings.getInstance().getApplicationSettings());

		diskSettingsMap.put(device, diskSetting); 
		diskSetting.addListener(new ISettingListener() {
			
			public void changed(Setting setting, Object oldValue) {
				setting.saveState(EmulatorSettings.getInstance().getApplicationSettings());
			}
		});
	}
	
	public String getName() {
		return "Disk Image DSR";
				
	}
	public short getCruBase() {
		return 0x1100;
	}
	
	/* calculate CRC for data address marks or sector data */
	/* borrowed from xmess-0.56.2.  seems like this only works for MFM */
	private static short calc_crc(int crc, int value) {
		int l, h;

		l = value ^ ((crc >> 8) & 0xff);
		crc = (crc & 0xff) | (l << 8);
		l >>= 4;
		l ^= (crc >> 8) & 0xff;
		crc <<= 8;
		crc = (crc & 0xff00) | l;
		l = (l << 4) | (l >> 4);
		h = l;
		l = (l << 2) | (l >> 6);
		l &= 0x1f;
		crc = crc ^ (l << 8);
		l = h & 0xf0;
		crc = crc ^ (l << 8);
		l = (h << 1) | (h >> 7);
		l &= 0xe0;
		crc = crc ^ l;
		return (short) crc;
	}
	
	private class DiskMMIOMemoryArea extends ConsoleMmioArea {
		public DiskMMIOMemoryArea() {
			super(4);
		}

		/* (non-Javadoc)
		 * @see v9t9.engine.memory.WordMemoryArea#readByte(v9t9.engine.memory.MemoryEntry, int)
		 */
		@Override
		public byte readByte(MemoryEntry entry, int addr) {
			if (addr < 0x5ff0)
				return romMemoryEntry.getArea().flatReadByte(romMemoryEntry, addr);
			
			byte ret = 0;
			
			DiskInfo dsk = getSelectedDisk();
			
			switch ((addr - 0x5ff0) >> 1) {
			case R_RDSTAT:
				if (dsk != null) {
					StringBuilder status = new StringBuilder();
					ret = dsk.status.calculate(dsk.command, status);
					info("FDC read status >" + HexUtils.toHex2(ret) + " : " + status);
				}
				break;

			case R_RTADDR:
				if (dsk != null) {
					ret = dsk.trackReg;
					info("FDC read track " + ret + " >" + HexUtils.toHex2(ret));
				}
				break;

			case R_RSADDR:
				if (dsk != null) {
					ret = dsk.sectorReg;
					info("FDC read sector " + ret + " >" + HexUtils.toHex2(ret));
				}
				break;

			case R_RDDATA:
				/* read from circular buffer */

				if (dsk != null) {
					ret = dsk.readByte();
				}
				break;

			case W_WTCMD:
			case W_WTADDR:
			case W_WSADDR:
			case W_WTDATA:
				ret = 0x00;
				//module_logger(&realDiskDSR, _L|L_1, _("FDC read write xxx >%04X = >%02X\n"), addr, (u8) ret);
				break;
			}
			return (byte) ~ret;
		}
		
		/* (non-Javadoc)
		 * @see v9t9.engine.memory.WordMemoryArea#writeByte(v9t9.engine.memory.MemoryEntry, int, byte)
		 */
		@Override
		public void writeByte(MemoryEntry entry, int addr, byte val) {
			if (addr < 0x5ff0) {
				romMemoryEntry.getArea().flatWriteByte(romMemoryEntry, addr, val);
				return;
			}

			val = (byte) ~val;
			
			DiskInfo dsk = getSelectedDisk();
			if (dsk == null)
				return;

			try {
				switch ((addr - 0x5ff0) >> 1) {
				case R_RDSTAT:
				case R_RTADDR:
				case R_RSADDR:
				case R_RDDATA:
					//module_logger(&realDiskDSR, _L|L_1, _("FDC write read xxx >%04X, >%02X\n"), addr, val);
					break;
	
				case W_WTCMD:
					dsk.FDCflush();
					dsk.buflen = dsk.bufpos = 0;
	
					info("FDC command >" + HexUtils.toHex2(val));
					//module_logger(&realDiskDSR, _L|L_1, _("FDC command >%02X\n"), val);
					
					dsk.command = val & 0xF0;
					
					// standardize commands
					if (dsk.command == 0x30 || dsk.command == 0x50 || dsk.command == 0x70
							|| dsk.command == (byte)0x90 || dsk.command == (byte)0xA0)
						dsk.command &= ~0x10;
					
					dsk.flags = (byte) (val & 0x1F);
					
					switch (dsk.command) {
					case FDC_restore:
						dsk.FDCrestore();
						break;
					case FDC_seek:
						dsk.FDCseek();
						break;
					case FDC_step:
						dsk.FDCstep();
						break;
					case FDC_stepin:
						dsk.stepout = false;
						dsk.FDCstep();
						break;
					case FDC_stepout:
						dsk.stepout = true;
						dsk.FDCstep();
						break;
					case FDC_readsector:
						dsk.FDCreadsector();
						break;
					case FDC_writesector:
						dsk.FDCwritesector();
						break;
					case FDC_readIDmarker:
						dsk.FDCreadIDmarker();
						break;
					case FDC_interrupt:
						dsk.FDCinterrupt();
						break;
					case FDC_writetrack:
						dsk.FDCwritetrack();
						break;
					case FDC_readtrack:
						dsk.FDCreadtrack();
						break;
					default:
						//module_logger(&realDiskDSR, _L|L_1, _("unknown FDC command >%02X\n"), val);
						info("Unknown FDC command >" + HexUtils.toHex2(val));
					}
					break;
	
				case W_WTADDR:
					dsk.trackReg = val;
					//DSK.status &= ~fdc_LOSTDATA;
					dsk.addr = W_WTADDR;
					info("FDC write track addr " + val + " >" + HexUtils.toHex2(val));
					//module_logger(&realDiskDSR, _L|L_1, _("FDC write track addr >%04X, >%02X\n"), addr, val);
					break;
	
				case W_WSADDR:
					dsk.sectorReg = val;
					//DSK.status &= ~fdc_LOSTDATA;
					info("FDC write sector addr " + val + " >" + HexUtils.toHex2(val));
					//module_logger(&realDiskDSR, _L|L_1, _("FDC write sector addr >%04X, >%02X\n"), addr, val);
					break;
	
				case W_WTDATA:
					if (!dsk.hold)
						info("FDC write data ("+dsk.trackbyteoffset+","+dsk.bufpos+") >"+HexUtils.toHex2(val)); 
					//			   (u8) val);
					if (!dsk.hold) {
						dsk.lastbyte = val;
						/*if (DSK.addr == W_WTADDR) {
							DSK.seektrack = val;
						}
						else if (DSK.addr == W_WSADDR)
						DSK.sector = val;*/
					} else {
						dsk.status.set(DiskInfo.StatusBit.DRQ_PIN);;
						
						if (dsk.command == FDC_writesector) {
							// normal write
							dsk.writeByte(val);
							

						} else if (dsk.command == FDC_writetrack) {
							if (true /* is FM */) {
								// for FM write, >F5 through >FE are special
								if (val == (byte) 0xf5 || val == (byte) 0xf6) {
									dsk.status.reset(DiskInfo.StatusBit.REC_NOT_FOUND);;
								} else if (val == (byte) 0xf7) {
									// write CRC
									dsk.writeByte((byte) (dsk.crc >> 8));
									dsk.writeByte((byte) (dsk.crc & 0xff));
								} else if (val >= (byte) 0xf8 && val <= (byte) 0xfb) {
									dsk.crc = -1;
									dsk.writeByte(val);
								} else {
									dsk.writeByte(val);
								}
							} else {
								dsk.writeByte(val);
							}
						} else {
							info("Unexpected data write >" + HexUtils.toHex2(val) + " for command >" + HexUtils.toHex2(dsk.command));
						}
					}
				}
			} catch (IOException e) {
				error(e.getMessage());
			}
		}
		
		/* (non-Javadoc)
		 * @see v9t9.engine.memory.WordMemoryArea#readWord(v9t9.engine.memory.MemoryEntry, int)
		 */
		@Override
		public short readWord(MemoryEntry entry, int addr) {
			return (short) ((readByte(entry, (addr & ~1)) << 8) 
			| (readByte(entry, (addr | 1)) & 0xff));
		}
		
		/* (non-Javadoc)
		 * @see v9t9.engine.memory.WordMemoryArea#writeWord(v9t9.engine.memory.MemoryEntry, int, short)
		 */
		@Override
		public void writeWord(MemoryEntry entry, int addr, short val) {
			writeByte(entry, (addr & ~1), (byte) (val >> 8));
			writeByte(entry, (addr | 1), (byte) (val & 0xff));
		}
		
	}
	public void activate(MemoryDomain console) throws IOException {
		if (romMemoryEntry == null)
			this.romMemoryEntry = DiskMemoryEntry.newWordMemoryFromFile(
					0x4000, 0x2000, "TI Disk DSR ROM", console,
					"disk.bin", 0, false);
		if (ioMemoryEntry == null) {
			ioArea = new DiskMMIOMemoryArea();
			ioMemoryEntry = new MemoryEntry("TI Disk DSR ROM MMIO", console, 0x5C00, 0x400, ioArea);
		}

		console.mapEntry(romMemoryEntry);
		console.mapEntry(ioMemoryEntry);
		
	}
	
	public void deactivate(MemoryDomain console) {
		console.unmapEntry(ioMemoryEntry);
		console.unmapEntry(romMemoryEntry);
	}

	public boolean handleDSR(MemoryTransfer xfer, short code) {
		info("RealDiskDSR: ignoring code = " + code);
		return false;
	}

	/**
	 * @param string
	 */
	private static void info(String string) {
		if (Executor.settingDumpFullInstructions.getBoolean())
			Executor.getDumpfull().println(string);
		System.out.println(string);
		
	}
	private static void info(String fmt, Object... args) {
		info(MessageFormat.format(fmt, args));
		
	}
	private static void error(String string) {
		if (Executor.settingDumpFullInstructions.getBoolean())
			Executor.getDumpfull().println(string);
		System.err.println(string);
		
	}
	private static void error(String fmt, Object... args) {
		error(MessageFormat.format(fmt, args));
	}

	
	public Setting[] getSettings() {
		List<Setting> settings = new ArrayList<Setting>();
		settings.add(diskImageDsrEnabled);
		settings.addAll(diskSettingsMap.values());
		return (Setting[]) settings.toArray(new Setting[settings.size()]);
	}
	public void saveState(IDialogSettings section) {
		diskImageDsrEnabled.saveState(section);
		for (Map.Entry<String, DiskInfo> entry : disks.entrySet())
			entry.getValue().saveState(section.addNewSection(entry.getKey()));
	}
	
	public void loadState(IDialogSettings section) {
		if (section == null) return;
		diskImageDsrEnabled.loadState(section);
		for (Map.Entry<String, DiskInfo> entry : disks.entrySet())
			entry.getValue().loadState(section.getSection(entry.getKey()));
	}
}
