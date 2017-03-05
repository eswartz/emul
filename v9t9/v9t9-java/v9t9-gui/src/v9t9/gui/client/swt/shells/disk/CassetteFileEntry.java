/*
  CassetteFileEntry.java

  (c) 2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.disk;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.ejs.gui.common.FontUtils;

import v9t9.common.cassette.CassetteFileUtils;
import v9t9.common.cassette.ICassetteDeck;
import ejs.base.properties.IProperty;

/**
 * @author ejs
 *
 */
public class CassetteFileEntry extends FileEntry {

	private Button recordStopButton;
	private Button playStopButton;
	private ICassetteDeck deck;

	/**
	 * @param dialog_
	 * @param parent
	 * @param setting_
	 * @param style
	 */
	public CassetteFileEntry(IDeviceSelectorDialog dialog_, Composite parent,
			IProperty setting_) {
		super(dialog_, parent, setting_, SWT.NONE);
		
		this.deck = setting_.getName().contains("1")
				? machine.getCassette().getCassette1()
				: machine.getCassette().getCassette2();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.disk.FileEntry#createControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createControls(Composite parent) {
		super.createControls(parent);
		
		int emSize = FontUtils.measureText(getDisplay(), getFont(), "M").x;
		
		if (deck.canRecord()) {
			recordStopButton = new Button(parent, SWT.TOGGLE);
			GridDataFactory.fillDefaults().hint(emSize*6, -1).grab(false, false).applyTo(recordStopButton);
			
			updateRecordStop();
			recordStopButton.setToolTipText("Press to toggle using the given file as cassette output");
			
			recordStopButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					boolean pressed = recordStopButton.getSelection();
					
					File file = new File(setting.getString());
					deck.setFile(file);
					
					if (pressed) {
						if (file.exists() && file.length() > 0) {
							boolean ret = MessageDialog.openConfirm(getShell(), "Overwrite cassette?", 
									"The cassette file already exists:\n\n" + file + "\n\nOverwrite?");
							if (!ret) {
								pressed = false;
							}
						}
						
						if (pressed) {
							deck.recordCassette();
						}
						
					} else {
						deck.stopCassette();
					}
					
					updateRecordStop();
					
				}
			});
		}
		
		playStopButton = new Button(parent, SWT.TOGGLE);
		GridDataFactory.fillDefaults().hint(emSize * 6, -1).grab(false, false).applyTo(playStopButton);
		
		updatePlayStop();
		playStopButton.setToolTipText("Press to toggle using the given file as cassette input");
		
		playStopButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean pressed = playStopButton.getSelection();
				
				if (pressed) {
					File file = new File(setting.getString());
					if (!file.exists()) {
						MessageDialog.openError(getShell(), "No cassette", 
								"No such file exists:\n\n" + file);
					} else {
						deck.setFile(new File(setting.getString()));
						deck.playCassette();
					}
				} else {
					deck.stopCassette();
				}
				
				updatePlayStop();
			}
		});
		
		if (!deck.canPlay()) {
			playStopButton.setToolTipText("This cassette deck cannot play back");
			playStopButton.setEnabled(false);
			playStopButton.setVisible(false);	// TODO: some caller enables everything recursively
		}
		
//		Button enterButton = new Button(parent, SWT.PUSH);
//		GridDataFactory.fillDefaults().hint(emSize * 10, -1).grab(false, false).applyTo(enterButton);
//		
//		enterButton.setText("Press ENTER");
//		enterButton.setToolTipText("Send an ENTER keypress to the 99/4A");
//		
//		enterButton.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				machine.getKeyboardHandler().pasteText("\r");
//			}
//		});
	}
	
	protected void updateRecordStop() {
		if (!deck.isRecording()) {
			recordStopButton.setText("Record");
			recordStopButton.setSelection(false);
			validatePath();
		} else {
			recordStopButton.setText("Stop");
			recordStopButton.setSelection(true);
		}
	}
	
	protected void updatePlayStop() {
		if (!deck.isPlaying()) {
			playStopButton.setText("Play");
			playStopButton.setSelection(false);
			validatePath();
		} else {
			playStopButton.setText("Stop");
			playStopButton.setSelection(true);
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.disk.FileEntry#validatePath()
	 */
	@Override
	protected String validateFile(File file) {
		if (file.isDirectory()) {
			return "Path must be a file";
		}
		if (file.exists()) {
			try {
				CassetteFileUtils.scanAudioFile(file);
			} catch (IOException e) {
				return "Could not read file";
			} catch (UnsupportedAudioFileException e) {
				return "Audio format is not supported";
			}
		}
		
		// don't complain about missing file here, but when playing
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.disk.FileEntry#getHistoryName()
	 */
	@Override
	protected String getHistoryName() {
		return "Cassette";
	}


	@Override
	protected void handleBrowse(IProperty setting) {
		FileDialog dialog =  new FileDialog(getShell(), SWT.SAVE);
		dialog.setText("Select file for " + setting.getName());
		String dir = new File(setting.getString()).getParent();
		String filename = new File(setting.getString()).getName();
		dialog.setFilterPath(dir);
		dialog.setFileName(filename);
		dialog.setFilterExtensions(new String[] { "*.wav;*.WAV;*.au;*.AU;*.AIFF;*.aiff;*.SND;*.snd", "*.*" });
		dialog.setFilterNames(new String[] { "Audio files (*.wav, *.au, *.aiff, *.snd)", "All files" });
		if (filename.toLowerCase().matches("\\.(wav|ai|aiff|snd)"))  {
			dialog.setFilterIndex(0);
		} else {
			dialog.setFilterIndex(1);
		}
		filename = dialog.open();
		if (filename != null) {
			switchPath(combo, filename);
			combo.setText(filename);
		}
	}


	protected void switchPath(Combo combo, String path) {
		// always set for disks
		setting.setString(path);

		validatePath();
	}

}
