/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
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
import org.eclipse.swt.widgets.Button;
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
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.BaseEmulatorWindow;
import v9t9.emulator.hardware.V9t9;
import v9t9.emulator.runtime.Executor;
import v9t9.engine.settings.ISettingListener;
import v9t9.engine.settings.Setting;
import v9t9.utils.Utils;

/**
 * Provide the emulator in an SWT window
 * @author ejs
 *
 */
public class SwtWindow extends BaseEmulatorWindow {
	
	protected Shell shell;
	protected Control videoControl;
	private ButtonBar buttonBar;
	private Button controlsExpander;
	private MemoryViewer cpuMemory;
	private Composite controlsComposite;
	
	public SwtWindow(Display display, final ISwtVideoRenderer renderer, final Machine machine) {
		super(machine);
		setVideoRenderer(renderer);
		
		shell = new Shell(display, SWT.SHELL_TRIM & ~SWT.RESIZE);
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
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = layout.marginWidth = 2;
		mainComposite.setLayout(layout);
		
		Composite topComposite = new Composite(mainComposite, SWT.NONE);
		layout = new GridLayout(2, false);
		topComposite.setLayout(layout);
		topComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		
		this.videoControl = renderer.createControl(topComposite, SWT.BORDER);
		
		final GridData rendererLayoutData = GridDataFactory.swtDefaults()
			.indent(0, 0)
			.align(SWT.CENTER, SWT.CENTER)
			.grab(true, true)
			.create();
		videoControl.setLayoutData(rendererLayoutData);
		
		createExpandableControlsComposite(topComposite);
		
		((ISwtVideoRenderer) videoRenderer).addMouseEventListener(new MouseAdapter() {
			
			@Override
			public void mouseDown(final MouseEvent e) {
				
				//System.out.println("Mouse detected " + e);
				if (e.button == 1) {
					videoControl.forceFocus();
					return;
				}
				if (e.button == 3) {
					
					// we need this horrible hack or else the
					// menu will disappear immediately because SWT and AWT
					// don't agree on events
					final Shell menuShell = new Shell(getShell(), SWT.ON_TOP | SWT.TOOL);
					menuShell.setSize(4, 4);
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
			}
			
		});
		
		createButtons(mainComposite);
		
		shell.open();
		
		shell.getDisplay().asyncExec(new Runnable() {
			public void run() {
				int zoom = Utils.readSavedInt(getApplicationSettings(), "ZoomLevel");
				if (zoom > 0) {
					videoRenderer.setZoom(zoom);
				}
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

	private void createButtons(Composite mainComposite) {
		File iconsFile = V9t9.getDataFile("icons/icons.png");
		Image icons = new Image(getShell().getDisplay(), iconsFile.getAbsolutePath());
		
		buttonBar = new ButtonBar(mainComposite, SWT.HORIZONTAL);
		GridLayout mainLayout = new GridLayout(1, false);
		mainLayout.marginHeight = mainLayout.marginWidth = 0;
		buttonBar.setLayout(mainLayout);
		

		createButton(buttonBar, 
				icons, new Rectangle(0, 64, 64, 64),
				"Send a NMI interrupt", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						sendNMI();
					}
				});

		createButton(buttonBar, 
				icons, new Rectangle(0, 256, 64, 64),
				"Reset the computer", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						sendReset();
					}
				});

		createStateButton(buttonBar,
				Executor.settingDumpFullInstructions, icons,
				new Rectangle(0, 128, 64, 64),
				new Rectangle(0, 0, 64, 64), "Toggle CPU logging");

		/*BasicButton basicButton =*/ /*createButton(
				icons, new Rectangle(0, 128, 64, 64),
				"Branch to Condensed BASIC",
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						SwtWindow.this.machine.getExecutor().controlCpu(new Executor.ICpuController() {

							public void act(Cpu cpu) {
								cpu.setPC((short)0xa000);								
								cpu.setWP((short)0x83e0);								
							}
							
						}) ;
					}
				});*/
		
		createButton(buttonBar, icons,
				new Rectangle(0, 192, 64, 64),
				"Paste into keyboard", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						pasteClipboardToKeyboard();
					}
			});
		
		createButton(buttonBar, icons,
				new Rectangle(0, 384, 64, 64),
				"Save machine state", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						saveMachineState();
					}

			});
		
		createButton(buttonBar, icons,
				new Rectangle(0, 448, 64, 64),
				"Load machine state", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						loadMachineState();
					}
			});

		createStateButton(buttonBar, Machine.settingPauseMachine, icons,
				new Rectangle(0, 512, 64, 64),
				new Rectangle(0, 0, 64, 64), "Pause machine");

		createStateButton(buttonBar, V9t9.settingMonitorDrawing, icons, new Rectangle(0, 576, 64, 64), 
				new Rectangle(0, 0, 64, 64), "Apply monitor effect to video");

		createButton(buttonBar, icons,
				new Rectangle(0, 640, 64, 64),
				"Take screenshot", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						screenshot();
					}
			});

		createButton(buttonBar, 
				icons, new Rectangle(0, 704, 64, 64),
				"Zoom the screen", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Control button = (Control) e.widget;
						Point size = button.getSize();
						showZoomMenu(button, size.x / 2, size.y / 2);
					}
				});
	}

	private void createExpandableControlsComposite(Composite parent) {
		final Composite controlsExpanderComposite = new Composite(parent, SWT.NONE);
		controlsExpanderComposite.setLayout(new GridLayout(2, false));
		GridDataFactory.swtDefaults().applyTo(controlsExpanderComposite);
		
		controlsExpander = new Button(controlsExpanderComposite, SWT.ARROW | SWT.RIGHT | SWT.NO_FOCUS);
		GridDataFactory.swtDefaults().applyTo(controlsExpander);
		
		controlsComposite = new Composite(controlsExpanderComposite, SWT.NONE);
		GridDataFactory.swtDefaults().hint(0, 0).applyTo(controlsComposite);
		
		controlsComposite.setVisible(false);
		
		controlsComposite.setLayout(new GridLayout(1, false));
		
		controlsExpander.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (controlsComposite.isVisible()) {
					controlsComposite.setVisible(false);
					GridDataFactory.swtDefaults().hint(0, 0).applyTo(controlsComposite);
				} else {
					controlsComposite.setVisible(true);
					Rectangle bounds = getShell().getClientArea();
					GridDataFactory.swtDefaults().hint(-1, bounds.height).applyTo(controlsComposite);
				}
				getShell().pack();
				videoControl.forceFocus();
			}
		});
		
		createControls();
	}

	private void createControls() {
		cpuMemory = new MemoryViewer(controlsComposite, SWT.NONE, machine.getMemory());
		
		GridDataFactory.fillDefaults().grab(false, true).applyTo(cpuMemory);
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
		fileMenuHeader.setMenu(fileMenu);
		
		MenuItem exit = new MenuItem(fileMenu, SWT.NONE);
		exit.setText("E&xit");
		exit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.exit(0);
			}
		});
		
		Menu viewMenu = appMenu;
		if (!isPopup) {
			MenuItem viewMenuHeader = new MenuItem(appMenu, SWT.CASCADE);
			viewMenuHeader.setText("&View");
			
			viewMenu = new Menu(control, SWT.DROP_DOWN);
			viewMenuHeader.setMenu(viewMenu);
		}
		
		MenuItem zoom = new MenuItem(viewMenu, SWT.CASCADE);
		zoom.setText("&Zoom");
		
		Menu zoomMenu = new Menu(zoom);
		populateZoomMenu(zoomMenu);
		zoom.setMenu(zoomMenu);
		
		return appMenu;
	}
	
	@Override
	public void dispose() {
		cpuMemory.dispose();
		super.dispose();
	}

	protected void showZoomMenu(final Control parent, final int x, final int y) {
			final Menu menu = createZoomMenu(parent);
			runMenu(parent, x, y, menu);
			menu.dispose();		
	}

	private void runMenu(final Control parent, final int x, final int y,
			final Menu menu) {
		if (parent != null) {
			Point loc = parent.toDisplay(x, y); 
			menu.setLocation(loc);
		}
		menu.setVisible(true);
		
		//System.out.println("Running menu");
		final Shell menuShell = getShell();
		Display display = menuShell.getDisplay();
		while (!menu.isDisposed() && menu.isVisible()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
	}

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
					getApplicationSettings().put("ZoomLevel", zoom);
				}

			});
		}
		return menu;
	}

	protected void setScreenZoom(int zoom) {
		System.out.println("Set zoom to " + zoom);
		videoRenderer.setZoom(zoom);
	}

	private BasicButton createButton(ButtonBar buttonBar, final Image icon, final Rectangle bounds, String tooltip, SelectionListener selectionListener) {
		BasicButton button = new BasicButton(buttonBar, SWT.PUSH, icon, bounds, tooltip);
		button.addSelectionListener(selectionListener);
		return button;
	}
	
	private BasicButton createStateButton(ButtonBar buttonBar, final Setting setting, final Image icon,
			final Rectangle bounds,
			final Rectangle checkBounds, String tooltip) {
		final BasicButton button = new BasicButton(buttonBar, SWT.TOGGLE, icon, bounds, tooltip);
		setting.addListener(new ISettingListener() {

			public void changed(final Setting setting, final Object oldValue) {
				Display.getDefault().asyncExec(new Runnable() {

					public void run() {
						if (button.isDisposed())
							return;
						if (setting.getBoolean()) {
							button.setOverlayBounds(checkBounds);
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
				setting.setBoolean(!setting.getBoolean());
			}
		});
		
		if (setting.getBoolean()) {
			button.setOverlayBounds(checkBounds);
			button.setSelection(setting.getBoolean());
		}
		return button;
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
			String fileName, boolean isSave) {
		FileDialog dialog = new FileDialog(getShell(), isSave ? SWT.SAVE : SWT.OPEN);
		dialog.setText(title);
		dialog.setFilterPath(directory);
		dialog.setFileName(fileName);
		String filename = dialog.open();
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
