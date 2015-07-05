/*
  FileExecutorComposite.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt;

import java.util.Collections;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import v9t9.common.client.IEmulatorContentHandler;
import v9t9.common.client.IEmulatorContentSource;

/**
 * Allow selecting a {@link IEmulatorContentHandler}
 * @author ejs
 *
 */
public class EmulatorSourceHandlerComposite extends Composite {
	
	private static String lastExecLabel;

	protected IEmulatorContentHandler selectedHandler;
	private ComboViewer handlerComboViewer;
	private Text descrText;

	public EmulatorSourceHandlerComposite(Composite parent) {
		super(parent, SWT.NONE);
		
		Composite composite = this;
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
		
		Label label;
		label = new Label(composite, SWT.WRAP);
		label.setText("Action:");
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).applyTo(label);
		
		handlerComboViewer = new ComboViewer(composite, SWT.BORDER | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(handlerComboViewer.getControl());
		handlerComboViewer.setLabelProvider(new LabelProvider() {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
			 */
			@Override
			public String getText(Object element) {
				IEmulatorContentHandler exec = (IEmulatorContentHandler) element;
				return exec != null ? exec.getLabel() : "Nothing";
			}
		}) ;
		handlerComboViewer.setContentProvider(new ArrayContentProvider());

		label = new Label(composite, SWT.WRAP);
		GridDataFactory.fillDefaults().grab(false, false).span(2, 1).applyTo(label);
		label.setText("Description:");
		
		descrText = new Text(composite, SWT.READ_ONLY | SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL) ;
		GridDataFactory.fillDefaults().grab(false, true).span(2, 1).indent(6, 0).minSize(-1, 96).applyTo(descrText);

		descrText.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		
		handlerComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IEmulatorContentHandler exec = (IEmulatorContentHandler) 
						((IStructuredSelection) event.getSelection()).getFirstElement();
				selectedHandler = exec;
				if (exec != null) {
					descrText.setText(exec.getDescription());
					descrText.setEnabled(true);
					lastExecLabel = exec.getLabel();
				} else {
					descrText.setText("");
					descrText.setEnabled(false);
				}
			}
		});


		handlerComboViewer.setInput(Collections.emptyList());
	}
	
	/**
	 * 
	 */
	public void updateExecs(IEmulatorContentSource source, IEmulatorContentHandler[] handlers) {
		
		if (selectedHandler != null) {
			boolean found = false;
			for (IEmulatorContentHandler e : handlers) {
				if (e.getLabel().equals(selectedHandler.getLabel())) {
					selectedHandler = e;
					found = true;
					break;
				}
			}
			if (!found) {
				selectedHandler = null;
			}
		}
		handlerComboViewer.setInput(handlers);
		
		if (selectedHandler == null) {
			if (lastExecLabel != null) {
				for (IEmulatorContentHandler exec : handlers) {
					if (exec.getLabel().equals(lastExecLabel)) {
						selectedHandler = exec;
						break;
					}
				}
			}
			if (selectedHandler == null) {
				selectedHandler = handlers.length == 1 ? handlers[0] : handlers[1];
			}
		}
		handlerComboViewer.setSelection(new StructuredSelection(selectedHandler));
		descrText.setText(selectedHandler.getDescription());
		descrText.setEnabled(true);
		
		handlerComboViewer.getControl().setEnabled(handlers != null && handlers.length > 1);
		
	}

	public IEmulatorContentHandler getContentHandler() {
		return selectedHandler;
	}

	/**
	 * @param handler
	 */
	public void setSelectedHandler(
			IEmulatorContentHandler handler) {
		this.selectedHandler = handler;
	}
	
}