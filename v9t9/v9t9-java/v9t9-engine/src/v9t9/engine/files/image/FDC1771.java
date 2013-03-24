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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import v9t9.common.files.IDiskImage;
import v9t9.common.files.IdMarker;
import v9t9.engine.dsr.realdisk.CRC16;
import ejs.base.properties.IPersistable;
import ejs.base.settings.ISettingSection;



public class FDC1771 implements IPersistable {

	
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
	
	byte seektrack; /* physically seeked track */
	byte seekside; /* the side we seeked to in the image */
	
	private FDCStatus status = new FDCStatus();
	
	private IDiskImage image;
	private Iterator<IdMarker> trackMarkerIter;
	private IdMarker currentMarker;
	private boolean heads;

	public long commandBusyExpiration;
	
	private Dumper dumper;
	private List<IdMarker> trackMarkers;

	/**
	 * 
	 */
	public FDC1771(Dumper dumper) {
		this.dumper = dumper;
	}
	
	/**
	 * @param image the image to set
	 */
	public void setImage(BaseDiskImage image) {
		if (this.image != image) {
			this.image = image;

			// refetch markers (and track) next time
			trackMarkerIter = null;
			currentMarker = null;
			
			seektrack = -1;
			seekside = -1;
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

		if (image == null) {
			status.set(StatusBit.NOT_READY);
			return;
		}
		
		if (buflen != 0) {
			//status &= ~fdc_LOSTDATA;

			status.reset(StatusBit.WRITE_PROTECT);
			status.reset(StatusBit.LOST_DATA);
			status.reset(StatusBit.CRC_ERROR);

			if (command == RealDiskConsts.FDC_writesector) {
				if (image.isReadOnly()) {
					status.set(StatusBit.WRITE_PROTECT);
				}
				if (currentMarker == null) {
					status.set(StatusBit.REC_NOT_FOUND);
				}
				else if (currentMarker.isInvalid()) {
					status.set(StatusBit.REC_NOT_FOUND);
				} else {
					image.writeSectorData(rwBuffer, 0, buflen, currentMarker);
				}
			} else if (command == RealDiskConsts.FDC_writetrack) {
				if (image.isReadOnly()) {
					status.set(StatusBit.WRITE_PROTECT);
					return;
				}
				image.writeTrackData(rwBuffer, 0, buflen);
			}
			
			try {
				image.commitTrack();
			} catch (IOException e) {
				status.set(StatusBit.NOT_READY);
				status.set(StatusBit.LOST_DATA);
			}
		
			//readCurrentTrackData();
		}

	}


	/*	Find a sector ID on the track */
	private boolean
	FDCfindIDmarker()
	{
		ensureTrackMarkers();
		int iters = trackMarkers.size();
		while (iters-- > 0) {
			if (!trackMarkerIter.hasNext()) {
				trackMarkerIter = trackMarkers.iterator();
			}
			if (trackMarkerIter.hasNext()) {
				currentMarker = trackMarkerIter.next();
				if (command != RealDiskConsts.FDC_readIDmarker || currentMarker.sideid == sideReg)
					return true;
			}
		}
		
		currentMarker = null;
		return false;
		
	}

	/*	Match the current ID with the desired track/sector id */
	private boolean
	FDCmatchIDmarker() {
		dumper.info("FDC match ID marker: looking for T{0}, S{1}", trackReg, sectorReg);
		
		status.reset(StatusBit.REC_NOT_FOUND);
		status.reset(StatusBit.CRC_ERROR);
	
		// FDC179x mode
		//byte desiredSide = (byte) ((flags & fl_side_number) != 0 ? 1 : 0);
	
		ensureTrackMarkers();
		
		int tries = trackMarkers.size();
		boolean found = false;
		while (!found && tries-- > 0) {

			addBusyTime(20);
			
			if (!FDCfindIDmarker())
				break;
			
			if (currentMarker.trackid == trackReg
				//&& sideid == desiredSide  // for FDC179x
				&& currentMarker.sectorid == sectorReg
				//&& crcid == crc
				)
			{
				found = true;
			}
		}

		if (!found) {
			dumper.error("FDCmatchIDmarker failed");
			status.set(StatusBit.REC_NOT_FOUND);
			return false;
		}
		
		dumper.info("FDCmatchIDmarker succeeded: track {0}, sector {1}, side {2}, size {3} (sector #{4})",
				currentMarker.trackid, currentMarker.sectorid, 
				currentMarker.sideid, currentMarker.sizeid, 
				currentMarker.trackid * 9 + currentMarker.sectorid);
		return true;
	}

	/**
	 * 
	 */
	private void ensureTrackMarkers() {
		if (trackMarkers == null || trackMarkers.isEmpty()) {
			if (image != null)
				trackMarkers = image.getTrackMarkers();
			else
				trackMarkers = Collections.<IdMarker>emptyList();
			trackMarkerIter = trackMarkers.iterator();
		}
	}

	private void updateSeek(byte track, byte side) throws IOException {
		if (seektrack != track || seekside != side) {
			// don't change anything until the track changes, 
			// so we can fetch the hidden sectors with the same id as normal ones
			
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
			addBusyTime(Math.abs(seektrack - track) * stepTime);

			seektrack = track;
			seekside = side;
			
			currentMarker = null;
			trackMarkerIter = null;
			trackMarkers = null;
			
			if (image != null)
				image.seekToCurrentTrack(seektrack, seekside);
		}
	}

	/**
	 * @param ms
	 */
	public void addBusyTime(int ms) {
		//status.set(StatusBit.BUSY);
		if (commandBusyExpiration == 0) {
			commandBusyExpiration = System.currentTimeMillis();
		}
		commandBusyExpiration += ms;
	}

	public void FDCrestore() throws IOException {
		dumper.info("FDC restore");
		
		trackReg = 0;
		updateSeek(trackReg, sideReg);
		
		status.set(StatusBit.TRACK_0);
		status.reset(StatusBit.REC_NOT_FOUND);
		status.reset(StatusBit.CRC_ERROR);
		status.reset(StatusBit.SEEK_ERROR);
		
		if ((flags & RealDiskConsts.fl_verify_track) != 0) {
			verifyTrack(trackReg);
		}
		
		currentMarker = null;
		trackMarkerIter = null;
		trackMarkers = null;
		
		if (image != null)
			image.seekToCurrentTrack(seektrack, seekside);

	}

	public void FDCseek() throws IOException {
		dumper.info("FDC seek, T{0} s{1}", lastbyte, sideReg);

		while (trackReg < lastbyte) {
			trackReg++;
			updateSeek(trackReg, sideReg);
		}
		while (trackReg > lastbyte) {
			trackReg--;
			updateSeek(trackReg, sideReg);
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
			verifyTrack(trackReg);
		}
		
	}

	/**
	 * 
	 */
	private void verifyTrack(byte track) {
		ensureTrackMarkers();
		
		status.reset(StatusBit.SEEK_ERROR);
		boolean found = false;
		for (int tries = 0; tries < trackMarkers.size(); tries++) {
			if (!FDCfindIDmarker())
				break;
			if (currentMarker.trackid == track) {
				found = true;
				break;
			}
		}
		
		if (!found) {
			status.set(StatusBit.SEEK_ERROR);
			dumper.error("FDC seek, could not find marker for track {0}", track);
		}

	}

	/**
	 * @throws IOException 
	 * 
	 */
	public void FDCstep() throws IOException {

		byte newtrack = (byte) (seektrack + (stepout ? -1 : 1));
		if ((flags & RealDiskConsts.fl_update_track) != 0)
			trackReg = newtrack;
		
		dumper.info("FDC step {2}, T{0} s{1}", newtrack, sideReg,
				stepout ? "out" : "in");
		
		status.reset(StatusBit.TRACK_0);
		if (newtrack == 0)
			status.set(StatusBit.TRACK_0);
		
		updateSeek(newtrack, sideReg);
		
		if ((flags & RealDiskConsts.fl_verify_track) != 0) {
			verifyTrack(newtrack);
		}
	}

	/**
	 * @throws IOException 
	 * 
	 */
	public void FDCreadsector() throws IOException {
		dumper.info("FDC read sector, T{0} S{1} s{2}", trackReg, sectorReg, sideReg);
		
		status.reset(StatusBit.LOST_DATA);
		status.reset(StatusBit.DRQ_PIN);
		
		if (image == null) {
			status.set(StatusBit.REC_NOT_FOUND);
			return;
		}

		if (!FDCmatchIDmarker()) {
			status.set(StatusBit.REC_NOT_FOUND);
			return;
		}
		
		buflen = 128 << (currentMarker.sizeid & 0xff);
		if (buflen > image.getTrackSize()) {
			status.set(StatusBit.REC_NOT_FOUND);
			return;
		}
		bufpos = 0;
		
		addBusyTime(buflen * 100 / 1000);
		
		image.readSectorData(currentMarker, rwBuffer, 0, buflen);
		
		status.set(StatusBit.DRQ_PIN);
	}


	/**
	 * @throws IOException 
	 * 
	 */
	public void FDCwritesector() throws IOException {
		dumper.info("FDC write sector, T{0} S{1} s{2}", trackReg, sectorReg, sideReg);

		status.reset(StatusBit.LOST_DATA);
		status.reset(StatusBit.DRQ_PIN);
		
		if (image == null) {
			status.set(StatusBit.REC_NOT_FOUND);
			return;
		}

		if (!FDCmatchIDmarker()) 
			return;
		
		// not sure this is true
		// http://nouspikel.group.shef.ac.uk//ti99/disks.htm#Sector%20size%20code
		if (true || (flags & RealDiskConsts.fl_length_coding) == 0)
			buflen = 128 << currentMarker.sizeid;
		else
			buflen = currentMarker.sizeid != 0 ? (currentMarker.sizeid & 0xff) * 16 : 4096;
			
		bufpos = 0;
		
		addBusyTime(buflen * 150 / 1000);
		
		status.set(StatusBit.DRQ_PIN);
		
		if (image.isReadOnly())
			status.set(StatusBit.WRITE_PROTECT);
	}

	/**
	 * 
	 */
	public void FDCreadIDmarker() {
		status.reset(StatusBit.LOST_DATA);
		status.reset(StatusBit.REC_NOT_FOUND);
		
		if (!FDCfindIDmarker()) {
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
		bufpos = 0;

		// the detected track is copied into the sector register (!)
		sectorReg = currentMarker.trackid;
		
		dumper.info("FDC read ID marker: track={0} sector={1} side={2} size={3}",
				currentMarker.trackid, currentMarker.sectorid, currentMarker.sideid, currentMarker.sizeid);

		
	}

	/**
	 * @throws IOException 
	 * 
	 */
	public void FDCinterrupt() throws IOException {
		dumper.info("FDC interrupt");
		
		if (image != null)
			image.setMotorTimeout(0);
		commandBusyExpiration = 0;
		status.clear();
		
		FDCflush();

		buflen = bufpos = 0;			
	}

	public void FDCwritetrack() {
		dumper.info("FDC write track, #{0}", seektrack);

		status.reset(StatusBit.LOST_DATA);
		
		buflen = RealDiskConsts.DSKbuffersize;  
			
		bufpos = 0;
		
		addBusyTime(buflen * 10 / 1000);
		
		if (image != null && image.isReadOnly())
			status.set(StatusBit.WRITE_PROTECT);
	}

	public void FDCreadtrack() throws IOException {
		dumper.info("FDC read track, #{0}", seektrack);

		status.reset(StatusBit.LOST_DATA);
		
		bufpos = 0;
		if (image != null) {
			buflen = image.getTrackSize();
			if (!image.isMotorRunning())
				status.set(StatusBit.BUSY);
			image.readTrackData(rwBuffer, 0, buflen);
		} else {
			buflen = 0;
		}
		

		addBusyTime(buflen * 10 / 1000);
	}

	public void saveState(ISettingSection section) {
	}

	public void loadState(ISettingSection section) {
		if (image != null) {
			try {
				image.closeDiskImage();
			} catch (IOException e) {
				dumper.error(e.getMessage());
			}
		}
		image = null;
	}

	/**
	 * @return
	 */
	public byte readByte() {
		byte ret = 0;

		if (hold && image != null && image.isMotorRunning() && buflen != 0) {
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
		if (buflen != 0 && image != null && image.isMotorRunning()) {
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
		dumper.info("Select side {0}", side);
		updateSeek(seektrack, side);
		sideReg = side;
	}

	/**
	 * @return
	 */
	public IDiskImage getImage() {
		return image;
	}

	public void setHold(boolean hold) {
		this.hold = hold;
	}

	public boolean isHold() {
		return hold;
	}

	public void setHeads(boolean heads) {
		this.heads = heads;
	}

	public boolean isHeads() {
		return heads;
	}

	public void setStatus(FDCStatus status) {
		this.status = status;
	}

	public FDCStatus getStatus() {
		return status;
	}

	public void setCommand(int command) {
		this.command = command;
	}

	public int getCommand() {
		return command;
	}

	public void setTrackReg(byte trackReg) {
		this.trackReg = trackReg;
	}

	public byte getTrackReg() {
		return trackReg;
	}

	public void setSectorReg(byte sectorReg) {
		this.sectorReg = sectorReg;
	}

	public byte getSectorReg() {
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

}