/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.io.File;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.ejs.coffee.core.utils.Setting;

import v9t9.emulator.EmulatorSettings;
import v9t9.emulator.hardware.dsrs.DsrHandler;
import v9t9.emulator.hardware.dsrs.DsrManager;

/**
 * Select and set up disks
 * @author ejs
 *
 */
public class DiskSelector extends Composite {

	private final IFocusRestorer focusRestorer;

	class DiskEntry  {
		private final Setting setting;
		public DiskEntry(final Composite parent, Setting setting_) {
			this.setting = setting_;
			
			Label label = new Label(parent, SWT.NONE);
			label.setText(setting.getName() + ": ");
			GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(label);
			
			final Combo combo = new Combo(parent, SWT.BORDER | SWT.DROP_DOWN);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(combo);
			
			String[] history = getHistory();
			if (history != null) {
				combo.setItems(history);
			}
			
			combo.setText(setting.getString());
			
			combo.addModifyListener(new ModifyListener() {
				
				public void modifyText(ModifyEvent e) {
					File dir = new File(combo.getText());
					if (dir.exists()) {
						String path = dir.getAbsolutePath();
						switchPath(combo, path);
					}
				}
			});
			
			
			Button browse = new Button(parent, SWT.PUSH);
			GridDataFactory.fillDefaults().grab(false, false).applyTo(browse);
			browse.setText("Browse...");
			browse.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					DirectoryDialog dialog =  new DirectoryDialog(parent.getShell(), SWT.NONE);
					dialog.setText("Select path for " + setting.getName());
					dialog.setFilterPath(setting.getString());
					String dirname = dialog.open();
					if (dirname != null) {
						switchPath(combo, dirname);
						combo.setText(dirname);
					}
				}
			});
		}
		/**
		 * @param combo 
		 * @param absolutePath
		 */
		protected void switchPath(Combo combo, String path) {
			if (path == null)
				return;
			setting.setString(path);
			for (String p : combo.getItems())
				if (p.equals(path))
					return;
			combo.add(path);
			setHistory(combo.getItems());
		}
		
	};

	private String[] getHistory() {
		String[] history = EmulatorSettings.getInstance().getHistorySettings().getArray("DiskSelector");
		return history;
	}
	private void setHistory(String[] history) {
		EmulatorSettings.getInstance().getHistorySettings().put("DiskSelector", history);
		EmulatorSettings.getInstance().save();
	}
	
	/**
	 * 
	 */
	public DiskSelector(Composite parent, DsrManager dsrManager, IFocusRestorer restorer) {
		
		super(parent, SWT.NONE);
		
		System.out.println("Creating DiskSelector");
		this.focusRestorer = restorer;

		GridLayoutFactory.fillDefaults().applyTo(this);
		
		for (DsrHandler handler : dsrManager.getDsrs()) {

			Composite section = new Composite(this, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(section);
			GridLayoutFactory.fillDefaults().numColumns(3).applyTo(section);
			
			Label label = new Label(section, SWT.NONE);
			label.setText("Settings for " + handler.getName() + ":");
			GridDataFactory.fillDefaults().span(3, 1).applyTo(label);
			
			for (Setting setting : handler.getSettings()) {
				/*DiskEntry diskEntry =*/ new DiskEntry(section, setting);
			}
		}
	}

}
