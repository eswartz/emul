/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.views.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.tm.te.ui.views.activator.UIPlugin;
import org.eclipse.tm.te.ui.views.interfaces.ImageConsts;
import org.eclipse.tm.te.ui.views.internal.nls.Messages;
import org.eclipse.tm.te.ui.views.internal.workingsets.WorkingSetsContentProvider;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.navigator.IExtensionStateModel;

/**
 * Provides the radio buttons at the top of the view menu that control the root of the Target
 * Explorer, which is either working sets of projects. When the state is changed through the
 * actions, the WorkingSetsContentProvider.SHOW_TOP_LEVEL_WORKING_SETS property in the extension
 * state model is updated.
 *
 * This is installed by the WorkingSetActionProvider.
 *
 * <p>
 * Copied and adapted from <code>org.eclipse.ui.internal.navigator.resources.actions.WorkingSetRootModeActionGroup</code>.
 */
public class WorkingSetRootModeActionGroup extends ActionGroup {

	/* default */ IExtensionStateModel stateModel;
	/* default */ StructuredViewer structuredViewer;

	private boolean hasContributedToViewMenu = false;
	private IAction workingSetsAction = null;
	private IAction targetsAction = null;
	/* default */ IAction[] actions;
	/* default */ int currentSelection;
	/* default */ MenuItem[] items;

	private class TopLevelContentAction extends Action {

		private final boolean groupWorkingSets;

		/**
		 * Construct an Action that represents a toggle-able state between Showing top level Working
		 * Sets and Projects.
		 *
		 * @param toGroupWorkingSets
		 */
		public TopLevelContentAction(boolean toGroupWorkingSets) {
			super("", AS_RADIO_BUTTON); //$NON-NLS-1$
			groupWorkingSets = toGroupWorkingSets;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.action.Action#run()
		 */
		@Override
		public void run() {
			if (stateModel.getBooleanProperty(WorkingSetsContentProvider.SHOW_TOP_LEVEL_WORKING_SETS) != groupWorkingSets) {
				stateModel.setBooleanProperty(WorkingSetsContentProvider.SHOW_TOP_LEVEL_WORKING_SETS, groupWorkingSets);

				structuredViewer.getControl().setRedraw(false);
				try {
					structuredViewer.refresh();
				}
				finally {
					structuredViewer.getControl().setRedraw(true);
				}
			}
		}
	}

	/**
	 * Create an action group that will listen to the stateModel and update the structuredViewer
	 * when necessary.
	 *
	 * @param aStructuredViewer
	 * @param aStateModel
	 */
	public WorkingSetRootModeActionGroup(StructuredViewer aStructuredViewer, IExtensionStateModel aStateModel) {
		super();
		structuredViewer = aStructuredViewer;
		stateModel = aStateModel;
	}

	/* (non-Javadoc)
	 * @see ActionGroup#fillActionBars(IActionBars)
	 */
	@Override
	public void fillActionBars(IActionBars actionBars) {
		if (hasContributedToViewMenu) return;
		IMenuManager topLevelSubMenu = new MenuManager(Messages.WorkingSetRootModeActionGroup_Top_Level_Element);
		addActions(topLevelSubMenu);
		actionBars.getMenuManager()
		                .insertBefore(IWorkbenchActionConstants.MB_ADDITIONS, topLevelSubMenu);
		hasContributedToViewMenu = true;
	}

	/**
	 * Adds the actions to the given menu manager.
	 */
	protected void addActions(IMenuManager viewMenu) {
		if (actions == null) actions = createActions();

		viewMenu.add(new Separator());
		items = new MenuItem[actions.length];

		for (int i = 0; i < actions.length; i++) {
			final int j = i;

			viewMenu.add(new ContributionItem() {

				@Override
				public void fill(Menu menu, int index) {

					int style = SWT.CHECK;
					if ((actions[j].getStyle() & IAction.AS_RADIO_BUTTON) != 0) style = SWT.RADIO;

					final MenuItem mi = new MenuItem(menu, style, index);
					items[j] = mi;
					mi.setText(actions[j].getText());
					mi.setSelection(currentSelection == j);
					mi.addSelectionListener(new SelectionAdapter() {

						@Override
						public void widgetSelected(SelectionEvent e) {
							if (currentSelection == j) {
								items[currentSelection].setSelection(true);
								return;
							}
							actions[j].run();

							// Update checked state
							items[currentSelection].setSelection(false);
							currentSelection = j;
							items[currentSelection].setSelection(true);
						}

					});

				}

				@Override
				public boolean isDynamic() {
					return false;
				}
			});
		}
	}

	private IAction[] createActions() {

		targetsAction = new TopLevelContentAction(false);
		targetsAction.setText(Messages.WorkingSetRootModeActionGroup_Target);
		targetsAction.setImageDescriptor(UIPlugin.getImageDescriptor(ImageConsts.VIEW));

		workingSetsAction = new TopLevelContentAction(true);
		workingSetsAction.setText(Messages.WorkingSetRootModeActionGroup_Working_Set);
		workingSetsAction.setImageDescriptor(UIPlugin.getImageDescriptor(ImageConsts.WORKING_SET));

		return new IAction[] { targetsAction, workingSetsAction };
	}

	/**
	 * Toggle whether top level working sets should be displayed as a group or collapse to just show
	 * their contents.
	 *
	 * @param showTopLevelWorkingSets
	 */
	public void setShowTopLevelWorkingSets(boolean showTopLevelWorkingSets) {
		if (actions == null) actions = createActions();

		currentSelection = showTopLevelWorkingSets ? 1 : 0;
		workingSetsAction.setChecked(showTopLevelWorkingSets);
		targetsAction.setChecked(!showTopLevelWorkingSets);

		if (items != null) {
			for (int i = 0; i < items.length; i++) {
				if (items[i] != null && actions[i] != null) items[i].setSelection(actions[i]
				                .isChecked());
			}
		}
		if (stateModel != null) {
			stateModel.setBooleanProperty(WorkingSetsContentProvider.SHOW_TOP_LEVEL_WORKING_SETS, showTopLevelWorkingSets);
		}
	}

	/**
	 * @param stateModel
	 */
	public void setStateModel(IExtensionStateModel stateModel) {
		this.stateModel = stateModel;
	}
}
