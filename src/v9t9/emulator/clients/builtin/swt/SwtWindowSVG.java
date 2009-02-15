/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.TextTransfer;
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
public class SwtWindowSVG extends BaseEmulatorWindow {
	
	protected Shell shell;
	protected Control videoControl;
	private ButtonBar buttonBar;
	public SwtWindowSVG(Display display, final ISwtVideoRenderer renderer, Machine machine) {
		super(machine);
		setVideoRenderer(renderer);
		
		shell = new Shell(display);
		shell.setText("V9t9");
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = layout.marginWidth = 0;
		shell.setLayout(layout);
		
		Composite mainComposite = shell;
		
		final Composite screenComposite = new Composite(mainComposite, SWT.BORDER);
		
		GridLayout screenLayout = new GridLayout();
		screenLayout.marginHeight = screenLayout.marginWidth = 2;
		screenComposite.setLayout(screenLayout);
		// need to FILL so we can detect when our space has shrunk or grown;
		// need to use extra space so the window will let the screen grow or shrink
		GridData screenLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		screenComposite.setLayoutData(screenLayoutData);
		
		this.videoControl = renderer.createControl(screenComposite);
		
		File iconsFile = new File("icons/icons.svg");
		SVGLoader icons = new SVGLoader(iconsFile);
		
		buttonBar = new ButtonBar(mainComposite, SWT.HORIZONTAL, videoRenderer);
		
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
		Rectangle displaySize = shell.getDisplay().getBounds();
		Point shellSize = shell.getSize();
		shell.setBounds(displaySize.width - shellSize.x, displaySize.height - shellSize.y, shellSize.x, shellSize.y);
		
		shell.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				renderer.setFocus();
			}
		});
		
		renderer.setFocus();

	}

	private BasicButton createButton(ButtonBar buttonBar, final SVGLoader icon, final Rectangle bounds, String tooltip, SelectionListener selectionListener) {
		BasicButton button = new BasicButton(buttonBar, SWT.PUSH, icon, bounds, tooltip);
		button.addSelectionListener(selectionListener);
		return button;
	}
	
	private BasicButton createStateButton(ButtonBar buttonBar, final Setting setting, final SVGLoader icon,
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
