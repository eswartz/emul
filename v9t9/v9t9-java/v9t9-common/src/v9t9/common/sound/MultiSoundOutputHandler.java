/*
  MultiSoundOutputHandler.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.sound;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import v9t9.common.machine.IMachine;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;

/**
 * @author ejs
 *
 */
public class MultiSoundOutputHandler {

	private List<ISoundRecordingHelper> helpers;
	private Set<IPropertyListener> listeners;

	/**
	 * @param machine
	 */
	public MultiSoundOutputHandler(IMachine machine) {
		helpers = new ArrayList<ISoundRecordingHelper>();
		listeners = new HashSet<IPropertyListener>();

	}

	public void dispose() {
		for (IPropertyListener listener: listeners)
			for (ISoundRecordingHelper helper : helpers)
				helper.getSoundFileSetting().removeListener(listener);

		for (ISoundRecordingHelper helper : helpers)
			helper.dispose();
	}

	/**
	 * Register a new recordable stream
	 * @param output
	 * @param fileSchema
	 * @param label
	 * @param recordSilence
	 */
	public void register(SoundRecordingHelper helper) {
		helpers.add(helper);
	}

	/**
	 * Tell whether anything is currently recording
	 * @return
	 */
	public boolean isRecording() {
		for (ISoundRecordingHelper helper : helpers)
			if (helper.getSoundFileSetting().getString() != null)
				return true;
		return false;
	}

	/**
	 * Add a listener for changes in the sound file property state
	 * @param listener
	 */
	public void addListenerAndFire(IPropertyListener listener) {
		listeners.add(listener);
		for (ISoundRecordingHelper helper : helpers)
			helper.getSoundFileSetting().addListenerAndFire(listener);
	}

	/**
	 * Get all the active files being recorded
	 * @return
	 */
	public Collection<String> getRecordings() {
		Collection<String> recordings = new ArrayList<String>(listeners.size());
		for (ISoundRecordingHelper helper : helpers) {
			IProperty prop = helper.getSoundFileSetting();
			String file = prop.getString();
			if (file != null) {
				recordings.add(file);
			}
		}
		return recordings;
	}

	/**
	 * @return
	 */
	public Collection<ISoundRecordingHelper> getRecordingHelpers() {
		return helpers;
	}
}
