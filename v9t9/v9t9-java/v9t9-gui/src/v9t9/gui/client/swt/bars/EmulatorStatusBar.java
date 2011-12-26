/**
 * Mar 11, 2011
 */
package v9t9.gui.client.swt.bars;

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
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import ejs.base.properties.IProperty;

import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.IExecutor;
import v9t9.common.dsr.IDeviceIndicatorProvider;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import v9t9.gui.Emulator;
import v9t9.gui.client.swt.SwtWindow;
import v9t9.gui.client.swt.shells.CpuMetricsCanvas;
import v9t9.gui.client.swt.shells.DiskSelectorDialog;
import v9t9.gui.client.swt.shells.ModuleSelector;

/**
 * @author ejs
 *
 */
public class EmulatorStatusBar extends BaseEmulatorBar {

	private Canvas cpuMetricsCanvas;
	private List<ImageDeviceIndicator> indicators;
	private ImageProvider deviceImageProvider;
	private IProperty realTime;
	private IProperty compile;
	private IProperty cyclesPerSecond;
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
		
		realTime = Settings.get(machine, ICpu.settingRealTime);
		compile = Settings.get(machine, IExecutor.settingCompile);
		cyclesPerSecond = Settings.get(machine, ICpu.settingCyclesPerSecond);

		
		deviceImageProvider = createDeviceImageProvider(swtWindow.getShell());

		if (machine.getModuleManager() != null) {
			createButton(IconConsts.MODULE_SWITCH,
				"Switch module", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						swtWindow.toggleToolShell(ModuleSelector.MODULE_SELECTOR_TOOL_ID,
								ModuleSelector.getToolShellFactory(machine, buttonBar));
					}
				}
			);
			new BlankIcon(buttonBar, SWT.NONE);
		}
		
		createButton(IconConsts.DISK_SETUP,
			"Setup disks", new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					swtWindow.toggleToolShell(DiskSelectorDialog.DISK_SELECTOR_TOOL_ID, 
							DiskSelectorDialog.getToolShellFactory(machine, buttonBar));
				}
			}
		);		
		indicators = new ArrayList<ImageDeviceIndicator>();
		
		for (IDeviceIndicatorProvider provider : machine.getModel().getDeviceIndicatorProviders(machine))
			addDeviceIndicatorProvider(provider);

		new BlankIcon(buttonBar, SWT.NONE);


		createButton(IconConsts.CPU_ACCELERATE, "Accelerate execution",
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
		return populateAccelMenu(menu);
	}
	

	private Menu populateAccelMenu(final Menu menu) {
		for (int mult = 1; mult <= 10; mult++) {
			createAccelMenuItem(menu, mult, mult + "x");
		}

		new MenuItem(menu, SWT.SEPARATOR);
		
		MenuItem item = new MenuItem(menu, SWT.CHECK);
		item.setText("Unbounded");
		if (!realTime.getBoolean()) {
			item.setSelection(true);
		}
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean setting = false;
				realTime.setBoolean(setting);
			}
		});

		new MenuItem(menu, SWT.SEPARATOR);
		
		for (int div = 2; div <= 5; div++) {
			createAccelMenuItem(menu, 1.0 / div, "1/" + div);
		}
		
		if (machine.getExecutor().getCompilerStrategy().canCompile()) {	
			new MenuItem(menu, SWT.SEPARATOR);
			item = new MenuItem(menu, SWT.CHECK);
			item.setText("Compile to Bytecode");
			if (compile.getBoolean()) {
				item.setSelection(true);
			}
			item.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					compile.setBoolean(!compile.getBoolean());
				}
			});
		}

		return menu;
	}

	private void createAccelMenuItem(final Menu menu, double factor, String label) {
		boolean isRealTime = realTime.getBoolean();
		int curCycles = cyclesPerSecond.getInt();
		MenuItem item = new MenuItem(menu, SWT.RADIO);
		final int cycles = (int) (machine.getCpu().getBaseCyclesPerSec() * factor);
		item.setText(((factor >= 1 && factor < 10) ? "&" : "") + label + " (" + cycles + ")");
		if (isRealTime && cycles == curCycles) {
			item.setSelection(true);
		}
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				realTime.setBoolean(true);
				cyclesPerSecond.setInt(cycles);
			}
		});
	}
	



	/**
	 * @return
	 */
	private static ImageProvider createDeviceImageProvider(Shell shell) {

		TreeMap<Integer, Image> mainIcons = new TreeMap<Integer, Image>();
		for (int size : new int[] { 16, 32, 64, 128 }) {
			Image icons = Emulator.loadImage(shell.getDisplay(), "icons/dev_icons_" + size + ".png");
			mainIcons.put(size, icons);
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
