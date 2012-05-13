/**
 * 
 */
package v9t9.common.demo;

import java.net.URI;
import java.util.ArrayList;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.events.NotifyEvent;
import v9t9.common.events.NotifyException;
import v9t9.common.settings.SettingSchema;

/**
 * @author ejs
 *
 */
public interface IDemoHandler {

	interface IDemoListener {
		void stopped(NotifyEvent event);
	}
	
	/** Array of URIs where demos may be fetched */
	SettingSchema settingBootDemosPath = 
			new SettingSchema(
					ISettingsHandler.INSTANCE,
					"BootDemosPath", String.class, new ArrayList<String>());
	/** Array of URIs where demos may be fetched */
	SettingSchema settingUserDemosPath = 
			new SettingSchema(
					ISettingsHandler.INSTANCE,
					"UserDemosPath", String.class, new ArrayList<String>());
	/** URI where demos will be recorded */
	SettingSchema settingRecordedDemosPath = 
		new SettingSchema(
				ISettingsHandler.INSTANCE,
				"RecordedDemosPath", ".");

	String[] DEMO_EXTENSIONS = new String[] { "dem|V9t9 demo file (*.dem)", "*|Other demo file" };
	
	/** This setting is true while a demo is being recorded. */
	SettingSchema settingRecordDemo = new SettingSchema(
				ISettingsHandler.TRANSIENT,
				"RecordDemo", new Boolean(false));

	/** This setting is true while a demo is being played back. */
	SettingSchema settingPlayingDemo = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"PlayingDemo", new Boolean(false));

	/** This setting is true while a demo is paused:
	 * 
	 * -- when recording, nothing is recorded, but changes are tracked to allow a clean
	 * transition when recording continues.
	 * 
	 * -- when playing, machine timer events are ignored.
	 *  */
	SettingSchema settingDemoPaused = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"DemoPaused", new Boolean(false));

	
	void dispose();

	void addListener(IDemoListener listener);
	void removeListener(IDemoListener listener);
	
	void startRecording(URI uri) throws NotifyException;
	void stopRecording() throws NotifyException;
	URI getRecordingURI();
	
	void startPlayback(URI uri) throws NotifyException;
	void stopPlayback() throws NotifyException;
	URI getPlaybackURI();

	/** Tell if the demo's contents are recognized by this machine */
	boolean isDemoSupported(URI uri);
	
}
