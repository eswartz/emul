/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tests;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestListener;
import junit.framework.TestResult;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.te.tests.activator.UIPlugin;
import org.eclipse.tm.te.tests.interfaces.IConfigurationProperties;
import org.eclipse.tm.te.tests.interfaces.IInterruptCondition;
import org.eclipse.tm.te.ui.views.interfaces.IUIConstants;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.osgi.framework.Bundle;

/**
 * Target Explorer: Core test case implementation.
 */
public class CoreTestCase extends TestCase {
	// Internal property id to store if or if not the views zoom state
	// got changed before executing the test case.
	private final static String VIEW_ZOOM_STATE_CHANGED = "viewZoomStateChanged"; //$NON-NLS-1$

	// The test configuration
	private final Properties configuration = new Properties();

	// The internal test listener.
	private final TestListener listener = new InternalTestListener();

	/**
	 * Listens to the test executions and logs the failures.
	 */
	private class InternalTestListener implements TestListener {

		/**
		 * Constructor.
		 */
		public InternalTestListener() {
		}

		/* (non-Javadoc)
		 * @see junit.framework.TestListener#startTest(junit.framework.Test)
		 */
		public void startTest(Test test) {
		}

		/* (non-Javadoc)
		 * @see junit.framework.TestListener#addError(junit.framework.Test, java.lang.Throwable)
		 */
		public synchronized void addError(Test test, Throwable error) {
			if (test != null && error != null) {
				// Log the error to the error log.
				IStatus status = new Status(IStatus.ERROR,
				                            UIPlugin.getUniqueIdentifier(),
				                            1,
				                            "Test case '" + test + "' failed with error. Possible cause: " + error.getLocalizedMessage(), //$NON-NLS-1$ //$NON-NLS-2$
				                            error
				                           );
				UIPlugin.getDefault().getLog().log(status);
			}
		}

		/* (non-Javadoc)
		 * @see junit.framework.TestListener#addFailure(junit.framework.Test, junit.framework.AssertionFailedError)
		 */
		public synchronized void addFailure(Test test, AssertionFailedError failure) {
			if (test != null && failure != null) {
				// Log the failure to the error log.
				IStatus status = new Status(IStatus.ERROR,
											UIPlugin.getUniqueIdentifier(),
				                            1,
				                            "Test case '" + test + "' failed. Failure: " + failure.getLocalizedMessage(), //$NON-NLS-1$ //$NON-NLS-2$
				                            failure
				                           );
				UIPlugin.getDefault().getLog().log(status);
			}
		}

		/* (non-Javadoc)
		 * @see junit.framework.TestListener#endTest(junit.framework.Test)
		 */
		public void endTest(Test test) {
			// nothing to do on end test
		}
	}

	/**
	 * Constructor.
	 */
	public CoreTestCase() {
		this(null);
	}

	/**
	 * Constructor.
	 *
	 * @param name The test name.
	 */
	public CoreTestCase(String name) {
		super(name);

		// Setup the test case configuration. Clear out the old configuration.
		configuration.clear();
		initialize();
	}

	/**
	 * Initialize the test configuration.
	 * <p>
	 * Clients may overwrite this method to modify the base configuration.
	 */
	protected void initialize() {
		Assert.isNotNull(configuration);

		setProperty(VIEW_ZOOM_STATE_CHANGED, false);
		setProperty(IConfigurationProperties.MAXIMIZE_VIEW, false);

		setProperty(IConfigurationProperties.TARGET_PERSPECTIVE, "org.eclipse.tm.te.ui.perspective"); //$NON-NLS-1$
	}

	/**
	 * Sets a boolean configuration property.
	 *
	 * @param key The property key. Must not be <code>null</code>!
	 * @param value The property value.
	 */
	protected final void setProperty(String key, boolean value) {
		Assert.isNotNull(key);
		setProperty(key, value ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
	}

	/**
	 * Checks if a boolean configuration property has been set to the
	 * given property value.
	 *
	 * @param key The property key. Must not be <code>null</code>!
	 * @param value The property value.
	 *
	 * @return <code>True</code> if the property values match, <code>false</code> if not.
	 */
	protected final boolean isProperty(String key, boolean value) {
		Assert.isNotNull(key);
		return (value ? Boolean.TRUE : Boolean.FALSE).equals(Boolean.valueOf(configuration.getProperty(key, "false"))); //$NON-NLS-1$
	}

	/**
	 * Sets a string configuration property.
	 * <p>
	 * If the value is <code>null</code>, the configuration property
	 * will be removed.
	 *
	 * @param key The property key. Must not be <code>null</code>!
	 * @param value The property value.
	 */
	protected final void setProperty(String key, String value) {
		Assert.isNotNull(key);
		if (value != null) configuration.setProperty(key, value);
		else configuration.remove(key);
	}

	/**
	 * Checks if a string configuration property has been set to the given property
	 * value (case insensitive).
	 * <p>
	 * If the value is <code>null</code>, the method returns <code>true</code> if
	 * the configuration property cannot be found.
	 *
	 * @param key The property key. Must not be <code>null</code>!
	 * @param value The property value.
	 *
	 * @return <code>True</code> if the property values match, <code>false</code> if not.
	 */
	protected final boolean isProperty(String key, String value) {
		Assert.isNotNull(key);
		return value != null ? value.equalsIgnoreCase(configuration.getProperty(key)) : !configuration.containsKey(key);
	}

	/**
	 * Returns the value of a string configuration property.
	 *
	 * @param key The property key. Must not be <code>null</code>!
	 * @return The property value or <code>null</code> if not set.
	 */
	protected final String getProperty(String key) {
		Assert.isNotNull(key);
		return configuration.getProperty(key, null);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		if (result != null) result.addListener(listener);
		super.run(result);
		if (result != null) result.removeListener(listener);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#runBare()
	 */
	@Override
	public void runBare() throws Throwable {
		long start = printStart(getName());
		try {
			super.runBare();
		} finally {
			printEnd(getName(), start);
		}
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#runTest()
	 */
	@Override
	protected void runTest() throws Throwable {
		long start = printStart(getName());
		try {
			super.runTest();
		} finally {
			printEnd(getName(), start);
		}
	}

	// Local date format presenting long date and time format.
	private final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.getDefault());

	/**
	 * Prints out the test start time.
	 *
	 * @param name The name. Must not be <code>null</code>.
	 * @return The start time in milliseconds.
	 */
	protected long printStart(String name) {
		assert name != null;
		long startTime = System.currentTimeMillis();
		if (name != null) {
			System.out.println("\n=== " + name + " started at: " + DATE_FORMAT.format(new Date(startTime))); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return startTime;
	}

	/**
	 * Prints out the test end time together with the test duration in milliseconds.
	 *
	 * @param name The name. Must not be <code>null</code>.
	 * @param startTime The start time in milliseconds.
	 */
	protected void printEnd(String name, long startTime) {
		assert name != null;
		long endTime = System.currentTimeMillis();
		if (name != null) {
			long duration = endTime - startTime;
			System.out.println("=== " + name + " finished at: " + DATE_FORMAT.format(new Date(endTime)) + " (duration: " + duration + " ms)"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		maximizeView();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		restoreView();
		flushEventQueue();
		super.tearDown();
	}

	/**
	 * Flush the display event queue.
	 * <p>
	 * Unhandled exceptions in the event loop event are caught as follows:
	 * In case multiple events from the event loop throw exceptions these are printed
	 * to stdout. The first exception found in the event loop is thrown to the caller.
	 *
	 * @throws Exception in case an unhandled event loop exception was found.
	 */
	protected void flushEventQueue() throws Exception {
		Display display = Display.getCurrent();
		if (display!=null) {
			//on the dispatch thread already
			Exception eventLoopException = null;
			while(!display.isDisposed()) {
				//loop until event queue is flushed
				try {
					if (!display.readAndDispatch()) {
						break;
					}
				} catch(Exception e) {
					if (eventLoopException==null) {
						eventLoopException = e;
					} else {
						System.out.println("Multiple unhandled event loop exceptions:"); //$NON-NLS-1$
						e.printStackTrace();
					}
				}
			}
			if (eventLoopException!=null) {
				throw eventLoopException;
			}
		} else {
			//calling from background thread
			final Exception[] ex = new Exception[1];
			display = Display.getDefault();
			display.syncExec(new Runnable() {
				public void run() {
					try {
						flushEventQueue();
					} catch(Exception e) {
						ex[0] = e;
					}
				}
			});
			if (ex[0]!=null) throw ex[0];
		}
	}

	/**
	 * Bring the Target Explorer view to front.
	 * <p>
	 * If the property {@link IConfigurationProperties#MAXIMIZE_VIEW} is set, the
	 * view will be maximized.
	 * <p>
	 * A possibly open Eclipse Intro View will be hidden automatically.
	 */
	protected void maximizeView() {
		final String perspectiveId = getProperty(IConfigurationProperties.TARGET_PERSPECTIVE);
		assertNotNull("Invalid null-value for test case perspective id!", perspectiveId); //$NON-NLS-1$

		// Find the Eclipse Intro page and hide it.
		hideView("org.eclipse.ui.internal.introview", perspectiveId); //$NON-NLS-1$

		// Show the Target Explorer view
		setProperty(VIEW_ZOOM_STATE_CHANGED, false);
		IViewPart part = showView(IUIConstants.ID_EXPLORER, perspectiveId);
		assertNotNull("Target Explorer view is not available!", part); //$NON-NLS-1$

		// Get the view reference for setting the maximized state
		IViewReference reference = findView(IUIConstants.ID_EXPLORER, perspectiveId);
		assertNotNull("Failed to lookup view reference for Target Explorer view!", reference); //$NON-NLS-1$
		if (reference.getPage().getPartState(reference) != IWorkbenchPage.STATE_MAXIMIZED
				&& isProperty(IConfigurationProperties.MAXIMIZE_VIEW, true)) {
			reference.getPage().toggleZoom(reference);
			setProperty(VIEW_ZOOM_STATE_CHANGED, true);
		} else if (reference.getPage().getPartState(reference) == IWorkbenchPage.STATE_MAXIMIZED
				&& isProperty(IConfigurationProperties.MAXIMIZE_VIEW, false)) {
			reference.getPage().toggleZoom(reference);
			setProperty(VIEW_ZOOM_STATE_CHANGED, true);
		}

		// Give the UI a chance to repaint if the view zoom state changed
		if (isProperty(VIEW_ZOOM_STATE_CHANGED, true)) {
			waitAndDispatch(1000);
		}
	}

	/**
	 * Restore the Target Explorer view state.
	 */
	protected void restoreView() {
		// restore the original view zoom state
		if (isProperty(VIEW_ZOOM_STATE_CHANGED, true)) {
			final String perspectiveId = getProperty(IConfigurationProperties.TARGET_PERSPECTIVE);
			assertNotNull("Invalid null-value for test case perspective id!", perspectiveId); //$NON-NLS-1$

			IViewReference reference = findView(IUIConstants.ID_EXPLORER, perspectiveId);
			assertNotNull("Failed to lookup view reference for RSE Remote Systems View!", reference); //$NON-NLS-1$
			if (reference.getPage().getPartState(reference) == IWorkbenchPage.STATE_MAXIMIZED
					&& isProperty(IConfigurationProperties.MAXIMIZE_VIEW, true)) {
				reference.getPage().toggleZoom(reference);
			} else if (reference.getPage().getPartState(reference) != IWorkbenchPage.STATE_MAXIMIZED
					&& isProperty(IConfigurationProperties.MAXIMIZE_VIEW, false)) {
				reference.getPage().toggleZoom(reference);
			}
			setProperty(VIEW_ZOOM_STATE_CHANGED, false);
		}
	}

	/**
	 * Lookup the view reference for the given view and perspective id's.
	 *
	 * @param viewId The view id. Must not be <code>null</code>.
	 * @param perspectiveId The perspective id. Must not be <code>null</code>.
	 *
	 * @return The view reference instance to the view or <code>null</code> if not available.
	 */
	public final IViewReference findView(String viewId, String perspectiveId) {
		Assert.isNotNull(viewId);
		Assert.isNotNull(perspectiveId);

		IWorkbench workbench = PlatformUI.getWorkbench();
		assertNotNull("Failed to query current workbench instance!", workbench); //$NON-NLS-1$
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		assertNotNull("Failed to query currently active workbench window!", window); //$NON-NLS-1$

		try {
			workbench.showPerspective(perspectiveId, window);
		} catch (WorkbenchException e) {
			IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
										"Failed to switch to requested perspective (id = " + perspectiveId + ")!", e); //$NON-NLS-1$ //$NON-NLS-2$
			UIPlugin.getDefault().getLog().log(status);
		}

		IWorkbenchPage page = window.getActivePage();
		assertNotNull("Failed to query currently active workbench page!", page); //$NON-NLS-1$

		return page.findViewReference(viewId);
	}

	/**
	 * Shows the view.
	 *
	 * @param viewId The view id. Must not be <code>null</code>.
	 * @param perspectiveId The perspective id. Must not be <code>null</code>.
	 *
	 * @return The view part instance to the view or <code>null</code> if it cannot be shown.
	 */
	public final IViewPart showView(String viewId, String perspectiveId) {
		Assert.isNotNull(viewId);
		Assert.isNotNull(perspectiveId);

		IWorkbench workbench = PlatformUI.getWorkbench();
		assertNotNull("Failed to query current workbench instance!", workbench); //$NON-NLS-1$
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		assertNotNull("Failed to query currently active workbench window!", window); //$NON-NLS-1$

		try {
			workbench.showPerspective(perspectiveId, window);
		} catch (WorkbenchException e) {
			IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
										"Failed to switch to requested perspective (id = " + perspectiveId + ")!", e); //$NON-NLS-1$ //$NON-NLS-2$
			UIPlugin.getDefault().getLog().log(status);
		}

		IWorkbenchPage page = window.getActivePage();
		assertNotNull("Failed to query currently active workbench page!", page); //$NON-NLS-1$

		IViewPart part = null;
		try {
			part = page.showView(viewId);
		} catch (PartInitException e) {
			IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
										"Failed to switch to requested perspective (id = " + perspectiveId + ")!", e); //$NON-NLS-1$ //$NON-NLS-2$
			UIPlugin.getDefault().getLog().log(status);
		}

		return part;
	}

	/**
	 * Hides the view.
	 *
	 * @param viewId The view id. Must not be <code>null</code>.
	 * @param perspectiveId The perspective id. Must not be <code>null</code>.
	 */
	public final void hideView(String viewId, String perspectiveId) {
		Assert.isNotNull(viewId);
		Assert.isNotNull(perspectiveId);

		IViewReference viewReference = findView(viewId, perspectiveId);
		if (viewReference != null) {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(viewReference);
			waitAndDispatch(1000);
		}
	}

	/**
	 * Convenience method to call {@link #waitAndDispatch(long, null)}.
	 *
	 * @param timeout The timeout in milliseconds. Must be larger than 0.
	 *
	 * @return <code>True</code> if the method returned because of the timeout, <code>false</code> if the
	 *         method returned because of the condition became true.
	 */
	public boolean waitAndDispatch(long timeout) {
		Assert.isTrue(timeout > 0);
		return waitAndDispatch(timeout, null);
	}

	/**
	 * Method does not return until either the timeout has been exceeded or
	 * the interrupt condition is fulfilled. If the method called in a UI thread,
	 * the method keeps the UI thread dispatching running. If called in a
	 * non-UI thread, the method uses {@link Thread#sleep(long)}.
	 * <p>
	 * If a timeout of 0 milliseconds is specified, the method waits until the
	 * given interrupt condition is fulfilled.
	 *
	 * @param timeout The timeout in milliseconds. Must be larger or equals than 0.
	 * @param condition The interrupt condition to test. Must not be <code>null</code> if the timeout is 0.
	 *
	 * @return <code>True</code> if the method returned because of the timeout, <code>false</code> if the
	 *         method returned because of the condition became true.
	 */
	public boolean waitAndDispatch(long timeout, IInterruptCondition condition) {
		Assert.isTrue(timeout >= 0);
		if (timeout == 0) Assert.isNotNull(condition);

		boolean isTimedOut= false;
		if (timeout >= 0) {
			long start = System.currentTimeMillis();
			final Display display = Display.findDisplay(Thread.currentThread());
			if (display != null) {
				long current = System.currentTimeMillis();
				while (timeout == 0 || (current - start) < timeout && !display.isDisposed()) {
					if (condition != null && condition.isTrue()) break;
					if (!display.readAndDispatch()) display.sleep();
					current = System.currentTimeMillis();
				}
				isTimedOut = (current - start) >= timeout && timeout > 0;
			} else {
				long current = System.currentTimeMillis();
				while (timeout == 0 || (current - start) < timeout) {
					if (condition != null && condition.isTrue()) break;
					try { Thread.sleep(50); } catch (InterruptedException e) { /* ignored on purpose */ }
					current = System.currentTimeMillis();
				}
				isTimedOut = (current - start) >= timeout && timeout > 0;
			}
		}
		if (condition != null) condition.dispose();

		return isTimedOut;
	}

	/**
	 * Calculates the absolute path to load test data from.
	 * <p>
	 * The returned path is calculated as follow:<br>
	 * <ul>
	 *     <li>Add org.eclipse.tm.te.tests bundle location</li>
	 *     <li>Add &quot;test.data&quot;</li>
	 *     <li>If &quot;hostSpecific&quot; is true, add &quot;Platform.getOS()&quot;</li>
	 *     <li>Add the given relative path</li>
	 * </ul>
	 * <p>
	 * The calculated path must be a readable directory, otherwise the method will
	 * return <code>null</code>.
	 *
	 * @param path The relative path segment to append. Must not be <code>null</code>.
	 * @param hostSpecific Specify <code>true</code> to include {@link Platform#getOS()}, <code>false</code> if not.
	 *
	 * @return The absolute path to the test data or <code>null</code>.
	 */
	protected final IPath getTestDataLocation(String path, boolean hostSpecific) {
		Assert.isNotNull(path);
		IPath root = null;

		if (path != null) {
			Bundle bundle = UIPlugin.getDefault().getBundle();
			if (bundle != null) {
				IPath relative = new Path ("test.data").append(path); //$NON-NLS-1$
				if (hostSpecific) relative = relative.append(Platform.getOS());

				URL url = FileLocator.find(bundle, relative, null);
				if (url != null) {
					try {
						root = new Path(FileLocator.resolve(url).getFile());
						if (!root.toFile().isDirectory() || !root.toFile().canRead()) {
							root = null;
						}
					} catch (IOException e) { /* ignored on purpose */ }
				}
			}
		}

		return root;
	}
}
