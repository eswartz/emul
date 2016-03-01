/*
  SoundRecordingHelper.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.sound;

import v9t9.common.client.ISoundHandler;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.SettingSchema;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.sound.ISoundOutput;
import ejs.base.sound.SoundFileListener;
import ejs.base.sound.SoundFormat;


/**
 * This class provides a useful way of recording sound to a file.
 * @author ejs
 * 
 */
public class SoundRecordingHelper implements ISoundRecordingHelper {

	protected SoundFileListener soundListener;
	
	protected IProperty soundFileSetting;

	protected final ISoundOutput output;

	protected IPropertyListener listener;

	protected IMachine machine;

	/**
	 * @param shell
	 */
	public SoundRecordingHelper(IMachine machine, ISoundOutput output, SettingSchema fileSchema,
			SoundFormat format,
			boolean includeSilence) {
		this.output = output;
		this.soundFileSetting = machine.getSettings().get(fileSchema);
		this.machine = machine;
		soundListener = new SoundFileListener();
		soundListener.started(format);
		soundListener.setIncludeSilence(includeSilence);
		
		soundListener.setPauseProperty(machine.getSettings().get(ISoundHandler.settingPauseSoundRecording));
		
		listener = new IPropertyListener() {
			public void propertyChanged(IProperty setting) {
				soundListener.setFileName(setting.getString());
			}
			
		};
		soundFileSetting.addListener(listener);
		
		output.addEmitter(soundListener);

	}

	/* (non-Javadoc)
	 * @see v9t9.gui.sound.ISoundRecordingHelper#getSoundFileSetting()
	 */
	@Override
	public IProperty getSoundFileSetting() {
		return soundFileSetting;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.gui.sound.ISoundRecordingHelper#dispose()
	 */
	@Override
	public void dispose() {
		soundFileSetting.removeListener(listener);
		output.removeEmitter(soundListener);
	}

	public void stop() {
		soundFileSetting.setString(null);
	}
	
}
