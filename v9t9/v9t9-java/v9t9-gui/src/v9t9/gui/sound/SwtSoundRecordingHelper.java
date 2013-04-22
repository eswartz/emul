/*
  SoundRecordingHelper.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.sound;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.ejs.gui.common.SwtDialogUtils;

import v9t9.common.events.IEventNotifier.Level;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.SettingSchema;
import v9t9.common.sound.SoundRecordingHelper;
import ejs.base.sound.ISoundOutput;
import ejs.base.sound.SoundFileListener;


/**
 * This class provides a useful way of recording sound to a file, with a helper
 * routine to attach commands to an SWT menu as well.
 * @author ejs
 * 
 */
public class SwtSoundRecordingHelper extends SoundRecordingHelper implements ISwtSoundRecordingHelper {
	protected final String label;


	public SwtSoundRecordingHelper(IMachine machine, ISoundOutput output,
			SettingSchema fileSchema, String label, boolean includeSilence) {
		super(machine, output, fileSchema, includeSilence);
		this.label = label;
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.sound.ISwtSoundRecordingHelper#populateSoundMenu(org.eclipse.swt.widgets.Menu)
	 */
	@Override
	public Menu populateSoundMenu(final Menu menu) {
		MenuItem item = new MenuItem(menu, SWT.CHECK);
		final String filename = soundFileSetting.getString();
		if (filename != null) {
			item.setText("Stop recording " + label + " to " + filename);
			item.setSelection(true);
			item.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					stop();
					machine.notifyEvent(Level.INFO, "Finished recording to " + filename);
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
							if (saveFile != null) {
								machine.notifyEvent(Level.INFO, "Started recording to " + saveFile);
							}
						}
					});
				}

			});
		}
		return menu;
	}
	
}
