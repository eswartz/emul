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
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.ejs.gui.common.SwtDialogUtils;

import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.IExecutor;
import v9t9.common.demos.IDemoHandler;
import v9t9.common.demos.IDemoManager;
import v9t9.common.dsr.IDeviceIndicatorProvider;
import v9t9.common.events.IEventNotifier.Level;
import v9t9.common.events.NotifyException;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import v9t9.gui.EmulatorGuiData;
import v9t9.gui.client.swt.SwtWindow;
import v9t9.gui.client.swt.shells.DemoProgressBar;
import v9t9.gui.client.swt.shells.DemoSelector;
import v9t9.gui.client.swt.shells.DiskSelectorDialog;
import v9t9.gui.client.swt.shells.ModuleSelector;
import v9t9.gui.client.swt.shells.ROMSetupDialog;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.settings.ISettingSection;
import ejs.base.settings.SettingsSection;

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

	private String lastSelectedRecordPath;
	private ISettingSection savedPreDemoState;
	protected IPropertyListener unpauseAfterDemoListener;
	private IPropertyListener playListener;

	/**
	 * @param swtWindow
	 * @param mainComposite
	 */
	public EmulatorStatusBar(final SwtWindow swtWindow, 
			ImageProvider iconImageProvider,
			Composite mainComposite, final IMachine machine,
			int[] colors, float[] points, int style) {
		super(swtWindow, iconImageProvider, 
				mainComposite, machine, colors, points, style);
		
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
			int demoIconIndex, String tooltip) {

		final IProperty pauseSetting = Settings.get(machine, IDemoHandler.settingDemoPaused);
		final IProperty recordSetting = Settings.get(machine, IDemoHandler.settingRecordDemo);
		final IProperty playSetting = Settings.get(machine, IDemoHandler.settingPlayingDemo);
		final IProperty reverseSetting = Settings.get(machine, IDemoHandler.settingDemoReversing);
		final IProperty useOldFormatSetting = Settings.get(machine, IDemoManager.settingUseDemoOldFormat);
		
		final BasicButton button = new BasicButton(buttonBar, SWT.PUSH,
				imageProvider, demoIconIndex, tooltip) {
			/* (non-Javadoc)
			 * @see v9t9.gui.client.swt.bars.ImageButton#isEventOverMenu(org.eclipse.swt.events.MouseEvent)
			 */
			@Override
			protected boolean isEventOverMenu(MouseEvent e) {
				if (super.isEventOverMenu(e))
					return true;
				
				if (recordSetting.getBoolean() || playSetting.getBoolean()) {
					// upper-left corner toggles these
					if (isPointOverDemoControlIcon(getSize(), e.x, e.y)) {
						return true;
					}
				}
				
				return false;
			}
		};

		IPropertyListener demoButtonStateListener = new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				setDemoButtonOverlay(button, playSetting, recordSetting, pauseSetting, reverseSetting);
			}
		};
		
		recordSetting.addListener(demoButtonStateListener);
		playSetting.addListener(demoButtonStateListener);
		reverseSetting.addListener(demoButtonStateListener);
		pauseSetting.addListener(demoButtonStateListener);
		
		// the demo button controls pausing (when something active)
		// else triggers the menu
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (!isPointOverDemoControlIcon(button.getSize(), e.x, e.y))
					toggleDemoDialog();
			}
		});
		
//
//		button.addMouseListener(new MouseAdapter() {
//			@Override
//			public void mouseDown(MouseEvent e) {
//				Rectangle bounds = button.getBounds();
//				if (e.y < bounds.height / 2) {
//					double rate = (Double) playRateSetting.getValue();
//					if (e.x < bounds.width / 3) {
//						rate /= 1.1;
//					}
//					else if (e.x < bounds.width * 2 / 3) {
//						
//					}
//					else {
//						rate *= 1.1;
//					}
//					playRateSetting.setValue(rate);
//				}
//			}
//			
//			@Override
//			public void mouseDoubleClick(MouseEvent e) {
//				toggleDemoDialog();
//			}
//		});


		button.setMenuOverlayBounds(imageProvider.imageIndexToBounds(IconConsts.MENU_OVERLAY));
		button.addMenuDetectListener(new MenuDetectListener() {

			public void menuDetected(MenuDetectEvent e) {
				final IDemoHandler handler = machine.getDemoHandler();
				if (handler != null) {
					Point menuLoc = button.toControl(((Control) e.widget).toDisplay(e.x, e.y));
					boolean pointOverDemoControlIcon = isPointOverDemoControlIcon(button.getSize(), menuLoc.x, menuLoc.y); 
					if ((recordSetting.getBoolean() || playSetting.getBoolean())
							&& pointOverDemoControlIcon) { 
						pauseSetting.setBoolean(!pauseSetting.getBoolean());
					} else {
						showDemoMenu(handler, e, e.x, e.y, 
								recordSetting, playSetting, pauseSetting,
								useOldFormatSetting);
					}
					
					e.doit = false;
				}
			}
		});
		
		// initialize state
		setDemoButtonOverlay(button, playSetting, recordSetting, pauseSetting, reverseSetting);
		//button.setSelection(pauseSetting.getBoolean());
		
		if (playListener != null)
			playSetting.removeListener(playListener);
		playListener = new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				if (property.getBoolean()) {
					swtWindow.showToolShell(
							DemoProgressBar.DEMO_PROGRESS_BAR_ID,
							DemoProgressBar.getToolShellFactory(buttonBar, machine, swtWindow));
					
					machine.setPaused(true);
					savedPreDemoState = new SettingsSection(null);
					machine.saveState(savedPreDemoState);
					
					final IProperty machinePauseSetting = Settings.get(
							machine, IMachine.settingPauseMachine);

					if (unpauseAfterDemoListener == null) {
						unpauseAfterDemoListener = new IPropertyListener() {
							
							@Override
							public void propertyChanged(IProperty property) {
	
								if (!property.getBoolean() && savedPreDemoState != null) {
									// make sure a playing demo stops!
									try {
										machine.getDemoHandler().stopPlayback();
									} catch (NotifyException e) {
									}
									
									machine.loadState(savedPreDemoState);
									savedPreDemoState = null;
									
									machinePauseSetting.removeListener(unpauseAfterDemoListener);
									
									// make sure still unpaused, in case demo playback
									// stopping above reset the pause state
									machinePauseSetting.setBoolean(false);
								}
							}
						};
					}
					machinePauseSetting.addListener(unpauseAfterDemoListener);
				} else {
					swtWindow.closeToolShell(
							DemoProgressBar.DEMO_PROGRESS_BAR_ID);
				}
			}
		};
		
		playSetting.addListener(playListener);
		return button;
		
	}

	/**
	 * @param button
	 * @param playSetting
	 * @param recordSetting
	 * @param pauseSetting
	 * @param reverseSetting
	 */
	protected void setDemoButtonOverlay(final BasicButton button,
			IProperty playSetting, IProperty recordSetting,
			IProperty pauseSetting, IProperty reverseSetting) {
		if (!playSetting.getBoolean() && !recordSetting.getBoolean()) {
			button.setOverlayBounds(null);
		}
		else if (pauseSetting.getBoolean()) {
			button.setOverlayBounds(imageProvider.imageIndexToBounds(IconConsts.PAUSE_OVERLAY));
		} else {
			button.setOverlayBounds(imageProvider.imageIndexToBounds(
					recordSetting.getBoolean() ? IconConsts.RECORD_OVERLAY : 
						reverseSetting.getBoolean() ? IconConsts.REVERSE_OVERLAY : IconConsts.PLAY_OVERLAY));
		} 
		
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (!button.isDisposed())
					button.redraw();
			}
		});

	}


	protected boolean isPointOverDemoControlIcon(Point size, int x, int y) {
		return x < size.x / 2 && y < size.y / 2;
	}


	private void showDemoMenu(final IDemoHandler demoHandler, TypedEvent e, int x, int y, 
			final IProperty recordSetting, final IProperty playSetting,
			final IProperty pauseSetting, final IProperty useOldFormatSetting) {
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
				final MenuItem stopItem = new MenuItem(menu, SWT.NONE);
				stopItem.setText("Stop playing " + currentFilename);
				//stopItem.setSelection(true);
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
		
		URI last_ = demoHandler.getRecordingURI();
		if (last_ == null)
			last_ = demoHandler.getPlaybackURI();
		final URI last = last_;
		if (last != null) {
			final MenuItem lastDemoItem = new MenuItem(menu, SWT.PUSH);
			lastDemoItem.setText("Play " + (last.getScheme().equals("file") ? last.getPath() : last.toString()));
			lastDemoItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						demoHandler.startPlayback(last);
					} catch (NotifyException ex) {
						machine.getEventNotifier().notifyEvent(ex.getEvent());
					}
				}
			});
		}
		
		////
		final IProperty recordPath = Settings.get(machine, IDemoManager.settingRecordedDemosPath);
		final String demoDir = (recordPath.getString() == null || recordPath.getString().isEmpty())
				? "/tmp" : recordPath.getString();
		
		if (currentFilename == null) {
			recordItem = new MenuItem(menu, SWT.RADIO);

			if (playSetting.getBoolean())
				recordItem.setEnabled(false);
			recordItem.setSelection(false);
			recordItem.setText("Record demo...");
			recordItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					
					boolean wasPaused = machine.setPaused(true);
					
					String name = "demo";
					if (lastSelectedRecordPath != null) {
						name = lastSelectedRecordPath;
					}
					
					String filenameBase = SwtDialogUtils.openFileSelectionDialog(
							menu.getShell(),
							"Select demo base name",
							demoDir,
							name, true,
							IDemoManager.DEMO_EXTENSIONS);
					File saveFile = null;
					if (filenameBase != null) {
						lastSelectedRecordPath = filenameBase;
						saveFile = SwtDialogUtils.getUniqueFile(filenameBase);
						if (saveFile == null) {
							SwtDialogUtils.showErrorMessage(menu.getShell(), "Save error", 
									"Too many demo files here!");
							machine.setPaused(wasPaused);
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
							if (ex.getCause() != null)
								machine.getEventNotifier().notifyEvent(ex.getEvent().context,
										ex.getEvent().level, ex.getEvent().message + "\n(" 
												+ ex.getCause().getMessage() +")");
							else
								machine.getEventNotifier().notifyEvent(ex.getEvent());
						}
					}
					machine.setPaused(wasPaused);
				}

			});
			
			final MenuItem oldFormatItem = new MenuItem(menu, SWT.CHECK);
			if (recordSetting.getBoolean())
				oldFormatItem.setEnabled(false);
			oldFormatItem.setSelection(useOldFormatSetting.getBoolean());
			oldFormatItem.setText("Use V9t9 v6.0 &Format");
			oldFormatItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					useOldFormatSetting.setBoolean(oldFormatItem.getSelection());
				}
			});
			
		}
		
		new MenuItem(menu, SWT.SEPARATOR);
		MenuItem rateItem = new MenuItem(menu, SWT.NONE);
		rateItem.setText("Playback rate:");
		rateItem.setEnabled(false);
		addRateMenuItems(menu);
		
		swtWindow.showMenu(menu, null, x, y);
	}


	protected void toggleDemoDialog() {
		swtWindow.toggleToolShell(DemoSelector.DEMO_SELECTOR_TOOL_ID,
				DemoSelector.getToolShellFactory(machine, buttonBar, swtWindow));
		
	}

	private Menu addRateMenuItems(final Menu menu) {
		IProperty playbackRate = Settings.get(machine, IDemoHandler.settingDemoPlaybackRate);
		final IProperty reversing = Settings.get(machine, IDemoHandler.settingDemoReversing);
		final IProperty pausing = Settings.get(machine, IDemoHandler.settingDemoPaused);
		
		MenuItem item = new MenuItem(menu, SWT.CHECK);
		item.setText("&Reverse");
		item.setSelection(reversing.getBoolean());
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				reversing.setBoolean(!reversing.getBoolean());
				pausing.setBoolean(false);
			}
		});
		
		new MenuItem(menu, SWT.SEPARATOR);
		
		createRateMenuItem(menu, playbackRate, -1, 1, "1x");
		createRateMenuItem(menu, playbackRate, -1, 1.5, "1.5x");
		createRateMenuItem(menu, playbackRate, -1, 2, "2x");
		createRateMenuItem(menu, playbackRate, -1, 3, "3x");
		createRateMenuItem(menu, playbackRate, -1, 5, "5x");
		
		new MenuItem(menu, SWT.SEPARATOR);
		
		createRateMenuItem(menu, playbackRate, -1, 1.0 / 2, "1/2");
		createRateMenuItem(menu, playbackRate, -1, 1.0 / 3, "1/3");
		createRateMenuItem(menu, playbackRate, -1, 1.0 / 4, "1/4");
		createRateMenuItem(menu, playbackRate, -1, 1.0 / 5, "1/5");
		
		return menu;
	}
	

	private void createRateMenuItem(final Menu menu, final IProperty playbackRate, int index, final double factor, String label) {
		MenuItem item = new MenuItem(menu, SWT.RADIO);
		item.setText(((index >= 0 && index < 10) ? "&" : "") + label);
		if (Math.abs((Double) playbackRate.getValue() - factor) < 0.01) {
			item.setSelection(true);
		}
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				playbackRate.setValue(factor);
			}
		});
	}
		
	
	private Menu createAccelMenu(final Control parent) {
		final Menu menu = new Menu(parent);
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
