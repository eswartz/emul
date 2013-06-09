/*
  DiskSelectorDialog.java

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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import v9t9.common.dsr.IDeviceSettings;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.IconSettingProperty;
import v9t9.gui.client.swt.bars.ImageCanvas;
import v9t9.gui.client.swt.shells.IToolShellFactory;
import ejs.base.properties.IProperty;

/**
 * Select and set up disks
 * @author ejs
 *
 */
public class DiskSelectorDialog extends Composite {

	public static final String DISK_SELECTOR_TOOL_ID = "disk.selector";

	public static IToolShellFactory getToolShellFactory(final IMachine machine, final ImageCanvas buttonBar) {
		return new IToolShellFactory() {
			Behavior behavior = new Behavior();
			{
				behavior.boundsPref = "DiskWindowBounds";
				behavior.centering = Centering.INSIDE;
				behavior.centerOverControl = buttonBar;
				behavior.dismissOnClickOutside = true;
			}
			public Control createContents(Shell shell) {
				return new DiskSelectorDialog(shell, machine);
			}
			public Behavior getBehavior() {
				return behavior;
			}
		};
	}

	
	public DiskSelectorDialog(Shell shell, IMachine machine) {
		
		super(shell, SWT.NONE);
		
		List<IDeviceSettings> list = machine.getModel().getDeviceSettings(machine);
		
		shell.setText("Disk Selector");

		GridLayoutFactory.fillDefaults().margins(6, 6).applyTo(this);

		Map<String, Group> groups = new HashMap<String, Group>();
		Map<String, Set<IProperty>> allSettings = new LinkedHashMap<String, Set<IProperty>>();
		
		for (IDeviceSettings setting : list) {
			Map<String, Collection<IProperty>> settings = setting.getEditableSettingGroups();
			for (Map.Entry<String, Collection<IProperty>> entry : settings.entrySet()) {
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
						else
							return 0;
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
				DiskSettingEntry comp = null;
				if (setting.getValue() instanceof String) {
					comp = new DiskEntry(group, machine, setting);
				} else if (setting.getValue() instanceof Boolean) {
					comp = new DiskEnableEntry(group, machine, setting);
				} else if (setting.getValue() != null && setting.getType().isEnum()) {
					comp = new DiskComboEntry(group, machine, setting);
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
