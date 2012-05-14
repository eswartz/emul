/**
 * 
 */
package v9t9.engine.demos;

import java.io.IOException;

import v9t9.common.demo.IDemoEvent;
import v9t9.common.demo.IDemoHandler;
import v9t9.common.demo.IDemoHandler.IDemoListener;
import v9t9.common.demo.IDemoInputStream;
import v9t9.common.events.IEventNotifier.Level;
import v9t9.common.events.NotifyEvent;
import v9t9.common.events.NotifyException;
import v9t9.common.machine.IMachine;
import v9t9.engine.demos.events.TimerTick;
import ejs.base.properties.IProperty;
import ejs.base.timer.FastTimer;
import ejs.base.utils.ListenerList;
import ejs.base.utils.ListenerList.IFire;

/**
 * @author ejs
 *
 */
public class DemoPlayer {

	private final IDemoInputStream is;
	private final IMachine machine;
	private boolean isFinished;
	private FastTimer timer;
	private IProperty pauseSetting;
	private final ListenerList<IDemoListener> listeners;
	private final int timerRate;
	private double playClock;
	private double playStepMs;
	private double rateMultiplier;

	public DemoPlayer(IMachine machine, IDemoInputStream is,
			ListenerList<IDemoListener> listeners) {
		this.machine = machine;
		this.is = is;
		this.timerRate = is.getTimerRate();
		this.listeners = listeners;
		
		pauseSetting = machine.getSettings().get(IDemoHandler.settingDemoPaused);
		
		setPlaybackRate(1.0);
	}

	public void start() {
		timer = new FastTimer("DemoPlayer");
		timer.scheduleTask(new Runnable() {
			public void run() {
				if (isFinished)
					return;
				
				playClock += playStepMs;
				stepDemo();
			}
		}, timerRate);
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
		if (!pauseSetting.getBoolean()) {
			try {
				processEvents();
			} catch (final NotifyException e) {
				isFinished = true;
				
				if (e.getEvent().level == Level.ERROR)
					e.printStackTrace();
				
				stop();
				listeners.fire(new IFire<IDemoHandler.IDemoListener>() {

					@Override
					public void fire(IDemoListener listener) {
						listener.stopped(e.getEvent());
					}
				});
			}
		}
	}
	
	protected void processEvents() throws NotifyException {
		IDemoEvent event;
		
		if (playClock < is.getElapsedTime())
			return;
		
		while (true) {
			event = is.readNext();
			
			if (event == null) {
				isFinished = true;
				throw new NotifyException(new NotifyEvent(System.currentTimeMillis(), null, 
						Level.INFO, "Demo playback finished"));
			}
			
			event.execute(machine);
			
			if (event instanceof TimerTick) {
				// synchronize with virtual clock
				long elapsed = ((TimerTick) event).getElapsedTime();
				if (elapsed >= playClock) {
					break;
				}
			}
		}
		
	}

	public void stop() {
		timer.cancel();
		
		machine.getSound().reset();
		machine.getSpeech().reset();
		
		try {
			is.close();
		} catch (IOException e) {
		}
	}

}
