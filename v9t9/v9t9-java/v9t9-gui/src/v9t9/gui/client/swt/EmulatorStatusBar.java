/**
 * Mar 11, 2011
 */
package v9t9.gui.client.swt;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

import v9t9.engine.dsr.IDeviceIndicatorProvider;
import v9t9.engine.memory.IMachine;
import v9t9.gui.Emulator;
import v9t9.gui.client.swt.ToolShell.Behavior;
import v9t9.gui.client.swt.ToolShell.Centering;

/**
 * @author ejs
 *
 */
public class EmulatorStatusBar extends EmulatorBar {

	private Canvas cpuMetricsCanvas;
	private List<ImageDeviceIndicator> indicators;
	private ImageProvider deviceImageProvider;

	/**
	 * @param swtWindow
	 * @param mainComposite
	 */
	public EmulatorStatusBar(final SwtWindow swtWindow, 
			ImageProvider iconImageProvider,
			Composite mainComposite, final IMachine machine,
			int[] colors, float midPoint, boolean isHorizontal) {
		super(swtWindow, iconImageProvider, 
				mainComposite, machine, colors, midPoint, isHorizontal);
		
		deviceImageProvider = createDeviceImageProvider(swtWindow.getShell());


		if (machine.getModuleManager() != null) {
			createButton(16,
				"Switch module", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						swtWindow.toggleToolShell(MODULE_SELECTOR_TOOL_ID, new IToolShellFactory() {
							ToolShell.Behavior behavior = new ToolShell.Behavior();
							{
								behavior.boundsPref = "ModuleWindowBounds";
								behavior.centering = Centering.INSIDE;
								behavior.centerOverControl = buttonBar;
								behavior.dismissOnClickOutside = true;
							}
							public Control createContents(Shell shell) {
								return new ModuleSelector(shell, machine);
							}
							public Behavior getBehavior() {
								return behavior;
							}
						});
					}
				}
			);
			new BlankIcon(buttonBar, SWT.NONE);
		}
		
		
		createButton(5,
			"Setup disks", new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					swtWindow.toggleToolShell(DISK_SELECTOR_TOOL_ID, new IToolShellFactory() {
						ToolShell.Behavior behavior = new ToolShell.Behavior();
						{
							behavior.boundsPref = "DiskWindowBounds";
							behavior.centering = Centering.INSIDE;
							behavior.centerOverControl = buttonBar;
							behavior.dismissOnClickOutside = true;
						}
						public Control createContents(Shell shell) {
							return new DiskSelectorDialog(shell, machine);
						}
						public Behavior getBehavior() {
							return behavior;
						}
					});
				}
			}
		);		
		indicators = new ArrayList<ImageDeviceIndicator>();
		
		for (IDeviceIndicatorProvider provider : machine.getModel().getDeviceIndicatorProviders(machine))
			addDeviceIndicatorProvider(provider);

		new BlankIcon(buttonBar, SWT.NONE);


		createButton(12, "Accelerate execution",
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Control button = (Control) e.widget;
						Point size = button.getSize();
						swtWindow.showMenu(createAccelMenu(button), button, size.x / 2, size.y / 2);
					}
				});

		cpuMetricsCanvas = new CpuMetricsCanvas(buttonBar.getComposite(), 
				SWT.BORDER | (isHorizontal ? SWT.HORIZONTAL : SWT.VERTICAL), 
				machine.getCpuMetrics());
		GridDataFactory.fillDefaults()
			.align(isHorizontal ? SWT.RIGHT : SWT.FILL, isHorizontal ? SWT.FILL : SWT.BOTTOM)
			.grab(false, false)
			.indent(4, 4).exclude(true).applyTo(cpuMetricsCanvas);

	}
	private Menu createAccelMenu(final Control parent) {
		final Menu menu = new Menu(parent);
		return swtWindow.populateAccelMenu(menu);
	}


	/**
	 * @return
	 */
	private static ImageProvider createDeviceImageProvider(Shell shell) {

		TreeMap<Integer, Image> mainIcons = new TreeMap<Integer, Image>();
		for (int size : new int[] { 16, 32, 64, 128 }) {
			URL iconsFile = Emulator.getDataURL("icons/dev_icons_" + size + ".png");
			if (iconsFile != null) {
				try {
					mainIcons.put(size, new Image(
							shell.getDisplay(), iconsFile.openStream()));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}

		return new MultiImageSizeProvider(mainIcons);
	}

	public void dispose() {
		for (ImageDeviceIndicator indic : indicators)
			indic.dispose();
		indicators.clear();
		cpuMetricsCanvas.dispose();
	}

	public void addDeviceIndicatorProvider(IDeviceIndicatorProvider provider) {
		ImageDeviceIndicator indic = new ImageDeviceIndicator(buttonBar, SWT.NONE, 
				deviceImageProvider, provider);
		//GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.FILL).grab(false, false).applyTo(indic);
		indicators.add(indic);
	}

}
