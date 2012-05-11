/**
 * 
 */
package v9t9.server.demo;

import java.io.IOException;

import ejs.base.properties.IProperty;
import ejs.base.timer.FastTimer;
import ejs.base.utils.ListenerList;
import ejs.base.utils.ListenerList.IFire;

import v9t9.common.demo.IDemoEvent;
import v9t9.common.demo.IDemoHandler;
import v9t9.common.demo.IDemoHandler.IDemoListener;
import v9t9.common.demo.IDemoInputStream;
import v9t9.common.events.IEventNotifier.Level;
import v9t9.common.events.NotifyEvent;
import v9t9.common.events.NotifyException;
import v9t9.common.machine.IMachine;
import v9t9.server.demo.events.TimerTick;

/**
 * @author ejs
 *
 */
public class DemoPlayer {

	private final IDemoInputStream is;
	private final IMachine machine;
	private FastTimer timer;
	private IProperty pauseSetting;
	private final ListenerList<IDemoListener> listeners;

	public DemoPlayer(IMachine machine, IDemoInputStream is, ListenerList<IDemoListener> listeners) {
		this.machine = machine;
		this.is = is;
		this.listeners = listeners;
		pauseSetting = machine.getSettings().get(IDemoHandler.settingDemoPaused);
	}

	public void start() {
		timer = new FastTimer("DemoPlayer");
		timer.scheduleTask(new Runnable() {
			
			@Override
			public void run() {
				if (!pauseSetting.getBoolean()) {
					try {
						processEvents();
					} catch (final NotifyException e) {
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
		}, 60);
	}
	
	protected void processEvents() throws NotifyException {
		IDemoEvent event;
		while (true) {
			event = is.readNext();
			
			if (event == null) {
				throw new NotifyException(new NotifyEvent(System.currentTimeMillis(), null, 
						Level.INFO, "Demo playback finished"));
			}
			
			event.execute(machine);
			
			if (event instanceof TimerTick)
				break;
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
