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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Target Explorer: Abstract table section implementation.
 */
public abstract class AbstractTableSection extends AbstractStructuredViewerSection implements ISelectionChangedListener, IDoubleClickListener {

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
			AbstractTableSection.this.selectionChanged(event);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.ui.forms.parts.TablePart#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
		 */
		@Override
		public void doubleClick(DoubleClickEvent event) {
			AbstractTableSection.this.doubleClick(event);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.ui.forms.parts.AbstractPartWithButtons#onButtonSelected(org.eclipse.swt.widgets.Button)
		 */
		@Override
		protected void onButtonSelected(Button button) {
			AbstractTableSection.this.onButtonSelected(button);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.ui.forms.parts.AbstractPartWithButtons#createButtonsPanel(org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
		 */
		@Override
		protected Composite createButtonsPanel(Composite parent, FormToolkit toolkit) {
			Composite panel = super.createButtonsPanel(parent, toolkit);
			initializeButtonsEnablement();
			return panel;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.ui.forms.parts.TablePart#createTableViewer(org.eclipse.swt.widgets.Composite, int)
		 */
		@Override
		protected TableViewer createTableViewer(Composite parent, int style) {
			return AbstractTableSection.this.createTableViewer(parent, style);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.ui.forms.parts.TablePart#configureTableViewer(org.eclipse.jface.viewers.TableViewer)
		 */
		@Override
		protected void configureTableViewer(TableViewer viewer) {
			super.configureTableViewer(viewer);
			AbstractTableSection.this.configureTableViewer(viewer);
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
	public AbstractTableSection(IManagedForm form, Composite parent, int style, String[] labels) {
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
	public AbstractTableSection(IManagedForm form, Composite parent, int style, boolean titleBar, String[] labels) {
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

	/**
	 * Creates the table viewer instance.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @param style The viewer style.
	 *
	 * @return The table viewer instance.
	 */
	protected TableViewer createTableViewer(Composite parent, int style) {
		return new TableViewer(parent, style);
	}

	/**
	 * Configures the table viewer instance.
	 *
	 * @param viewer The table viewer instance. Must not be <code>null</code<.
	 */
	protected void configureTableViewer(TableViewer viewer) {
		Assert.isNotNull(viewer);
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
