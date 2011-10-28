/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.ui.wizards.controls;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm.te.tcf.ui.nls.Messages;
import org.eclipse.tm.te.ui.dialogs.NameValuePairDialog;
import org.eclipse.tm.te.ui.forms.parts.TablePart;
import org.eclipse.tm.te.ui.swt.SWTControlUtil;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.services.IDisposable;

/**
 * Peer attributes table part implementation.
 */
public class PeerAttributesTablePart extends TablePart implements IDisposable {
	// The list of table nodes
	/* default */ final List<TableNode> nodes = new ArrayList<TableNode>();

	// A list of names which are banned from using
	private String[] bannedNames;

	/**
	 * Peer attributes table table node implementation.
	 */
	protected static class TableNode extends PlatformObject {
		/**
		 * The node name.
		 */
		public String name = ""; //$NON-NLS-1$

		/**
		 * The node value.
		 */
		public String value = ""; //$NON-NLS-1$
	}

	/**
	 * Peer attributes table label provider implementation.
	 */
	protected static class TableLabelProvider extends LabelProvider implements ITableLabelProvider {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		@Override
		public String getColumnText(Object element, int columnIndex) {
			String text = null;
			if (element instanceof TableNode) {
				switch (columnIndex) {
				case 0:
					text = ((TableNode)element).name;
					break;
				case 1:
					text = ((TableNode)element).value;
					break;
				}
			}
			return text != null ? text : ""; //$NON-NLS-1$
		}
	}

	/**
	 * Constructor.
	 */
	public PeerAttributesTablePart() {
		super(new String[] {
						Messages.PeerAttributesTablePart_button_new,
						Messages.PeerAttributesTablePart_button_edit,
						Messages.PeerAttributesTablePart_button_remove
		});

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.services.IDisposable#dispose()
	 */
	@Override
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.forms.parts.TablePart#configureTableViewer(org.eclipse.jface.viewers.TableViewer)
	 */
	@Override
	protected void configureTableViewer(final TableViewer viewer) {
		super.configureTableViewer(viewer);

		if (viewer != null && viewer.getTable() != null && !viewer.getTable().isDisposed()) {
			Table table = viewer.getTable();

			// Create the table columns
			new TableColumn(table, SWT.NONE).setText(Messages.PeerAttributesTablePart_column_name);
			new TableColumn(table, SWT.NONE).setText(Messages.PeerAttributesTablePart_column_value);

			// Create and configure the table layout
			TableLayout tableLayout = new TableLayout();
			tableLayout.addColumnData(new ColumnWeightData(40, true));
			tableLayout.addColumnData(new ColumnWeightData(60, true));
			table.setLayout(tableLayout);

			table.setHeaderVisible(true);
			table.setLinesVisible(true);

			// Setup the cell editors
			viewer.setColumnProperties(new String[] { Messages.PeerAttributesTablePart_column_name, Messages.PeerAttributesTablePart_column_value });

			CellEditor[] cellEditors = new CellEditor[viewer.getColumnProperties().length];
			cellEditors[0] = new TextCellEditor(table);
			((Text)cellEditors[0].getControl()).setTextLimit(250);
			cellEditors[1] = new TextCellEditor(table);
			((Text)cellEditors[1].getControl()).setTextLimit(250);

			viewer.setCellEditors(cellEditors);
			viewer.setCellModifier(new ICellModifier() {
				@Override
				public boolean canModify(Object element, String property) {
				    return element instanceof TableNode;
				}
				@Override
				public Object getValue(Object element, String property) {
					String value = null;
					if (element instanceof TableNode) {
						if (Messages.PeerAttributesTablePart_column_name.equals(property)) {
							value = ((TableNode)element).name;
						}
						else if (Messages.PeerAttributesTablePart_column_value.equals(property)) {
							value = ((TableNode)element).value;
						}
					}
				    return value;
				}
				@Override
				public void modify(Object element, String property, Object value) {
					if (element instanceof TableItem) element = ((TableItem)element).getData();
					if (element instanceof TableNode) {
						if (Messages.PeerAttributesTablePart_column_name.equals(property)) {
							((TableNode)element).name = value != null ? value.toString() : ""; //$NON-NLS-1$
						}
						else if (Messages.PeerAttributesTablePart_column_value.equals(property)) {
							((TableNode)element).value = value != null ? value.toString() : ""; //$NON-NLS-1$
						}
						viewer.setInput(nodes);
					}
				}
			});

			// Create and set content and label provider
			viewer.setContentProvider(new ArrayContentProvider());
			viewer.setLabelProvider(new TableLabelProvider());

			// Attach listeners
			viewer.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					updateButtons();
				}
			});
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.forms.parts.AbstractPartWithButtons#createControl(org.eclipse.swt.widgets.Composite, int, int, org.eclipse.ui.forms.widgets.FormToolkit)
	 */
	@Override
	public void createControl(Composite parent, int style, int span, FormToolkit toolkit) {
		super.createControl(parent, style, span, toolkit);
		nodes.clear();
		getTableViewer().setInput(nodes);
		updateButtons();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.forms.parts.AbstractPartWithButtons#onButtonSelected(org.eclipse.swt.widgets.Button)
	 */
	@Override
	protected void onButtonSelected(Button button) {
		Assert.isNotNull(button);

		if (Messages.PeerAttributesTablePart_button_new.equals(button.getText())) {
			onNewPressed();
		}
		else if (Messages.PeerAttributesTablePart_button_edit.equals(button.getText())) {
			onEditPressed();
		}
		else if (Messages.PeerAttributesTablePart_button_remove.equals(button.getText())) {
			onRemovePressed();
		}
	}

	/**
	 * Update the button enablements.
	 */
	protected void updateButtons() {
		int selectionCount = getTableViewer().getTable().getSelectionCount();

		SWTControlUtil.setEnabled(getButton(Messages.PeerAttributesTablePart_button_edit), selectionCount == 1);
		SWTControlUtil.setEnabled(getButton(Messages.PeerAttributesTablePart_button_remove), selectionCount == 1);
	}

	/**
	 * Called from {@link #onButtonSelected(Button)} if "New..." got pressed.
	 */
	protected void onNewPressed() {
		doEditTableNode(null);
	}

	/**
	 * Called from {@link #onButtonSelected(Button)} if "Edit..." got pressed.
	 */
	protected void onEditPressed() {
		// Get the selection from the table
		ISelection selection = getTableViewer().getSelection();
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			doEditTableNode((TableNode)((IStructuredSelection)selection).getFirstElement());
		}
	}

	/**
	 * Called from {@link #onButtonSelected(Button)} if "Remove..." got pressed.
	 */
	protected void onRemovePressed() {
		// Get the selection from the table
		ISelection selection = getTableViewer().getSelection();
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			TableNode node = (TableNode)((IStructuredSelection)selection).getFirstElement();
			int index = nodes.indexOf(node);
			nodes.remove(node);
			getTableViewer().setInput(nodes);
			if (index < nodes.size()) getTableViewer().setSelection(new StructuredSelection(nodes.get(index)));
			updateButtons();
		}
	}

	/**
	 * Opens the name/pair dialog.
	 *
	 * @param node The node to edit or <code>null</code>.
	 */
	protected void doEditTableNode(TableNode node) {
		// If the node is null, the dialog will be opened as "Add" dialog
		boolean addMode = node == null;

		// Determine the initial values
		String name = node != null ? node.name : ""; //$NON-NLS-1$
		String value = node != null ? node.value : ""; //$NON-NLS-1$

		// Determine the used names
		Set<String> usedNames = new HashSet<String>(convertList2Map(nodes).keySet());

		// Add banned names to the used names list
		if (bannedNames != null) {
			for (String bannedName : bannedNames) {
				usedNames.add(bannedName);
			}
		}

		// Remove the current name
		usedNames.remove(name);

		// Determine the dialog title, the title and the default message
		String dialogTitle = addMode ? Messages.PeerAttributesTablePart_add_dialogTitle : Messages.PeerAttributesTablePart_edit_dialogTitle;
		String title = addMode ? Messages.PeerAttributesTablePart_add_title : Messages.PeerAttributesTablePart_edit_title;
		String message = addMode ? Messages.PeerAttributesTablePart_add_message : Messages.PeerAttributesTablePart_edit_message;

		// Construct the name/value pair dialog
		NameValuePairDialog dialog = new NameValuePairDialog(getViewer().getControl().getShell(),
							dialogTitle, title, message,
							new String[] { Messages.PeerAttributesTablePart_column_name, Messages.PeerAttributesTablePart_column_value },
							new String[] { name, value },
							usedNames
						);

		// Open the dialog
		if (dialog.open() == Window.OK) {
			// If the user pressed OK, copy the data to the given node
			// or create a new node in add mode.
			String[] pair = dialog.getNameValuePair();
			if (addMode) {
				node = new TableNode();
				nodes.add(node);
			}

			Assert.isNotNull(node);
			node.name = pair[0];
			node.value = pair[1];

			// Refresh the view
			getTableViewer().setInput(nodes);
			getTableViewer().setSelection(new StructuredSelection(node));
		}
	}

	/**
	 * Convert the given list into a map.
	 *
	 * @param list The list of table node. Must not be <code>null</code>:
	 * @return The corresponding map.
	 */
	private static Map<String, String> convertList2Map(List<TableNode> list) {
		Assert.isNotNull(list);
		Map<String, String> map = new LinkedHashMap<String, String>();
		for (TableNode node : list) {
			map.put(node.name, node.value);
		}
		return map;
	}

	/**
	 * Returns the configured attributes.
	 *
	 * @return The configured attributes.
	 */
	public Map<String, String> getAttributes() {
		return convertList2Map(nodes);
	}

	/**
	 * Set a list of banned names.
	 *
	 * @param bannedNames The list of banned names or <code>null</code>.
	 */
	public final void setBannedNames(String[] bannedNames) {
		this.bannedNames = bannedNames;
	}

	/**
	 * Returns the list of banned names.
	 *
	 * @return The list of banned names or <code>null</code>.
	 */
	public final String[] getBannedNames() {
		return bannedNames;
	}
}
