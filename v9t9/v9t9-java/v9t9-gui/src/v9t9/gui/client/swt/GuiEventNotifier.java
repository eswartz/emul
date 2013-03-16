/*
  GuiEventNotifier.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;

import v9t9.common.events.BaseEventNotifier;
import v9t9.common.events.NotifyEvent;

/**
 * @author ejs
 *
 */
public final class GuiEventNotifier extends BaseEventNotifier {
	private static final Logger log = Logger.getLogger(GuiEventNotifier.class);
	
	ToolTip lastTooltip = null;
	private final SwtWindow swtWindow;
	private Timer timer;
	
	public GuiEventNotifier(SwtWindow swtWindow) 
	{
		this.swtWindow = swtWindow;
		timer = new Timer(true);
		startConsumerThread();
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.BaseEventNotifier#canConsume()
	 */
	@Override
	protected boolean canConsume() {
		final boolean[] consume = { true };
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				NotifyEvent event = peekNextEvent();
				if (event != null && event.isPriority && lastTooltip != null && !lastTooltip.isDisposed()) {
					lastTooltip.dispose();
					lastTooltip = null;
				}
				consume[0] = lastTooltip == null || lastTooltip.isDisposed() || !lastTooltip.isVisible();
			}
		});
		return consume[0];
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.BaseEventNotifier#consumeEvent(v9t9.emulator.clients.builtin.IEventNotifier.NotifyEvent)
	 */
	@Override
	protected void consumeEvent(final NotifyEvent event) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				System.err.println(event);
				if (event.level == Level.INFO)
					log.debug(event);
				else if (event.level == Level.WARNING)
					log.warn(event);
				else
					log.error(event);
				
				if (swtWindow.getShell().isDisposed())
					return;
				
				if (lastTooltip != null)
					lastTooltip.dispose();
				
				int status = 0;
				if (event.level == Level.INFO)
					status = SWT.ICON_INFORMATION;
				else if (event.level == Level.WARNING)
					status = SWT.ICON_WARNING;
				else
					status = SWT.ICON_ERROR;
				
				Shell shell = Display.getDefault().getActiveShell();
				if (shell == null)
					shell = swtWindow.getShell();
				final ToolTip tip = new ToolTip(shell, SWT.BALLOON | SWT.WRAP | status);
				String text = event.message != null ? event.message : "";
				tip.setText(text);
				
				long delay = Math.min(10000, Math.max(1000, text.length() * 100));
				//System.out.println("tooltip delay: " + delay);
				tip.setAutoHide(false);
				
				timer.schedule(new TimerTask() {

					@Override
					public void run() {
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								if (!tip.isDisposed())
									tip.dispose();
							}
						});
					}
					
				}, delay);
				
				if (event.context instanceof Event) {
					Event e = (Event)event.context;
					Control b = (Control) e.widget;
					tip.setLocation(b.toDisplay(e.x, e.y + b.getSize().y));
				} else {
					Point pt = swtWindow.getButtonBar().getTooltipLocation();
					tip.setLocation(pt);
				}
				tip.setVisible(true);
				
				lastTooltip = tip;
			}
		});
	}
}