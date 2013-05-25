/*
  SelectDiskImageDialog.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.fileimport;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import v9t9.common.files.Catalog;
import v9t9.common.files.IDiskDriveSetting;
import v9t9.common.files.IFileExecutor;
import v9t9.common.machine.IMachine;
import ejs.base.properties.IProperty;
import ejs.base.settings.ISettingProperty;

class SelectDiskImageDialog extends MessageDialog {

	private Map<String, IProperty> diskSettingMap;
	protected IProperty theProperty;
	private Catalog catalog;
	private IMachine machine;
	private FileExecutorComposite execComp;

	public SelectDiskImageDialog(Shell parentShell,  
			String dialogTitle,
			IMachine machine,
			Map<String, IProperty> diskSettingMap,
			Catalog catalog,
			String messageFormat,
			String label) {
		super(parentShell, dialogTitle, 
				null /*image*/,
				MessageFormat.format(messageFormat, 
						label.trim()),
				MessageDialogWithToggle.QUESTION,
				new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL },
				0);
		this.machine = machine;
		this.diskSettingMap = diskSettingMap;
		this.catalog = catalog;
		
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Load Disk");
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.MessageDialog#createCustomArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createCustomArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);
		
		Label label;
		label = new Label(composite, SWT.WRAP);
		label.setText("Drive:");
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).applyTo(label);
		
		final ComboViewer driveComboViewer = new ComboViewer(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(driveComboViewer.getControl());
		driveComboViewer.setLabelProvider(new LabelProvider() {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
			 */
			@Override
			public String getText(Object element) {
				@SuppressWarnings("unchecked")
				Map.Entry<String, IProperty> ent = (Entry<String, IProperty>) element;
				return ent != null ? ent.getValue().getLabel() : "???";
			}
		}) ;
		driveComboViewer.setContentProvider(new ArrayContentProvider());
		driveComboViewer.setComparator(new ViewerComparator());
		
		
		label = new Label(composite, SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(label);
		
		label = new Label(composite, SWT.WRAP);
		label.setText("If this disk contains programs, you may ask V9t9 to run them:");
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(label);
		
		
		execComp = new FileExecutorComposite(composite, machine);
		GridDataFactory.fillDefaults().grab(true, true).span(2, 1).indent(12, 12). applyTo(execComp);
		
		//////
		
		driveComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				@SuppressWarnings("unchecked")
				Map.Entry<String, IProperty> ent = (Entry<String, IProperty>) 
						((IStructuredSelection) event.getSelection()).getFirstElement();
				if (ent != null) {
					theProperty = ent.getValue();
				} else {
					theProperty = null;
				}
				
				updateExecs();
				
				Button button = getButton(OK);
				if (button != null) {
					button.setEnabled(theProperty != null);
				}
			}
		});


		// go
		
		driveComboViewer.setInput(diskSettingMap.entrySet());
		driveComboViewer.addFilter(new ViewerFilter() {
			
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				@SuppressWarnings("unchecked")
				Map.Entry<String, IProperty> ent = (Entry<String, IProperty>) element;
				if (ent.getValue() instanceof ISettingProperty) {
					return ((ISettingProperty) ent.getValue()).isEnabled();
				}
				return true;
			}
		});
		
		driveComboViewer.setSelection(new StructuredSelection(diskSettingMap.entrySet().iterator().next()));
				
		
		updateExecs();
		
		return composite;
	}
	
	/**
	 * 
	 */
	protected void updateExecs() {
		int drive = 1;
		if (theProperty != null) 
			drive = ((IDiskDriveSetting) theProperty).getDrive();
		catalog.deviceName = "DSK" + drive;
		execComp.updateExecs(drive, catalog, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.MessageDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getButton(OK).setEnabled(theProperty != null);
	}

	/**
	 * 
	 */
	public IProperty getDiskProperty() {
		return theProperty;
	}
	
	/**
	 * @return the execs
	 */
	public IFileExecutor getFileExecutor() {
		return execComp.getFileExecutor();
	}
	
}