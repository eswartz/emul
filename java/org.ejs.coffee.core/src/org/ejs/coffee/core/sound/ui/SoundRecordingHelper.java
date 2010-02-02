/**
 * 
 */
package org.ejs.coffee.core.sound.ui;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.ejs.coffee.core.sound.ISoundOutput;
import org.ejs.coffee.core.sound.SoundFileListener;
import org.ejs.coffee.core.utils.ISettingListener;
import org.ejs.coffee.core.utils.Setting;

/**
 * This class provides a useful way of recording sound to a file, with a helper
 * routine to attach commands to an SWT menu as well.
 * @author ejs
 * 
 */
public class SoundRecordingHelper {

	private SoundFileListener iSoundListener;
	
	private Setting soundFileSetting;

	private final ISoundOutput output;

	private ISettingListener listener;

	private final String label;

	/**
	 * @param shell
	 */
	public SoundRecordingHelper(ISoundOutput output, Setting settingRecordSoundOutputFile, String label) {
		this.output = output;
		this.soundFileSetting = settingRecordSoundOutputFile;
		this.label = label;
		iSoundListener = new SoundFileListener();
		
		listener = new ISettingListener() {
			public void changed(Setting setting, Object oldValue) {
				iSoundListener.setFileName(setting.getString());
			}
			
		};
		settingRecordSoundOutputFile.addListener(listener);
		
		output.addListener(iSoundListener);

	}

	/**
	 * @return the soundFileSetting
	 */
	public Setting getSoundFileSetting() {
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
					String filenameBase = openFileSelectionDialog(
							menu.getShell(),
							"Record " + label + " to...", 
							"/tmp", 
							label, true,
							SoundFileListener.getSoundFileExtensions());
					File saveFile = null;
					if (filenameBase != null) {
						saveFile = getUniqueFile(filenameBase);
						if (saveFile == null) {
							showErrorMessage(menu.getShell(), "Save error", 
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
	
	protected void showErrorMessage(Shell shell, String title, String msg) {
		MessageDialog.openError(shell, title, msg);
	}

	protected String openFileSelectionDialog(Shell shell, String title, String directory,
			String fileName, boolean isSave, String[] extensions) {
		FileDialog dialog = new FileDialog(shell, isSave ? SWT.SAVE : SWT.OPEN);
		dialog.setText(title);
		dialog.setFilterPath(directory);
		dialog.setFileName(fileName);
		
		if (extensions != null) {
			String[] exts = new String[extensions.length];
			String[] names = new String[extensions.length];
			int idx = 0;
			for (String extension : extensions) {
				String[] split = extension.split("\\|");
				exts[idx] = "*." + split[0];
				names[idx] = split[1];
				idx++;
			}
			dialog.setFilterExtensions(exts);
			dialog.setFilterNames(names);
		}
		String filename = dialog.open();
		
		if (filename != null && extensions != null) {
			int extIdx = new File(filename).getName().lastIndexOf('.');
			if (extIdx < 0) {
				filename += '.' + dialog.getFilterExtensions()[dialog.getFilterIndex()].substring(2);
			}
		}
		return filename;
	}
	
	protected File getUniqueFile(String filenameBase) {
		File fileBase = new File(filenameBase);
		File dir = fileBase.getParentFile();
		String base = fileBase.getName();
		int extPtr = base.lastIndexOf('.');
		if (extPtr < 0) extPtr = base.length();
		String ext = base.substring(extPtr);
		base = base.substring(0, extPtr);
		
		File saveFile = null; 
		for (int count = 0; count < 10000; count++) {
			saveFile = new File(dir, base + (count != 0 ? "" + count : "") + ext);
			if (!saveFile.exists())
				break;
		}
		if (saveFile.exists())
			return null;
		return saveFile;
	}
	public void showMenu(Menu menu, final Control parent, final int x, final int y) {
		runMenu(parent, x, y, menu);
		menu.dispose();		
	}

	private void runMenu(final Control parent, final int x, final int y,
			final Menu menu) {
		if (parent != null) {
			Point loc = parent.toDisplay(x, y); 
			menu.setLocation(loc);
		}
		System.out.println("position: " + menu.getParent().getLocation());
		menu.setVisible(true);
		
		Display display = parent.getShell().getDisplay();
		while (display.readAndDispatch()) /**/ ;

		while (!menu.isDisposed() && menu.isVisible()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
	}

	/**
	 * 
	 */
	public void dispose() {
		soundFileSetting.removeListener(listener);
		output.removeListener(iSoundListener);
	}
	
}
