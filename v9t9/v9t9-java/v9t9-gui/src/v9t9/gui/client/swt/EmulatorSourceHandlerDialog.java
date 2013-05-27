/*
  EmulatorSourceHandlerDialog.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt;

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

import v9t9.common.client.IClient;
import v9t9.common.client.IEmulatorContentHandler;
import v9t9.common.client.IEmulatorContentSource;

/**
 * @author ejs
 *
 */
public class EmulatorSourceHandlerDialog extends MessageDialog {

	private EmulatorSourceHandlerComposite handlerComposite;

	private IClient client;

	private IEmulatorContentSource[] sources;

	private IEmulatorContentSource selectedSource;

	private ComboViewer sourceViewer;
	
	/**
	 * @param parentShell
	 * @param sources 
	 */
	public EmulatorSourceHandlerDialog(Shell parentShell, IClient client,
			IEmulatorContentSource[] sources) {
		super(parentShell, 
				"Handle Content", 
				null /*image*/,
				sources.length == 1 ?  "V9t9 recognized the content."
						: "V9t9 recognized one or more kinds of content.",
				MessageDialogWithToggle.QUESTION,
				new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL },
				0);
		
		
		this.client = client;
		this.sources = sources;
		
		setShellStyle(getShellStyle() | SWT.RESIZE);

	}

	protected void setHandlers(IEmulatorContentSource source, IEmulatorContentHandler[] handlers) {
		this.selectedSource = source;
		handlerComposite.updateExecs(source, handlers);
	}

	public IEmulatorContentHandler getHandler() {
		return handlerComposite != null ? handlerComposite.getContentHandler() : null;
	}
	

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Handle Content");
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

		if (sources.length != 1) {
			label = new Label(composite, SWT.WRAP);
			label.setText("Content:");
			sourceViewer = new ComboViewer(composite, SWT.BORDER | SWT.READ_ONLY);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(sourceViewer.getControl());
			sourceViewer.setLabelProvider(new LabelProvider() {
				/* (non-Javadoc)
				 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
				 */
				@Override
				public String getText(Object element) {
					IEmulatorContentSource source = (IEmulatorContentSource) element;
					return source.getLabel();
				}
			}) ;
			sourceViewer.setContentProvider(new ArrayContentProvider());
			sourceViewer.setComparator(new ViewerComparator());
	
		} else {
			label = new Label(composite, SWT.WRAP);
			label.setText(sources[0].getLabel());
			
		}
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).applyTo(label);
		
//		label = new Label(composite, SWT.HORIZONTAL);
//		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(label);
		
//		descrLabel = new Label(composite, SWT.WRAP);
//		descrLabel.setText("Description:");
//		GridDataFactory.fillDefaults().grab(true, false).indent(6, 6).span(2, 1).applyTo(descrLabel);
		
		handlerComposite = new EmulatorSourceHandlerComposite(composite);
		GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(handlerComposite);
		
		//////
		
		if (sourceViewer != null) {
			sourceViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					IEmulatorContentSource source = 
							(IEmulatorContentSource) ((IStructuredSelection) event.getSelection()).getFirstElement();
	
					setHandlers(source, client.getEmulatorContentHandlers(source));
					
					Button button = getButton(OK);
					if (button != null) {
						button.setEnabled(selectedSource != null);
					}
				}
			});
	
			// go
			sourceViewer.setInput(sources);
			
			if (sources.length > 0) {
				sourceViewer.setSelection(new StructuredSelection(sources[0]));
			}
		} else {
			setHandlers(sources[0], client.getEmulatorContentHandlers(sources[0]));
		}
		
		
		return composite;
	}

}
