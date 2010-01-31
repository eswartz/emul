/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.ejs.coffee.core.utils.ISettingListener;
import org.ejs.coffee.core.utils.PrefUtils;
import org.ejs.coffee.core.utils.Setting;

import v9t9.emulator.EmulatorSettings;
import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.BaseEmulatorWindow;
import v9t9.emulator.clients.builtin.sound.JavaSoundHandler;
import v9t9.emulator.clients.builtin.swt.debugger.DebuggerWindow;
import v9t9.emulator.hardware.V9t9;
import v9t9.emulator.runtime.Cpu;
import v9t9.emulator.runtime.Executor;

/**
 * Provide the emulator in an SWT window
 * @author ejs
 *
 */
public class SwtWindow extends BaseEmulatorWindow {
	
	protected Shell shell;
	protected Control videoControl;
	private ButtonBar buttonBar;
	private List<Shell> toolShells;
	private Timer toolUiTimer;
	private Image mainIcons;
	private Canvas cpuMetricsCanvas;
	
	public SwtWindow(Display display, final ISwtVideoRenderer renderer, final Machine machine) {
		super(machine);
		setVideoRenderer(renderer);
		
		toolShells = new ArrayList<Shell>();
		toolUiTimer = new Timer(true);
		
		shell = new Shell(display, SWT.SHELL_TRIM | SWT.RESIZE);
		shell.setText("V9t9");
		
		File iconFile = V9t9.getDataFile("icons/v9t9.png");
		Image icon = new Image(shell.getDisplay(), iconFile.getAbsolutePath());
		
		shell.setImage(icon);

		shell.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}
			
		});
		
		if (false) {
			Menu bar = new Menu(shell, SWT.BAR);
			createAppMenu(shell, bar, false);
			shell.setMenuBar(bar);
		}
		
		Composite mainComposite = shell;
		GridLayoutFactory.fillDefaults().margins(2, 2).applyTo(mainComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(mainComposite);
		
		Composite topComposite = new Composite(mainComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(topComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(topComposite);
		
		this.videoControl = renderer.createControl(topComposite, SWT.BORDER);
		
		final GridData rendererLayoutData = GridDataFactory.swtDefaults()
			.indent(0, 0)
			.align(SWT.CENTER, SWT.CENTER)
			.grab(true, true)
			.create();
		videoControl.setLayoutData(rendererLayoutData);
		
		((ISwtVideoRenderer) videoRenderer).addMouseEventListener(new MouseAdapter() {
			
			@Override
			public void mouseDown(final MouseEvent e) {
				
				//System.out.println("Mouse detected " + e);
				if (e.button == 1) {
					videoControl.forceFocus();
					return;
				}
				if (!SWT.getPlatform().equals("win32") && e.button == 3) {
					showAppMenu(e);
					
					
				}
			}
			
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.MouseAdapter#mouseUp(org.eclipse.swt.events.MouseEvent)
			 */
			@Override
			public void mouseUp(MouseEvent e) {
				if (SWT.getPlatform().equals("win32") && e.button == 3) {
					showAppMenu(e);
				}
			}
			
		});
		
		createButtons(mainComposite);
		
		cpuMetricsCanvas = new CpuMetricsCanvas(buttonBar, SWT.BORDER, machine.getCpuMetrics());
		
		
		JavaSoundHandler.settingPlaySound.addListener(new ISettingListener() {

			public void changed(Setting setting, Object oldValue) {
				JavaSoundHandler.settingPlaySound.saveState(EmulatorSettings.getInstance().getApplicationSettings());
			}
			
		});
		
		shell.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						shell.layout(true);
						//shell.pack();
;					}
				});
			}
		});
		
		shell.open();
		
		shell.getDisplay().asyncExec(new Runnable() {
			public void run() {
				DialogSettings applicationSettings = EmulatorSettings.getInstance().getApplicationSettings();
				
				int zoom = PrefUtils.readSavedInt(applicationSettings, "ZoomLevel");
				if (zoom > 0) {
					videoRenderer.setZoom(zoom);
				}
				
				Cpu.settingCyclesPerSecond.loadState(applicationSettings);
				Cpu.settingRealTime.loadState(applicationSettings);
				JavaSoundHandler.settingPlaySound.loadState(applicationSettings);
			}
		});
		
		shell.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				renderer.setFocus();
			}
		});
		
		renderer.setFocus();
	}

	/**
	 * 
	 */
	protected void showAppMenu(MouseEvent e) {
		// we need this horrible hack or else the
		// menu will disappear immediately because SWT and AWT
		// don't agree on events
		final Shell menuShell = new Shell(getShell(), SWT.ON_TOP | SWT.TOOL);
		menuShell.setSize(16, 16);
		Point shellLoc = (((Control)e.widget).toDisplay(e.x, e.y));
		menuShell.setLocation(shellLoc);
		final Menu menu = createAppMenu(menuShell, menuShell, true);
		menuShell.open();
		menuShell.setVisible(false);
		menuShell.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				menu.dispose();
			}

			public void focusGained(FocusEvent e) {
				
			}
		});
		menuShell.getDisplay().syncExec(new Runnable() {
			public void run() {
				runMenu(null, 0, 0, menu);
				menuShell.dispose();		
			}
		});
		
	}

	private void createButtons(Composite parent) {
		File iconsFile = V9t9.getDataFile("icons/icons.png");
		mainIcons = new Image(getShell().getDisplay(), iconsFile.getAbsolutePath());
		
		buttonBar = new ButtonBar(parent, SWT.HORIZONTAL);
		GridLayout mainLayout = new GridLayout(1, false);
		mainLayout.marginHeight = mainLayout.marginWidth = 0;
		buttonBar.setLayout(mainLayout);

		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BOTTOM).applyTo(buttonBar);

		createButton(buttonBar, 1,
				"Send a NMI interrupt", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						sendNMI();
					}
				});

		createButton(buttonBar, 4,
				"Reset the computer", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						sendReset();
					}
				});

		createStateButton(buttonBar,
				Executor.settingDumpFullInstructions, 
				2, 0, "Toggle CPU logging");

		createButton(buttonBar,
				7, "Create debugger window", 
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						final Shell shell = new Shell(getShell(), SWT.DIALOG_TRIM | SWT.RESIZE);
						final DebuggerWindow window = new DebuggerWindow(shell, SWT.NONE, machine, toolUiTimer);
						createToolShell(shell, window, "DebuggerWindowBounds");	
					}
			}
		);
		
		/*
		createButton(buttonBar,
				0, 
				"Branch to Condensed BASIC",
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Cpu cpu = SwtWindow.this.machine.getExecutor().cpu;
						cpu.setPC((short)0xa000);								
						cpu.setWP((short)0x83e0);								
					}
				});
		*/
		
		createButton(buttonBar, 3,
				"Paste into keyboard", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						pasteClipboardToKeyboard();
					}
			});
		
		createButton(buttonBar, 6,
				"Load or save machine state", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Control button = (Control) e.widget;
						Point size = button.getSize();
						showMenu(createFilePopupMenu(button), button, size.x / 2, size.y / 2);
					}

			});
		
		createStateButton(buttonBar, Machine.settingPauseMachine,
				8, 0,
				 "Pause machine");

		createStateButton(buttonBar, V9t9.settingMonitorDrawing,  
				9, 0, 
				"Apply monitor effect to video");

		createButton(buttonBar, 10,
				"Take screenshot", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						screenshot();
					}
			});

		/*
		createButton(buttonBar, 11,
				"Zoom the screen", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Control button = (Control) e.widget;
						Point size = button.getSize();
						showMenu(createZoomMenu(button), button, size.x / 2, size.y / 2);
					}
				});
		*/
		createButton(buttonBar, 12,
				"Accelerate execution", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Control button = (Control) e.widget;
						Point size = button.getSize();
						showMenu(createAccelMenu(button), button, size.x / 2, size.y / 2);
					}
				});
		
		final BasicButton soundButton = createStateButton(buttonBar, 
				JavaSoundHandler.settingPlaySound, 
				true, null,
				13, 14,
				"Sound options");
	/*	soundButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Control button = (Control) e.widget;
				Point size = button.getSize();
				showMenu(createSoundMenu(button), button, size.x / 2, size.y / 2);
			}
		});*/
		
		soundButton.setMenuOverlayBounds(mainIconIndexToBounds(15));
		soundButton.addMenuDetectListener(new MenuDetectListener() {

			public void menuDetected(MenuDetectEvent e) {
				Control button = (Control) e.widget;
				Menu menu = new Menu(button);
				if (machine.getSound().getSoundHandler() instanceof JavaSoundHandler) {
					JavaSoundHandler javaSoundHandler = (JavaSoundHandler) machine.getSound().getSoundHandler();
					javaSoundHandler.getSoundRecordingHelper().populateSoundMenu(menu);
					javaSoundHandler.getSpeechRecordingHelper().populateSoundMenu(menu);
				}
				showMenu(menu, button, e.x, e.y);
			}
		});
	}

	/*
	private void createControls() {
		Button spawnMemoryViewButton = new Button(controlsComposite, SWT.PUSH | SWT.NO_FOCUS);
		spawnMemoryViewButton.setText("View Memory...");
		spawnMemoryViewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final Shell shell = new Shell(getShell(), SWT.DIALOG_TRIM | SWT.RESIZE);
				final MemoryViewer cpuMemory = new MemoryViewer(shell, SWT.NONE, machine.getMemory(), toolUiTimer);
				createToolShell(shell, cpuMemory, "MemoryWindowBounds");
			}
		});
		
		
		Button spawnCpuViewButton = new Button(controlsComposite, SWT.PUSH | SWT.NO_FOCUS);
		spawnCpuViewButton.setText("View CPU...");
		spawnCpuViewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final Shell shell = new Shell(getShell(), SWT.DIALOG_TRIM | SWT.RESIZE);
				final CpuViewer cpuViewer = new CpuViewer(shell, SWT.NONE, machine, toolUiTimer);
				createToolShell(shell, cpuViewer, "CpuWindowBounds");
			}
		});
	}
	 */
	
	protected void createToolShell(final Shell shell, final Composite tool, final String boundsPref) {
		shell.setImage(getShell().getImage());
		shell.setLayout(new GridLayout(1, false));
		
		final GridData data = GridDataFactory.fillDefaults().grab(true, true).hint(400, 300).create();
		tool.setLayoutData(data);
		
		shell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				tool.dispose();
			}
		});
		
		shell.open();
		
		String boundsStr = EmulatorSettings.getInstance().getApplicationSettings().get(boundsPref);
		if (boundsStr != null) {
			Rectangle savedBounds = PrefUtils.readBoundsString(boundsStr);
			if (savedBounds != null)
				shell.setBounds(savedBounds);
		}
		
		shell.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				// try to stay the same (user controlled) size and not 
				// grow to full screen when next packed
				data.heightHint = tool.getSize().y;
			}
		});
		
		shell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				Rectangle bounds = shell.getBounds();
				String boundsStr = PrefUtils.writeBoundsString(bounds);
				EmulatorSettings.getInstance().getApplicationSettings().put(boundsPref, boundsStr);
			}
		});
		addToolShell(shell);
		
	}

	protected void addToolShell(final Shell toolShell) {
		toolShells.add(toolShell);
		toolShell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				toolShells.remove(toolShell);
			}
		});
	}

	private Menu createAppMenu(Decorations control, Object parent, boolean isPopup) {
		Menu appMenu;
		if (parent instanceof Decorations)
			appMenu = new Menu((Decorations) parent, SWT.POP_UP);
		else if (parent instanceof Menu)
			appMenu = (Menu) parent;
		else if (parent instanceof MenuItem)
			appMenu = new Menu((MenuItem) parent);
		else if (parent instanceof Control)
			appMenu = new Menu((Control) parent);
		else
			throw new IllegalArgumentException(parent.toString());
		
		MenuItem fileMenuHeader = new MenuItem(appMenu, SWT.CASCADE);
		fileMenuHeader.setText("&File");
		
		Menu fileMenu = new Menu(control, SWT.DROP_DOWN);
		populateFileMenu(fileMenu, true);
		fileMenuHeader.setMenu(fileMenu);

		MenuItem editMenuHeader = new MenuItem(appMenu, SWT.CASCADE);
		editMenuHeader.setText("&Edit");
		
		Menu editMenu = new Menu(control, SWT.DROP_DOWN);
		populateEditMenu(editMenu);
		editMenuHeader.setMenu(editMenu);

		/*
		MenuItem exit = new MenuItem(fileMenu, SWT.NONE);
		exit.setText("E&xit");
		exit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.exit(0);
			}
		});
		*/
		Menu viewMenu = appMenu;
		if (!isPopup) {
			MenuItem viewMenuHeader = new MenuItem(appMenu, SWT.CASCADE);
			viewMenuHeader.setText("&View");
			
			viewMenu = new Menu(control, SWT.DROP_DOWN);
			viewMenuHeader.setMenu(viewMenu);
		}
		
		/*
		MenuItem zoom = new MenuItem(viewMenu, SWT.CASCADE);
		zoom.setText("&Zoom");
		
		Menu zoomMenu = new Menu(zoom);
		populateZoomMenu(zoomMenu);
		zoom.setMenu(zoomMenu);
		*/
		
		Menu emuMenu = appMenu;
		if (!isPopup) {
			MenuItem emuMenuHeader = new MenuItem(appMenu, SWT.CASCADE);
			emuMenuHeader.setText("&Emulation");
			
			emuMenu = new Menu(control, SWT.DROP_DOWN);
			emuMenuHeader.setMenu(emuMenu);
		}
		
		MenuItem accel= new MenuItem(emuMenu, SWT.CASCADE);
		accel.setText("&Accelerate");
		
		Menu accelMenu = new Menu(accel);
		populateAccelMenu(accelMenu);
		accel.setMenu(accelMenu);
		
		return appMenu;
	}
	
	@Override
	public void dispose() {
		cpuMetricsCanvas.dispose();
		
		toolUiTimer.cancel();
		for (Object shell : toolShells.toArray()) {
			((Shell)shell).dispose();
		}
		super.dispose();
	}

	protected void showMenu(Menu menu, final Control parent, final int x, final int y) {
		runMenu(parent, x, y, menu);
		menu.dispose();		
	}

	private void runMenu(final Control parent, final int x, final int y,
			final Menu menu) {
		if (parent != null) {
			Point loc = parent.toDisplay(x, y); 
			menu.setLocation(loc);
		}
		System.out.println("position: " + menu.getParent().getLocation());
		menu.setVisible(true);
		
		//System.out.println("Running menu");
		final Shell menuShell = getShell();
		Display display = menuShell.getDisplay();
		while (display.readAndDispatch()) /**/ ;

		while (!menu.isDisposed() && menu.isVisible()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
	}

	/*
	private Menu createZoomMenu(final Control parent) {
		final Menu menu = new Menu(parent);
		return populateZoomMenu(menu);
	}

	private Menu populateZoomMenu(final Menu menu) {
		int curZoom = videoRenderer.getZoom();
		int[] zooms = { 1, 2, 3, 4, 5, 6, 7, 8 };
		for (final int zoom : zooms) {
			MenuItem item = new MenuItem(menu, SWT.RADIO);
			item.setText("" + zoom);
			if (zoom == curZoom)
				item.setSelection(true);
			item.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					setScreenZoom(zoom);
					EmulatorSettings.getInstance().getApplicationSettings().put("ZoomLevel", zoom);
				}

			});
		}
		return menu;
	}
	
	protected void setScreenZoom(int zoom) {
		System.out.println("Set zoom to " + zoom);
		videoRenderer.setZoom(zoom);
	}
	 */
	private Menu createFilePopupMenu(final Control parent) {
		final Menu menu = new Menu(parent);
		return populateFileMenu(menu, false);
	}
	
	private Menu populateFileMenu(final Menu menu, boolean withExit) {
		MenuItem open = new MenuItem(menu, SWT.NONE);
		open.setText("&Open machine state");
		open.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				loadMachineState();
			}
		});
		MenuItem save = new MenuItem(menu, SWT.NONE);
		save.setText("&Save machine state");
		save.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				saveMachineState();
			}
		});

		if (withExit) {
			MenuItem exit = new MenuItem(menu, SWT.NONE);
			exit.setText("E&xit");
			exit.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					System.exit(0);
				}
			});
		}
		
		return menu;
	}
	
	private Menu populateEditMenu(final Menu menu) {
		MenuItem paste = new MenuItem(menu, SWT.NONE);
		paste.setText("&Paste into keyboard");
		paste.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				pasteClipboardToKeyboard();
			}
		});
		
		return menu;
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
		if (!Cpu.settingRealTime.getBoolean()) {
			item.setSelection(true);
		}
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean setting = false;
				Cpu.settingRealTime.setBoolean(setting);
				Cpu.settingRealTime.saveState(EmulatorSettings.getInstance().getApplicationSettings());
			}
		});

		new MenuItem(menu, SWT.SEPARATOR);
		
		for (int div = 2; div <= 5; div++) {
			createAccelMenuItem(menu, 1.0 / div, "1/" + div);
		}

		return menu;
	}

	private void createAccelMenuItem(final Menu menu, double factor, String label) {
		boolean isRealTime = Cpu.settingRealTime.getBoolean();
		int curCycles = Cpu.settingCyclesPerSecond.getInt();
		MenuItem item = new MenuItem(menu, SWT.RADIO);
		final int cycles = (int) (Cpu.TMS_9900_BASE_CYCLES_PER_SEC * factor);
		item.setText(label + " (" + cycles + ")");
		if (isRealTime && cycles == curCycles) {
			item.setSelection(true);
		}
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Cpu.settingRealTime.setBoolean(true);
				Cpu.settingCyclesPerSecond.setInt(cycles);
				Cpu.settingRealTime.saveState(EmulatorSettings.getInstance().getApplicationSettings());
				Cpu.settingCyclesPerSecond.saveState(EmulatorSettings.getInstance().getApplicationSettings());
			}
		});
	}
	
	
	private BasicButton createButton(ButtonBar buttonBar, int iconIndex, String tooltip, SelectionListener selectionListener) {
		Rectangle bounds = mainIconIndexToBounds(iconIndex);
		BasicButton button = new BasicButton(buttonBar, SWT.PUSH, mainIcons, bounds, tooltip);
		button.addSelectionListener(selectionListener);
		return button;
	}
	
	private Rectangle mainIconIndexToBounds(int iconIndex) {
		Rectangle bounds = mainIcons.getBounds();
		int unit = bounds.width;
		return new Rectangle(0, unit * iconIndex, unit, unit); 
	}

	private BasicButton createStateButton(ButtonBar buttonBar, final Setting setting, 
			final boolean inverted, final Point noClickCorner, 
			int iconIndex, final int overlayIndex, String tooltip) {
		final BasicButton button = new BasicButton(buttonBar, SWT.PUSH, 
				mainIcons, 
				mainIconIndexToBounds(iconIndex), 
				tooltip);
		setting.addListener(new ISettingListener() {

			public void changed(final Setting setting, final Object oldValue) {
				Display.getDefault().asyncExec(new Runnable() {

					public void run() {
						if (button.isDisposed())
							return;
						if (setting.getBoolean() != inverted) {
							button.setOverlayBounds(mainIconIndexToBounds(overlayIndex));
						} else {
							button.setOverlayBounds(null);
						}
						if (setting.getBoolean() != button.getSelection()) {
							button.setSelection(setting.getBoolean());
						}
						button.redraw();
					}
					
				});
			}
			
		});
		
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (noClickCorner != null) {
					if (e.x >= noClickCorner.x || e.y >= noClickCorner.y)
						return;
				}
				machine.asyncExec(new Runnable() {
					public void run() {
						setting.setBoolean(!setting.getBoolean());
					}
				});
				//getShell().getDisplay().asyncExec(new Runnable() {
					//public void run() {
					//}
				//});
			}
		});
		
		if (setting.getBoolean() != inverted) {
			button.setOverlayBounds(mainIconIndexToBounds(overlayIndex));
			button.setSelection(setting.getBoolean());
		}
		return button;
	}
	private BasicButton createStateButton(ButtonBar buttonBar, final Setting setting, 
			int iconIndex, int overlayIndex, String tooltip) {
		return createStateButton(buttonBar, setting, false, null, iconIndex, overlayIndex, tooltip);
	}
	public Shell getShell() {
		return shell;
	}

	protected void pasteClipboardToKeyboard() {
		Clipboard clip = new Clipboard(shell.getDisplay());
		String contents = (String) clip.getContents(TextTransfer.getInstance());
		if (contents == null) {
			contents = (String) clip.getContents(RTFTransfer.getInstance());
		}
		if (contents != null) {
			machine.getKeyboardState().pasteText(contents);
		} else {
			showErrorMessage("Paste Error", 
					"Cannot paste: no text on clipboard");
		}
		clip.dispose();
		
	}

	@Override
	protected void showErrorMessage(String title, String msg) {
		MessageDialog.openError(getShell(), title, msg);
	}


	@Override
	protected String openFileSelectionDialog(String title, String directory,
			String fileName, boolean isSave, String[] extensions) {
		FileDialog dialog = new FileDialog(getShell(), isSave ? SWT.SAVE : SWT.OPEN);
		dialog.setText(title);
		dialog.setFilterPath(directory);
		dialog.setFileName(fileName);
		
		if (extensions != null) {
			String[] exts = new String[extensions.length];
			String[] names = new String[extensions.length];
			int idx = 0;
			for (String extension : extensions) {
				String[] split = extension.split("\\|");
				exts[idx] = "*" + split[0];
				names[idx] = split[1];
				idx++;
			}
			dialog.setFilterExtensions(exts);
			dialog.setFilterNames(names);
		}
		String filename = dialog.open();
		
		if (filename != null && extensions != null) {
			int extIdx = new File(filename).getName().lastIndexOf('.');
			if (extIdx < 0) {
				filename += '.' + dialog.getFilterExtensions()[dialog.getFilterIndex()];
			}
		}
		return filename;
	}
	
	@Override
	protected String openDirectorySelectionDialog(String title, String directory) {
		DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.NONE);
		dialog.setText(title);
		dialog.setFilterPath(directory);
		String dirname = dialog.open();
		return dirname;
	}

}
