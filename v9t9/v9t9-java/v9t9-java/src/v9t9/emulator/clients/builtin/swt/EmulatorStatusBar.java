/**
 * Mar 11, 2011
 */
package v9t9.emulator.clients.builtin.swt;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import v9t9.emulator.Emulator;
import v9t9.emulator.common.Machine;

/**
 * @author ejs
 *
 */
public class EmulatorStatusBar {

	private Canvas cpuMetricsCanvas;
	private ImageBar bar;
	private List<ImageDeviceIndicator> indicators;
	private MultiImageSizeProvider deviceIconImageProvider;

	/**
	 * @param swtWindow
	 * @param mainComposite
	 */
	public EmulatorStatusBar(SwtWindow swtWindow, Composite mainComposite, Machine machine) {

		TreeMap<Integer, Image> mainIcons = new TreeMap<Integer, Image>();
		for (int size : new int[] { 16, 32, 64, 128 }) {
			URL iconsFile = Emulator.getDataURL("icons/dev_icons_" + size + ".png");
			if (iconsFile != null) {
				try {
					mainIcons.put(size, new Image(
							swtWindow.getShell().getDisplay(), iconsFile.openStream()));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}

		deviceIconImageProvider = new MultiImageSizeProvider(mainIcons);
		
		bar = new ImageBar(mainComposite, SWT.VERTICAL, null, true);
		
		bar.addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent e) {
				paint(e.gc);
			}
		});
		
		indicators = new ArrayList<ImageDeviceIndicator>();
		
		for (IDeviceIndicatorProvider provider : machine.getModel().getDeviceIndicatorProviders(machine))
			addDeviceIndicatorProvider(provider);

		//new BlankIcon(bar, SWT.NONE);
		//new BlankIcon(bar, SWT.NONE);

		cpuMetricsCanvas = new CpuMetricsCanvas(bar.getComposite(), SWT.BORDER, machine.getCpuMetrics());
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.FILL).grab(false, false)
			.indent(2, 2)./*exclude(true).*/applyTo(cpuMetricsCanvas);
	
	}
	
	public void dispose() {
		for (ImageDeviceIndicator indic : indicators)
			indic.dispose();
		indicators.clear();
		cpuMetricsCanvas.dispose();
	}

	protected void paint(GC gc) {
		Color outer = bar.getParent().getBackground();
		Color ours = bar.getDisplay().getSystemColor(SWT.COLOR_GRAY);
		gc.setForeground(outer);
		gc.setBackground(ours);
		Point size = bar.getSize();
		gc.fillGradientRectangle(0, size.y / 2, size.x, size.y / 2, true);
		gc.fillGradientRectangle(0, size.y / 2, size.x, -size.y / 2, true);
	}

	public void addDeviceIndicatorProvider(IDeviceIndicatorProvider provider) {
		ImageDeviceIndicator indic = new ImageDeviceIndicator(bar, SWT.NONE, 
				deviceIconImageProvider, provider);
		//GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.FILL).grab(false, false).applyTo(indic);
		indicators.add(indic);
	}

	/**
	 * @return
	 */
	public Control getImageBar() {
		return bar;
	}
}
