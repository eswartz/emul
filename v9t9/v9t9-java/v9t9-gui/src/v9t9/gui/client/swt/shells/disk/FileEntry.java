/**
 * 
 */
package v9t9.gui.client.swt.shells.disk;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.ejs.gui.common.FontUtils;

import v9t9.gui.EmulatorGuiData;
import ejs.base.properties.IProperty;

/**
 * @author ejs
 *
 */
public abstract class FileEntry extends BaseSettingEntry implements IEntryHistoryListener {

	protected Combo combo;
	protected Label icon;
	protected Image doesNotExistImage;
	protected Image blankImage;
	protected EntryHistory entryHistory;
	private String historyId;

	/**
	 * @param dialog_
	 * @param parent
	 * @param setting_
	 * @param style
	 */
	public FileEntry(IDeviceSelectorDialog dialog_, Composite parent,
			IProperty setting_, int style) {
		super(dialog_, parent, setting_, style);
		doesNotExistImage = EmulatorGuiData.loadImage(getDisplay(), "icons/error.png");
//		blankImage = new Image(getDisplay(), doesNotExistImage.getBounds());
		
		entryHistory = dialog_.getEntryHistory();
		historyId = "DiskSelector." + getHistoryName();
	}


	protected final void validatePath() {
		String path = setting.getString();
		File file = path != null && !path.isEmpty() ? new File(path) : null;
		String err = file != null ? validateFile(file) : "Provide a file path";
		
		if (err == null) {
			icon.setImage(blankImage);
			icon.setToolTipText("");

//			if (combo.indexOf(path) < 0 && file.exists()) {
//				// only store history for real places
//				combo.add(path);
//				setHistory(historyId, combo.getItems());
//			}
			
		} else {
			icon.setImage(doesNotExistImage);
			icon.setToolTipText(err);
		}
	}

	
	/**
	 * Validate the given file, returning a string for error
	 * @param file
	 * @return error or <code>null</code>
	 */
	protected abstract String validateFile(File file);


	@Override
	protected void createControls(final Composite parent) {
		GridLayoutFactory.fillDefaults().spacing(2,0).numColumns(8).applyTo(this);
		int emSize = FontUtils.measureText(getDisplay(), getFont(), "M").x;
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (doesNotExistImage != null) 
					doesNotExistImage.dispose();
				if (blankImage != null) 
					blankImage.dispose();
				doesNotExistImage = null;
				blankImage = null;
				entryHistory.removeListener(FileEntry.this);
			}
		});
		
		Label label = new Label(parent, SWT.NONE);
		// assume all the labels are the same size
		String text = setting.getLabel() + ": ";
		label.setText(text);
		GridDataFactory.fillDefaults().hint(text.length()*emSize*2/3, -1).align(SWT.LEFT, SWT.CENTER).applyTo(label);
		
		label.setToolTipText(setting.getDescription());

		icon = new Label(parent, SWT.NONE);
		icon.setImage(blankImage);
		GridDataFactory.fillDefaults().minSize(16, 16).hint(16, 16).applyTo(icon);

		combo = new Combo(parent, SWT.BORDER | SWT.DROP_DOWN);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(combo);
		
		combo.setToolTipText(setting.getDescription());
		
		String[] history = entryHistory.getItems(historyId);
		if (history != null) {
			combo.setItems(history);
		}
		
		entryHistory.addListener(this);
		
		String str = setting.getString();
		combo.setText(str != null ? str : "");
		
		combo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateEntry();
			}
		});
		combo.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				updateEntry();
				commitEntry();
			}
		});
		combo.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == '\r') {
					updateEntry();
					commitEntry();
					e.doit = false;
				}
			}
		});
		
		
		final Button browse = new Button(parent, SWT.PUSH);
		GridDataFactory.fillDefaults().hint(emSize*10, -1).grab(false, false).applyTo(browse);
		browse.setText("Browse...");
		browse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleBrowse(setting);
			}
		});			
		

		final Button eject = new Button(parent, SWT.PUSH);
		eject.setImage(EmulatorGuiData.loadImage(getDisplay(), "icons/icon_clear.gif"));
		eject.setToolTipText("Clear the entry and remove from history");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(false, false).applyTo(eject);

		eject.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String current = combo.getText();
				combo.setText("");
				
				String[] items = entryHistory.getItems(historyId);
				
				List<String> l = new ArrayList<String>(Arrays.asList(items));
				l.remove(current);
				
				entryHistory.setHistory(historyId, l.toArray(new String[l.size()]));
				
				updateEntry();
			}
		});
		
		
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				updateEntry();
			}
		});

	}

	protected abstract void handleBrowse(IProperty setting);

	protected void updateEntry() {
		String text = combo.getText();
		String path;
		if (!text.isEmpty()) {
			File dir = new File(combo.getText());
			path = dir.getAbsolutePath();
		} else {
			path = "";
		}
		switchPath(combo, path);
		updateSetting();
	}
	protected void commitEntry() {
		String text = combo.getText();
		if (text.isEmpty())
			return;
		File dir = new File(combo.getText());
		String path = dir.getAbsolutePath();
		if (combo.indexOf(path) < 0) {
			combo.add(path);
			entryHistory.setHistory(historyId, combo.getItems());
		}
	}

	protected abstract void switchPath(Combo combo, String path);

	protected abstract String getHistoryName();
	
	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.disk.IEntryHistoryListener#historyChanged(java.lang.String, java.lang.String[])
	 */
	@Override
	public void historyChanged(final String historyId, final String[] items) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (isDisposed())
					return;
				if (historyId.equals(FileEntry.this.historyId)) {
					String current = combo.getText();
					combo.setItems(items);
					combo.setText(current);
				}
			}
		});
	}

}