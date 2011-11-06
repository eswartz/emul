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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import v9t9.emulator.clients.builtin.BaseEmulatorWindow;
import v9t9.emulator.clients.builtin.sound.JavaSoundHandler;
import v9t9.emulator.clients.builtin.swt.ToolShell.Behavior;
import v9t9.emulator.clients.builtin.swt.ToolShell.Centering;
import v9t9.emulator.clients.builtin.swt.debugger.DebuggerWindow;
import v9t9.emulator.common.IEventNotifier.Level;
import v9t9.emulator.common.Machine;
import v9t9.emulator.runtime.cpu.Cpu9900;
import v9t9.emulator.runtime.cpu.Executor;

/**
 * @author ejs
 *
 */
public class EmulatorButtonBar extends EmulatorBar  {
	/**
	 * @param parent 
	 * @param isHorizontal 
	 * @param parent
	 * @param style
	 * @param focusRestorer
	 * @param smoothResize
	 */
	public EmulatorButtonBar(SwtWindow window, ImageProvider imageProvider, Composite parent, 
			final Machine machine, 
			int[] colors, float midPoint, boolean isHorizontal) {
		super(window, imageProvider, parent, machine, colors, midPoint, isHorizontal);
		
		if (isHorizontal) {
			GridData gd = ((GridData) buttonBar.getLayoutData());
			gd.verticalSpan = 2;
		}
		
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

		createToggleStateButton(Executor.settingDumpFullInstructions,
				2, 
				0, "Toggle CPU logging");

		createToggleStateButton(Machine.settingPauseMachine, 8,
				0, "Pause machine");


		createButton(7,
				"Create debugger window", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						swtWindow.toggleToolShell(DEBUGGER_TOOL_ID, new IToolShellFactory() {
							ToolShell.Behavior behavior = new ToolShell.Behavior();
							{
								behavior.boundsPref = "DebuggerWindowBounds";
								behavior.dismissOnClickOutside = false;
								behavior.centerOverControl = buttonBar;
							}
							public Control createContents(Shell shell) {
								return new DebuggerWindow(shell, SWT.NONE, machine, swtWindow.getToolUiTimer());
							}
							public Behavior getBehavior() {
								return behavior;
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
		
		createToggleStateButton(BaseEmulatorWindow.settingMonitorDrawing, 9,  
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
	

		final ImageImportSupport imageSupport = new ImageImportSupport(swtWindow.getEventNotifier(), swtWindow.getVideoRenderer());
		BasicButton imageImportButton = createButton(17, "Import image (drag onto icon!)",
			new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					swtWindow.toggleToolShell(IMAGE_IMPORTER_ID, new IToolShellFactory() {
						Behavior behavior = new Behavior();
						{
							behavior.boundsPref = "ImageImporterBounds";
							behavior.centering = Centering.OUTSIDE;
							behavior.centerOverControl = swtWindow.getShell();
							behavior.dismissOnClickOutside = true;
						}
						public Control createContents(Shell shell) {
							ImageImportDialog dialog = imageSupport.createImageImportDialog(shell);
							imageSupport.addImageImportDnDControl(dialog);
							return dialog;
						}
						@Override
						public Behavior getBehavior() {
							return behavior;
						}
					});
				}
			}
		);
		imageSupport.setImageImportDnDControl(imageImportButton);
		
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
		createToggleStateButton(BaseEmulatorWindow.settingFullScreen, 
				11, 0, "Toggle fullscreen");

		
		final BasicButton soundButton = createStateButton(JavaSoundHandler.settingPlaySound, 
				true, 
				null, 13,
				14, true, "Sound options");
	/*	soundButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Control button = (Control) e.widget;
				Point size = button.getSize();
				showMenu(createSoundMenu(button), button, size.x / 2, size.y / 2);
			}
		});*/
		
		soundButton.setMenuOverlayBounds(imageProvider.imageIndexToBounds(15));
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

}
