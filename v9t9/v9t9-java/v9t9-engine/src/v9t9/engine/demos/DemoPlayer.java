/*
  DemoPlayer.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.demos;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import v9t9.common.demos.IDemoEvent;
import v9t9.common.demos.IDemoHandler;
import v9t9.common.demos.IDemoHandler.IDemoListener;
import v9t9.common.demos.IDemoInputStream;
import v9t9.common.demos.IDemoPlaybackActor;
import v9t9.common.demos.IDemoPlayer;
import v9t9.common.demos.IDemoReversePlaybackActor;
import v9t9.common.events.NotifyEvent;
import v9t9.common.events.NotifyEvent.Level;
import v9t9.common.machine.IMachine;
import v9t9.engine.demos.events.TimerTick;
import ejs.base.properties.IProperty;
import ejs.base.utils.ListenerList;
import ejs.base.utils.ListenerList.IFire;

/**
 * @author ejs
 *
 */
public class DemoPlayer implements IDemoPlayer {

	static abstract class DemoEventEntry {
		final IDemoEvent event;
		final long clock;
		
		public DemoEventEntry(IDemoEvent event, long elapsedTime) {
			this.event = event;
			clock = elapsedTime;
		}
	}
	static class DemoPlayEventEntry extends DemoEventEntry {
		public DemoPlayEventEntry(IDemoEvent event, long elapsedTime) {
			super(event, elapsedTime);
		}
	}
	static class DemoReverseEventEntry extends DemoEventEntry {
		public DemoReverseEventEntry(IDemoEvent event, long elapsedTime) {
			super(event, elapsedTime);
		}
	}
	
	private final IDemoInputStream is;
	private List<DemoEventEntry> demoEvents = new ArrayList<DemoPlayer.DemoEventEntry>();
	private int demoCursor;
	
	private IProperty pauseSetting;
	private IProperty reverseSetting;
	private IProperty playSetting;
	
	private final IMachine machine;
	private boolean isFinished;

	private long elapsedTime;
	private final int timerRate;
	private double playClock;
	private double playStepMs;
	private double rateMultiplier;
	
	private final ListenerList<IDemoListener> listeners;
	private Map<String, IDemoPlaybackActor> eventToActorMap = new HashMap<String, IDemoPlaybackActor>();
	private Runnable demoTask;
	private IDemoPlaybackActor[] playActors;
	
	private Map<String, IDemoReversePlaybackActor> eventToReverseActorMap = new HashMap<String, IDemoReversePlaybackActor>();
	private IDemoReversePlaybackActor[] reverseActors;
	private URI uri;
	private double totalTime = -1;
	private IProperty rateSetting;
	

	public DemoPlayer(IMachine machine, URI uri, IDemoInputStream is,
			ListenerList<IDemoListener> listeners) throws IOException {
		this.machine = machine;
		this.uri = uri;
		this.is = is;
		this.timerRate = is.getTimerRate();
		this.listeners = listeners;
		
		playSetting = machine.getSettings().get(IDemoHandler.settingPlayingDemo);
		pauseSetting = machine.getSettings().get(IDemoHandler.settingDemoPaused);
		reverseSetting = machine.getSettings().get(IDemoHandler.settingDemoReversing);
		rateSetting = machine.getSettings().get(IDemoHandler.settingDemoPlaybackRate);
		
		setPlaybackRate(1.0);
		
		playActors = machine.getDemoManager().createPlaybackActors();
		for (IDemoPlaybackActor actor : playActors) {
			eventToActorMap.put(actor.getEventIdentifier(), actor);
			actor.setup(machine);
		}
		

		reverseActors = machine.getDemoManager().createReversePlaybackActors();
		for (IDemoReversePlaybackActor actor : reverseActors) {
			eventToReverseActorMap.put(actor.getEventIdentifier(), actor);
			actor.setup(machine);
		}
		
	}

	public synchronized void start() {
		if (demoTask != null) {
			stop();
		}
		
		totalTime = -1;
		
//		Settings.get(machine, IVdpChip.settingDumpVdpAccess).setBoolean(true);
//		Settings.get(machine, ICpu.settingDumpFullInstructions).setBoolean(true);
		
		demoTask = new Runnable() {
			public void run() {
				stepDemo();
			}
		};
		
		for (IDemoPlaybackActor actor : playActors) {
			actor.setupPlayback(this);
		}
		for (IDemoReversePlaybackActor actor : reverseActors) {
			actor.setupReversePlayback(this);
		}
		
		//reverseSetting.setBoolean(false);
		if (reverseSetting.getBoolean()) {
			double origRate = rateSetting.getDouble();
			rateSetting.setDouble(1000000);
			try {
				try {
					playForwardEvents(getTotalTime());
				} catch (IOException e) {
					failed(e);
				}
			} finally {
				rateSetting.setDouble(origRate);
				pauseSetting.setBoolean(false);
			}
		}
		
		machine.getFastMachineTimer().scheduleTask(demoTask, timerRate);
	}

	/**
	 * 
	 */
	protected synchronized void stepDemo() {
		if (isFinished)
			return;
		
		if (!pauseSetting.getBoolean()) {
			try {
				if (!reverseSetting.getBoolean()) {
					playClock = playForwardEvents(playClock + playStepMs);
				} else {
					playClock = playReverseEvents(playClock - playStepMs);
				}
				

				for (Object l : listeners.toArray()) {
					if (l instanceof IDemoHandler.IDemoPlaybackListener) {
						((IDemoHandler.IDemoPlaybackListener) l).updatedPosition(playClock);
					}
				}
				
			} catch (final IOException e) {
				failed(e);

			}
		}

	}

	/**
	 * @param e
	 */
	private void failed(final IOException e) {
		isFinished = true;
		
		e.printStackTrace();
		
		listeners.fire(new IFire<IDemoHandler.IDemoListener>() {

			@Override
			public void fire(IDemoListener listener) {
				listener.firedEvent(new NotifyEvent(System.currentTimeMillis(), null, 
							Level.ERROR, e.getMessage()));
			}
		});
		
	}

	/**
	 * @param multiplier the rate, as a multiplier (1.0 = normal, 0.5 = half)
	 */
	public void setPlaybackRate(double multiplier) {
		this.rateMultiplier = multiplier;
		playStepMs = (1000. * rateMultiplier / is.getTimerRate());
	}
	
	/**
	 * @return the rate, as a multiplier (1.0 = normal, 0.5 = half)
	 */
	public double getPlaybackRate() {
		return rateMultiplier;
	}


	/**
	 * @return
	 */
	protected synchronized double playForwardEvents(double target) throws IOException {
		IDemoEvent event;

		if (isFinished)
			return getTotalTime();

	
		while (true) {
		
			
			DemoEventEntry entry;
			
			int origCursor = demoCursor;
			
			if (demoCursor < demoEvents.size()) {
				entry = demoEvents.get(demoCursor++);
				if (entry instanceof DemoReverseEventEntry) {
					continue;
				}
				event = entry.event;
				elapsedTime = entry.clock;
			} else {
				event = is.readNext();
				if (event != null) {
					entry = new DemoPlayEventEntry(event, is.getElapsedTime());
					demoEvents.add(entry);
					demoCursor++;
				}
			}
			
			if (event == null) {
				pauseSetting.setBoolean(true);
				
				listeners.fire(new IFire<IDemoHandler.IDemoListener>() {
	
					@Override
					public void fire(IDemoListener listener) {
						listener.firedEvent(new NotifyEvent(System.currentTimeMillis(), null, 
								Level.INFO, "Demo playback finished"));
					}
				});
				return getTotalTime();
			}

			boolean atEnd = demoCursor == demoEvents.size();


			if (atEnd) {
				IDemoReversePlaybackActor revActor = eventToReverseActorMap.get(event.getIdentifier());
				if (revActor != null)
					revActor.queueEventForReversing(this, event);
				
				flushReverseEvents();
			}
			
			if (event instanceof TimerTick) {
				// synchronize with next instant in virtual clock 
				long elapsed = ((TimerTick) event).getElapsedTime();
				
				// stop short of moving the clock if limit is reached
				if (elapsed >= target) {
					demoCursor = origCursor;
					return target;
				}
				
				
				elapsedTime = elapsed;

			}
			
			executeEvent(event);
		}
		
	}


	/**
	 * @throws IOException 
	 * 
	 */
	private void flushReverseEvents() throws IOException {
		// flush reversers for all events that preceded the tick
		if (reverseActors == null)
			return;
		
		for (IDemoReversePlaybackActor actor : reverseActors) {
			IDemoEvent[] evs = actor.emitReversedEvents(this);
			for (IDemoEvent ev : evs) {
				if (ev != null) {
					demoEvents.add(new DemoReverseEventEntry(ev, elapsedTime));
					demoCursor++;
				}
			}
		}
		
	}

	protected synchronized double playReverseEvents(double target) throws IOException {
		IDemoEvent event;
		
		flushReverseEvents();

		while (true) {
			if (demoCursor > 0) {
				DemoEventEntry entry = demoEvents.get(--demoCursor);
				if (entry instanceof DemoPlayEventEntry) {
					continue;
				}
				
				if (entry.clock < target) {
					demoCursor++;
					return target;
				}
					
				event = entry.event;
				
				elapsedTime = entry.clock;
				
			} else {
				reverseSetting.setBoolean(false);
				pauseSetting.setBoolean(true);
				return 0;
			}
			
			executeEvent(event);
			
			if (event instanceof TimerTick) {
				// synchronize with virtual clock
				long elapsed = ((TimerTick) event).getElapsedTime();
				elapsedTime = elapsed;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoPlayer#executeEvent(v9t9.common.demo.IDemoEvent)
	 */
	@Override
	public void executeEvent(IDemoEvent event) throws IOException {
		IDemoPlaybackActor actor = eventToActorMap.get(event.getIdentifier());
		if (actor != null)
			actor.executeEvent(this, event);
	}
	
	public synchronized void stop() {
		demoEvents.clear();
		demoCursor = 0;
		
		playSetting.setBoolean(false);
		
		machine.getFastMachineTimer().cancelTask(demoTask);
		demoTask = null;
		
		for (IDemoReversePlaybackActor actor : reverseActors) {
			actor.cleanupReversePlayback(this);
		}
		reverseActors = null;
		
		for (IDemoPlaybackActor actor : playActors) {
			actor.cleanupPlayback(this);
		}

		try {
			is.close();
		} catch (IOException e) {
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoPlayer#getMachine()
	 */
	@Override
	public IMachine getMachine() {
		return machine;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoPlayer#getInputStream()
	 */
	@Override
	public IDemoInputStream getInputStream() {
		return is;
	}


	/**
	 * Get the total time the demo consumes
	 * @param uri
	 * @return 0 for unknown or time in ms
	 */
	
	protected long calculateTotalTime(URI uri) {
		IDemoInputStream is = null;
		try {
			long time = -1;
			is = machine.getDemoManager().createDemoReader(uri);
			IDemoEvent ev;
			while ((ev = is.readNext()) != null) {
				if (ev instanceof TimerTick) {
					time = ((TimerTick) ev).getElapsedTime();
				}
			}
			return time;
		} catch (Throwable t) {
			return 0;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
			
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoPlayer#getTotalTime()
	 */
	@Override
	public double getTotalTime() {
		if (totalTime < 0) {
			totalTime = calculateTotalTime(uri);
		}
		return totalTime;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoPlayer#seekToTime(long)
	 */
	@Override
	public synchronized double seekToTime(double time) throws IOException {
		if (time > playClock) {
			playForwardEvents(time);
		}
		else if (time < playClock) {
			playReverseEvents(time);
		}
		playClock = elapsedTime;
		return playClock;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoPlayer#getCurrentTime()
	 */
	@Override
	public synchronized double getCurrentTime() {
		return playClock;
	}
}
