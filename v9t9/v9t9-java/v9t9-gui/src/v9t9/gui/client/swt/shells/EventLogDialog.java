/*
  KeyboardDialog.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import v9t9.common.events.IEventNotifier;
import v9t9.common.events.IEventNotifierListener;
import v9t9.common.events.NotifyEvent;
import v9t9.common.machine.IMachine;
import v9t9.gui.client.swt.bars.IImageProvider;
import v9t9.gui.client.swt.bars.ImageCanvas;

/**
 * Shows a log of events notified
 * @author ejs
 *
 */
public class EventLogDialog extends Composite {

	public static final String EVENT_LOG_ID = "event.log";
	public static IToolShellFactory getToolShellFactory(final IMachine machine, final ImageCanvas buttonBar, final IImageProvider imageProvider) {
		return new IToolShellFactory() {
			Behavior behavior = new Behavior();
			{
				behavior.boundsPref = "EventLogWindowBounds";
				behavior.centering = null;
				behavior.centerOverControl = buttonBar;
				behavior.dismissOnClickOutside = false;
			}
			public Control createContents(Shell shell) {
				return new EventLogDialog(shell, machine.getEventNotifier());
			}
			public Behavior getBehavior() {
				return behavior;
			}
		};
	}

	
	private IEventNotifier notifier;
	private IEventNotifierListener listener;
	private TableViewer eventViewer;
	
	public EventLogDialog(Shell shell, IEventNotifier notifier_) {
		
		super(shell, SWT.NONE);
		this.notifier = notifier_;
		
		listener = new IEventNotifierListener() {
			
			@Override
			public void eventNotified(NotifyEvent event) {
				addEvent(event);
			}
		};
		notifier.addListener(listener);
		
		shell.setText("Event Log");

		GridLayoutFactory.fillDefaults().margins(6, 6).applyTo(this);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(this);
		
		createEventTable(this);
		
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				notifier.removeListener(listener);
			}
		});
		
		
	}

	static class EventLabelProvider extends BaseLabelProvider implements ITableLabelProvider {

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
			NotifyEvent event = (NotifyEvent) element;
			return event.message;
		}
		
	}
	/**
	 * @param shell
	 */
	private void createEventTable(Composite parent) {
		eventViewer = new TableViewer(parent);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(eventViewer.getControl());
		
		Table table = eventViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		eventViewer.setContentProvider(new ArrayContentProvider());
		eventViewer.setLabelProvider(new EventLabelProvider());
		eventViewer.setInput(notifier.getEvents());
	}

	/**
	 * @param event
	 */
	protected void addEvent(final NotifyEvent event) {
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				eventViewer.add(event);
			}
		});
	}
}
