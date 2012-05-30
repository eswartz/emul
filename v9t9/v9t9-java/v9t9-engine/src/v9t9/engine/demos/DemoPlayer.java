/**
 * 
 */
package v9t9.engine.demos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import v9t9.common.demos.IDemoEvent;
import v9t9.common.demos.IDemoHandler;
import v9t9.common.demos.IDemoInputStream;
import v9t9.common.demos.IDemoPlaybackActor;
import v9t9.common.demos.IDemoPlayer;
import v9t9.common.demos.IDemoHandler.IDemoListener;
import v9t9.common.demos.IDemoReversePlaybackActor;
import v9t9.common.events.IEventNotifier.Level;
import v9t9.common.events.NotifyEvent;
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
		IDemoEvent event;
		long clock;
		
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
	

	public DemoPlayer(IMachine machine, IDemoInputStream is,
			ListenerList<IDemoListener> listeners) {
		this.machine = machine;
		this.is = is;
		this.timerRate = is.getTimerRate();
		this.listeners = listeners;
		
		playSetting = machine.getSettings().get(IDemoHandler.settingPlayingDemo);
		pauseSetting = machine.getSettings().get(IDemoHandler.settingDemoPaused);
		reverseSetting = machine.getSettings().get(IDemoHandler.settingDemoReversing);
		
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

	public void start() {
		if (demoTask != null) {
			stop();
		}
		
//		Settings.get(machine, IVdpChip.settingDumpVdpAccess).setBoolean(true);
//		Settings.get(machine, ICpu.settingDumpFullInstructions).setBoolean(true);
		
		demoTask = new Runnable() {
			public void run() {
				if (isFinished)
					return;
				
				if (!pauseSetting.getBoolean()) {
					if (!reverseSetting.getBoolean()) {
						playClock += playStepMs;
						stepDemoForward();
					} else {
						playClock -= playStepMs;
						stepDemoBackward();
					}
				}
			}
		};
		
		for (IDemoPlaybackActor actor : playActors) {
			actor.setupPlayback(this);
		}
		for (IDemoReversePlaybackActor actor : reverseActors) {
			actor.setupReversePlayback(this);
		}
		
		reverseSetting.setBoolean(false);
		
		machine.getFastMachineTimer().scheduleTask(demoTask, timerRate);
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
	protected void stepDemoForward() {
		if (isFinished)
			return;
		
		try {
			playForwardEvents();
		} catch (final IOException e) {
			isFinished = true;
			
			e.printStackTrace();
			
			listeners.fire(new IFire<IDemoHandler.IDemoListener>() {

				@Override
				public void fire(IDemoListener listener) {
					listener.stopped(new NotifyEvent(System.currentTimeMillis(), null, 
								Level.ERROR, e.getMessage()));
				}
			});

		}
	
//		if (isFinished) {
//			listeners.fire(new IFire<IDemoHandler.IDemoListener>() {
//
//				@Override
//				public void fire(IDemoListener listener) {
//					listener.stopped(new NotifyEvent(System.currentTimeMillis(), null, 
//							Level.INFO, "Demo playback finished"));
//				}
//			});
//		}
	}
	
	protected void playForwardEvents() throws IOException {
		IDemoEvent event;
		
		if (playClock < elapsedTime)
			return;
		
		while (true) {
			if (demoCursor < demoEvents.size()) {
				DemoEventEntry entry = demoEvents.get(demoCursor++);
				if (entry instanceof DemoReverseEventEntry)
					continue;
				event = entry.event;
				elapsedTime = entry.clock;
			} else {
				event = is.readNext();
				if (event != null) {
					demoEvents.add(new DemoPlayEventEntry(event, is.getElapsedTime()));
					demoCursor++;
				}
			}
			
			if (event == null) {
				pauseSetting.setBoolean(true);
				
				listeners.fire(new IFire<IDemoHandler.IDemoListener>() {
	
					@Override
					public void fire(IDemoListener listener) {
						listener.stopped(new NotifyEvent(System.currentTimeMillis(), null, 
								Level.INFO, "Demo playback finished"));
					}
				});
				break;
			}

			IDemoReversePlaybackActor revActor = eventToReverseActorMap.get(event.getIdentifier());
			if (revActor != null)
				revActor.queueEventForReversing(this, event);
			
			executeEvent(event);
			
			if (event instanceof TimerTick) {
				// synchronize with virtual clock
				long elapsed = ((TimerTick) event).getElapsedTime();
				elapsedTime = elapsed;
				
				if (demoCursor == demoEvents.size()) {
					// flush reversers
					for (IDemoReversePlaybackActor actor : reverseActors) {
						IDemoEvent[] evs = actor.emitReversedEvents(this);
						for (IDemoEvent ev : evs) {
							demoEvents.add(new DemoReverseEventEntry(ev, elapsedTime));
							demoCursor++;
						}
					}
				}
				
				if (elapsed >= playClock) {
					break;
				}
			}
		}
		
	}


	/**
	 * @return
	 */
	protected void stepDemoBackward() {
		try {
			playReverseEvents();
		} catch (final IOException e) {
			isFinished = true;
			
			e.printStackTrace();
			
			listeners.fire(new IFire<IDemoHandler.IDemoListener>() {

				@Override
				public void fire(IDemoListener listener) {
					listener.stopped(new NotifyEvent(System.currentTimeMillis(), null, 
								Level.ERROR, e.getMessage()));
				}
			});

		}
	}
	

	protected void playReverseEvents() throws IOException {
		IDemoEvent event;
		
		if (playClock > elapsedTime)
			return;
		
		while (true) {
			if (demoCursor > 0) {
				DemoEventEntry entry = demoEvents.get(--demoCursor);
				if (entry instanceof DemoPlayEventEntry)
					continue;
				event = entry.event;
				elapsedTime = entry.clock;
			} else {
				reverseSetting.setBoolean(false);
				pauseSetting.setBoolean(true);
				return;
			}
			
			executeEvent(event);
			
			if (event instanceof TimerTick) {
				// synchronize with virtual clock
				long elapsed = ((TimerTick) event).getElapsedTime();
				elapsedTime = elapsed;
				if (elapsed < playClock) {
					break;
				}
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
	
	public void stop() {
		playSetting.setBoolean(false);
		
		machine.getFastMachineTimer().cancelTask(demoTask);
		demoTask = null;
		
		for (IDemoReversePlaybackActor actor : reverseActors) {
			actor.cleanupReversePlayback(this);
		}
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

}
