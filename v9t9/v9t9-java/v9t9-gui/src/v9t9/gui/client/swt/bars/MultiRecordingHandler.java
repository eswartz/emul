/**
 * 
 */
package v9t9.gui.client.swt.bars;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.widgets.Menu;

import v9t9.common.machine.IMachine;
import v9t9.common.settings.SettingSchema;
import v9t9.gui.sound.SoundRecordingHelper;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.sound.ISoundOutput;

/**
 * @author ejs
 *
 */
public class MultiRecordingHandler {

	private IMachine machine;
	private List<SoundRecordingHelper> helpers;
	private Set<IPropertyListener> listeners;

	/**
	 * @param machine
	 */
	public MultiRecordingHandler(IMachine machine) {
		this.machine = machine;
		helpers = new ArrayList<SoundRecordingHelper>();
		listeners = new HashSet<IPropertyListener>();

	}

	public void dispose() {
		for (IPropertyListener listener: listeners)
			for (SoundRecordingHelper helper : helpers)
				helper.getSoundFileSetting().removeListener(listener);

		for (SoundRecordingHelper helper : helpers)
			helper.dispose();
	}

	/**
	 * Register a new recordable stream
	 * @param output
	 * @param fileSchema
	 * @param label
	 * @param recordSilence
	 */
	public void register(ISoundOutput output,
			SettingSchema fileSchema, String label, boolean recordSilence) {
		SoundRecordingHelper soundRecordingHelper = new SoundRecordingHelper(machine, 
				output, fileSchema, label, recordSilence);
		helpers.add(soundRecordingHelper);
	}

	/**
	 * Tell whether anything is currently recording
	 * @return
	 */
	public boolean isRecording() {
		for (SoundRecordingHelper helper : helpers)
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
		for (SoundRecordingHelper helper : helpers)
			helper.getSoundFileSetting().addListenerAndFire(listener);
	}

	/**
	 * Get all the active files being recorded
	 * @return
	 */
	public Collection<String> getRecordings() {
		Collection<String> recordings = new ArrayList<String>(listeners.size());
		for (SoundRecordingHelper helper : helpers) {
			IProperty prop = helper.getSoundFileSetting();
			String file = prop.getString();
			if (file != null) {
				recordings.add(file);
			}
		}
		return recordings;
	}

	/**
	 * Add menu items to start/stop recording
	 * @param menu
	 */
	public void populateSoundMenu(Menu menu) {
		for (SoundRecordingHelper helper : helpers) {
			helper.populateSoundMenu(menu);
		}		
	}

}
