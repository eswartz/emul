/**
 * Mar 11, 2011
 */
package v9t9.gui.client.swt.bars;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.ejs.gui.common.SwtDialogUtils;

import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.IExecutor;
import v9t9.common.demo.IDemoHandler;
import v9t9.common.dsr.IDeviceIndicatorProvider;
import v9t9.common.events.IEventNotifier.Level;
import v9t9.common.events.NotifyException;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import v9t9.gui.EmulatorGuiData;
import v9t9.gui.client.swt.SwtWindow;
import v9t9.gui.client.swt.shells.DemoSelector;
import v9t9.gui.client.swt.shells.DiskSelectorDialog;
import v9t9.gui.client.swt.shells.ModuleSelector;
import v9t9.gui.client.swt.shells.ROMSetupDialog;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;

/**
 * This is the bar of buttons and status icons on the left-hand side of
 * the emulator window.  It contains the main configuration controls
 * and information about disk/device activity. 
 * @author ejs
 *
 */
public class EmulatorStatusBar extends BaseEmulatorBar {

	private List<ImageDeviceIndicator> indicators;
	private ImageProvider deviceImageProvider;
	private IProperty realTime;
	private IProperty compile;
	private IProperty cyclesPerSecond;
	private IProperty devicesChanged;
	private BlankIcon indicatorsBlank;
	
	/**
	 * @param swtWindow
	 * @param mainComposite
	 */
	public EmulatorStatusBar(final SwtWindow swtWindow, 
			ImageProvider iconImageProvider,
			Composite mainComposite, final IMachine machine,
			int[] colors, float[] points, boolean isHorizontal) {
		super(swtWindow, iconImageProvider, 
				mainComposite, machine, colors, points, isHorizontal);
		
		realTime = Settings.get(machine, ICpu.settingRealTime);
		compile = Settings.get(machine, IExecutor.settingCompile);
		cyclesPerSecond = Settings.get(machine, ICpu.settingCyclesPerSecond);

		
		deviceImageProvider = createDeviceImageProvider(swtWindow.getShell());

		createButton(IconConsts.ROM_SETUP,
				"Setup ROMs", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						ROMSetupDialog dialog = ROMSetupDialog.createDialog(
								swtWindow.getShell(), machine, swtWindow);
			        	dialog.open();
					}
				}
			);
		

		createDemoButton(IconConsts.DEMO,
				IconConsts.PLAY_OVERLAY, IconConsts.RECORD_OVERLAY,
				IconConsts.PAUSE_OVERLAY,
				"Play or record demo");

		new BlankIcon(buttonBar, SWT.NONE);
			
		if (machine.getModuleManager() != null) {
			createButton(IconConsts.MODULE_SWITCH,
				"Switch module", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						swtWindow.toggleToolShell(ModuleSelector.MODULE_SELECTOR_TOOL_ID,
								ModuleSelector.getToolShellFactory(machine, buttonBar, swtWindow));
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
		
		devicesChanged = Settings.get(machine, IDeviceIndicatorProvider.settingDevicesChanged);

		indicators = new ArrayList<ImageDeviceIndicator>();
		
		indicatorsBlank = new BlankIcon(buttonBar, SWT.NONE);
		
		for (IDeviceIndicatorProvider provider : machine.getModel().getDeviceIndicatorProviders(machine))
			addDeviceIndicatorProvider(provider);


		devicesChanged.addListener(new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				buttonBar.getDisplay().asyncExec(new Runnable() {
					public void run() {
						for (ImageDeviceIndicator indic : indicators) {
							indic.dispose();
						}
						indicators.clear();

						for (IDeviceIndicatorProvider provider : machine.getModel().getDeviceIndicatorProviders(machine))
							addDeviceIndicatorProvider(provider);
						
						buttonBar.layout(true, true);
					}
				});
			}
		});

		createButton(IconConsts.CPU_ACCELERATE, "Accelerate execution",
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Control button = (Control) e.widget;
						Point size = button.getSize();
						swtWindow.showMenu(createAccelMenu(button), button, size.x / 2, size.y / 2);
					}
				});

	}


	private BasicButton createDemoButton( 
			int demoIconIndex,
			final int playOverlay, final int recordOverlay,
			final int pauseOverlay, String tooltip) {
		
		final BasicButton button = new BasicButton(buttonBar, SWT.PUSH,
				imageProvider, demoIconIndex, tooltip);
		
		final IProperty pauseSetting = Settings.get(machine, IDemoHandler.settingDemoPaused);
		final IProperty recordSetting = Settings.get(machine, IDemoHandler.settingRecordDemo);
		final IProperty playSetting = Settings.get(machine, IDemoHandler.settingPlayingDemo);
		
		addSettingToggleListener(button, recordSetting, demoIconIndex, recordOverlay,
				true, false);
		
		addSettingToggleListener(button, playSetting, demoIconIndex, playOverlay,
				true, false);
		
		// the demo button controls pausing (when something active)
		// else triggers the menu
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (recordSetting.getBoolean() || playSetting.getBoolean()) {
					if (pauseSetting.getBoolean())
						button.setOverlayBounds(imageProvider.imageIndexToBounds(
								recordSetting.getBoolean() ? recordOverlay : playOverlay));
					else
						button.setOverlayBounds(imageProvider.imageIndexToBounds(pauseOverlay));
					
					pauseSetting.setBoolean(!pauseSetting.getBoolean());

					button.redraw();

				} else {
					toggleDemoDialog();
				}
			}
		});

		button.setMenuOverlayBounds(imageProvider.imageIndexToBounds(IconConsts.MENU_OVERLAY));
		button.addMenuDetectListener(new MenuDetectListener() {

			public void menuDetected(MenuDetectEvent e) {
				final IDemoHandler handler = machine.getDemoHandler();
				if (handler != null) {
					showDemoMenu(handler, e, e.x, e.y, 
							recordSetting, playSetting, pauseSetting);
				}
			}
		});

		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				toggleDemoDialog();
			}
		});

		// initialize state
		if (recordSetting.getBoolean() || playSetting.getBoolean()) {
			if (!pauseSetting.getBoolean()) {
				button.setOverlayBounds(imageProvider.imageIndexToBounds(
						recordSetting.getBoolean() ? recordOverlay : playOverlay));
			} else {
				button.setOverlayBounds(imageProvider.imageIndexToBounds(pauseOverlay));
			}
		}					
		button.setSelection(pauseSetting.getBoolean());
		
		return button;
		
	}

	private void showDemoMenu(final IDemoHandler demoHandler, TypedEvent e, int x, int y, 
			final IProperty recordSetting, final IProperty playSetting,
			final IProperty pauseSetting) {
		Control button = (Control) e.widget;
		final Menu menu = new Menu(button);

		String currentFilename = null;
		
		if (playSetting.getBoolean()) {
			URI uri = demoHandler.getPlaybackURI();
			currentFilename = String.valueOf(uri);
		} else if (recordSetting.getBoolean()) {
			URI uri = demoHandler.getRecordingURI();
			currentFilename = String.valueOf(uri);
		}
		MenuItem recordItem;
		
		if (currentFilename != null) {
			if (playSetting.getBoolean()) {
				final MenuItem stopItem = new MenuItem(menu, SWT.RADIO);
				stopItem.setText("Stop playing " + currentFilename);
				stopItem.setSelection(true);
				stopItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						try {
							demoHandler.stopPlayback();
						} catch (NotifyException ex) {
							machine.getEventNotifier().notifyEvent(ex.getEvent());
						}
					}
	
				});
			} else {
				recordItem = new MenuItem(menu, SWT.RADIO);
				
				recordItem.setText("Stop recording " + currentFilename);
				recordItem.setSelection(true);
				recordItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						try {
							demoHandler.stopRecording();
						} catch (NotifyException ex) {
							machine.getEventNotifier().notifyEvent(ex.getEvent());
						}
					}

				});
			}
		} 
		
		////
		
		final URI lastRecorded = demoHandler.getRecordingURI();
		if (lastRecorded != null) {
			final MenuItem lastDemoItem = new MenuItem(menu, SWT.PUSH);
			lastDemoItem.setText("Play " + lastRecorded.getPath());
			lastDemoItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						demoHandler.startPlayback(lastRecorded);
					} catch (NotifyException ex) {
						machine.getEventNotifier().notifyEvent(ex.getEvent());
					}
				}
			});
		}
		
		////
		final IProperty searchPath = Settings.get(machine, IDemoHandler.settingUserDemosPath);
		final IProperty recordPath = Settings.get(machine, IDemoHandler.settingRecordedDemosPath);
		final String demoDir = (recordPath.getString() == null || recordPath.getString().isEmpty())
				? "/tmp" : recordPath.getString();
		
		makeBrowseItem(demoHandler, menu, menu, demoDir, searchPath, "Browse for demos...");
		
		if (currentFilename == null) {
			recordItem = new MenuItem(menu, SWT.RADIO);

			if (playSetting.getBoolean())
				recordItem.setEnabled(false);
			recordItem.setSelection(false);
			recordItem.setText("Record demo...");
			recordItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String filenameBase = SwtDialogUtils.openFileSelectionDialog(
							menu.getShell(),
							"Record demo file",
							recordPath.getString(),
							"demo", true,
							IDemoHandler.DEMO_EXTENSIONS);
					File saveFile = null;
					if (filenameBase != null) {
						saveFile = SwtDialogUtils.getUniqueFile(filenameBase);
						if (saveFile == null) {
							SwtDialogUtils.showErrorMessage(menu.getShell(), "Save error", 
									"Too many demo files here!");
							return;
						}
						
						URI parent = saveFile.getParentFile().toURI();
						recordPath.setString(parent.toString());
						
						try {
							demoHandler.startRecording(saveFile.toURI());
							machine.getEventNotifier().notifyEvent(
									null, Level.INFO, 
									"Recording to " + saveFile);
						} catch (NotifyException ex) {
							machine.getEventNotifier().notifyEvent(ex.getEvent());
						}
					}
					
				}

			});
		}
		
		
		swtWindow.showMenu(menu, null, x, y);
	}


	/**
	 * 
	 */
	protected void toggleDemoDialog() {
		swtWindow.toggleToolShell(DemoSelector.DEMO_SELECTOR_TOOL_ID,
				DemoSelector.getToolShellFactory(machine, buttonBar, swtWindow));
		
	}


	/**
	 * @param demoHandler
	 * @param menu
	 * @param playSubMenu
	 * @param demoDir
	 * @param searchPath
	 */
	protected void makeBrowseItem(final IDemoHandler demoHandler,
			final Menu menu, Menu playSubMenu, final String demoDir,
			final IProperty searchPath, String label) {
		MenuItem playBrowseItem = new MenuItem(playSubMenu, SWT.PUSH); 
		playBrowseItem.setText(label);
		
		playBrowseItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String filename = SwtDialogUtils.openFileSelectionDialog(
						menu.getShell(),
						"Select demo file", 
						demoDir, 
						null, false,
						IDemoHandler.DEMO_EXTENSIONS);
				if (filename != null) {
					File playFile = new File(filename);
					String parent = playFile.getParentFile().getAbsolutePath(); 
					if (!searchPath.getList().contains(parent)) {
						searchPath.getList().add(parent);
						searchPath.firePropertyChange();
					}
					
					try {
						demoHandler.startPlayback(playFile.toURI());
					} catch (NotifyException ex) {
						machine.getEventNotifier().notifyEvent(ex.getEvent());
					}
				}
			}
		});
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
			Image icons = EmulatorGuiData.loadImage(shell.getDisplay(), "icons/dev_icons_" + size + ".png");
			mainIcons.put(size, icons);
		}

		return new MultiImageSizeProvider(mainIcons);
	}

	public void dispose() {
		for (ImageDeviceIndicator indic : indicators)
			indic.dispose();
		indicators.clear();
	}

	public void addDeviceIndicatorProvider(IDeviceIndicatorProvider provider) {
		ImageDeviceIndicator indic = new ImageDeviceIndicator(buttonBar, SWT.NONE, 
				deviceImageProvider, provider);
		indic.moveAbove(indicatorsBlank);
		indicators.add(indic);
	}

}
