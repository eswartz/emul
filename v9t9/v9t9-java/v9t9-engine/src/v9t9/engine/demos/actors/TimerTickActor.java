/*
  TimerTickActor.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.demos.actors;

import java.io.IOException;
import java.util.LinkedList;

import v9t9.common.cpu.ICpu;
import v9t9.common.demos.IDemoActorProvider;
import v9t9.common.demos.IDemoEvent;
import v9t9.common.demos.IDemoHandler;
import v9t9.common.demos.IDemoOutputStream;
import v9t9.common.demos.IDemoPlaybackActor;
import v9t9.common.demos.IDemoPlayer;
import v9t9.common.demos.IDemoRecorder;
import v9t9.common.demos.IDemoRecordingActor;
import v9t9.common.demos.IDemoReversePlaybackActor;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import v9t9.engine.demos.events.TimerTick;
import ejs.base.properties.IProperty;
import ejs.base.timer.FastTimer;

/**
 * @author ejs
 *
 */
public class TimerTickActor extends BaseDemoActor implements IDemoReversePlaybackActor {

	public static class Provider implements IDemoActorProvider {
		@Override
		public String getEventIdentifier() {
			return TimerTick.ID;
		}
		@Override
		public IDemoPlaybackActor createForPlayback() {
			return new TimerTickActor();
		}
		@Override
		public IDemoRecordingActor createForRecording() {
			return new TimerTickActor();
		}
		@Override
		public IDemoReversePlaybackActor createForReversePlayback() {
			return new TimerTickActor();
		}
		
	}
	
	private FastTimer timer;
	private Runnable timerTask;
	private IProperty pauseDemoSetting;
	private LinkedList<IDemoEvent> reversedEventsList;
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#getEventIdentifier()
	 */
	@Override
	public String getEventIdentifier() {
		return TimerTick.ID;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#setup(v9t9.common.machine.IMachine)
	 */
	@Override
	public void setup(IMachine machine) {
		timer = new FastTimer("Demo Timer Tick");
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#connectForRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public void connectForRecording(final IDemoRecorder recorder) throws IOException {
		pauseDemoSetting = Settings.get(recorder.getMachine(), 
				IDemoHandler.settingDemoPaused);
		
		timerTask = new Runnable() {
			
			@Override
			public void run() {
				try {
					synchronized (recorder) {
						IDemoOutputStream os = recorder.getOutputStream();
						if (os != null) {
							recorder.flushData();
						}
					}
				} catch (final Throwable t) {
					recorder.fail(t);
				}				
			}
		};
		
		timer.scheduleTask(timerTask, recorder.getOutputStream().getTimerRate());
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#flushRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public void flushRecording(IDemoRecorder recorder) throws IOException {
		if (!pauseDemoSetting.getBoolean()) {
			recorder.getOutputStream().writeEvent(
					new TimerTick(recorder.getOutputStream().getElapsedTime()));
		}

	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#disconnectFromRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public void disconnectFromRecording(IDemoRecorder recorder) {
		timer.cancelTask(timerTask);
		timer.cancel();
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#executeEvent(v9t9.common.demo.IDemoPlayer, v9t9.common.demo.IDemoEvent)
	 */
	@Override
	public void executeEvent(IDemoPlayer player, IDemoEvent event)
			throws IOException {
		// contribute time, so sound, etc. will proceed
		IMachine machine = player.getMachine();
		ICpu cpu = machine.getCpu();
		
		cpu.tick();
		int cycles = (int) (cpu.getBaseCyclesPerSec() / player.getInputStream().getTimerRate());
		
		IVdpChip vdp = machine.getVdp();
		vdp.addCpuCycles(cycles);
		vdp.tick();
		vdp.syncVdpInterrupt(machine);
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
		reversedEventsList.add(0, event);
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
