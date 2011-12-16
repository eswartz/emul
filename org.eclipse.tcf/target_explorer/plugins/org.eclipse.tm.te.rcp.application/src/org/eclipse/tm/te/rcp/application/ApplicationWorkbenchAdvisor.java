/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.rcp.application;

import java.text.Collator;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.tm.te.rcp.application.nls.Messages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.PluginActionBuilder;


/**
 * Target Explorer, RCP: Workbench advisor implementation.
 */
@SuppressWarnings("restriction")
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	private final static String PERSPECTIVE_ID = "org.eclipse.tm.te.ui.perspective"; //$NON-NLS-1$

	private static ApplicationWorkbenchAdvisor workbenchAdvisor = null;

	private Listener settingsChangeListener;

	/**
	 * Creates a new workbench advisor instance.
	 */
	public ApplicationWorkbenchAdvisor() {
		super();
		if (workbenchAdvisor != null) { throw new IllegalStateException(); }
		workbenchAdvisor = this;

		Listener closeListener = new Listener() {
			public void handleEvent(Event event) {
				boolean doExit = ApplicationWorkbenchWindowAdvisor.promptOnExit(null);
				event.doit = doExit;
				if (!doExit) event.type = SWT.None;
			}
		};
		Display.getDefault().addListener(SWT.Close, closeListener);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.application.WorkbenchAdvisor#initialize
	 */
	@Override
	public void initialize(IWorkbenchConfigurer configurer) {

		PluginActionBuilder.setAllowIdeLogging(false);

		// make sure we always save and restore workspace state
		configurer.setSaveAndRestore(true);

		// show Help button in JFace dialogs
		TrayDialog.setDialogHelpAvailable(true);

		Policy.setComparator(Collator.getInstance());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.application.WorkbenchAdvisor#preStartup()
	 */
	@Override
	public void preStartup() {

		// Suspend background jobs while we startup
		Job.getJobManager().suspend();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.application.WorkbenchAdvisor#postStartup()
	 */
	@Override
	public void postStartup() {
		try {
			initializeSettingsChangeListener();
			Display.getCurrent().addListener(SWT.Settings, settingsChangeListener);
		} finally {// Resume background jobs after we startup
			Job.getJobManager().resume();
		}
	}

	/**
	 * Initialize the listener for settings changes.
	 */
	private void initializeSettingsChangeListener() {
		settingsChangeListener = new Listener() {

			boolean currentHighContrast = Display.getCurrent().getHighContrast();

			public void handleEvent(org.eclipse.swt.widgets.Event event) {
				if (Display.getCurrent().getHighContrast() == currentHighContrast) return;

				currentHighContrast = !currentHighContrast;

				// make sure they really want to do this
				if (new MessageDialog(null, Messages.SystemSettingsChange_title, null, Messages.SystemSettingsChange_message,
						MessageDialog.QUESTION, new String[] { Messages.SystemSettingsChange_yes,
						Messages.SystemSettingsChange_no }, 1).open() == Window.OK) {
					PlatformUI.getWorkbench().restart();
				}
			}
		};

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdvisor#preShutdown()
	 */
	@Override
	public boolean preShutdown() {
		Display.getCurrent().removeListener(SWT.Settings, settingsChangeListener);
		return super.preShutdown();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.application.WorkbenchAdvisor#createWorkbenchWindowAdvisor(org.eclipse.ui.application.IWorkbenchWindowConfigurer)
	 */
	@Override
	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(this, configurer);
	}

	/**
	 * Return true if the intro plugin is present and false otherwise.
	 *
	 * @return boolean
	 */
	public boolean hasIntro() {
		return getWorkbenchConfigurer().getWorkbench().getIntroManager().hasIntro();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdvisor
	 */
	@Override
	public String getInitialWindowPerspectiveId() {
		return PERSPECTIVE_ID;
	}
}
