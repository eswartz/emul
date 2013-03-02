/*
  IDemoManager.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.demos;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.events.NotifyException;
import v9t9.common.files.IPathFileLocator;
import v9t9.common.settings.SettingSchema;

/**
 * This interface covers the high-level aspects of managing a list of available
 * demos along a search path.
 * @author ejs
 *
 */
public interface IDemoManager {

	/** Array of URIs where demos may be fetched */
	SettingSchema settingBootDemosPath = 
			new SettingSchema(
					ISettingsHandler.USER,
					"BootDemosPath", String.class, new ArrayList<String>());
	/** Array of URIs where demos may be fetched */
	SettingSchema settingUserDemosPath = 
			new SettingSchema(
					ISettingsHandler.USER,
					"UserDemosPath", String.class, new ArrayList<String>());
	/** URI where demos will be recorded */
	SettingSchema settingRecordedDemosPath = 
		new SettingSchema(
				ISettingsHandler.USER,
				"RecordedDemosPath", ".");
	
	/** if set, record demos in V9t9 6.0 format */
	SettingSchema settingUseDemoOldFormat = 
		new SettingSchema(
				ISettingsHandler.TRANSIENT,
				"UseOldDemoFormat", Boolean.FALSE);

	
	String[] DEMO_EXTENSIONS = new String[] { "dem|V9t9 demo file (*.dem)", "*|Other demo file" };
	
	/** locate the demos */
	IPathFileLocator getDemoLocator();
	
	/** get an array of all known demos along paths */
	IDemo[] getDemos();
	
	/** refresh demo list */
	void reload();
	
	/** register this demo (e.g., just recorded) */
	void addDemo(IDemo demo);

	/**
	 * remove a demo
	 */
	void removeDemo(IDemo demo);
	
	IDemoInputStream createDemoReader(URI uri) throws IOException, NotifyException;
	IDemoOutputStream createDemoWriter(URI uri) throws IOException, NotifyException;

	void registerActorProvider(IDemoActorProvider actorProvider);
	IDemoPlaybackActor[] createPlaybackActors();
	IDemoRecordingActor[] createRecordingActors();
	IDemoReversePlaybackActor[] createReversePlaybackActors();
	
}
