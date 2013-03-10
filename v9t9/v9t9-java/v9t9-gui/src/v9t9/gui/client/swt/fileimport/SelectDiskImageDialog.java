/**
 * 
 */
package v9t9.gui.client.swt.fileimport;

import java.text.MessageFormat;
import java.util.Collections;
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
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import v9t9.common.files.Catalog;
import v9t9.common.files.IDiskImageSetting;
import v9t9.common.files.IFileExecutionHandler;
import v9t9.common.files.IFileExecutor;
import v9t9.common.machine.IMachine;

import ejs.base.properties.IProperty;

class SelectDiskImageDialog extends MessageDialog {

	private Map<String, IProperty> diskSettingMap;
	protected IProperty theProperty;
	private IFileExecutor[] execs;
	private IFileExecutionHandler execHandler;
	private Catalog catalog;
	protected IFileExecutor selectedExec;
	private ComboViewer execComboViewer;
	private IMachine machine;
	private Text descrText;

	public SelectDiskImageDialog(Shell parentShell,  
			String dialogTitle,
			IMachine machine,
			Map<String, IProperty> diskSettingMap,
			Catalog catalog,
			//IFileExecutionHandler execHandler,
			String diskVolumeName) {
		super(parentShell, dialogTitle, 
				null /*image*/,
				MessageFormat.format("Select the drive into which to load the disk ''{0}''", 
						diskVolumeName.trim()),
				MessageDialogWithToggle.QUESTION,
				new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL },
				0);
		this.machine = machine;
		this.diskSettingMap = diskSettingMap;
		this.catalog = catalog;
		this.execHandler = machine.getEmulatedFileHandler().getFileExecutionHandler();
		
		setShellStyle(getShellStyle() | SWT.RESIZE);
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
		
		
		//
		label = new Label(composite, SWT.WRAP);
		label.setText("Action:");
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).applyTo(label);
		
		execComboViewer = new ComboViewer(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(execComboViewer.getControl());
		execComboViewer.setLabelProvider(new LabelProvider() {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
			 */
			@Override
			public String getText(Object element) {
				IFileExecutor exec = (IFileExecutor) element;
				return exec != null ? exec.getLabel() : "Nothing";
			}
		}) ;
		execComboViewer.setContentProvider(new ArrayContentProvider());

		label = new Label(composite, SWT.WRAP);
		GridDataFactory.fillDefaults().grab(false, false).span(2, 1).applyTo(label);
		label.setText("Description:");
		
		descrText = new Text(composite, SWT.READ_ONLY | SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL) ;
		GridDataFactory.fillDefaults().grab(false, true).span(2, 1).indent(6, 0).minSize(-1, 96).applyTo(descrText);
		
		descrText.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		
		//////
		
		driveComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				@SuppressWarnings("unchecked")
				Map.Entry<String, IProperty> ent = (Entry<String, IProperty>) 
						((IStructuredSelection) event.getSelection()).getFirstElement();
				theProperty = ent.getValue();
				
				updateExecs();
				
				Button button = getButton(OK);
				if (button != null) {
					button.setEnabled(theProperty != null);
				}
			}
		});


		execComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IFileExecutor exec = (IFileExecutor) 
						((IStructuredSelection) event.getSelection()).getFirstElement();
				selectedExec = exec;
				if (exec != null) {
					descrText.setText(exec.getDescription());
					descrText.setEnabled(true);
				} else {
					descrText.setText("");
					descrText.setEnabled(false);
				}
			}
		});


		
		// go
		
		driveComboViewer.setInput(diskSettingMap.entrySet());
		execComboViewer.setInput(Collections.emptyList());
		
		driveComboViewer.setSelection(new StructuredSelection(diskSettingMap.entrySet().iterator().next()));
		
		updateExecs();
		
		return composite;
	}
	
	/**
	 * 
	 */
	protected void updateExecs() {
		if (catalog != null) {
			execs = execHandler.analyze(machine, ((IDiskImageSetting) theProperty).getDrive(), catalog);
			
			IFileExecutor[] allExecs = new IFileExecutor[execs.length + 1];
			allExecs[0] = new DoNothingFileExecutor();
			System.arraycopy(execs, 0, allExecs, 1, execs.length);
			execs = allExecs;
			
			if (selectedExec != null) {
				boolean found = false;
				for (IFileExecutor e : execs) {
					if (e.getLabel().equals(selectedExec.getLabel())) {
						selectedExec = e;
						found = true;
						break;
					}
				}
				if (!found) {
					selectedExec = null;
				}
			}
			execComboViewer.setInput(execs);
			
			if (selectedExec == null) {
				selectedExec = execs.length == 1 ? execs[0] : execs[1];
			}
			execComboViewer.setSelection(new StructuredSelection(selectedExec));
			descrText.setText(selectedExec.getDescription());
			descrText.setEnabled(true);
			
		} else {
			execs = null;
			selectedExec = null;
			execComboViewer.setInput(Collections.emptyList());
			descrText.setText("");
			descrText.setEnabled(false);
		}				
		
		execComboViewer.getControl().setEnabled(execs != null && execs.length > 1);
		
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
		return selectedExec;
	}
	
}