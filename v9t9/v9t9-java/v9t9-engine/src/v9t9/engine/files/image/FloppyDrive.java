/*
  FloppyDrive.java

  (c) 2013-2014 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.files.image;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

import v9t9.common.files.IDiskImage;
import v9t9.common.files.IDiskImageMapper;
import v9t9.common.files.IDiskImageMapper.IDiskImageListener;
import v9t9.common.files.IdMarker;
import v9t9.common.machine.IMachine;
import v9t9.engine.Dumper;
import ejs.base.properties.IProperty;

/**
 * This represents a "physical" floppy drive controlled by the FDC.
 * It maintains its own state for the current track (and track cache)
 * and a motor, whose spin emulates physical time. 
 * 
 * @author ejs
 *
 */
public class FloppyDrive {
	enum SpinState {
		STOPPED,
		SPINNING_UP,
		SPINNING,
		SPINNING_DOWN
	};
	
	private int num;
	
	private IDiskImage image;
	private Iterator<IdMarker> trackMarkerIter;
	private IdMarker currentMarker;
	private boolean heads;
	
	private List<IdMarker> trackMarkers;
	

	private TimerTask motorTickTask;

	private Dumper dumper;

	//private FDCStatus status;

	private IDiskImageMapper imageMapper;

	private IProperty settingRealTime;

	private SpinState spinState = SpinState.STOPPED;
	private long motorTimeout;

	private FDC1771 fdc;

	private boolean motorRequest;

	private IProperty activeProperty;
	
	public FloppyDrive(IMachine machine, 
			Dumper dumper_, int num_, 
			IProperty motorProperty_, FDC1771 fdc) {
		this.dumper = dumper_;
		this.num = num_;
//		this.motorProperty = motorProperty_;
		this.fdc = fdc;

		this.activeProperty = motorProperty_;

		settingRealTime = machine.getSettings().get(RealDiskSettings.diskImageRealTime);

		imageMapper = machine.getEmulatedFileHandler().getDiskImageMapper();
		
		final String name = RealDiskSettings.getDiskImageSetting(num);
		File defaultDiskImage = RealDiskSettings.getDefaultDiskImage(name);
		imageMapper.registerDiskImageSetting(
				RealDiskSettings.diskImagesEnabled, 
				name, defaultDiskImage.getAbsolutePath());
		
		imageMapper.addListener(new IDiskImageListener() {
			
			@Override
			public void diskChanged(String device, IDiskImage oldImage,
					IDiskImage newImage) {
				if (name.equals(device))
					setImage(newImage);
			}
		});

		// add motor timer
		motorTickTask = new TimerTask() {
			
			@Override
			public void run() {
				updateMotorSpinState();
			}
		};
		
		machine.getMachineTimer().scheduleAtFixedRate(motorTickTask, 0, 100);
		
	}
	
	
	
	protected synchronized void updateMotorSpinState() {
//		!drive.isMotorAtTargetSpeed() && 
//		(!realTime || drive.getMotorTimeout() > 0)
		long now = System.currentTimeMillis();

		boolean realTime = settingRealTime.getBoolean();
		
		if (motorRequest) {
			if (spinState == null || spinState == SpinState.STOPPED) {
				// We have a delay when the motor goes from
				// stopped to spinning at full speed.
				dumper.info("DSK{0}: motor starting", num);
				motorTimeout = now + (realTime ? 1500 : 0);
				spinState = SpinState.SPINNING_UP;
				
				activeProperty.setBoolean(true);

			} else if (spinState == SpinState.SPINNING_UP || spinState == SpinState.SPINNING_DOWN) {
				if (spinState != SpinState.SPINNING_UP)
					spinState = SpinState.SPINNING_UP;
				
				if (now >= motorTimeout) {
					spinState = SpinState.SPINNING;
					dumper.info("DSK{0}: motor spinning", num);
					
					//status.reset(StatusBit.BUSY);
					this.motorTimeout = System.currentTimeMillis() + 4320;
				}
			} else if (spinState == SpinState.SPINNING) {
				// once spinning, stay spinning 
				motorTimeout = now + 4230;
			}
		} else {
			// no request, let it spin down
			if (spinState != SpinState.STOPPED) {
				spinState = SpinState.SPINNING_DOWN;
				
				if (now >= motorTimeout) {
					dumper.info("DSK{0}: motor off", num);
					spinState = SpinState.STOPPED;
					motorTimeout = 0;
					
					activeProperty.setBoolean(false);
				}
			} else {
//				status.reset(StatusBit.BUSY);
			}
		}
		
	}



	@Override
	public String toString() {
		return "FDCDrive [num=" + num + ", image=" + image + "]";
	}



	/**
	 * @param image2
	 */
	public void setImage(IDiskImage image) {
		if (this.image != image) {
			this.image = image;

			// refetch markers (and track) next time
			trackMarkerIter = null;
			currentMarker = null;
			trackMarkers = null;
		}
		
	}

	/**
	 * @return
	 */
	public IDiskImage getImage() {
		return image;
	}

	/**
	 * @return
	 */
	public IdMarker findIDMarker() {
		ensureTrackMarkers();
		if (trackMarkerIter == null)
			return null;
		int iters = trackMarkers.size();
		while (iters-- > 0) {
			if (!trackMarkerIter.hasNext()) {
				trackMarkerIter = trackMarkers.iterator();
			}
			if (trackMarkerIter.hasNext()) {
				currentMarker = trackMarkerIter.next();
				//if (command != RealDiskConsts.FDC_readIDmarker || currentMarker.sideid == sideReg)
				return currentMarker;
			}
		}
		
		currentMarker = null;
		return null;
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

	public void setTrack(int track) throws IOException {
		
		if (image != null) {
			if (track != image.getTrack()) {
				currentMarker = null;
				trackMarkerIter = null;
				trackMarkers = null;
				image.setTrack(track);
			}
		}
	}
	public void setSide(int side) throws IOException {
		
		if (image != null) {
			if (side != image.getSide()) {
				currentMarker = null;
				trackMarkerIter = null;
				trackMarkers = null;
				image.setSide(side);
			}
		}
	}

	public int getSeekTrack() {
		return image != null ? image.getTrack() : -1;
	}
	

	/**
	 * @return
	 */
	public IdMarker getCurrentMarker() {
		return currentMarker;
	}



	/**
	 * @return
	 */
	public int writeTrack() {
		int buflen = RealDiskConsts.DSKbuffersize;  

		fdc.addBusyTime(buflen * 10 / 1000);
		return buflen;
	}

	/**
	 * @return
	 * @throws IOException 
	 */
	public int readTrack(byte[] rwBuffer) throws IOException {
		int buflen;
		
		if (image != null) {
			buflen = image.getTrackSize();
			image.readTrackData(rwBuffer, 0, buflen);
		} else {
			buflen = 0;
		}
		
		return buflen;
	}

	/**
	 * 
	 */
	public void reset() {
		if (image != null) {
			try {
				image.closeDiskImage();
			} catch (IOException e) {
				dumper.error(e.getMessage());
			}
		}
		//image = null;		
	}

	public void setHeads(boolean heads) {
		this.heads = heads;
	}

	public boolean isHeads() {
		return heads;
	}

	/**
	 * 
	 */
	public void activate() {
		// just in case the image went missing
		if (image instanceof BaseDiskImage) {
			((BaseDiskImage) image).closeIfMissing();
		}
		if (image instanceof BaseDiskImage) {
			if (((BaseDiskImage) image).getHandle() == null) {
				try {
					image.openDiskImage();
				} catch (IOException e) {
					dumper.error(e.getMessage());
				}
			}
		}
	}

	/**
	 * Tell whether the motor has spun up.
	 * @return the motorRunning
	 */
	public synchronized boolean isMotorAtTargetSpeed() {
		return spinState == SpinState.SPINNING;
	}



	/**
	 * @param on
	 */
	public synchronized void setMotor(boolean on) {
		if (on) {
			motorRequest = true;
			updateMotorSpinState();
		} else {
			motorRequest = false;
		}
		
	}



	/**
	 * @return
	 */
	public synchronized boolean isMotorRunning() {
		return spinState != SpinState.STOPPED;
	}



	/**
	 * @return
	 */
	public boolean isReadOnly() {
		return image == null || image.isReadOnly();
	}



	/**
	 * @throws IOException 
	 * 
	 */
	public void restore() throws IOException {
		currentMarker = null;
		trackMarkerIter = null;
		trackMarkers = null;
		
		if (image != null)
			image.setTrack(0);
		
	}

	public IProperty getActiveProperty() {
		return activeProperty;
	}

}
