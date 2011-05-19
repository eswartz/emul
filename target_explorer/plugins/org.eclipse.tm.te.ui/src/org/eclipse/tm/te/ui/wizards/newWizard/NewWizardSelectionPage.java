/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.wizards.newWizard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tm.te.ui.activator.UIPlugin;
import org.eclipse.tm.te.ui.interfaces.IUIConstants;
import org.eclipse.tm.te.ui.nls.Messages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.ITriggerPoint;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.internal.activities.ws.WorkbenchTriggerPoints;
import org.eclipse.ui.internal.dialogs.WizardContentProvider;
import org.eclipse.ui.internal.dialogs.WizardPatternFilter;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * Target Explorer: The New Target creation wizard selection page implementation.
 */
@SuppressWarnings("restriction")
public class NewWizardSelectionPage extends WizardPage {
	// The wizards settings storage id where the expanded categories are remembered
	private static final String EXPANDED_CATEGORIES_SETTINGS_ID = "filteredTree.expandedCatogryIds"; //$NON-NLS-1$

	// The wizards settings storage id where the selected wizard descriptor id is remembered
	private static final String SELECTED_WIZARD_DESCRIPTOR_SETTINGS_ID = "filteredTree.selectedWizardDescriptorId"; //$NON-NLS-1$

	// The default expanded category id's
	private static final String[] DEFAULT_EXPANDED_CATEGORY_IDS = new String[] { "org.eclipse.tm.te.ui.newWizards.category.general" }; //$NON-NLS-1$

	// The new target wizard registry
	private NewWizardRegistry wizardRegistry;

	// References to the page subcontrol's
	private FilteredTree filteredTree;
	private PatternFilter filteredTreeFilter;

	// The selected wizard descriptor
	private IWizardDescriptor selectedWizardDescriptor;

	// The wizard nodes per wizard descriptor
	private final Map<IWizardDescriptor, IWizardNode> wizardNodes = new HashMap<IWizardDescriptor, IWizardNode>();

	// The workbench instance as passed in by init(...)
	private IWorkbench workbench;
	// The selection as passed in by init(...)
	private IStructuredSelection selection;

	/**
	 * Internal class. The wizard viewer comparator is responsible for
	 * the sorting in the tree. Current implementation is not prioritizing
	 * categories.
	 */
	/* default */ static class NewWizardViewerComparator extends ViewerComparator {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ViewerComparator#isSorterProperty(java.lang.Object, java.lang.String)
		 */
		@Override
		public boolean isSorterProperty(Object element, String property) {
			// The comparator is affected if the label of the elements should change.
			return property.equals(IBasicPropertyConstants.P_TEXT);
		}
	}

 	/**
	 * Constructor.
	 *
	 * @param wizardRegistry The new target wizard registry. Must not be <code>null</code>.
	 */
	public NewWizardSelectionPage(NewWizardRegistry wizardRegistry) {
		super(NewWizardSelectionPage.class.getSimpleName());

		setTitle(getDefaultTitle());
		setDescription(getDefaultDescription());

		Assert.isNotNull(wizardRegistry);
		this.wizardRegistry = wizardRegistry;
	}

	/**
	 * Returns the default page title.
	 *
	 * @return The default page title. Must be never <code>null</code>.
	 */
	protected String getDefaultTitle() {
		return Messages.NewWizardSelectionPage_title;
	}

	/**
	 * Returns the default page description.
	 *
	 * @return The default page description. Must be never <code>null</code>.
	 */
	protected String getDefaultDescription() {
		return Messages.NewWizardSelectionPage_description;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		label.setText(Messages.NewWizardSelectionPage_wizards);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		filteredTreeFilter = new WizardPatternFilter();
		filteredTree = new FilteredTree(composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, filteredTreeFilter, true);
		filteredTree.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.heightHint = 325; layoutData.widthHint = 450;
		filteredTree.setLayoutData(layoutData);

		final TreeViewer treeViewer = filteredTree.getViewer();
		treeViewer.setContentProvider(new WizardContentProvider());
		treeViewer.setLabelProvider(new WorkbenchLabelProvider());
		treeViewer.setComparator(new NewWizardViewerComparator());

		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				onSelectionChanged();
			}
		});
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				// Double-click on a connection type is triggering the sub wizard
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection)event.getSelection();
					// The tree is single selection, so look for the first element only.
					Object element = selection.getFirstElement();
					if (element instanceof IWizardDescriptor) {
						// Double-click on a connection type is triggering the sub wizard
						if (canFlipToNextPage()) getWizard().getContainer().showPage(getNextPage());
					} else if (event.getViewer() instanceof TreeViewer) {
						TreeViewer viewer = (TreeViewer)event.getViewer();
						if (viewer.isExpandable(element)) {
							viewer.setExpandedState(element, !viewer.getExpandedState(element));
						}
					}
				}
			}
		});

		treeViewer.setInput(wizardRegistry.getRootCategory());

		// apply the standard dialog font
		Dialog.applyDialogFont(composite);

		setControl(composite);

		// Restore the tree state
		restoreWidgetValues();

		// Initialize the context help id
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IUIConstants.HELP_NEW_WIZARD_SELECTION_PAGE);
	}

	/**
	 * Initialize the page with the current workbench instance and the
	 * current workbench selection.
	 *
	 * @param workbench The current workbench.
	 * @param selection The current object selection.
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
	}

	/**
	 * Returns the current workbench.
	 *
	 * @return The current workbench or <code>null</code> if not set.
	 */
	public IWorkbench getWorkbench() {
		return workbench;
	}

	/**
	 * Returns the current object selection.
	 *
	 * @return The current object selection or <code>null</code> if not set.
	 */
	public IStructuredSelection getSelection() {
		return selection;
	}

	/**
	 * Called from the selection listener to propagate the current
	 * system type selection to the underlying wizard.
	 */
	protected void onSelectionChanged() {
		if (filteredTree.getViewer().getSelection() instanceof IStructuredSelection) {
			IStructuredSelection filteredTreeSelection = (IStructuredSelection)filteredTree.getViewer().getSelection();
			if (filteredTreeSelection.getFirstElement() instanceof IWizardDescriptor) {
				selectedWizardDescriptor = (IWizardDescriptor)filteredTreeSelection.getFirstElement();

				// Update the description if the current wizard descriptor has one
				if (selectedWizardDescriptor.getDescription() != null && !"".equals(selectedWizardDescriptor.getDescription())) { //$NON-NLS-1$
					setDescription(selectedWizardDescriptor.getDescription());
				} else {
					if (!getDefaultDescription().equals(getDescription())) setDescription(getDefaultDescription());
				}
			} else {
				selectedWizardDescriptor = null;
			}

			// Create the wizard node for the selected descriptor if not yet done
			if (!wizardNodes.containsKey(selectedWizardDescriptor)) {
				wizardNodes.put(selectedWizardDescriptor, new NewWizardNode(this, selectedWizardDescriptor));
			}
		}

		// Update the wizard container UI elements
		IWizardContainer container = getContainer();
		if (container != null && container.getCurrentPage() != null) {
			container.updateWindowTitle();
			container.updateTitleBar();
			container.updateButtons();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
	 */
	@Override
	public IWizardPage getNextPage() {
        ITriggerPoint triggerPoint = workbench.getActivitySupport().getTriggerPointManager().getTriggerPoint(WorkbenchTriggerPoints.NEW_WIZARDS);
        if (triggerPoint == null || WorkbenchActivityHelper.allowUseOf(triggerPoint, getSelectedNode())) {
        	IWizardNode selectedNode = getSelectedNode();
            if (selectedNode != null) {
            	// Determine if the content got create before(!) triggering
            	// the wizard creation
        		boolean isCreated = selectedNode.isContentCreated();
            	// Get the wizard from the selected node (triggers wizard creation if needed)
            	IWizard wizard = selectedNode.getWizard();
            	if (wizard != null) {
            		// If the wizard got created by the call to getWizard(),
            		// then allow the wizard to create its pages
            		if (!isCreated) wizard.addPages();
            		// Return the starting page of the wizard
            		return wizard.getStartingPage();
            	}
            }
		}
        return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#canFlipToNextPage()
	 */
	@Override
	public boolean canFlipToNextPage() {
		if (selectedWizardDescriptor != null && selectedWizardDescriptor.hasPages()) {
			return super.canFlipToNextPage();
		}
		return false;
	}

	/**
	 * Returns if or if not the wizard can be finished early.
	 *
	 * @return <code>True</code> if the wizard can be finished early, <code>false</code> otherwise.
	 */
	public boolean canFinishEarly() {
		return selectedWizardDescriptor != null && selectedWizardDescriptor.canFinishEarly();
	}

	/**
	 * Returns the wizard node for the currently selected
	 * wizard descriptor.
	 *
	 * @return The wizard node or <code>null</code> if none.
	 */
	public IWizardNode getSelectedNode() {
		IWizardNode node = null;
		if (selectedWizardDescriptor != null) {
			node = wizardNodes.get(selectedWizardDescriptor);
		}
		return node;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getDialogSettings()
	 */
	@Override
	protected IDialogSettings getDialogSettings() {
		// If the wizard is set and returns dialog settings, we re-use them here
		IDialogSettings settings = super.getDialogSettings();
		// If the dialog settings could not set from the wizard, fallback to the plugin's
		// dialog settings store.
		if (settings == null) settings = UIPlugin.getDefault().getDialogSettings();
		String sectionName = this.getClass().getName();
		if (settings.getSection(sectionName) == null) settings.addNewSection(sectionName);
		settings = settings.getSection(sectionName);

		return settings;
	}

	/**
	 * Restore the tree state from the dialog settings.
	 */
	public void restoreWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			String[] expandedCategories = settings.getArray(EXPANDED_CATEGORIES_SETTINGS_ID);
			// by default we expand always the "General" category.
			if (expandedCategories == null) expandedCategories = DEFAULT_EXPANDED_CATEGORY_IDS;
			if (expandedCategories != null) {
				List<IWizardCategory> expanded = new ArrayList<IWizardCategory>();
				for (int i = 0; i < expandedCategories.length; i++) {
					String categoryId = expandedCategories[i];
					if (categoryId != null && !"".equals(categoryId.trim())) { //$NON-NLS-1$
						IWizardCategory category = wizardRegistry.findCategory(categoryId);
						if (category != null && !expanded.contains(category)) {
							expanded.add(category);
						}
					}
				}

				if (expanded.size() > 0) filteredTree.getViewer().setExpandedElements(expanded.toArray());
			}

			String selectedWizardDescriptorId = settings.get(SELECTED_WIZARD_DESCRIPTOR_SETTINGS_ID);
			if (selectedWizardDescriptorId != null && !"".equals(selectedWizardDescriptorId.trim())) { //$NON-NLS-1$
				IWizardDescriptor descriptor = wizardRegistry.findWizard(selectedWizardDescriptorId);
				if (descriptor != null) {
					filteredTree.getViewer().setSelection(new StructuredSelection(descriptor), true);
				}
			}
		}
	}

	/**
	 * Saves the tree state to the wizards settings store.
	 */
	public void saveWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			List<String> expandedCategories = new ArrayList<String>();
			Object[] expanded = filteredTree.getViewer().getVisibleExpandedElements();
			for (int i = 0; i < expanded.length; i++) {
				if (expanded[i] instanceof IWizardCategory) {
					expandedCategories.add(((IWizardCategory)expanded[i]).getId());
				}
			}
			settings.put(EXPANDED_CATEGORIES_SETTINGS_ID, expandedCategories.toArray(new String[expandedCategories.size()]));

			if (selectedWizardDescriptor != null) {
				settings.put(SELECTED_WIZARD_DESCRIPTOR_SETTINGS_ID, selectedWizardDescriptor.getId());
			} else {
				settings.put(SELECTED_WIZARD_DESCRIPTOR_SETTINGS_ID, ""); //$NON-NLS-1$
			}
		}
	}
}