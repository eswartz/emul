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
import org.eclipse.swt.graphics.Image;
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
	public EmulatorStatusBar(SwtWindow swtWindow, Composite mainComposite, Machine machine,
			boolean isHorizontal) {

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
		
		bar = new ImageBar(mainComposite, isHorizontal ? SWT.HORIZONTAL : SWT.VERTICAL, null, true);
		
		indicators = new ArrayList<ImageDeviceIndicator>();
		
		for (IDeviceIndicatorProvider provider : machine.getModel().getDeviceIndicatorProviders(machine))
			addDeviceIndicatorProvider(provider);

		//new BlankIcon(bar, SWT.NONE);
		//new BlankIcon(bar, SWT.NONE);

		cpuMetricsCanvas = new CpuMetricsCanvas(bar.getComposite(), 
				SWT.BORDER | (isHorizontal ? SWT.HORIZONTAL : SWT.VERTICAL), 
				machine.getCpuMetrics());
		GridDataFactory.fillDefaults()
			.align(isHorizontal ? SWT.RIGHT : SWT.FILL, isHorizontal ? SWT.FILL : SWT.BOTTOM)
			.grab(false, false)
			.indent(2, 2).exclude(true).applyTo(cpuMetricsCanvas);
	
	}
	
	public void dispose() {
		for (ImageDeviceIndicator indic : indicators)
			indic.dispose();
		indicators.clear();
		cpuMetricsCanvas.dispose();
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
