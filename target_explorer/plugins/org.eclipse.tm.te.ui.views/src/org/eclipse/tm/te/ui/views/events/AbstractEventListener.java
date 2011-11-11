/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.views.events;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.tm.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tm.te.runtime.model.interfaces.IModelNodeProvider;
import org.eclipse.tm.te.ui.views.interfaces.IUIConstants;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * Abstract UI event listener updating the target explorer view.
 */
public abstract class AbstractEventListener extends org.eclipse.tm.te.ui.events.AbstractEventListener {
	// Reference to the target explorer viewer instance
	private CommonViewer viewer = null;
	// Reference to the refresh job
	private RefreshJob refreshJob = null;
	// Reference to the update job
	private UpdateJob updateJob = null;

	/**
	 * Returns the Target Explorer view.
	 *
	 * @return The Target Explorer view or <code>null</code>.
	 */
	protected CommonViewer getViewer() {
		if (viewer == null) {
			IWorkbench workbench = PlatformUI.getWorkbench();
			IWorkbenchWindow window = workbench != null ? workbench.getActiveWorkbenchWindow() : null;
			IWorkbenchPage page = window != null ? window.getActivePage() : null;
			if (page != null) {
				IViewPart part = page.findView(IUIConstants.ID_EXPLORER);
				if (part instanceof CommonNavigator) {
					viewer = ((CommonNavigator)part).getCommonViewer();
				}
			}
		}
		return viewer;
	}

	/**
	 * Trigger a refresh of the given node. If the node
	 * is <code>null</code>, everything will be refreshed.
	 *
	 * @param node The node or <code>null</code>.
	 * @param scheduled <code>True</code> to schedule the refresh for asynchronous execution, <code>false</code> to
	 *                  execute the refresh synchronously.
	 *
	 * @see CommonViewer#refresh()
	 * @see CommonViewer#refresh(Object)
	 */
	protected void refresh(Object node, boolean scheduled) {
		CommonViewer viewer = getViewer();
		if (viewer == null) return;

		if (scheduled) {
			scheduleRefreshJob(node != null ? node : viewer, viewer);
		} else {
			if (node != null) {
				viewer.refresh(node);
			} else {
				viewer.refresh();
			}
		}
	}

	/**
	 * Trigger a update of the given node.
	 *
	 * @param node The node. Must not be <code>null</code>.
	 * @param scheduled <code>True</code> to schedule the update for asynchronous execution, <code>false</code> to
	 *                  execute the update synchronously.
	 *
	 * @see CommonViewer#update(Object, String[])
	 */
	protected void update(Object node, boolean scheduled) {
		Assert.isNotNull(node);

		CommonViewer viewer = getViewer();
		if (viewer == null) return;

		if (scheduled) {
			scheduleUpdateJob(node, viewer);
		} else {
			viewer.update(node, null);
		}
	}

	private static final int SCHEDULE_TIME = 1000;

	/**
	 * Abstract refresh or update job implementation
	 */
	private abstract class AbstractJob extends Job {

		/* default */ final CommonViewer parentViewer;
		private final Queue<Object> nodes = new ConcurrentLinkedQueue<Object>();
		private boolean done = true;

		/**
		 * Constructor.
		 *
		 * @param name The job name.
		 * @param viewer The viewer instance. Must not be <code>null</code>.
		 */
		protected AbstractJob(String name, CommonViewer viewer) {
			super(name);

			Assert.isNotNull(viewer);
			this.parentViewer = viewer;

			setPriority(Job.SHORT);
			setSystem(true);
		}

		/**
		 * Adds the given node to the job.
		 *
		 * @param element The element to add or <code>null</code> to refresh everything.
		 */
		protected void addNode(Object element) {
			// if whole tree should be refreshed, clear the queue
			if (element instanceof CommonViewer) {
				nodes.clear();
			}
			// if the element to refresh is not in list
			else if (!nodes.contains(element)) {
				// if model node look at parent/child relationship
				if (element instanceof IModelNodeProvider) {
					IModelNode node = ((IModelNodeProvider)element).getModelNode();
					Iterator<Object> it = nodes.iterator();
					while (it.hasNext() && element != null && node != null) {
						Object obj = it.next();
						if (obj instanceof IModelNodeProvider) {
							IModelNode rNode = ((IModelNodeProvider)obj).getModelNode();
							if (rNode != null) {
								// if parent already in list -> skip
								if (rNode.equals(node.getParent())) {
									element = null;
								}
								// if child in list -> remove child
								else if (node.equals(rNode.getParent())) {
									it.remove();
								}
							}
						}
					}
				}
			}
			// skip element if already in list
			else {
				element = null;
			}
			// add to list if not skipped
			if (element != null) nodes.add(element);

			// if job is not scheduled, reschedule it
			if (done) {
				done = false;
				this.schedule(SCHEDULE_TIME);
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			Object node = nodes.poll();
			while (node != null) {
				Runnable runnable = newRunnable(node);
				if (runnable != null) {
					try {
						PlatformUI.getWorkbench().getDisplay().asyncExec(runnable);
					}
					catch (Exception e) {
						// if display is disposed, silently ignore.
					}
				}
				// get the next element to refresh
				node = nodes.poll();
			}
			// set job to done so the next add would reschedule it
			done = true;

			return Status.OK_STATUS;
		}

		/**
		 * Creates the runnable.
		 *
		 * @param node The node. Must not be <code>null</code>.
		 * @return The runnable or <code>null</code>
		 */
		protected Runnable newRunnable(final Object node) {
			Assert.isNotNull(node);
			return null;
		}
	}

	/**
	 * Refresh Job implementation.
	 */
	private class RefreshJob extends AbstractJob {

		/**
		 * Constructor.
		 *
		 * @param viewer The viewer instance. Must not be <code>null</code>.
		 */
		public RefreshJob(CommonViewer viewer) {
			super(RefreshJob.class.getSimpleName(), viewer);
		}

		/* (non-Javadoc)
		 * @see com.windriver.te.tcf.softice.ui.internal.events.EventListener.AbstractJob#newRunnable(java.lang.Object)
		 */
		@Override
		protected Runnable newRunnable(final Object node) {
			return new Runnable() {
				@Override
                public void run() {
					if (node instanceof CommonViewer) {
						parentViewer.refresh();
					} else {
						parentViewer.refresh(node);
					}
				}
			};
		}
	}

	/**
	 * Update Job implementation.
	 */
	private class UpdateJob extends AbstractJob {

		/**
		 * Constructor.
		 *
		 * @param viewer The viewer instance. Must not be <code>null</code>.
		 */
		public UpdateJob(CommonViewer viewer) {
			super(UpdateJob.class.getSimpleName(), viewer);
		}


		/* (non-Javadoc)
		 * @see com.windriver.te.tcf.softice.ui.internal.events.EventListener.AbstractJob#newRunnable(java.lang.Object)
		 */
		@Override
		protected Runnable newRunnable(final Object node) {
			return new Runnable() {
				@Override
                public void run() {
					if (node instanceof CommonViewer) {
						parentViewer.refresh();
					} else {
						parentViewer.update(node, null);
					}
				}
			};
		}
	}

	/**
	 * Schedule the asynchronous refresh job.
	 *
	 * @param node The node. Must not be <code>null</code>.
	 * @param viewer The viewer instance. Must not be <code>null</code>.
	 */
	private void scheduleRefreshJob(Object node, CommonViewer viewer) {
		Assert.isNotNull(node);
		Assert.isNotNull(viewer);

		if (refreshJob == null) {
			refreshJob = new RefreshJob(viewer);
		}
		refreshJob.addNode(node);
	}


	/**
	 * Schedule the asynchronous update job.
	 *
	 * @param node The node. Must not be <code>null</code>.
	 * @param viewer The viewer instance. Must not be <code>null</code>.
	 */
	private void scheduleUpdateJob(Object node, CommonViewer viewer) {
		Assert.isNotNull(node);
		Assert.isNotNull(viewer);

		if (updateJob == null) {
			updateJob = new UpdateJob(viewer);
		}
		updateJob.addNode(node);
	}
}
