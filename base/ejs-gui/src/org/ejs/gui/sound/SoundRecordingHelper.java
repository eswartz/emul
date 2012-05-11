/**
 * 
 */
package org.ejs.gui.sound;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.ejs.gui.common.SwtDialogUtils;

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

	private SoundFileListener iSoundListener;
	
	private IProperty soundFileSetting;

	private final ISoundOutput output;

	private IPropertyListener listener;

	private final String label;

	/**
	 * @param shell
	 */
	public SoundRecordingHelper(ISoundOutput output, IProperty settingRecordSoundOutputFile, String label) {
		this.output = output;
		this.soundFileSetting = settingRecordSoundOutputFile;
		this.label = label;
		iSoundListener = new SoundFileListener();
		
		listener = new IPropertyListener() {
			public void propertyChanged(IProperty setting) {
				iSoundListener.setFileName(setting.getString());
			}
			
		};
		settingRecordSoundOutputFile.addListener(listener);
		
		output.addListener(iSoundListener);

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
					soundFileSetting.setString(null);
				}

			});
		} else {
			item.setText("Record " + label  + "...");
			item.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String filenameBase = SwtDialogUtils.openFileSelectionDialog(
							menu.getShell(),
							"Record " + label + " to...", 
							"/tmp", 
							label, true,
							SoundFileListener.getSoundFileExtensions());
					File saveFile = null;
					if (filenameBase != null) {
						saveFile = SwtDialogUtils.getUniqueFile(filenameBase);
						if (saveFile == null) {
							SwtDialogUtils.showErrorMessage(menu.getShell(), "Save error", 
									"Too many " + label + " files here!");
							return;
						}
					}
					soundFileSetting.setString(saveFile != null ? saveFile.getAbsolutePath() : null);
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
		output.removeListener(iSoundListener);
	}
	
}
