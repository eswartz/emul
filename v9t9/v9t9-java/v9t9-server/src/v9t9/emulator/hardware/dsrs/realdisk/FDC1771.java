/**
 * Mar 1, 2011
 */
package v9t9.emulator.hardware.dsrs.realdisk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ejs.coffee.core.properties.IPersistable;
import org.ejs.coffee.core.settings.ISettingSection;


public class FDC1771 implements IPersistable {

	
	boolean hold; /* holding for data? */
	byte lastbyte; /* last byte written to WDDATA when hold off */
	byte rwBuffer[] = new byte[RealDiskImageDsr.DSKbuffersize]; /* read/write contents */
	int buflen; /* max length of data expected for a write/read */
	int bufpos; /* offset into buffer */

	int command; /* command being executed (0xE0 mask) */
	byte flags; /* flags sent with command being executed (0x1F mask) */

	/* command-specified */
	byte trackReg; /* desired track */
	byte sideReg; /* current side */
	byte sectorReg; /* desired sector */
	short crc; /* current CRC */

	boolean stepout; /* false: in, true: out */
	
	byte seektrack; /* physically seeked track */
	byte seekside; /* the side we seeked to in the image */
	
	protected FDCStatus status = new FDCStatus();
	
	private BaseDiskImage image;
	private List<IdMarker> trackMarkers;
	private Iterator<IdMarker> trackMarkerIter;
	private IdMarker currentMarker;
	public boolean heads;

	public long commandBusyExpiration;

	/**
	 * 
	 */
	public FDC1771() {
	}
	
	/**
	 * @param image the image to set
	 */
	public void setImage(BaseDiskImage image) {
		if (this.image != image) {
			this.image = image;

			// refetch markers (and track) next time
			trackMarkerIter = null;
			trackMarkers = null;
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
			if (hold && (command == RealDiskImageDsr.FDC_writesector || command == RealDiskImageDsr.FDC_writetrack)) {
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

			if (command == RealDiskImageDsr.FDC_writesector)
				image.writeSectorData(rwBuffer, 0, buflen, currentMarker, status);
			else if (command == RealDiskImageDsr.FDC_writetrack)
				image.writeTrackData(rwBuffer, 0, buflen, status);
			
			image.commitTrack(status);
		
			//readCurrentTrackData();
		}

	}

	private void ensureTrackMarkers() {
		if (trackMarkers == null) {
			if (image == null) {
				trackMarkers = new ArrayList<IdMarker>();
			} else {
				trackMarkers = image.getTrackMarkers();
			}
			trackMarkerIter = trackMarkers.iterator();
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
				if (command != RealDiskImageDsr.FDC_readIDmarker || currentMarker.sideid == sideReg)
					return true;
			}
		}
		
		currentMarker = null;
		return false;
		
	}

	/*	Match the current ID with the desired track/sector id */
	private boolean
	FDCmatchIDmarker() {
		RealDiskImageDsr.info("FDC match ID marker: looking for T{0}, S{1}", trackReg, sectorReg);
		
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
			RealDiskImageDsr.error("FDCmatchIDmarker failed");
			status.set(StatusBit.REC_NOT_FOUND);
			return false;
		}
		
		RealDiskImageDsr.info("FDCmatchIDmarker succeeded: track {0}, sector {1}, side {2}, size {3} (sector #{4})",
				currentMarker.trackid, currentMarker.sectorid, 
				currentMarker.sideid, currentMarker.sizeid, 
				currentMarker.trackid * 9 + currentMarker.sectorid);
		return true;
	}

	private void updateSeek(byte track, byte side) throws IOException {
		if (seektrack != track || seekside != side) {
			// don't change anything until the track changes, 
			// so we can fetch the hidden sectors with the same id as normal ones
			
			int stepTime = 0;
			switch (flags & FDC1771Constants.fl_step_rate) {
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
			trackMarkers = null;
			trackMarkerIter = null;
			
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
		RealDiskImageDsr.info("FDC restore");
		
		trackReg = 0;
		updateSeek(trackReg, sideReg);
		
		status.set(StatusBit.TRACK_0);
		status.reset(StatusBit.REC_NOT_FOUND);
		status.reset(StatusBit.CRC_ERROR);
		status.reset(StatusBit.SEEK_ERROR);
		
		if ((flags & RealDiskImageDsr.fl_verify_track) != 0) {
			verifyTrack(trackReg);
		}
		
	}

	public void FDCseek() throws IOException {
		RealDiskImageDsr.info("FDC seek, T{0} s{1}", lastbyte, sideReg);

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

		if ((flags & RealDiskImageDsr.fl_verify_track) != 0) {
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
		   	RealDiskImageDsr.error("FDC seek, could not find marker for track {0}", track);
		}

	}

	/**
	 * @throws IOException 
	 * 
	 */
	public void FDCstep() throws IOException {

		byte newtrack = (byte) (seektrack + (stepout ? -1 : 1));
		if ((flags & RealDiskImageDsr.fl_update_track) != 0)
			trackReg = newtrack;
		
		RealDiskImageDsr.info("FDC step {2}, T{0} s{1}", newtrack, sideReg,
				stepout ? "out" : "in");
		
		status.reset(StatusBit.TRACK_0);
		if (newtrack == 0)
			status.set(StatusBit.TRACK_0);
		
		updateSeek(newtrack, sideReg);
		
		if ((flags & RealDiskImageDsr.fl_verify_track) != 0) {
			verifyTrack(newtrack);
		}
	}

	/**
	 * @throws IOException 
	 * 
	 */
	public void FDCreadsector() throws IOException {
		RealDiskImageDsr.info("FDC read sector, T{0} S{1} s{2}", trackReg, sectorReg, sideReg);
		
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
		RealDiskImageDsr.info("FDC write sector, T{0} S{1} s{2}", trackReg, sectorReg, sideReg);

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
		if (true || (flags & RealDiskImageDsr.fl_length_coding) == 0)
			buflen = 128 << currentMarker.sizeid;
		else
			buflen = currentMarker.sizeid != 0 ? (currentMarker.sizeid & 0xff) * 16 : 4096;
			
		bufpos = 0;
		
		addBusyTime(buflen * 150 / 1000);
		
		status.set(StatusBit.DRQ_PIN);
		
		if (image.readonly)
			status.set(StatusBit.WRITE_PROTECT);
	}

	/**
	 * 
	 */
	public void FDCreadIDmarker() {
		status.reset(StatusBit.LOST_DATA);
		
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
		
		RealDiskImageDsr.info("FDC read ID marker: track={0} sector={1} side={2} size={3}",
				currentMarker.trackid, currentMarker.sectorid, currentMarker.sideid, currentMarker.sizeid);

		
	}

	/**
	 * @throws IOException 
	 * 
	 */
	public void FDCinterrupt() throws IOException {
		RealDiskImageDsr.info("FDC interrupt");
		
		if (image != null)
			image.motorTimeout = 0;
		commandBusyExpiration = 0;
		status.clear();
		
		FDCflush();

		buflen = bufpos = 0;			
	}

	public void FDCwritetrack() {
		RealDiskImageDsr.info("FDC write track, #{0}", seektrack);

		status.reset(StatusBit.LOST_DATA);
		
		buflen = RealDiskImageDsr.DSKbuffersize;  
			
		bufpos = 0;
		
		addBusyTime(buflen * 10 / 1000);
		
		if (image != null && image.readonly)
			status.set(StatusBit.WRITE_PROTECT);
	}

	public void FDCreadtrack() throws IOException {
		RealDiskImageDsr.info("FDC read track, #{0}", seektrack);

		status.reset(StatusBit.LOST_DATA);
		
		bufpos = 0;
		if (image != null) {
			buflen = image.getTrackSize();
			if (!image.motorRunning)
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
				RealDiskImageDsr.error(e.getMessage());
			}
		}
		image = null;
	}

	/**
	 * @return
	 */
	public byte readByte() {
		byte ret = 0;

		if (hold && image != null && image.motorRunning && buflen != 0) {
			ret = rwBuffer[bufpos++];
			crc = RealDiskImageDsr.calc_crc(crc, ret & 0xff);
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
		if (buflen != 0 && image != null && image.motorRunning) {
			/* fill circular buffer */
			if (bufpos < buflen) {
				rwBuffer[bufpos++] = val;
				crc = RealDiskImageDsr.calc_crc(crc, val);
			} else {
				status.reset(StatusBit.DRQ_PIN);
				RealDiskImageDsr.error("Tossing extra byte >{0}", Integer.toHexString(val & 0xff));
			}
		}	
	}

	/**
	 * @param side
	 * @throws IOException 
	 */
	public void setSide(byte side) throws IOException {
		RealDiskImageDsr.info("Select side {0}", side);
		updateSeek(seektrack, side);
		sideReg = side;
	}

	/**
	 * @return
	 */
	public BaseDiskImage getImage() {
		return image;
	}

}