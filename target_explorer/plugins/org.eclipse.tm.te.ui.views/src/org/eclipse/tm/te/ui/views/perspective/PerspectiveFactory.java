/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.views.perspective;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;
import org.eclipse.ui.PlatformUI;

/**
 * Target Explorer: Perspective factory.
 */
public class PerspectiveFactory extends PlatformObject implements IPerspectiveFactory {
	private final static String[] VIEWS_FOR_LEFT_AREA = new String[] {
		"org.eclipse.tm.te.ui.views.TargetExplorer", //$NON-NLS-1$
		"org.eclipse.ui.navigator.ProjectExplorer" //$NON-NLS-1$
	};

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	public void createInitialLayout(IPageLayout layout) {
		// editor is placed for free
		String editorArea = layout.getEditorArea();

		boolean leftAreaActive = false;
		for (String viewId : VIEWS_FOR_LEFT_AREA) {
			leftAreaActive |= PlatformUI.getWorkbench().getViewRegistry().find(viewId) != null;
			if (leftAreaActive) break;
		}

		if (leftAreaActive) {
			// place resource navigator to the left of editor area
			IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, 0.3f, editorArea); //$NON-NLS-1$

			for (String viewId : VIEWS_FOR_LEFT_AREA) {
				if (PlatformUI.getWorkbench().getViewRegistry().find(viewId) != null) {
					left.addView(viewId);
				}
			}
		} else {
			layout.createPlaceholderFolder("left", IPageLayout.LEFT, 0.3f, editorArea); //$NON-NLS-1$
		}

		// place console below the main editor
		IFolderLayout lowerRight = layout.createFolder("lowerRight", IPageLayout.BOTTOM, 0.7f, editorArea); //$NON-NLS-1$
		if (PlatformUI.getWorkbench().getViewRegistry().find("org.eclipse.pde.runtime.LogView") != null) //$NON-NLS-1$
			lowerRight.addView("org.eclipse.pde.runtime.LogView"); //$NON-NLS-1$
		if (PlatformUI.getWorkbench().getViewRegistry().find("org.eclipse.ui.views.TaskList") != null) //$NON-NLS-1$
			lowerRight.addPlaceholder("org.eclipse.ui.views.TaskList"); //$NON-NLS-1$

		// place details view port to the right of editor area
		IPlaceholderFolderLayout right = layout.createPlaceholderFolder("right", IPageLayout.RIGHT, 0.75f, editorArea); //$NON-NLS-1$
		if (PlatformUI.getWorkbench().getViewRegistry().find(IPageLayout.ID_OUTLINE) != null)
			right.addPlaceholder(IPageLayout.ID_OUTLINE);
	}

}
