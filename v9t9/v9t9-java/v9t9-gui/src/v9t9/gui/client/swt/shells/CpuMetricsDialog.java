/*
  CpuMetricsDialog.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import v9t9.common.cpu.ICpuMetrics;

/**
 * @author ejs
 *
 */
public class CpuMetricsDialog extends Window {
	private final ICpuMetrics cpuMetrics;

	public CpuMetricsDialog(Shell parentShell, ICpuMetrics cpuMetrics) {
		super(parentShell);
		this.cpuMetrics = cpuMetrics;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("CPU Metrics");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#getInitialSize()
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(400, 400);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		final CpuMetricsCanvas canvas = new CpuMetricsCanvas(parent, SWT.NONE, cpuMetrics, false);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(canvas);
		
		parent.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				canvas.dispose();
			}
		});
		return parent;
	}

}
