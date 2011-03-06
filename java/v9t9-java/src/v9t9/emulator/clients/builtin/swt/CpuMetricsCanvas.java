/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import v9t9.emulator.runtime.cpu.CpuMetrics;
import v9t9.emulator.runtime.cpu.CpuMetrics.IMetricsListener;
import v9t9.emulator.runtime.cpu.CpuMetrics.MetricEntry;

/**
 * @author Ed
 *
 */
public class CpuMetricsCanvas extends Canvas {

	private CpuMetrics cpuMetrics;
	private Color bgcolor;
	private Color realCyclesColor;
	private Color idealCyclesColor;
	private Color realInterruptsColor;
	private Color idealInterruptsColor;
	private int lastMaxCycles;
	private int lastMaxInterrupts;

	private int lastTooltipIndex;
	private IMetricsListener metricsListener;
	private Color gridcolor;
	
	public CpuMetricsCanvas(final Composite parent, int style, CpuMetrics cpuMetrics) {
		super(parent, style | SWT.NO_BACKGROUND);
		this.cpuMetrics = cpuMetrics;
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).grab(false, false)
			.indent(2, 2)./*exclude(true).*/applyTo(this);
		
		setVisible(true);
		updateTooltip(null);
		
		bgcolor = getDisplay().getSystemColor(SWT.COLOR_BLACK);
		gridcolor = getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
		realCyclesColor = getDisplay().getSystemColor(SWT.COLOR_GREEN);
		idealCyclesColor = getDisplay().getSystemColor(SWT.COLOR_YELLOW);
		
		realInterruptsColor = getDisplay().getSystemColor(SWT.COLOR_CYAN);
		idealInterruptsColor = getDisplay().getSystemColor(SWT.COLOR_WHITE);
		
		parent.addControlListener(new ControlListener() {

			public void controlMoved(ControlEvent e) {
			}

			public void controlResized(ControlEvent e) {
				Rectangle bounds = parent.getClientArea();
				Rectangle metrics = new Rectangle(bounds.x + (bounds.width - bounds.height), bounds.y,
						bounds.height, bounds.height);
				metrics.x += 2;
				metrics.y += 2;
				metrics.width -= 4;
				metrics.height -= 4;
				setBounds(metrics);				
			}
			
		});
		
		addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				doPaint(e);
				
				if (lastTooltipIndex != -1) {
					updateTooltip(CpuMetricsCanvas.this.cpuMetrics.getEntry(lastTooltipIndex, getClientArea().width));
				}
			}
			
		});
		
		addMouseTrackListener(new MouseTrackAdapter() {
			@Override
			public void mouseEnter(org.eclipse.swt.events.MouseEvent e) {
				lastTooltipIndex = -1;
			}
			@Override
			public void mouseExit(org.eclipse.swt.events.MouseEvent e) {
				lastTooltipIndex = -1;
			}
		});
		addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(org.eclipse.swt.events.MouseEvent e) {
				int i = e.x;
				lastTooltipIndex = i;
				MetricEntry entry = CpuMetricsCanvas.this.cpuMetrics.getEntry(i, getClientArea().width);
				updateTooltip(entry);
			}
		});
		
		metricsListener = new IMetricsListener() {

			public void metricsChanged() {
				getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (!isDisposed())
							redraw();
					}
				});
			}
			
		};
		cpuMetrics.addListener(metricsListener);
	}

	protected void updateTooltip(MetricEntry entry) {
		String text;
		text = "This tracks the emulation performance, in cycles and in interrupts";
		if (entry != null) {
			text = entry.toSummary() + "\n\n" + text;
		}
		
		setToolTipText(text);
	}

	@Override
	public void dispose() {
		cpuMetrics.removeListener(metricsListener);
		bgcolor.dispose();
		gridcolor.dispose();
		realCyclesColor.dispose();
		idealCyclesColor.dispose();
		super.dispose();
	}
	protected void doPaint(PaintEvent e) {
		if (isDisposed())
			return;
		Rectangle rect = getClientArea();
		Point size = new Point(rect.width, rect.height);
		e.gc.setBackground(bgcolor);
		e.gc.fillRectangle(new Rectangle(0, 0, size.x, size.y));
		
		MetricEntry[] entries = cpuMetrics.getLastEntries(size.x);
		
		// get scale
		int maxCycles = 0;
		int maxInterrupts = 0;
		for (MetricEntry entry : entries) {
			maxCycles = Math.max(maxCycles, Math.max(entry.getIdealCycles(), entry.getCycles()));
			maxInterrupts = Math.max(maxInterrupts, Math.max(entry.getIdealInterrupts(), entry.getInterrupts()));
		}
		
		int nextMaxCycles = maxCycles;
		maxCycles = (lastMaxCycles + nextMaxCycles) / 2; 
		if (maxCycles == 0)
			maxCycles = 1;
		else if (maxCycles < 3000000)
			maxCycles = 3000000;

		int nextMaxInterrupts = maxInterrupts;
		maxInterrupts = (lastMaxInterrupts + nextMaxInterrupts) / 2; 
		if (maxInterrupts == 0)
			maxInterrupts = 1;

		
		
		e.gc.setForeground(gridcolor);
		e.gc.setLineDash(new int[] { 1, 2 });
		for (int s = 1; s <= maxCycles; s += 2000000) {
			int y = size.y - s * size.y / maxCycles;
			e.gc.drawLine(0, y, size.x, y);
		}
		
		e.gc.setForeground(idealCyclesColor);
		e.gc.setLineDash(null);
		
		int x = 0;
		for (MetricEntry entry : entries) {
			int y = size.y - entry.getIdealCycles() * size.y / maxCycles;
			e.gc.drawPoint(x, y);
			e.gc.drawPoint(x, y + 1);
			x++;
		}
		
		e.gc.setForeground(realCyclesColor);
		x = 0;
		for (MetricEntry entry : entries) {
			e.gc.drawPoint(x, size.y - entry.getCycles() * size.y / maxCycles);
			x++;
		}
		
		e.gc.setForeground(idealInterruptsColor);
		x = 0;
		for (MetricEntry entry : entries) {
			int y = size.y - entry.getIdealInterrupts() * size.y / maxInterrupts / 2;
			e.gc.drawPoint(x, y);
			e.gc.drawPoint(x, y + 1);
			x++;
		}
		
		e.gc.setForeground(realInterruptsColor);
		x = 0;
		for (MetricEntry entry : entries) {
			e.gc.drawPoint(x, size.y - entry.getInterrupts() * size.y / maxInterrupts / 2);
			x++;
		}
		
		lastMaxCycles = nextMaxCycles;
		lastMaxInterrupts = nextMaxInterrupts;
	}

}
