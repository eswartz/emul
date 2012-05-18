/**
 * 
 */
package v9t9.engine.demos;

import java.io.IOException;
import java.net.URI;

import v9t9.common.demos.IDemoHandler;
import v9t9.common.demos.IDemoInputStream;
import v9t9.common.demos.IDemoOutputStream;
import v9t9.common.events.NotifyEvent;
import v9t9.common.events.NotifyException;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.utils.ListenerList;

/**
 * Stock implementation of demo handler.
 * @author ejs
 *
 */
public class DemoHandler implements IDemoHandler {

	private URI lastRecordingURI;
	private URI lastPlaybackURI;
	private DemoRecorder recorder;
	private DemoPlayer player;
	private IMachine machine;
	
	private ListenerList<IDemoListener> listeners = new ListenerList<IDemoHandler.IDemoListener>();
	private IProperty demoPauseSetting;
	private IProperty recordSetting;
	private IProperty playSetting;
	private IProperty playRateSetting;
	private IDemoListener demoListener;
	private IPropertyListener playbackRatePropertyListener;
	private boolean wasPaused;

	/**
	 * 
	 */
	public DemoHandler(IMachine machine_) {
		this.machine = machine_;
		
		demoPauseSetting = Settings.get(machine, IDemoHandler.settingDemoPaused);
		recordSetting = Settings.get(machine, IDemoHandler.settingRecordDemo);
		playSetting = Settings.get(machine, IDemoHandler.settingPlayingDemo);
		playRateSetting = Settings.get(machine, IDemoHandler.settingDemoPlaybackRate);
		
		demoListener = new IDemoListener() {
			
			@Override
			public void stopped(NotifyEvent event) {
				try {
					machine.getDemoHandler().stopPlayback();
					machine.getDemoHandler().stopRecording();
				} catch (NotifyException ex) {
					//machine.getEventNotifier().notifyEvent(ex.getEvent());
				}
				machine.getEventNotifier().notifyEvent(event);
			}
		};
			
		listeners.add(demoListener);
	}
	
	public void dispose() {
		try {
			stopRecording();
			stopPlayback();
		} catch (NotifyException e) {
			
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.client.IDemoHandler#startRecording(java.net.URI)
	 */
	@Override
	public synchronized void startRecording(URI uri) throws NotifyException {
		if (recorder != null) {
			stopRecording();
		}
		
		lastRecordingURI = uri;
		try {
			IDemoOutputStream writer;
			
			writer = machine.getDemoManager().createDemoWriter(uri);
			
			recorder = new DemoRecorder(machine, writer, listeners);
			
			recordSetting.setBoolean(true);
			demoPauseSetting.setBoolean(false);
		} catch (Throwable e) {
			throw new NotifyException(uri, "Failed to create demo " + uri, e);
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.IDemoHandler#stopRecording()
	 */
	@Override
	public synchronized void stopRecording() throws NotifyException {
		recordSetting.setBoolean(false);
		demoPauseSetting.setBoolean(false);
		
		if (recorder == null)
			return;
		
		try {
			recorder.stop();
		} catch (Throwable e) {
			throw new NotifyException(lastRecordingURI, "Failed to finish writing demo " + lastRecordingURI, e);
		}
		
		recorder = null;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.client.IDemoHandler#getRecordingURI()
	 */
	@Override
	public URI getRecordingURI() {
		return lastRecordingURI;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.IDemoHandler#startPlayback(java.net.URI)
	 */
	@Override
	public synchronized void startPlayback(URI uri) throws NotifyException {
		if (player != null) {
			stopPlayback();
		}
		
		lastPlaybackURI = uri;
		try {
			IDemoInputStream is = machine.getDemoManager().createDemoReader(uri);
			if (is != null) {
				player = new DemoPlayer(machine, is, listeners);
				
				playbackRatePropertyListener = new IPropertyListener() {
					
					@Override
					public void propertyChanged(IProperty property) {
						player.setPlaybackRate((Double) property.getValue());
					}
				};
				playRateSetting.addListenerAndFire(playbackRatePropertyListener);
				
				playSetting.setBoolean(true);
				demoPauseSetting.setBoolean(false);
				
				// note: must follow the above
				wasPaused = machine.setPaused(true);
				
				player.start();
			} else {
				throw new NotifyException(uri, "Unrecognized demo format in " + uri);
			}
		} catch (IOException e) {
			throw new NotifyException(uri, "Failed to open demo " + uri, e);
		}
		
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.IDemoHandler#stopPlayback()
	 */
	@Override
	public synchronized void stopPlayback() throws NotifyException {
		if (player == null)
			return;
		
		player.stop();
		
		playRateSetting.removeListener(playbackRatePropertyListener);

		machine.setPaused(wasPaused);
		
		// note: must follow the above
		playSetting.setBoolean(false);
		demoPauseSetting.setBoolean(false);

		player = null;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.client.IDemoHandler#getPlaybackURI()
	 */
	@Override
	public URI getPlaybackURI() {
		return lastPlaybackURI;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoHandler#addListener(v9t9.common.demo.IDemoHandler.IDemoListener)
	 */
	@Override
	public void addListener(IDemoListener listener) {
		listeners.add(listener);
	}
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoHandler#removeListener(v9t9.common.demo.IDemoHandler.IDemoListener)
	 */
	@Override
	public void removeListener(IDemoListener listener) {
		listeners.remove(listener);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoHandler#isDemoSupported(java.net.URI)
	 */
	@Override
	public boolean isDemoSupported(URI uri) {
		try {
			IDemoInputStream is = machine.getDemoManager().createDemoReader(uri);
			if (is == null)
				return false;
			is.close();
			return true;
		} catch (NotifyException e) {		
			return false;
		} catch (IOException e) {		
			return false;
		}
	}

}
