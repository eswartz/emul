/**
 * 
 */
package v9t9.gui.sound;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.ejs.gui.common.SwtDialogUtils;

import v9t9.common.client.ISoundHandler;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.SettingSchema;

import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.sound.ISoundOutput;
import ejs.base.sound.SoundFileListener;


/**
 * This class provides a useful way of recording sound to a file, with a helper
 * routine to attach commands to an SWT menu as well.
 * @author ejs
 * 
 */
public class SoundRecordingHelper {

	private SoundFileListener soundListener;
	
	private IProperty soundFileSetting;

	private final ISoundOutput output;

	private IPropertyListener listener;

	private final String label;

	private IMachine machine;

	/**
	 * @param shell
	 */
	public SoundRecordingHelper(IMachine machine, ISoundOutput output, SettingSchema fileSchema,
			String label) {
		this.output = output;
		this.soundFileSetting = machine.getSettings().get(fileSchema);
		this.label = label;
		this.machine = machine;
		soundListener = new SoundFileListener();
		soundListener.setPauseProperty(machine.getSettings().get(ISoundHandler.settingPauseSoundRecording));
		
		listener = new IPropertyListener() {
			public void propertyChanged(IProperty setting) {
				soundListener.setFileName(setting.getString());
			}
			
		};
		soundFileSetting.addListener(listener);
		
		output.addEmitter(soundListener);

	}

	/**
	 * @return the soundFileSetting
	 */
	public IProperty getSoundFileSetting() {
		return soundFileSetting;
	}
	public Menu createSoundMenu(final Control parent) {
		final Menu menu = new Menu(parent);
		return populateSoundMenu(menu);
	}

	/**
	 * Add a record/stop recording item to a menu
	 * @param menu
	 * @return the menu
	 */
	public Menu populateSoundMenu(final Menu menu) {
		MenuItem item = new MenuItem(menu, SWT.CHECK);
		String filename = soundFileSetting.getString();
		if (filename != null) {
			item.setText("Stop recording " + label + " to " + filename);
			item.setSelection(true);
			item.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					stop();
				}

			});
		} else {
			item.setText("Record " + label  + "...");
			item.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final Shell shell = menu.getShell();
					machine.getClient().asyncExecInUI(new Runnable() {
						public void run() {
							String filenameBase = SwtDialogUtils.openFileSelectionDialog(
									shell,
									"Record " + label + " to...", 
									"/tmp", 
									label, true,
									SoundFileListener.getSoundFileExtensions());
							File saveFile = null;
							if (filenameBase != null) {
								saveFile = SwtDialogUtils.getUniqueFile(filenameBase);
								if (saveFile == null) {
									SwtDialogUtils.showErrorMessage(shell, "Save error", 
											"Too many " + label + " files here!");
									return;
								}
							}
							soundFileSetting.setString(saveFile != null ? saveFile.getAbsolutePath() : null);
						}
					});
				}

			});
		}
		return menu;
	}
	
	/**
	 * 
	 */
	public void dispose() {
		soundFileSetting.removeListener(listener);
		output.removeEmitter(soundListener);
	}

	/**
	 * 
	 */
	public void stop() {
		soundFileSetting.setString(null);
		
	}
	
}
