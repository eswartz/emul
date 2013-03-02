/*
  VdpRegisterDemoActor.java

  (c) 2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.engine.demos.actors;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;

import v9t9.common.demos.IDemoActorProvider;
import v9t9.common.demos.IDemoEvent;
import v9t9.common.demos.IDemoPlaybackActor;
import v9t9.common.demos.IDemoPlayer;
import v9t9.common.demos.IDemoRecorder;
import v9t9.common.demos.IDemoRecordingActor;
import v9t9.common.demos.IDemoReversePlaybackActor;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess;
import v9t9.common.machine.SimpleRegisterWriteTracker;
import v9t9.engine.demos.events.VideoWriteRegisterEvent;

/**
 * @author ejs
 *
 */
public class VdpRegisterDemoActor extends BaseDemoActor implements IDemoReversePlaybackActor {
	public static class Provider implements IDemoActorProvider {
		@Override
		public String getEventIdentifier() {
			return VideoWriteRegisterEvent.ID;
		}
		@Override
		public IDemoPlaybackActor createForPlayback() {
			return new VdpRegisterDemoActor();
		}
		@Override
		public IDemoRecordingActor createForRecording() {
			return new VdpRegisterDemoActor();
		}
		@Override
		public IDemoReversePlaybackActor createForReversePlayback() {
			return new VdpRegisterDemoActor();
		}
		
	}

	private IVdpChip vdp;
	private SimpleRegisterWriteTracker vdpRegisterListener;
	private LinkedList<IDemoEvent> reversedEventsList;

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#getEventIdentifier()
	 */
	@Override
	public String getEventIdentifier() {
		return VideoWriteRegisterEvent.ID;
	}
	
	@Override
	public void setup(IMachine machine) {
		vdp = machine.getVdp();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#connectForRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public void connectForRecording(IDemoRecorder recorder) throws IOException {
		vdpRegisterListener = new SimpleRegisterWriteTracker(vdp,
				vdp.getFirstRegister(),
				vdp.getRecordableRegs());
		vdpRegisterListener.addRegisterListener();		
		

		// send video regs
		IRegisterAccess vra = vdp;
		int lastReg = vra.getFirstRegister() + vra.getRegisterCount();
		for (int i = vra.getFirstRegister(); i < lastReg; i++) {
			recorder.getOutputStream().writeEvent(
					new VideoWriteRegisterEvent(i, vra.getRegister(i)));
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#flushRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public void flushRecording(IDemoRecorder recorder) throws IOException {
		synchronized (vdpRegisterListener) {
			Map<Integer, Integer> changes = vdpRegisterListener.getChanges();
			synchronized (changes) {
				for (Map.Entry<Integer, Integer> chg : changes.entrySet()) {
					if (chg.getKey() >= 0) {
						recorder.getOutputStream().writeEvent(
								new VideoWriteRegisterEvent(chg.getKey(), chg.getValue()));
					}
				}
				vdpRegisterListener.clearChanges();
			}
		}		
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#disconnectFromRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public void disconnectFromRecording(IDemoRecorder recorder) {
		vdpRegisterListener.removeRegisterListener();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#executeEvent(v9t9.common.demo.IDemoPlayer, v9t9.common.demo.IDemoEvent)
	 */
	@Override
	public void executeEvent(IDemoPlayer player, IDemoEvent event)
			throws IOException {
		VideoWriteRegisterEvent ev = (VideoWriteRegisterEvent) event;
		vdp.setRegister(ev.getReg(), ev.getVal());		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoReversePlaybackActor#setupReversePlayback(v9t9.common.demos.IDemoPlayer)
	 */
	@Override
	public void setupReversePlayback(IDemoPlayer player) {
		reversedEventsList = new LinkedList<IDemoEvent>();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoReversePlaybackActor#queueEventForReversing(v9t9.common.demos.IDemoPlayer, v9t9.common.demos.IDemoEvent)
	 */
	@Override
	public void queueEventForReversing(IDemoPlayer player, IDemoEvent event)
			throws IOException {
		VideoWriteRegisterEvent ev = (VideoWriteRegisterEvent) event;
		VideoWriteRegisterEvent newEvent = new VideoWriteRegisterEvent(ev.getReg(), 
				vdp.getRegister(ev.getReg()));
		reversedEventsList.add(0, newEvent);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoReversePlaybackActor#emitReversedEvents(v9t9.common.demos.IDemoPlayer)
	 */
	@Override
	public IDemoEvent[] emitReversedEvents(IDemoPlayer player)
			throws IOException {
		IDemoEvent[] evs = (IDemoEvent[]) reversedEventsList.toArray(new IDemoEvent[reversedEventsList.size()]);
		reversedEventsList.clear();
		return evs;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoReversePlaybackActor#cleanupReversePlayback(v9t9.common.demos.IDemoPlayer)
	 */
	@Override
	public void cleanupReversePlayback(IDemoPlayer player) {
		reversedEventsList = null;
	}

}
