/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.ejs.coffee.core.utils.ISettingEnabledListener;
import org.ejs.coffee.core.utils.Setting;

import v9t9.emulator.EmulatorSettings;
import v9t9.emulator.clients.builtin.ISettingDecorator;
import v9t9.emulator.hardware.dsrs.DsrHandler;
import v9t9.emulator.hardware.dsrs.DsrManager;

/**
 * Select and set up disks
 * @author ejs
 *
 */
public class DiskSelector extends Composite {
	
	abstract class SettingEntry extends Composite {
		protected final Setting setting;
		private ISettingEnabledListener enableListener;
		
		public SettingEntry(final Composite parent, Setting setting_, int style) {
			super(parent, style);
			
			this.setting = setting_;
			
			enableListener = new ISettingEnabledListener() {
				
				public void changed(Setting setting) {
					updateSetting();
				}
			};
			setting.addEnabledListener(enableListener);
			
			addDisposeListener(new DisposeListener() {
				
				public void widgetDisposed(DisposeEvent e) {
					setting.removeEnabledListener(enableListener);
				}
			});
			
			if (setting instanceof ISettingDecorator) {
				ImageDescriptor descriptor = ((ISettingDecorator) setting).getIcon();
				Label icon = new Label(this, SWT.NONE);
				final Image iconImage = descriptor.createImage();
				icon.setImage(iconImage);
				parent.addDisposeListener(new DisposeListener() {
					
					public void widgetDisposed(DisposeEvent e) {
						iconImage.dispose();
					}
				});
				
			} else {
				new Label(this, SWT.NONE);
			}
			
			createControls(this);
		}
		
		protected void updateSetting() {
			SettingEntry.this.setVisible(setting.isEnabled());
			GridData data = (GridData) SettingEntry.this.getLayoutData();
			data.exclude = !setting.isEnabled();
			SettingEntry.this.getShell().layout(true, true);
			
		}

		abstract protected void createControls(Composite parent);
		
	};

	class DiskEntry extends SettingEntry {
		private Combo combo;
		private Button browse;
		
		public DiskEntry(final Composite parent, Setting setting_) {
			super(parent, setting_, SWT.NONE);
			
		}
		
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.swt.DiskSelector.SettingEntry#createControls(org.eclipse.swt.widgets.Composite)
		 */
		@Override
		protected void createControls(final Composite parent) {
			GridLayoutFactory.fillDefaults().numColumns(4).applyTo(this);
			
			Label label = new Label(parent, SWT.NONE);
			label.setText(setting.getLabel() + ": ");
			GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(label);
			
			label.setToolTipText(setting.getDescription());
			
			combo = new Combo(parent, SWT.BORDER | SWT.DROP_DOWN);
			GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(combo);
			
			combo.setToolTipText(setting.getDescription());
			
			String[] history = getHistory(getHistoryName());
			if (history != null) {
				combo.setItems(history);
			}
			
			combo.setText(setting.getString());
			
			combo.addModifyListener(new ModifyListener() {
				
				public void modifyText(ModifyEvent e) {
					File dir = new File(combo.getText());
					String path = dir.getAbsolutePath();
					// always set for disks 
					if (isDiskImage())
						setting.setString(path);
					// only store history for real places
					if (dir.exists()) {
						switchPath(combo, path);
					}
				}

			});
			
			
			browse = new Button(parent, SWT.PUSH);
			GridDataFactory.fillDefaults().grab(false, false).applyTo(browse);
			browse.setText("Browse...");
			browse.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (isDiskImage()) {
						FileDialog dialog =  new FileDialog(parent.getShell(), SWT.OPEN);
						dialog.setText("Select image for " + setting.getName());
						dialog.setFilterPath(new File(setting.getString()).getParent());
						dialog.setFileName(new File(setting.getString()).getName());
						String filename = dialog.open();
						if (filename != null) {
							switchPath(combo, filename);
							combo.setText(filename);
						}
					} else {
						DirectoryDialog dialog =  new DirectoryDialog(parent.getShell(), SWT.NONE);
						dialog.setText("Select path for " + setting.getName());
						dialog.setFilterPath(setting.getString());
						String dirname = dialog.open();
						if (dirname != null) {
							switchPath(combo, dirname);
							combo.setText(dirname);
						}
					}
				}
			});			
		}
		
		private boolean isDiskImage() {
			return setting.getName().contains("Image");
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
			setHistory(getHistoryName(), combo.getItems());
		}

		private String getHistoryName() {
			return isDiskImage() ? "Images" : "Directories";
		}
		
	};


	class BooleanEntry extends SettingEntry {
		public BooleanEntry(final Composite parent, Setting setting_) {
			super(parent, setting_, SWT.NONE);
		}

		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.swt.DiskSelector.SettingEntry#createControls(org.eclipse.swt.widgets.Composite)
		 */
		@Override
		protected void createControls(Composite parent) {
			GridLayoutFactory.fillDefaults().numColumns(2).applyTo(this);


			final Button checkbox = new Button(parent, SWT.CHECK);
			checkbox.setText(setting.getLabel());
			GridDataFactory.fillDefaults().grab(true, false).applyTo(checkbox);
			
			checkbox.setToolTipText(setting.getDescription());
			
			checkbox.setSelection(setting.getBoolean());
			
			checkbox.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					setting.setBoolean(checkbox.getSelection());
					
				};
			});
		}
		
		
	};


	private String[] getHistory(String name) {
		String[] history = EmulatorSettings.getInstance().getHistorySettings().getArray("DiskSelector." + name);
		return history;
	}
	private void setHistory(String name, String[] history) {
		EmulatorSettings.getInstance().getHistorySettings().put("DiskSelector." + name, history);
		EmulatorSettings.getInstance().save();
	}
	/**
	 * 
	 */
	public DiskSelector(Shell shell, DsrManager dsrManager) {
		
		super(shell, SWT.NONE);
		
		shell.setText("Disk Selector");

		GridLayoutFactory.fillDefaults().applyTo(this);

		Map<String, Group> groups = new HashMap<String, Group>();
		Map<String, List<Setting>> allSettings = new LinkedHashMap<String, List<Setting>>();
		

		for (DsrHandler handler : dsrManager.getDsrs()) {
			Map<String, Collection<Setting>> settings = handler.getEditableSettingGroups();
			for (Map.Entry<String, Collection<Setting>> entry : settings.entrySet()) {
				List<Setting> groupSettings = allSettings.get(entry.getKey());
				if (groupSettings == null) {
					groupSettings = new ArrayList<Setting>();
					allSettings.put(entry.getKey(), groupSettings);
				}
				groupSettings.addAll(entry.getValue());
			}
		}
		
		for (Map.Entry<String, List<Setting>> entry : allSettings.entrySet()) {
			String name = entry.getKey();
			Group group = groups.get(name);
			
			List<Setting> groupSettings = entry.getValue();
			Collections.sort(groupSettings,
					new Comparator<Setting>() {

						public int compare(Setting o1, Setting o2) {
							return o1.getLabel().compareTo(o2.getLabel());
						}
				
			});
			
			if (group == null) {
				Composite section = new Composite(this, SWT.NONE);
				GridDataFactory.fillDefaults().grab(true, false).applyTo(section);
				GridLayoutFactory.fillDefaults().applyTo(section);
				
				Label label = new Label(section, SWT.NONE);
				label.setText(name);
				GridDataFactory.fillDefaults().span(4, 1).applyTo(label);
				
				group = new Group(section, SWT.SHADOW_OUT);
				GridLayoutFactory.fillDefaults().margins(6, 6).applyTo(group);
				GridDataFactory.fillDefaults().grab(true, true).applyTo(group);
				
				groups.put(name, group);
			}
			
			for (Setting setting : groupSettings) {
				SettingEntry comp = null;
				if (setting.getValue() instanceof String) {
					comp = new DiskEntry(group, setting);
				} else if (setting.getValue() instanceof Boolean) {
					comp = new BooleanEntry(group, setting);
				}
				if (comp != null) {
					GridDataFactory.fillDefaults().grab(true, false).applyTo(comp);
					comp.updateSetting();
				}
			}
		}
		
		this.pack();
	}

}
