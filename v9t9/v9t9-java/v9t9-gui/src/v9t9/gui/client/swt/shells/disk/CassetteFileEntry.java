/**
 * 
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

import v9t9.common.cassette.CassetteFileUtils;
import v9t9.common.cassette.ICassetteDeck;
import ejs.base.properties.IProperty;

/**
 * @author ejs
 *
 */
public class CassetteFileEntry extends FileEntry {

	private int mode;
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
			IProperty setting_, int mode) {
		super(dialog_, parent, setting_, SWT.NONE);
		this.mode = mode;
		
		this.deck = setting_.getName().contains("Input") || setting_.getName().contains("1")
				? machine.getCassette().getCassette1()
				: machine.getCassette().getCassette2();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.disk.FileEntry#createControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createControls(Composite parent) {
		super.createControls(parent);
		
		if (mode == SWT.SAVE) {
			recordStopButton = new Button(parent, SWT.TOGGLE);
			GridDataFactory.fillDefaults().grab(false, false).applyTo(recordStopButton);
			
			updateRecordStop();
			
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
							if (!ret)
								return;
						}
						
						deck.recordCassette();
						
					} else {
						deck.stopCassette();
					}
					
					updateRecordStop();
					
				}
			});
		} else {
			playStopButton = new Button(parent, SWT.TOGGLE);
			GridDataFactory.fillDefaults().grab(false, false).applyTo(playStopButton);
			
			updatePlayStop();
			
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
		}
	}
	
	protected void updateRecordStop() {
		if (!deck.isRecording()) {
			recordStopButton.setText("Record");
			recordStopButton.setSelection(false);
		} else {
			recordStopButton.setText("Stop");
			recordStopButton.setSelection(true);
		}
	}
	
	protected void updatePlayStop() {
		if (!deck.isPlaying()) {
			playStopButton.setText("Play");
			playStopButton.setSelection(false);
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
		if (mode == SWT.SAVE) {
			if (file.isDirectory()) {
				return "Path must be a file";
			}
			return null;
		} else {
			if (!file.exists()) {
				return "File does not exist";
			} else {
				try {
					CassetteFileUtils.scanAudioFile(file);
				} catch (IOException e) {
					return "Could not read file";
				} catch (UnsupportedAudioFileException e) {
					return "Audio format is not supported";
				}
			}
			return null;
		}
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
		FileDialog dialog =  new FileDialog(getShell(), mode);
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
