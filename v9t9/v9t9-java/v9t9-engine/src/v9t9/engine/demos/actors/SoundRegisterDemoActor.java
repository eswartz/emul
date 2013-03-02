/*
  SoundRegisterDemoActor.java

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
import java.util.List;

import v9t9.common.demos.IDemoActorProvider;
import v9t9.common.demos.IDemoEvent;
import v9t9.common.demos.IDemoPlaybackActor;
import v9t9.common.demos.IDemoPlayer;
import v9t9.common.demos.IDemoRecorder;
import v9t9.common.demos.IDemoRecordingActor;
import v9t9.common.demos.IDemoReversePlaybackActor;
import v9t9.common.hardware.ISoundChip;
import v9t9.common.machine.FullRegisterWriteTracker;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess;
import v9t9.engine.demos.events.SoundWriteRegisterEvent;

/**
 * @author ejs
 *
 */
public class SoundRegisterDemoActor extends BaseDemoActor implements IDemoReversePlaybackActor {
	public static class Provider implements IDemoActorProvider {
		@Override
		public String getEventIdentifier() {
			return SoundWriteRegisterEvent.ID;
		}
		@Override
		public IDemoPlaybackActor createForPlayback() {
			return new SoundRegisterDemoActor();
		}
		@Override
		public IDemoRecordingActor createForRecording() {
			return new SoundRegisterDemoActor();
		}
		@Override
		public IDemoReversePlaybackActor createForReversePlayback() {
			return new SoundRegisterDemoActor();
		}
		
	}
	
	private FullRegisterWriteTracker soundRegisterListener;
	private ISoundChip sound;
	private LinkedList<IDemoEvent> reversedEventsList;
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#getEventIdentifier()
	 */
	@Override
	public String getEventIdentifier() {
		return SoundWriteRegisterEvent.ID;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#setup(v9t9.common.machine.IMachine)
	 */
	@Override
	public void setup(IMachine machine) {
		sound = machine.getSound();		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#connectForRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public void connectForRecording(IDemoRecorder recorder) throws IOException {
		soundRegisterListener = new FullRegisterWriteTracker(sound);
		soundRegisterListener.addRegisterListener();
		
		// send sound regs 
		IRegisterAccess sra = sound;
		int slastReg = sra.getFirstRegister() + sra.getRegisterCount();
		for (int i = sra.getFirstRegister(); i < slastReg; i++) {
			recorder.getOutputStream().writeEvent(
					new SoundWriteRegisterEvent(i, sra.getRegister(i)));
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#flushRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public void flushRecording(IDemoRecorder recorder) throws IOException {
		synchronized (soundRegisterListener) {
			List<Long> changes = soundRegisterListener.getChanges();
			synchronized (changes) {
				for (Long ent : changes) {
					recorder.getOutputStream().writeEvent(
							new SoundWriteRegisterEvent(
									(int) (ent >> 32), (int) (ent & 0xffffffff)));
				}
			}
			soundRegisterListener.clearChanges();
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#disconnectFromRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public void disconnectFromRecording(IDemoRecorder recorder) {
		soundRegisterListener.removeRegisterListener();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#executeEvent(v9t9.common.demo.IDemoPlayer, v9t9.common.demo.IDemoEvent)
	 */
	@Override
	public void executeEvent(IDemoPlayer player, IDemoEvent event)
			throws IOException {
		SoundWriteRegisterEvent ev = (SoundWriteRegisterEvent) event;
		sound.setRegister(ev.getReg(), ev.getVal());
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.demos.actors.BaseDemoActor#cleanupPlayback(v9t9.common.demo.IDemoPlayer)
	 */
	@Override
	public void cleanupPlayback(IDemoPlayer player) {
		super.cleanupPlayback(player);
		
		player.getMachine().getSound().reset();
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
		SoundWriteRegisterEvent ev = (SoundWriteRegisterEvent) event;
		reversedEventsList.add(0, new SoundWriteRegisterEvent(ev.getReg(), 
				sound.getRegister(ev.getReg())));
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
		reversedEventsList.clear();
	}

}
