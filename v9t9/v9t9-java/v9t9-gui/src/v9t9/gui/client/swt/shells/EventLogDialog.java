/*
  KeyboardDialog.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells;

import java.sql.Date;
import java.text.DateFormat;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
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
	
	final static int COLUMN_MESSAGE = 0;
	final static int COLUMN_TIME = 1;

	static class EventLabelProvider extends BaseLabelProvider implements ITableLabelProvider {

		private Display display;

		public EventLabelProvider(Display display) {
			this.display = display;
			
		}
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			NotifyEvent event = (NotifyEvent) element;
			if (columnIndex == COLUMN_MESSAGE) {
				switch (event.level) {
				case ERROR:
					return display.getSystemImage(SWT.ICON_ERROR);
				case WARNING:
					return display.getSystemImage(SWT.ICON_WARNING);
				case INFO:
					return display.getSystemImage(SWT.ICON_INFORMATION);
				}
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		@Override
		public String getColumnText(Object element, int columnIndex) {
			NotifyEvent event = (NotifyEvent) element;
			if (columnIndex == COLUMN_MESSAGE) {
				return event.message;
			} else if (columnIndex == COLUMN_TIME) {
				return DateFormat.getDateTimeInstance().format(new Date(event.timestamp));
			} else {
				return null;
			}
		}
		
	}
	/**
	 * @param shell
	 */
	private void createEventTable(Composite parent) {
		Composite tableComp = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(tableComp);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tableComp);
		
		eventViewer = new TableViewer(tableComp);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(eventViewer.getControl());
		
		Table table = eventViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		TableViewerColumn messageColumn = new TableViewerColumn(eventViewer, SWT.LEFT);
		messageColumn.getColumn().setText("Message");
		TableViewerColumn timeColumn = new TableViewerColumn(eventViewer, SWT.LEFT);
		timeColumn.getColumn().setText("Time");
		
		// auto-resize columns
        TableColumnLayout layout = new TableColumnLayout();
        tableComp.setLayout(layout);
        layout.setColumnData(messageColumn.getColumn(), new ColumnWeightData(70));
        layout.setColumnData(timeColumn.getColumn(), new ColumnWeightData(30));

		eventViewer.setContentProvider(new ArrayContentProvider());
		eventViewer.setLabelProvider(new EventLabelProvider(parent.getDisplay()));
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
