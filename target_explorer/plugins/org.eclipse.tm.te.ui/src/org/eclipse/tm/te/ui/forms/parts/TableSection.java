/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.forms.parts;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Target Explorer: Table section implementation.
 */
public class TableSection extends AbstractStructuredViewerSection implements ISelectionChangedListener, IDoubleClickListener {

	/**
	 * Table section table part adapter implementation.
	 */
	protected class TablePartAdapter extends TablePart {

		/**
		 * Constructor.
		 *
		 * @param labels The list of label to apply to the created buttons in the given order. Must not be <code>null</code>.
		 */
		public TablePartAdapter(String[] labels) {
			super(labels);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.ui.forms.parts.TablePart#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
		 */
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			TableSection.this.selectionChanged(event);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.ui.forms.parts.TablePart#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
		 */
		@Override
		public void doubleClick(DoubleClickEvent event) {
			TableSection.this.doubleClick(event);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.ui.forms.parts.AbstractPartWithButtons#onButtonSelected(org.eclipse.swt.widgets.Button)
		 */
		@Override
		protected void onButtonSelected(Button button) {
			TableSection.this.onButtonSelected(button);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.ui.forms.parts.AbstractPartWithButtons#createButtonsPanel(org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
		 */
		@Override
		protected void createButtonsPanel(Composite parent, FormToolkit toolkit) {
			super.createButtonsPanel(parent, toolkit);
			initializeButtonsEnablement();
		}
	}

	/**
	 * Constructor.
	 *
	 * @param form The parent managed form. Must not be <code>null</code>.
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @param style The section style.
	 * @param labels The list of label to apply to the created buttons in the given order. Must not be <code>null</code>.
	 */
	public TableSection(IManagedForm form, Composite parent, int style, String[] labels) {
		this(form, parent, style, true, labels);
	}

	/**
	 * Constructor.
	 *
	 * @param form The parent managed form. Must not be <code>null</code>.
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @param style The section style.
	 * @param titleBar If <code>true</code>, the title bar style bit is added to <code>style</code>.
	 * @param labels The list of label to apply to the created buttons in the given order. Must not be <code>null</code>.
	 */
	public TableSection(IManagedForm form, Composite parent, int style, boolean titleBar, String[] labels) {
		super(form, parent, style, titleBar, labels);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.forms.parts.AbstractStructuredViewerSection#createViewerPart(java.lang.String[])
	 */
	@Override
	protected AbstractStructuredViewerPart createViewerPart(String[] labels) {
		return new TablePartAdapter(labels);
	}

	/**
	 * Returns the table part instance.
	 *
	 * @return The table part instance.
	 */
	protected TablePart getTablePart() {
		return (TablePart)getViewerPart();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.forms.parts.AbstractSection#createClient(org.eclipse.ui.forms.widgets.Section, org.eclipse.ui.forms.widgets.FormToolkit)
	 */
	@Override
	protected void createClient(Section section, FormToolkit toolkit) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	public void doubleClick(DoubleClickEvent event) {
	}

	/**
	 * Initialize the enablement of the buttons in the buttons bar.
	 * <p>
	 * Called from {@link TablePartAdapter#createButtonsPanel(Composite, FormToolkit)}.
	 */
	protected void initializeButtonsEnablement() {
	}
}
