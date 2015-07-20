/*
  DeviceSelectorDialog.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.disk;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import v9t9.common.dsr.DeviceEditorIdConstants;
import v9t9.common.dsr.IDeviceSettings;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.IconSettingProperty;
import v9t9.common.settings.SettingSchemaProperty;
import v9t9.gui.client.swt.bars.ImageCanvas;
import v9t9.gui.client.swt.shells.IToolShellFactory;
import ejs.base.properties.IProperty;

/**
 * Edit settings from DSR groups
 * @author ejs
 *
 */
public class DeviceSettingsDialog extends Composite implements IDeviceSelectorDialog {

	public static final String DEVICE_SETTINGS_TOOL_ID = "device.settings";

	public static IToolShellFactory getToolShellFactory(final IMachine machine, final ImageCanvas buttonBar,
			final String title, final String[] settingGroups) {
		return new IToolShellFactory() {
			Behavior behavior = new Behavior();
			{
				behavior.boundsPref = "DeviceWindowBounds." + title;
				behavior.centering = Centering.INSIDE;
				behavior.centerOverControl = null;
				behavior.dismissOnClickOutside = true;
				behavior.defaultBounds = new Rectangle(0, 0, 700, 300);
			}
			public Control createContents(Shell shell) {
				return new DeviceSettingsDialog(shell, machine, title, settingGroups);
			}
			public Behavior getBehavior() {
				return behavior;
			}
		};
	}

	protected IMachine machine;
	protected boolean needReset;
	private String[] settingGroups;
	private EntryHistory entryHistory;
	
	public DeviceSettingsDialog(Shell shell, IMachine machine_, String title, String[] settingGroups) {
		
		super(shell, SWT.NONE);
		this.machine = machine_;
		this.settingGroups = settingGroups;
		this.entryHistory = new EntryHistory(machine.getSettings().getUserSettings());
		
		List<IDeviceSettings> list = machine.getModel().getDeviceSettings(machine);
		
		shell.setText(title);

		GridLayoutFactory.fillDefaults().margins(6, 6).applyTo(this);

		Map<String, Group> groups = new HashMap<String, Group>();
		Map<String, Set<IProperty>> allSettings = new LinkedHashMap<String, Set<IProperty>>();
		
		for (IDeviceSettings setting : list) {
			Map<String, Collection<IProperty>> settings = setting.getEditableSettingGroups();
			for (Map.Entry<String, Collection<IProperty>> entry : settings.entrySet()) {
				if (!acceptsGroup(entry.getKey()))
					continue;
				Set<IProperty> groupSettings = allSettings.get(entry.getKey());
				if (groupSettings == null) {
					groupSettings = new LinkedHashSet<IProperty>();
					allSettings.put(entry.getKey(), groupSettings);
				}
				groupSettings.addAll(entry.getValue());
			}
		}
		
		for (Map.Entry<String, Set<IProperty>> entry : allSettings.entrySet()) {
			String name = entry.getKey();
			Group group = groups.get(name);
			
			Set<IProperty> groupSettings = entry.getValue();
			IProperty[] groupSettingsArr = groupSettings.toArray(new IProperty[groupSettings.size()]);
			Arrays.sort(groupSettingsArr,
				new Comparator<IProperty>() {

					public int compare(IProperty o1, IProperty o2) {
						if (o1 instanceof IconSettingProperty && o2 instanceof IconSettingProperty)
							return o1.getLabel().compareTo(o2.getLabel());
						else if (o1 instanceof IconSettingProperty)
							return -1;
						else if (o2 instanceof IconSettingProperty)
							return 1;
						else
							return 0; // keep original order
					}
			});
			groupSettings.clear();
			groupSettings.addAll(Arrays.asList(groupSettingsArr));
			
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
			
			
			for (IProperty setting : groupSettings) {
				if (setting.isHidden()) 
					continue;
				BaseSettingEntry comp = null;

				String editorId = null;
				if (setting instanceof SettingSchemaProperty) {
					editorId = ((SettingSchemaProperty) setting).getSchema().getEditorId();
				}
				
				if (editorId == null) {
					if (setting.getValue() instanceof String || setting.getType() == String.class) {
						editorId = DeviceEditorIdConstants.ID_TEXT;
					} else if (setting.getValue() instanceof Boolean) {
						editorId = DeviceEditorIdConstants.ID_CHECKBOX;
					} else if (setting.getType().isEnum()) {
						editorId = DeviceEditorIdConstants.ID_DROPDOWN;
					}
				}
				if (editorId != null) {
					comp = createEditor(editorId, group, setting);
				}
				if (comp != null) {
					comp.createControls(comp);
					GridDataFactory.fillDefaults().grab(true, false).applyTo(comp);
					comp.updateSetting();
				} else {		
					System.err.println("cannot make editor for " + setting);
				}
			}
		}
		
		this.pack();
		
		getShell().addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (needReset)
					machine.reset();
			}
		});
	}
	
	protected BaseSettingEntry createEditor(String editorId, Composite group, IProperty setting) {
		if (DeviceEditorIdConstants.ID_DISK_IMAGE.equals(editorId)) {
			return new DiskImageEntry(this, group, setting);
		} else if (DeviceEditorIdConstants.ID_DISK_DIRECTORY.equals(editorId)) {
			return new DiskDirectoryEntry(this, group, setting);
		} else if (DeviceEditorIdConstants.ID_CASSETTE_FILE.equals(editorId)) {
			return new CassetteFileEntry(this, group, setting);
		} else if (DeviceEditorIdConstants.ID_CHECKBOX.equals(editorId)) {
			return new DiskEnableEntry(this, group, setting);
		} else if (DeviceEditorIdConstants.ID_DROPDOWN.equals(editorId)) {
			return new DiskComboEntry(this, group, setting);
		} else if (DeviceEditorIdConstants.ID_PRESS_ENTER.equals(editorId)) {
			return new PressEnterEntry(this, group, setting);
		} else {
			return null;
		}
	}

	protected boolean acceptsGroup(String key) {
		for (String group : settingGroups) {
			if (group.equals(key))
				return true;
		}
		return false;
	}

	/**
	 * @return the machine
	 */
	public IMachine getMachine() {
		return machine;
	}


	/**
	 * 
	 */
	public void warnResetNeeded() {
		if (!needReset) {
			needReset = true;
			MessageDialog.openInformation(getShell(), "Reset Needed", 
					"Changing this setting requires restarting the emulated machine.\n\n"+
					"This will happen when this dialog is closed.");
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.disk.IDeviceSelectorDialog#getEntryHistory()
	 */
	@Override
	public EntryHistory getEntryHistory() {
		return entryHistory;
	}
}
