/*
  FDC1771.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.files.image;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import v9t9.common.files.IdMarker;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.SettingSchemaProperty;
import v9t9.engine.dsr.realdisk.CRC16;
import ejs.base.properties.IPersistable;
import ejs.base.properties.IProperty;
import ejs.base.settings.ISettingSection;
import ejs.base.utils.HexUtils;



public class FDC1771 implements IPersistable {
	private byte lastStatus = -1;

	/** currently selected disk */
	private byte selectedDisk = 0;
	private FloppyDrive drive = null;

	private boolean hold; /* holding for data? */
	private byte lastbyte; /* last byte written to WDDATA when hold off */
	byte rwBuffer[] = new byte[RealDiskConsts.DSKbuffersize]; /* read/write contents */
	private int buflen; /* max length of data expected for a write/read */
	private int bufpos; /* offset into buffer */

	private int command; /* command being executed (0xE0 mask) */
	private byte flags; /* flags sent with command being executed (0x1F mask) */

	/* command-specified */
	private byte trackReg; /* desired track */
	byte sideReg; /* current side */
	private byte sectorReg; /* desired sector */

	private CRC16 crcAlg = new CRC16(0x1021);
	
	private boolean stepout; /* false: in, true: out */
	
	private FDCStatus status = new FDCStatus();
	
	private Dumper dumper;
	private FloppyDrive[] drives;

	private Map<String,FloppyDrive> driveMap;

	private long commandBusyExpiration;

	private IProperty settingRealTime;


	/**
	 * @param machine 
	 * 
	 */
	public FDC1771(IMachine machine, Dumper dumper, int numDrives) {
		
		this.dumper = dumper;
		settingRealTime = machine.getSettings().get(RealDiskSettings.diskImageRealTime);
		
		drives = new FloppyDrive[numDrives];
		
		driveMap = new HashMap<String, FloppyDrive>();
		
		for (int num = 1; num <= drives.length; num++) {

			String name = RealDiskSettings.getDiskImageSetting(num);

			IProperty motorProperty = new SettingSchemaProperty(name + "Motor", Boolean.FALSE);
			motorProperty.addEnablementDependency(machine.getSettings()
					.get(RealDiskSettings.diskImagesEnabled));
			
			drives[num - 1] = new FloppyDrive(machine,  
					dumper, status, num, motorProperty, this);
			
			driveMap.put(name, drives[num - 1]);
		}
		
	}

	public void FDChold(boolean onoff) throws IOException {
		if (onoff) {
			// info("FDChold on");
			/* about to read or write */
			status.set(StatusBit.DRQ_PIN);
		} else {
			// info("FDChold off");
			if (hold && (command == RealDiskConsts.FDC_writesector || command == RealDiskConsts.FDC_writetrack)) {
				FDCflush();
			}
		}

	}
	

	/**
	 * 
	 */
	public void FDCflush() throws IOException {
		
		if (!hold) return;

		if (drive == null || drive.getImage() == null) {
			status.set(StatusBit.NOT_READY);
			return;
		}
		
		if (buflen != 0) {
			//status &= ~fdc_LOSTDATA;

			status.reset(StatusBit.WRITE_PROTECT);
			status.reset(StatusBit.LOST_DATA);
			status.reset(StatusBit.CRC_ERROR);

			if (command == RealDiskConsts.FDC_writesector) {
				if (drive.isReadOnly()) {
					status.set(StatusBit.WRITE_PROTECT);
				}
				IdMarker currentMarker = drive.getCurrentMarker();
				if (currentMarker == null) {
					status.set(StatusBit.REC_NOT_FOUND);
				}
				else if (currentMarker.isInvalid()) {
					status.set(StatusBit.REC_NOT_FOUND);
				} else {
					drive.getImage().writeSectorData(rwBuffer, 0, buflen, currentMarker);
				}
			} else if (command == RealDiskConsts.FDC_writetrack) {
				if (drive.isReadOnly()) {
					status.set(StatusBit.WRITE_PROTECT);
					return;
				}
				drive.getImage().writeTrackData(rwBuffer, 0, buflen);
			}
			
			try {
				drive.getImage().commitTrack();
			} catch (IOException e) {
				status.set(StatusBit.NOT_READY);
				status.set(StatusBit.LOST_DATA);
			}
		}
		

	}

	public void FDCrestore() throws IOException {
		dumper.info("FDC restore");

		if (drive == null)
			return;
		
		trackReg = 0;
		
		updateSeek(drive, trackReg);
			
		status.set(StatusBit.TRACK_0);
		status.reset(StatusBit.REC_NOT_FOUND);
		status.reset(StatusBit.CRC_ERROR);
		status.reset(StatusBit.SEEK_ERROR);
		
		if ((flags & RealDiskConsts.fl_verify_track) != 0) {
			matchIDMarker(trackReg, (byte) -1, sideReg);
		}
		
		drive.restore();
	}

	public void FDCseek() throws IOException {
		dumper.info("FDC seek, T{0} s{1}", lastbyte, sideReg);

		if (drive == null)
			return;
		
		while (trackReg < lastbyte) {
			trackReg++;
			updateSeek(drive, trackReg);

		}
		while (trackReg > lastbyte) {
			trackReg--;
			updateSeek(drive, trackReg);
		}
		/*
		trackReg = lastbyte;
		updateSeek(trackReg, sideReg);
		 */
		
		status.reset(StatusBit.SEEK_ERROR);
		status.reset(StatusBit.TRACK_0);
		if (trackReg == 0)
			status.set(StatusBit.TRACK_0);

		if ((flags & RealDiskConsts.fl_verify_track) != 0) {
			matchIDMarker(trackReg, (byte) -1, sideReg);
		}
		
	}

	/**
	 * @throws IOException
	 */
	protected void updateSeek(FloppyDrive drive, byte newtrack) throws IOException {
		int stepTime = 0;
		switch (flags & RealDiskConsts.fl_step_rate) {
		case 0x00:
		case 0x01:
			stepTime = 6;
			break;
		case 0x02:
			stepTime = 10;
			break;
		case 0x03:
			stepTime = 20;
			break;
		}
		addBusyTime(Math.abs(newtrack - drive.getSeekTrack()) * stepTime);
		
		drive.setTrack(newtrack);

	}
	/**
	 * @throws IOException 
	 * 
	 */
	public void FDCstep() throws IOException {

		if (drive == null)
			return;
		
		byte track = (byte) (drive.getSeekTrack() + (stepout ? -1 : 1));
		dumper.info("FDC step {2}, T{0} s{1}", track, sideReg,
				stepout ? "out" : "in");
		
		if ((flags & RealDiskConsts.fl_update_track) != 0)
			trackReg = track;
		
		status.reset(StatusBit.TRACK_0);
		if (track == 0)
			status.set(StatusBit.TRACK_0);
		
		updateSeek(drive, track);
		
		if ((flags & RealDiskConsts.fl_verify_track) != 0) {
			//ensureTrackMarkers();
			
			status.reset(StatusBit.SEEK_ERROR);
			
			IdMarker marker = matchIDMarker(track, (byte) -1, sideReg);
			
//			boolean found = false;
//			
//			for (int tries = 0; tries < trackMarkers.size(); tries++) {
//				if (findIDMarker(command, sideReg) == null)
//					break;
//				if (currentMarker.trackid == track) {
//					found = true;
//					break;
//				}
//			}
			
			if (marker == null) {
				status.set(StatusBit.SEEK_ERROR);
				dumper.error("FDC seek, could not find marker for track {0}", track);
			}
		}
	}


	/**
	 * @return
	 */
	protected IdMarker matchIDMarker(byte trackReg, byte sectorReg, byte sideReg) {

		int command = getCommand() & 0xf0;
		
		boolean isSeek = command == RealDiskConsts.FDC_restore || 
				command == RealDiskConsts.FDC_seek ||
				command == RealDiskConsts.FDC_step ||
				command == RealDiskConsts.FDC_stepin ||
				command == RealDiskConsts.FDC_stepout;
				
		if (isSeek) {
			status.reset(StatusBit.SEEK_ERROR);
			if (drive == null) {
				status.set(StatusBit.SEEK_ERROR);
				return null;
			}
		} else {
			status.reset(StatusBit.REC_NOT_FOUND);
			status.reset(StatusBit.CRC_ERROR);
			if (drive == null) {
				status.set(StatusBit.NOT_READY);
				status.set(StatusBit.REC_NOT_FOUND);
				return null;
			}
		}
	
		
		// FDC179x mode
		//byte desiredSide = (byte) ((flags & fl_side_number) != 0 ? 1 : 0);
	
		IdMarker firstMarker = null;
		//ensureTrackMarkers();
		
		//int tries = trackMarkers.size();
		IdMarker matchedMarker = null;
		while (matchedMarker == null) {
			IdMarker currentMarker = null;

			addBusyTime(20);
			
			if ((currentMarker = drive.findIDMarker()) == null)
				break;
			
			if (currentMarker.trackid == trackReg
				&& ((command & RealDiskConsts.fl_side_compare) == 0 || currentMarker.sideid == sideReg)  
				&& (isSeek || currentMarker.sectorid == sectorReg)
				//&& crcid == crc
				)
			{
				matchedMarker = currentMarker;
				break;
			}
			
			if (firstMarker == null) {
				firstMarker = currentMarker;
			} else if (currentMarker.equals(firstMarker)) {
				break;
			}
		}

		if (matchedMarker == null) {
			dumper.error("FDCmatchIDmarker failed for T=" + trackReg+"; S=" + sectorReg + "; s="+ sideReg);
			if (isSeek)
				status.set(StatusBit.SEEK_ERROR);
			else
				status.set(StatusBit.REC_NOT_FOUND);
			return null;
		}
		
		dumper.info("FDCmatchIDmarker succeeded: track {0}, sector {1}, side {2}, size {3} (sector #{4})",
				matchedMarker.trackid, matchedMarker.sectorid, 
				matchedMarker.sideid, matchedMarker.sizeid, 
				matchedMarker.trackid * 9 + matchedMarker.sectorid);
		
		return matchedMarker;
	}
	

	/**
	 * @throws IOException 
	 * 
	 */
	public void FDCreadsector() throws IOException {
		dumper.info("FDC read sector, T{0} S{1} s{2}", trackReg, sectorReg, sideReg);

		buflen = 0;
		bufpos = 0;

		status.reset(StatusBit.LOST_DATA);
		status.reset(StatusBit.DRQ_PIN);
		
		IdMarker currentMarker = matchIDMarker(trackReg, sectorReg, sideReg);
		if (currentMarker == null) {
			return;
		}
		
		buflen = 128 << (currentMarker.sizeid & 0xff);
//		if (buflen > image.getTrackSize()) {
//			status.set(StatusBit.REC_NOT_FOUND);
//			return 0;
//		}
		buflen = Math.min(buflen, rwBuffer.length);
		
		addBusyTime(buflen * 80 / 1000);		// TODO: real timing
		
		//image.readSectorData(currentMarker, rwBuffer, 0, buflen);
		drive.getImage().readSectorData(currentMarker, rwBuffer, 0, buflen);
		
		status.set(StatusBit.DRQ_PIN);

	}


	/**
	 * @throws IOException 
	 * 
	 */
	public void FDCwritesector() throws IOException {
		dumper.info("FDC write sector, T{0} S{1} s{2}", trackReg, sectorReg, sideReg);

		buflen = 0;
		bufpos = 0;

		status.reset(StatusBit.LOST_DATA);
		status.reset(StatusBit.DRQ_PIN);
		
		IdMarker currentMarker = matchIDMarker(trackReg, sectorReg, sideReg);
		if (currentMarker == null) {
			return;
		}
		
		// not sure this is true
		// http://nouspikel.group.shef.ac.uk//ti99/disks.htm#Sector%20size%20code
		if (true || (flags & RealDiskConsts.fl_length_coding) == 0)
			buflen = 128 << currentMarker.sizeid;
		else
			buflen = currentMarker.sizeid != 0 ? (currentMarker.sizeid & 0xff) * 16 : 4096;
			
		addBusyTime(buflen * 80 / 1000);	// TODO: real timing
		
		status.set(StatusBit.DRQ_PIN);
		
		if (drive.isReadOnly())
			status.set(StatusBit.WRITE_PROTECT);


	}

	/**
	 * 
	 */
	public void FDCreadIDmarker() {
		if (drive == null)
			return;

		bufpos = 0;
		buflen = 0;
		
		status.reset(StatusBit.LOST_DATA);
		status.reset(StatusBit.REC_NOT_FOUND);

		IdMarker currentMarker = drive.findIDMarker();
		if (currentMarker == null) {
			status.set(StatusBit.REC_NOT_FOUND);
			return;
		}
		if ((command & RealDiskConsts.fl_side_compare) != 0 && (currentMarker.sideid != sideReg)) {
			status.set(StatusBit.REC_NOT_FOUND);
			return;
		}

		int ptr = 0;

		rwBuffer[ptr++] = currentMarker.trackid;
		rwBuffer[ptr++] = currentMarker.sideid;
		rwBuffer[ptr++] = currentMarker.sectorid;
		rwBuffer[ptr++] = currentMarker.sizeid;
		rwBuffer[ptr++] = (byte) (currentMarker.crcid >> 8);
		rwBuffer[ptr++] = (byte) (currentMarker.crcid & 0xff);

		buflen = 6;

		// the detected track is copied into the sector register (!)
		sectorReg = drive.getCurrentMarker().trackid;
		
		dumper.info("FDC read ID marker: track={0} sector={1} side={2} size={3}",
				currentMarker.trackid, currentMarker.sectorid, currentMarker.sideid, currentMarker.sizeid);
		
	}

	/**
	 * @throws IOException 
	 * 
	 */
	public void FDCinterrupt() throws IOException {
		dumper.info("FDC interrupt");
		
		commandBusyExpiration = 0;

		status.clear();
		
		FDCflush();

		buflen = bufpos = 0;			
	}

	public void FDCwritetrack() {
		dumper.info("FDC write track, #{0}", drive != null ? drive.getSeekTrack() : -1);
		
		if (drive == null)
			return;
		
		bufpos = 0;
		buflen = drive.writeTrack(); 
		

	}

	public void FDCreadtrack() throws IOException {
		dumper.info("FDC read track, #{0}", drive != null ? drive.getSeekTrack() : -1);

		if (drive == null)
			return;
		
		status.reset(StatusBit.LOST_DATA);

		bufpos = 0;
		buflen = drive.readTrack(rwBuffer);
		
		if (!isMotorAtTargetSpeed())
			status.set(StatusBit.BUSY);

	}

	public void saveState(ISettingSection section) {
	}

	public void loadState(ISettingSection section) {
		for (FloppyDrive drive : drives) {
			drive.reset();
		}
	}

	/**
	 * @return
	 */
	public byte readByte() {
		byte ret = 0;

		if (hold && buflen != 0 && drive != null && drive.isMotorAtTargetSpeed()) {
			ret = rwBuffer[bufpos++];
			crcAlg.feed(ret);
			if (bufpos >= buflen) {
				status.reset(StatusBit.DRQ_PIN);
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
		if (buflen != 0 && drive != null && drive.isMotorAtTargetSpeed()) {
			/* fill circular buffer */
			if (bufpos < buflen) {
				rwBuffer[bufpos++] = val;
				crcAlg.feed(val);
			} else {
				status.reset(StatusBit.DRQ_PIN);
				dumper.error("Tossing extra byte >{0}", Integer.toHexString(val & 0xff));
			}
		}	
	}

	/**
	 * @param side
	 * @throws IOException 
	 */
	public void setSide(byte side) throws IOException {
		// side affects all drives
		dumper.info("Select side {0}", side);
		sideReg = side;
		for (FloppyDrive drive : drives) {
			drive.setSide(side);
		}
	}

	public void setHold(boolean hold) {
		try {
			FDChold(hold);
		} catch (IOException e) {
			dumper.error(e.getMessage());
		}
		this.hold = hold;
	}

	public boolean isHold() {
		return hold;
	}

	public FDCStatus getStatus() {
		return status;
	}

	public void setCommand(int command) {
		this.command = command;
		
		setCommandBusyExpiration(System.currentTimeMillis() + 1);
	}

	public int getCommand() {
		return command;
	}

	public void setTrackReg(byte trackReg) {
		this.trackReg = trackReg;
		dumper.info(("FDC write track addr " + trackReg + " >" + HexUtils.toHex2(trackReg)));
	}

	public byte getTrackReg() {
		dumper.info(("FDC read track " + trackReg + " >" + HexUtils.toHex2(trackReg)));
		return trackReg;
	}

	public void setSectorReg(byte sectorReg) {
		this.sectorReg = sectorReg;
		dumper.info(("FDC write sector addr " + sectorReg + " >" + HexUtils.toHex2(sectorReg)));
	}

	public byte getSectorReg() {
		dumper.info(("FDC read sector " + sectorReg + " >" + HexUtils.toHex2(sectorReg)));
		return sectorReg;
	}

	public void setLastbyte(byte lastbyte) {
		this.lastbyte = lastbyte;
	}

	public byte getLastbyte() {
		return lastbyte;
	}

	public int setBufpos(int bufpos) {
		this.bufpos = bufpos;
		return bufpos;
	}

	public int getBufpos() {
		return bufpos;
	}

	public void resetCrc() {
		crcAlg.reset();
	}

	public short getCrc() {
		return crcAlg.read();
	}

	public void setBuflen(int buflen) {
		this.buflen = buflen;
	}

	public int getBuflen() {
		return buflen;
	}

	public void setFlags(byte flags) {
		this.flags = flags;
	}

	public byte getFlags() {
		return flags;
	}

	public void setStepout(boolean stepout) {
		this.stepout = stepout;
	}

	public boolean isStepout() {
		return stepout;
	}

	public int getSelectedDisk() {
		return selectedDisk;
	}

	public void selectDisk(int newnum, boolean on) {
		//module_logger(&realDiskDSR, _L|L_1, _("CRU disk select, #%d\n"), newnum);
		
		if (on) {
			selectedDisk = (byte) newnum;
			drive = drives[newnum - 1];
			drive.activate();

		} else {
			if (newnum == selectedDisk) {
				if (drive != null)
					drive.reset();
				
				selectedDisk = 0;
				drive = null;
			}
		}
				
	}

	/**
	 * @param b
	 */
	public void setHeads(boolean b) {
		if (drive != null)
			drive.setHeads(b);
	}

	/**
	 * @return
	 */
	public boolean isHeads() {
		if (drive != null)
			return drive.isHeads();
		return false;
	}
	

	public void setDiskMotor(boolean on) {
		if (drive == null)
			return;
		
		drive.setMotor(on);
	}

	public boolean isMotorAtTargetSpeed() {
		if (drive != null)
			return drive.isMotorAtTargetSpeed();
		else
			return false;
	}




	/**
	 * @return
	 */
	public byte readStatus() {
		byte ret = getStatus().calculate(getCommand());
		
		if (drive != null) {
			if (!drive.isMotorAtTargetSpeed()
				|| getCommandBusyExpiration() > System.currentTimeMillis())
				ret |= StatusBit.BUSY.getVal();
		}
		
		if (ret != lastStatus) {
			StringBuilder status = new StringBuilder();
			getStatus().toString(getCommand());
			dumper.info(("FDC read status >" + HexUtils.toHex2(ret) + " : " + status));
		}
		lastStatus = ret;

		return ret;
	}

	/**
	 * @param key
	 * @return
	 */
	public FloppyDrive getDrive(String key) {
		FloppyDrive drive = driveMap.get(key);
		return drive;
	}

	/**
	 * @param val
	 */
	public void writeData(byte val) {
		if (!isHold())
			dumper.info(("FDC write data ("+getBufpos()+") >"+HexUtils.toHex2(val))); 
		//			   (u8) val);
		if (!isHold()) {
			setLastbyte(val);
		} else {
			getStatus().set(StatusBit.DRQ_PIN);;
			
			if (getCommand() == RealDiskConsts.FDC_writesector) {
				// normal write
				writeByte(val);
				

			} else if (getCommand() == RealDiskConsts.FDC_writetrack) {
				if (true /* is FM */) {
					// for FM write, >F5 through >FE are special
					if (val == (byte) 0xf5 || val == (byte) 0xf6) {
						getStatus().reset(StatusBit.REC_NOT_FOUND);;
					} else if (val == (byte) 0xf7) {
						// write CRC
						writeByte((byte) (getCrc() >> 8));
						writeByte((byte) (getCrc() & 0xff));
					} else if (val >= (byte) 0xf8 && val <= (byte) 0xfb) {
						resetCrc();
						writeByte(val);
					} else {
						writeByte(val);
					}
				} else {
					writeByte(val);
				}
			} else {
				dumper.info(("Unexpected data write >" + HexUtils.toHex2(val) + " for command >" + HexUtils.toHex2(getCommand())));
			}
		}
		
	}

	/**
	 * @param val
	 */
	public void writeCommand(byte val) {
		try {
			if (getStatus().is(StatusBit.BUSY) && (val & 0xf0) != 0xf0) {
				dumper.info(("FDC writing command >" + HexUtils.toHex2(val) + " while busy!"));
				//return;
			}
			
			FDCflush();
			setBuflen(setBufpos(0));
			
			dumper.info(("FDC command >" + HexUtils.toHex2(val)));
			//module_logger(&realDiskDSR, _L|L_1, _("FDC command >%02X\n"), val);
			
			setCommand(val & 0xF0);
			
			// standardize commands
			if (getCommand() == 0x30 || getCommand() == 0x50 || getCommand() == 0x70
					|| getCommand() == (byte)0x90 || getCommand() == (byte)0xA0)
				setCommand(getCommand() & (~0x10));
			
			setFlags((byte) (val & 0x1F));
		
			getStatus().reset(StatusBit.BUSY);
			
			switch (getCommand()) {
			case RealDiskConsts.FDC_restore:
				FDCrestore();
				break;
			case RealDiskConsts.FDC_seek:
				FDCseek();
				break;
			case RealDiskConsts.FDC_step:
				FDCstep();
				break;
			case RealDiskConsts.FDC_stepin:
				setStepout(false);
				FDCstep();
				break;
			case RealDiskConsts.FDC_stepout:
				setStepout(true);
				FDCstep();
				break;
			case RealDiskConsts.FDC_readsector:
				FDCreadsector();
				break;
			case RealDiskConsts.FDC_writesector:
				FDCwritesector();
				break;
			case RealDiskConsts.FDC_readIDmarker:
				FDCreadIDmarker();
				break;
			case RealDiskConsts.FDC_interrupt:
				FDCinterrupt();
				break;
			case RealDiskConsts.FDC_writetrack:
				FDCwritetrack();
				break;
			case RealDiskConsts.FDC_readtrack:
				FDCreadtrack();
				break;
			default:
				//module_logger(&realDiskDSR, _L|L_1, _("unknown FDC command >%02X\n"), val);
				dumper.info(("Unknown FDC command >" + HexUtils.toHex2(val)));
			}
		} catch (IOException e) {
			dumper.error(e.getMessage());
		}  catch(Throwable t) {
			dumper.error(t.getMessage());
		}
		
	}
	/**
	 * @return the commandBusyExpiration
	 */
	public long getCommandBusyExpiration() {
		return commandBusyExpiration;
	}

	/**
	 * @param ms
	 */
	public void addBusyTime(int ms) {
		//status.set(StatusBit.BUSY);
		if (commandBusyExpiration == 0) {
			commandBusyExpiration = System.currentTimeMillis();
		}
		commandBusyExpiration += settingRealTime.getBoolean() ? ms : 0;
	}


	/**
	 * @param l
	 */
	public void setCommandBusyExpiration(long l) {
		this.commandBusyExpiration = l;
	}

	/**
	 * @return
	 */
	public boolean isMotorRunning() {
		return drive != null && drive.isMotorRunning();
	}

}