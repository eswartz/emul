/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TreeMap;

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
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.ejs.coffee.core.properties.IProperty;
import org.ejs.coffee.core.properties.IPropertyListener;
import org.ejs.coffee.core.properties.SettingProperty;
import org.ejs.coffee.core.utils.PrefUtils;

import v9t9.emulator.Emulator;
import v9t9.emulator.clients.builtin.BaseEmulatorWindow;
import v9t9.emulator.clients.builtin.sound.JavaSoundHandler;
import v9t9.emulator.clients.builtin.swt.ImageButton.ImageProvider;
import v9t9.emulator.clients.builtin.swt.debugger.DebuggerWindow;
import v9t9.emulator.common.BaseEventNotifier;
import v9t9.emulator.common.EmulatorSettings;
import v9t9.emulator.common.IEventNotifier;
import v9t9.emulator.common.Machine;
import v9t9.emulator.common.NotifyEvent;
import v9t9.emulator.common.IEventNotifier.Level;
import v9t9.emulator.runtime.cpu.Cpu;
import v9t9.emulator.runtime.cpu.Executor;

/**
 * Provide the emulator in an SWT window
 * @author ejs
 *
 */
public class SwtWindow extends BaseEmulatorWindow {
	
	private static final String EMULATOR_WINDOW_BOUNDS = "EmulatorWindowBounds";
	protected static final String MODULE_SELECTOR_TOOL_ID = "module.selector";
	protected static final String DISK_SELECTOR_TOOL_ID = "disk.selector";
	protected static final String DEBUGGER_TOOL_ID = "debugger";
	protected Shell shell;
	protected Control videoControl;
	private ButtonBar buttonBar;
	private Map<String, ToolShell> toolShells;
	private Timer toolUiTimer;
	private TreeMap<Integer, Image> mainIcons;
	private ImageProvider imageProvider;
	private Canvas cpuMetricsCanvas;
	private IFocusRestorer focusRestorer;
	private final IEventNotifier eventNotifier;
	private Composite topComposite;
	private MouseJoystickHandler mouseJoystickHandler;
	
	public SwtWindow(Display display, final Machine machine) {
		super(machine);
				
		toolShells = new HashMap<String, ToolShell>();
		toolUiTimer = new Timer(true);
		
		shell = new Shell(display, SWT.SHELL_TRIM | SWT.RESIZE);
		shell.setText("V9t9 [" + machine.getModel().getIdentifier() + "]");
		
		File iconFile = Emulator.getDataFile("icons/v9t9.png");
		Image icon = new Image(shell.getDisplay(), iconFile.getAbsolutePath());
		
		shell.setImage(icon);
		
		shell.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				String boundsPref = PrefUtils.writeBoundsString(shell.getBounds());
				EmulatorSettings.INSTANCE.getSettings().put(EMULATOR_WINDOW_BOUNDS, boundsPref);
				dispose();
			}
			
		});
		
		shell.addControlListener(new ControlAdapter() {
			@Override
			public void controlMoved(ControlEvent e) {
				recenterToolShells();
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
		
		topComposite = new Composite(mainComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(topComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(topComposite);
		
		createButtons(mainComposite);
		
		cpuMetricsCanvas = new CpuMetricsCanvas(buttonBar, SWT.BORDER, machine.getCpuMetrics());
		
		eventNotifier = new BaseEventNotifier() {

			ToolTip lastTooltip = null;
			
			{
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
						if (lastTooltip != null)
							lastTooltip.dispose();
						
						int status = 0;
						if (event.level == Level.INFO)
							status = SWT.ICON_INFORMATION;
						else if (event.level == Level.WARNING)
							status = SWT.ICON_WARNING;
						else
							status = SWT.ICON_ERROR;
						
						ToolTip tip = new ToolTip(shell, SWT.BALLOON | status);
						tip.setText(event.message);
						tip.setAutoHide(true);
						if (event.context instanceof Event) {
							Event e = (Event)event.context;
							Control b = (Control) e.widget;
							tip.setLocation(b.toDisplay(e.x, e.y + b.getSize().y));
						} else {
							//Point pt = Display.getDefault().getCursorLocation();
							Point pt = buttonBar.getParent().toDisplay(buttonBar.getLocation());
							//System.out.println(pt);
							pt.y += buttonBar.getSize().y;
							pt.x += buttonBar.getSize().x * 3 / 4;
							tip.setLocation(pt);
						}
						tip.setVisible(true);
						
						lastTooltip = tip;
					}
				});
			}
			
		};

		EmulatorSettings.INSTANCE.register(JavaSoundHandler.settingPlaySound);
	}
	
	public void setSwtVideoRenderer(final ISwtVideoRenderer renderer) {
		setVideoRenderer(renderer);
		
		this.videoControl = renderer.createControl(topComposite, SWT.BORDER);
		
		final GridData rendererLayoutData = GridDataFactory.swtDefaults()
			.indent(0, 0)
			.align(SWT.CENTER, SWT.CENTER)
			.grab(true, true)
			.create();
		videoControl.setLayoutData(rendererLayoutData);
		
		renderer.addMouseEventListener(new MouseAdapter() {
			
			public void mouseDown(final MouseEvent e) {
				//System.out.println("Mouse detected " + e);
				if (!SWT.getPlatform().equals("win32") && e.button == 3) {
					showAppMenu(e);
				}
			}
			
			public void mouseUp(MouseEvent e) {
				if (e.button == 1) {
					handleClickOutsideToolWindow(videoControl.toDisplay(e.x, e.y));
					focusRestorer.restoreFocus();
				}
				if (SWT.getPlatform().equals("win32") && e.button == 3) {
					showAppMenu(e);
				}
			}

		});
		
		focusRestorer = new IFocusRestorer() {
			public void restoreFocus() {
				videoRenderer.setFocus();
			}
		};
		
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
		
		
		EmulatorSettings.INSTANCE.register(JavaSoundHandler.settingPlaySound);

		String boundsPref = EmulatorSettings.INSTANCE.getSettings().get(EMULATOR_WINDOW_BOUNDS);
		Rectangle rect = PrefUtils.readBoundsString(boundsPref);
		if (rect != null) {
			adjustRectVisibility(shell, rect);
			shell.setBounds(rect);
		}
		
		shell.open();
		
		shell.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				focusRestorer.restoreFocus();
			}
		});
		renderer.setFocus();
	}
	
	public static void adjustRectVisibility(Shell shell, Rectangle rect) {
		Rectangle screen = shell.getDisplay().getClientArea();
		
		// in GTK, for some reason, all bounds lose their positions
		if (rect.x == 0 && rect.y == 0) {
			rect.x = screen.width / 2 - rect.width / 2;
			rect.y = screen.height / 2 - rect.height / 2;
		}
		if (rect.x > screen.x + screen.width)
			rect.x = screen.x + screen.width - rect.width; 
		if (rect.y > screen.y + screen.height)
			rect.y = screen.y + screen.height - rect.height; 
		if (rect.x < screen.x)
			rect.x = 0;
		if (rect.y < screen.y)
			rect.y = 0;
	}

	public void setMouseJoystickHandler(MouseJoystickHandler handler) {
		//mouseJoystickHandler = new MouseJoystickHandler(videoControl, key);
		this.mouseJoystickHandler = handler; 
		((ISwtVideoRenderer)videoRenderer).addMouseEventListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				mouseJoystickHandler.setEnabled(!mouseJoystickHandler.isEnabled());
				if (eventNotifier != null)
					if (mouseJoystickHandler.isEnabled())
						eventNotifier.notifyEvent(null, Level.INFO, "Using mouse as joystick");
					else
						eventNotifier.notifyEvent(null, Level.INFO, "Releasing mouse as joystick");
			}
		});
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
		mainIcons = new TreeMap<Integer, Image>();
		for (int size : new int[] { 16, 32, 64, 128 }) {
			File iconsFile = Emulator.getDataFile("icons/icons_" + size + ".png");
			mainIcons.put(size, new Image(getShell().getDisplay(), iconsFile.getAbsolutePath()));
		}
		
		buttonBar = new ButtonBar(parent, SWT.HORIZONTAL, focusRestorer, true);
		
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BOTTOM).applyTo(buttonBar);

		//imageProvider = new MultiImageSizeProvider(mainIcons);
		SVGLoader svgIconLoader = new SVGLoader(Emulator.getDataFile("icons/icons.svg"));
		imageProvider = new SVGImageProvider(mainIcons, buttonBar, svgIconLoader);
		
		buttonBar.addControlListener(new ControlAdapter() {
			@Override
			public void controlMoved(ControlEvent e) {
				recenterToolShells();
			}
			@Override
			public void controlResized(ControlEvent e) {
				recenterToolShells();
			}
		});
		
		buttonBar.getDisplay().addFilter(SWT.MouseUp, new Listener() {
			
			public void handleEvent(Event event) {
				if (!(event.widget instanceof Control))
					return;
				if (event.button == 1) {
					Point pt = ((Control)event.widget).toDisplay(event.x, event.y);
					handleClickOutsideToolWindow(pt);
				}
			}
		});
		

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
						toggleToolShell(DEBUGGER_TOOL_ID, "DebuggerWindowBounds", false, false, new IToolShellFactory() {
							public Control createContents(Shell shell) {
								return new DebuggerWindow(shell, SWT.NONE, machine, toolUiTimer);
							}
						});
					}
			}
		);
		
		if (machine.getModuleManager() != null) {
			createButton(buttonBar,
				16, "Switch module", 
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						toggleToolShell(MODULE_SELECTOR_TOOL_ID, "ModuleWindowBounds", true, true, new IToolShellFactory() {
							public Control createContents(Shell shell) {
								return new ModuleSelector(shell, machine);
							}
						});
					}
				}
			);
		}
		
		if (machine.getDsrManager() != null) {
			createButton(buttonBar,
				5, "Setup disks", 
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						toggleToolShell(DISK_SELECTOR_TOOL_ID, "DiskWindowBounds", true, true, new IToolShellFactory() {
							public Control createContents(Shell shell) {
								return new DiskSelector(shell, machine.getDsrManager());
							}
						});
					}
				}
			);
		}
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

		createStateButton(buttonBar, BaseEmulatorWindow.settingMonitorDrawing,  
				9, 0, 
				"Apply monitor effect to video");

		createButton(buttonBar, 10,
				"Take screenshot", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						File file = screenshot();
						if (file != null) {
							eventNotifier.notifyEvent(e, Level.INFO, "Recorded screenshot to " + file);
						}
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
				MenuItem vitem = new MenuItem(menu, SWT.CASCADE);
				vitem.setText("Volume");
				
				final Menu volumeMenu = new Menu(vitem);

				int curVol = JavaSoundHandler.settingSoundVolume.getInt();
				int[] vols = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
				for (final int vol : vols) {
					MenuItem item = new MenuItem(volumeMenu, SWT.RADIO);
					item.setText("" + vol);
					if (vol == curVol)
						item.setSelection(true);
					item.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							JavaSoundHandler.settingSoundVolume.setInt(vol);
						}

					});
				}
				vitem.setMenu(volumeMenu);
				showMenu(menu, button, e.x, e.y);
			}
		});
	}

	/**
	 * @param debuggerToolId
	 * @param iToolShellFactory
	 */
	protected void toggleToolShell(String toolId, String boundsPref, 
			boolean keepCentered, boolean dismissOnClickOutside,
			IToolShellFactory toolShellFactory) {
		if (!restoreToolShell(toolId)) {
			Shell shell = new Shell(getShell(), SWT.RESIZE | SWT.TOOL);
			final ToolShell toolShell = new ToolShell(shell, focusRestorer, boundsPref, 
							keepCentered ? buttonBar : null,
							dismissOnClickOutside);
			Control tool = toolShellFactory.createContents(shell);
			toolShell.init(tool);
			addToolShell(toolId, toolShell);
		}
		
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


	protected void recenterToolShells() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				for (ToolShell shell : toolShells.values()) {
					if (shell.isKeepCentered()) {
						shell.recenterTo(null);
						shell.centerShell();
					}
				}	
			}
		});
		
	}
	protected void hideShell(final Shell shell) {
		Thread hider = new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						shell.setVisible(false);
					}
				});
			}
		};
		hider.start();
	}
	protected boolean restoreToolShell(String toolId) {
		ToolShell tool = toolShells.get(toolId);
		if (tool != null) {
			tool.restore();
			return true;
		} 
		return false;
	}
	protected boolean closeToolShell(String toolId) {
		ToolShell old = toolShells.get(toolId);
		if (old != null) {
			old.dispose();
			focusRestorer.restoreFocus();
			return true;
		} 
		return false;
	}
	
	protected void addToolShell(final String toolId, final ToolShell toolShell) {
		ToolShell old = toolShells.get(toolId);
		if (old != null)
			old.dispose();
		toolShells.put(toolId, toolShell);
		toolShell.getShell().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				toolShells.remove(toolId);
			}
		});
	}

	/**
	 * Take down any transient tool windows when clicking outside them
	 * @param pt display click location
	 */
	protected void handleClickOutsideToolWindow(final Point pt) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				ToolShell[] toolShellArr = (ToolShell[]) toolShells.values().toArray(new ToolShell[toolShells.values().size()]);
				for (ToolShell toolShell : toolShellArr) {
					Shell shell = toolShell.getShell();
					if (toolShell.isDismissOnClickOutside() && !shell.isDisposed() && shell.isVisible()
							&& System.currentTimeMillis() > toolShell.getClickOutsideCheckTime()) {
						Rectangle bounds = shell.getBounds();
						System.out.println(pt + "/"+ bounds);
						if (pt.x < bounds.x - 16 || pt.y < bounds.y - 16 
								|| pt.x > bounds.x + bounds.width + 16 || pt.y > bounds.y + bounds.height + 16) {
							shell.dispose();
						}
					}
				}
					
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
		
		if (mouseJoystickHandler != null) {
			MenuItem mouseJoystickItem = new MenuItem(appMenu, SWT.CHECK);
			mouseJoystickItem.setText("Use mouse as joystick");
			mouseJoystickItem.setAccelerator(SWT.CTRL | SWT.ALT | ' ');
			mouseJoystickItem.setSelection(mouseJoystickHandler.isEnabled());
			mouseJoystickItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					mouseJoystickHandler.setEnabled(!mouseJoystickHandler.isEnabled());
				}
			});
		}
		
		return appMenu;
	}
	
	@Override
	public void dispose() {
		
		cpuMetricsCanvas.dispose();
		
		toolUiTimer.cancel();
		
		ToolShell[] shellArray = (ToolShell[]) toolShells.values().toArray(new ToolShell[toolShells.values().size()]);
		for (ToolShell shell : shellArray) {
			shell.dispose();
		}
		toolShells.clear();
		
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
		final int cycles = (int) (machine.getCpu().getBaseCyclesPerSec() * factor);
		item.setText(label + " (" + cycles + ")");
		if (isRealTime && cycles == curCycles) {
			item.setSelection(true);
		}
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Cpu.settingRealTime.setBoolean(true);
				Cpu.settingCyclesPerSecond.setInt(cycles);
			}
		});
	}
	
	
	private BasicButton createButton(ButtonBar buttonBar, int iconIndex, String tooltip, SelectionListener selectionListener) {
		Rectangle bounds = mainIconIndexToBounds(iconIndex);
		BasicButton button = new BasicButton(buttonBar, SWT.PUSH, imageProvider, bounds, tooltip);
		button.addSelectionListener(selectionListener);
		return button;
	}
	
	private Rectangle mainIconIndexToBounds(int iconIndex) {
		Rectangle bounds = mainIcons.values().iterator().next().getBounds();
		int unit = bounds.width;
		return new Rectangle(0, unit * iconIndex, unit, unit); 
	}

	private BasicButton createStateButton(ButtonBar buttonBar, final SettingProperty setting, 
			final boolean inverted, final Point noClickCorner, 
			int iconIndex, final int overlayIndex, String tooltip) {
		final BasicButton button = new BasicButton(buttonBar, SWT.PUSH, 
				imageProvider, 
				mainIconIndexToBounds(iconIndex), 
				tooltip);
		setting.addListener(new IPropertyListener() {

			public void propertyChanged(final IProperty setting) {
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
	private BasicButton createStateButton(ButtonBar buttonBar, final SettingProperty setting, 
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

	/**
	 * @return
	 */
	public IEventNotifier getEventNotifier() {
		return eventNotifier;
	}


}
