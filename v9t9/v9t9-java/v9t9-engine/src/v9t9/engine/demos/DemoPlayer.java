/**
 * 
 */
package v9t9.engine.demos;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import v9t9.common.demos.IDemoActor;
import v9t9.common.demos.IDemoEvent;
import v9t9.common.demos.IDemoHandler;
import v9t9.common.demos.IDemoInputStream;
import v9t9.common.demos.IDemoPlayer;
import v9t9.common.demos.IDemoHandler.IDemoListener;
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

	private final IDemoInputStream is;
	private final IMachine machine;
	private boolean isFinished;
	private IProperty pauseSetting;
	private final ListenerList<IDemoListener> listeners;
	private final int timerRate;
	private double playClock;
	private double playStepMs;
	private double rateMultiplier;
	private Map<String, IDemoActor> eventToActorMap = new HashMap<String, IDemoActor>();
	private Runnable demoTask;

	public DemoPlayer(IMachine machine, IDemoInputStream is,
			ListenerList<IDemoListener> listeners) {
		this.machine = machine;
		this.is = is;
		this.timerRate = is.getTimerRate();
		this.listeners = listeners;
		
		pauseSetting = machine.getSettings().get(IDemoHandler.settingDemoPaused);
		
		setPlaybackRate(1.0);
		
		for (IDemoActor actor : machine.getDemoManager().getActors()) {
			eventToActorMap.put(actor.getEventIdentifier(), actor);
			actor.setup(machine);
		}
	}

	public void start() {
//		Settings.get(machine, IVdpChip.settingDumpVdpAccess).setBoolean(true);
//		Settings.get(machine, ICpu.settingDumpFullInstructions).setBoolean(true);
					
		demoTask = new Runnable() {
			public void run() {
				if (isFinished)
					return;
				
				if (!pauseSetting.getBoolean()) {
					playClock += playStepMs;
					stepDemo();
				}
			}
		};
		machine.getFastMachineTimer().scheduleTask(demoTask, timerRate);
		
		for (IDemoActor actor : machine.getDemoManager().getActors()) {
			actor.setupPlayback(this);
		}
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
	protected void stepDemo() {
		if (isFinished)
			return;
		
		try {
			processEvents();
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
	
		if (isFinished) {
			stop();
			listeners.fire(new IFire<IDemoHandler.IDemoListener>() {

				@Override
				public void fire(IDemoListener listener) {
					listener.stopped(new NotifyEvent(System.currentTimeMillis(), null, 
							Level.INFO, "Demo playback finished"));
				}
			});
		}
	}
	
	protected void processEvents() throws IOException {
		IDemoEvent event;
		
		if (playClock < is.getElapsedTime())
			return;
		
		while (true) {
			event = is.readNext();
			
			if (event == null) {
				isFinished = true;
				break;
			}
			
			executeEvent(event);
			
			if (event instanceof TimerTick) {
				// synchronize with virtual clock
				long elapsed = ((TimerTick) event).getElapsedTime();
				if (elapsed >= playClock) {
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
		IDemoActor actor = eventToActorMap.get(event.getIdentifier());
		if (actor != null)
			actor.executeEvent(this, event);
	}
	
	public void stop() {
		machine.getFastMachineTimer().cancelTask(demoTask);
		
		for (IDemoActor actor : machine.getDemoManager().getActors()) {
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
