/**
 * 
 */
package v9t9.server.demo;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;

import ejs.base.properties.IProperty;
import ejs.base.utils.ListenerList;

import v9t9.common.demo.IDemoHandler;
import v9t9.common.events.NotifyException;
import v9t9.common.files.IPathFileLocator;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;

/**
 * @author ejs
 *
 */
public class DemoHandler implements IDemoHandler {

	private URI lastRecordingURI;
	private URI lastPlaybackURI;
	private DemoRecorder recorder;
	private DemoPlayer player;
	private final IPathFileLocator locator;
	private IMachine machine;
	
	private ListenerList<IDemoListener> listeners = new ListenerList<IDemoHandler.IDemoListener>();
	private IProperty demoPauseSetting;
	private IProperty recordSetting;
	private IProperty playSetting;
	private IProperty machinePauseSetting;

	/**
	 * 
	 */
	public DemoHandler(IMachine machine) {
		this.machine = machine;
		this.locator = machine.getPathFileLocator();
		
		machinePauseSetting = Settings.get(machine, IMachine.settingPauseMachine);
		demoPauseSetting = Settings.get(machine, IDemoHandler.settingDemoPaused);
		recordSetting = Settings.get(machine, IDemoHandler.settingRecordDemo);
		playSetting = Settings.get(machine, IDemoHandler.settingPlayingDemo);
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
	public void startRecording(URI uri) throws NotifyException {
		if (recorder != null) {
			stopRecording();
		}
		
		lastRecordingURI = uri;
		try {
			recorder = new DemoRecorder(new DemoFormatWriter(locator.createOutputStream(uri)), listeners);
			
			recordSetting.setBoolean(true);
			demoPauseSetting.setBoolean(false);
		} catch (IOException e) {
			throw new NotifyException(uri, "Failed to create demo " + uri, e);
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.IDemoHandler#stopRecording()
	 */
	@Override
	public void stopRecording() throws NotifyException {
		recordSetting.setBoolean(false);
		demoPauseSetting.setBoolean(false);
		
		if (recorder == null)
			return;
		
		try {
			recorder.stop();
		} catch (IOException e) {
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
	public void startPlayback(URI uri) throws NotifyException {
		if (player != null) {
			stopPlayback();
		}
		
		lastPlaybackURI = uri;
		try {
			InputStream is = locator.createInputStream(uri);
			byte[] header = new byte[4];
			is.mark(4);
			is.read(header);
			if (Arrays.equals(header, DemoFormat.DEMO_MAGIC_HEADER)) {
				is.reset();
				player = new DemoPlayer(machine, new DemoFormatReader(is), listeners);
				player.start();
				
				machinePauseSetting.setBoolean(true);
				playSetting.setBoolean(true);
				demoPauseSetting.setBoolean(false);
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
	public void stopPlayback() throws NotifyException {
		playSetting.setBoolean(false);
		demoPauseSetting.setBoolean(false);
		machinePauseSetting.setBoolean(false);
		
		if (player == null)
			return;
		
		player.stop();
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
}
