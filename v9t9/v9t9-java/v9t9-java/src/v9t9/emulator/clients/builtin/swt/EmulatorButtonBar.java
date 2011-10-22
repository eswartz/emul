/**
 * Mar 11, 2011
 */
package v9t9.emulator.clients.builtin.swt;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.ejs.coffee.core.properties.IProperty;
import org.ejs.coffee.core.properties.IPropertyListener;
import org.ejs.coffee.core.properties.SettingProperty;

import v9t9.emulator.clients.builtin.BaseEmulatorWindow;
import v9t9.emulator.clients.builtin.sound.JavaSoundHandler;
import v9t9.emulator.clients.builtin.swt.debugger.DebuggerWindow;
import v9t9.emulator.common.IEventNotifier.Level;
import v9t9.emulator.common.Machine;
import v9t9.emulator.runtime.cpu.Cpu9900;
import v9t9.emulator.runtime.cpu.Executor;

/**
 * @author ejs
 *
 */
public class EmulatorButtonBar  {
	protected static final String MODULE_SELECTOR_TOOL_ID = "module.selector";
	protected static final String DISK_SELECTOR_TOOL_ID = "disk.selector";
	protected static final String DEBUGGER_TOOL_ID = "debugger";

	private final SwtWindow swtWindow;
	private ImageBar buttonBar;
	private final Machine machine;
	/**
	 * @param mainComposite 
	 * @param parent
	 * @param style
	 * @param focusRestorer
	 * @param smoothResize
	 */
	public EmulatorButtonBar(SwtWindow window, Composite mainComposite, Machine machine) {
		this.swtWindow = window;
		this.machine = machine;
		
		createButtons(mainComposite);
	}


	private void createButtons(Composite parent) {

		buttonBar = new ImageBar(parent, SWT.VERTICAL, swtWindow.getFocusRestorer(), true);
		
		// SLLLOOOOOOWWWW
		//SVGLoader svgIconLoader = new SVGLoader(Emulator.getDataFile("icons/icons.svg"));
		//imageProvider = new SVGImageProvider(mainIcons, buttonBar, svgIconLoader);
		
		buttonBar.addControlListener(new ControlAdapter() {
			@Override
			public void controlMoved(ControlEvent e) {
				swtWindow.recenterToolShells();
			}
			@Override
			public void controlResized(ControlEvent e) {
				swtWindow.recenterToolShells();
			}
		});
		
		buttonBar.getDisplay().addFilter(SWT.MouseUp, new Listener() {
			
			public void handleEvent(Event event) {
				if (!(event.widget instanceof Control))
					return;
				if (event.button == 1) {
					Point pt = ((Control)event.widget).toDisplay(event.x, event.y);
					swtWindow.handleClickOutsideToolWindow(pt);
				}
			}
		});
		

		createButton(1, "Send a non-maskable interrupt",
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						machine.getCpu().setPin(Cpu9900.PIN_LOAD);
					}
				});

		createButton(4, "Reset the computer",
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						machine.getCpu().setPin(Cpu9900.PIN_RESET);
					}
				});

		createStateButton(Executor.settingDumpFullInstructions,
				2, 
				0, "Toggle CPU logging");

		createButton(7,
				"Create debugger window", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						swtWindow.toggleToolShell(DEBUGGER_TOOL_ID, "DebuggerWindowBounds", false, false, new IToolShellFactory() {
							public Control createContents(Shell shell) {
								return new DebuggerWindow(shell, SWT.NONE, machine, swtWindow.getToolUiTimer());
							}
						});
					}
			}
		);
		
		if (machine.getModuleManager() != null) {
			createButton(16,
				"Switch module", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						swtWindow.toggleToolShell(MODULE_SELECTOR_TOOL_ID, "ModuleWindowBounds", true, true, new IToolShellFactory() {
							public Control createContents(Shell shell) {
								return new ModuleSelector(shell, machine);
							}
						});
					}
				}
			);
		}
		
		createButton(5,
			"Setup disks", new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					swtWindow.toggleToolShell(DISK_SELECTOR_TOOL_ID, "DiskWindowBounds", true, true, new IToolShellFactory() {
						public Control createContents(Shell shell) {
							return new DiskSelector(shell, machine);
						}
					});
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
		
		createButton(3, "Paste into keyboard",
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						swtWindow.pasteClipboardToKeyboard();
					}
			});
		
		createButton(6, "Load or save machine state",
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Control button = (Control) e.widget;
						Point size = button.getSize();
						swtWindow.showMenu(createFilePopupMenu(button), button, size.x / 2, size.y / 2);
					}

			});
		
		createStateButton(Machine.settingPauseMachine, 8,
				0, "Pause machine");

		createStateButton(BaseEmulatorWindow.settingMonitorDrawing, 9,  
				0, "Apply monitor effect to video");

		createButton(10, "Take screenshot",
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						File file = swtWindow.screenshot();
						if (file != null) {
							swtWindow.getEventNotifier().notifyEvent(e, Level.INFO, "Recorded screenshot to " + file);
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
		createButton(12, "Accelerate execution",
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Control button = (Control) e.widget;
						Point size = button.getSize();
						swtWindow.showMenu(createAccelMenu(button), button, size.x / 2, size.y / 2);
					}
				});
		
		final BasicButton soundButton = createStateButton(JavaSoundHandler.settingPlaySound, 
				true, 
				null, 13,
				14, "Sound options");
	/*	soundButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Control button = (Control) e.widget;
				Point size = button.getSize();
				showMenu(createSoundMenu(button), button, size.x / 2, size.y / 2);
			}
		});*/
		
		soundButton.setMenuOverlayBounds(swtWindow.getIconImageProvider().imageIndexToBounds(15));
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
				swtWindow.showMenu(menu, button, e.x, e.y);
			}
		});

		createStateButton(BaseEmulatorWindow.settingFullScreen, 
				11, 0, "Toggle fullscreen");

	}

	private BasicButton createButton(int iconIndex, String tooltip, SelectionListener selectionListener) {
		BasicButton button = new BasicButton(buttonBar, SWT.PUSH,
				swtWindow.getIconImageProvider(), iconIndex, tooltip);
		button.addSelectionListener(selectionListener);
		return button;
	}
	
	private BasicButton createStateButton(final SettingProperty setting, final boolean inverted, 
			final Point noClickCorner, int iconIndex, 
			final int overlayIndex, String tooltip) {
		final BasicButton button = new BasicButton(buttonBar, SWT.PUSH,
				swtWindow.getIconImageProvider(), iconIndex, tooltip);
		setting.addListener(new IPropertyListener() {

			public void propertyChanged(final IProperty setting) {
				Display.getDefault().asyncExec(new Runnable() {

					public void run() {
						if (button.isDisposed())
							return;
						if (setting.getBoolean() != inverted) {
							button.setOverlayBounds(swtWindow.getIconImageProvider().imageIndexToBounds(overlayIndex));
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
			}
		});
		
		if (setting.getBoolean() != inverted) {
			button.setOverlayBounds(swtWindow.getIconImageProvider().imageIndexToBounds(overlayIndex));
			button.setSelection(setting.getBoolean());
		}
		return button;
	}

	private BasicButton createStateButton(final SettingProperty setting, int iconIndex, 
			int overlayIndex, String tooltip) {
		return createStateButton(setting, false, null, iconIndex, overlayIndex, tooltip);
	}
	
	private Menu createAccelMenu(final Control parent) {
		final Menu menu = new Menu(parent);
		return swtWindow.populateAccelMenu(menu);
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
		return swtWindow.populateFileMenu(menu, false);
	}


	/**
	 * @return
	 */
	public Point getTooltipLocation() {
		Point pt = buttonBar.getParent().toDisplay(buttonBar.getLocation());
		//System.out.println(pt);
		pt.y += buttonBar.getSize().y;
		pt.x += buttonBar.getSize().x * 3 / 4;
		return pt;
	}


	/**
	 * @return
	 */
	public Control getButtonBar() {
		return buttonBar;
	}


	/**
	 * 
	 */
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

}
