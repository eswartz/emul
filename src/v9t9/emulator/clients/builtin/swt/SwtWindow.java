/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.BaseEmulatorWindow;
import v9t9.emulator.hardware.V9t9;
import v9t9.emulator.runtime.Executor;
import v9t9.engine.settings.ISettingListener;
import v9t9.engine.settings.Setting;

/**
 * Provide the emulator in an SWT window
 * @author ejs
 *
 */
public class SwtWindow extends BaseEmulatorWindow {
	
	protected Shell shell;
	protected Control videoControl;
	private ButtonBar buttonBar;
	public SwtWindow(Display display, final ISwtVideoRenderer renderer, Machine machine) {
		super(machine);
		setVideoRenderer(renderer);
		
		shell = new Shell(display);
		shell.setText("V9t9");
		
		File iconFile = new File("icons/v9t9.png");
		Image icon = new Image(shell.getDisplay(), iconFile.getAbsolutePath());
		
		shell.setImage(icon);

		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = layout.marginWidth = 0;
		shell.setLayout(layout);
		
		shell.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				shell.getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (!shell.isDisposed())
							shell.pack();
					}
				});
			}
		});
		
		Composite mainComposite = shell;
		
		/*
		final Composite screenComposite = new Composite(mainComposite, SWT.BORDER);
		
		GridLayout screenLayout = new GridLayout();
		screenLayout.marginHeight = screenLayout.marginWidth = 2;
		screenComposite.setLayout(screenLayout);
		
		// need to FILL so we can detect when our space has shrunk or grown;
		// need to use extra space so the window will let the screen grow or shrink
		final GridData screenLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		screenComposite.setLayoutData(screenLayoutData);
		
		this.videoControl = renderer.createControl(screenComposite);
		
		final GridData rendererLayoutData = GridDataFactory.fillDefaults().indent(0, 0)
			.align(SWT.FILL, SWT.FILL).grab(true, true).create();
		videoControl.setLayoutData(rendererLayoutData);
*/
		/*
		screenComposite.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				GridData screenData = (GridData)videoControl.getLayoutData();
				screenLayoutData.widthHint = screenData.widthHint;
				screenLayoutData.heightHint = screenData.heightHint;
				System.out.println("laying out screenComposite to " + ((Control) e.widget).getSize());
				System.out.println("suggesting exact size of " + screenData.widthHint + " x " + screenData.heightHint);

			}
		});
		 */
		
		this.videoControl = renderer.createControl(mainComposite);
		
		final GridData rendererLayoutData = GridDataFactory.swtDefaults().indent(0, 0)
			.align(SWT.FILL, SWT.FILL)
			.grab(true, true)
			.create();
		videoControl.setLayoutData(rendererLayoutData);
		
		videoControl.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				/*
				Point size = videoControl.getSize();
				System.out.println("videoControl size is " + size);
				int width = videoRenderer.getCanvas().getWidth();
				int height = videoRenderer.getCanvas().getHeight();
				int zoom = 1;
				while (width * (zoom + 1) <= size.x && height * (zoom + 1) <= size.y) {
					zoom++;
				}
				width *= zoom;
				height *= zoom;
				if (width != size.x || height != size.y) {
					System.out.println("Hinting size: " + width + "/"+height);
					rendererLayoutData.widthHint = width;
					rendererLayoutData.heightHint = height;
					final Point newSize = new Point(width, height);
					getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							
							//videoControl.setSize(newSize);
							getShell().pack();
							
						}
					});
				}
				*/
				getShell().getDisplay().asyncExec(new Runnable() {
					public void run() {
						
						//videoControl.setSize(newSize);
			//			getShell().pack();
						
					}
				});
				
			}
		});
		
		File iconsFile = new File("icons/icons.png");
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

		shell.open();
		shell.pack();
		//Rectangle displaySize = shell.getDisplay().getBounds();
		//Point shellSize = shell.getSize();
		//shell.setBounds(displaySize.width - shellSize.x, displaySize.height - shellSize.y, shellSize.x, shellSize.y);
		
		shell.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				renderer.setFocus();
			}
		});
		
		renderer.setFocus();

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
