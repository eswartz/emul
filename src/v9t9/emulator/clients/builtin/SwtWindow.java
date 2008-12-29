/**
 * 
 */
package v9t9.emulator.clients.builtin;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.video.SwtVideoRenderer;
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
	private Composite controlsComposite;
	public SwtWindow(Display display, SwtVideoRenderer renderer, Machine machine) {
		super(machine);
		setVideoRenderer(renderer);
		
		shell = new Shell(display);
		shell.setText("V9t9");
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		shell.setLayout(layout);
		
		Composite mainComposite = shell;
		
		final Composite screenComposite = new Composite(mainComposite, SWT.BORDER);
		
		screenComposite.setLayout(new GridLayout());
		screenComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		/*
		GridData screenLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		screenLayoutData.minimumHeight = 256;
		screenLayoutData.minimumWidth = 192;
		screenLayoutData.widthHint = 256 * 3;
		screenLayoutData.heightHint = 192 * 3;
		screenComposite.setLayoutData(screenLayoutData);
		*/
		this.videoControl = renderer.createControl(screenComposite);
		//this.videoControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		
		File iconsFile = new File("icons/icons.png");
		Image icons = new Image(getShell().getDisplay(), iconsFile.getAbsolutePath());
		
		controlsComposite = new Composite(mainComposite, SWT.NO_RADIO_GROUP | SWT.NO_FOCUS);
		layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		controlsComposite.setLayout(layout);
		controlsComposite.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, true));
		
		/*BasicButton abortButton =*/ createButton(icons, 
				new Rectangle(0, 64, 64, 64), "Send a NMI interrupt",
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						sendNMI();
						restoreFocus();
					}
				});

		createButton(icons, 
				new Rectangle(0, 256, 64, 64), "Reset the computer",
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						sendReset();
						restoreFocus();
					}
				});

		/*BasicButton logButton =*/ createStateButton(Executor.settingDumpFullInstructions,
				icons, new Rectangle(0, 128, 64, 64),
				new Rectangle(0, 0, 64, 64),
				"Toggle CPU logging");
		
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
		
		createButton(icons, new Rectangle(0, 192, 64, 64),
				"Paste into keyboard",
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						pasteClipboardToKeyboard();
					}
			});
		
		createButton(icons, new Rectangle(0, 384, 64, 64),
				"Save machine state",
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						saveMachineState();
					}

			});
		
		createButton(icons, new Rectangle(0, 448, 64, 64),
				"Load machine state",
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						loadMachineState();
					}
			});

		createStateButton(Machine.settingPauseMachine, icons, new Rectangle(0, 512, 64, 64),
				new Rectangle(0, 0, 64, 64),
				"Pause machine");

		screenComposite.setFocus();
		shell.open();
		shell.setBounds(800, 800, shell.getSize().x, shell.getSize().y);
		shell.pack();
	}

	class BasicButton extends Composite {

		private final Button button;
		private final Rectangle bounds;
		private Image icon;
		private Rectangle overlayBounds;

		public BasicButton(Composite parent, int style, Image icon_, Rectangle bounds_, String tooltip) {
			super(parent, SWT.NO_FOCUS | SWT.NO_RADIO_GROUP);
			this.icon = icon_;
			this.bounds = bounds_;
			
			addKeyListener(new KeyListener() {
				
				public void keyPressed(KeyEvent e) {
					e.doit = false;
				}
	
				public void keyReleased(KeyEvent e) {
					e.doit = false;
				}
				
			});
			
			GridData data = new GridData(bounds.width, bounds.height);
			setLayoutData(data);
			setLayout(new FillLayout());
			
			button = new Button(this, style | SWT.NO_FOCUS | SWT.NO_BACKGROUND);
			
			//button.setImage(icon);
			button.setToolTipText(tooltip);
			
			
			button.addPaintListener(new PaintListener() {
	
				public void paintControl(PaintEvent e) {
					e.gc.drawImage(icon, bounds.x, bounds.y, bounds.width, bounds.height, 
							0, 0, bounds.width, bounds.height);
					if (overlayBounds != null)
						e.gc.drawImage(icon, overlayBounds.x, overlayBounds.y, overlayBounds.width, overlayBounds.height, 
								0, 0, overlayBounds.width, overlayBounds.height);
				}
				
			});
			button.addKeyListener(new KeyListener() {
	
				public void keyPressed(KeyEvent e) {
					e.doit = false;
				}
	
				public void keyReleased(KeyEvent e) {
					e.doit = false;
				}
				
			});
			button.addTraverseListener(new TraverseListener() {
	
				public void keyTraversed(TraverseEvent e) {
					e.doit = false;
				}
				
			});
			
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					e.doit = false;
				}
				@Override
				public void widgetSelected(SelectionEvent e) {
					restoreFocus();
				}
			});
			
			button.addTraverseListener(new TraverseListener() {

				public void keyTraversed(TraverseEvent e) {
					e.doit = false;
				}
				
			});
		}
		
		public Button getButton() {
			return button;
		}

		public void setOverlayBounds(Rectangle overlayBounds) {
			this.overlayBounds = overlayBounds;
		}
	}

	private BasicButton createButton(final Image icon, final Rectangle bounds, String tooltip, SelectionListener selectionListener) {
		BasicButton button = new BasicButton(controlsComposite, SWT.PUSH, icon, bounds, tooltip);
		button.getButton().addSelectionListener(selectionListener);
		return button;
	}
	
	private BasicButton createStateButton(final Setting setting, final Image icon, final Rectangle bounds,
			final Rectangle checkBounds,
			String tooltip) {
		final BasicButton button = new BasicButton(controlsComposite, SWT.TOGGLE, icon, bounds, tooltip);
		setting.addListener(new ISettingListener() {

			public void changed(final Setting setting, final Object oldValue) {
				Display.getDefault().asyncExec(new Runnable() {

					public void run() {
						if (setting.getBoolean()) {
							button.setOverlayBounds(checkBounds);
						} else {
							button.setOverlayBounds(null);
						}
						if (setting.getBoolean() != button.getButton().getSelection()) {
							button.getButton().setSelection(setting.getBoolean());
						}
						button.redraw();
					}
					
				});
			}
			
		});
		
		button.getButton().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setting.setBoolean(!setting.getBoolean());
			}
		});
		
		return button;
	}
	protected void restoreFocus() {
		videoControl.setFocus();
	}

	public Shell getShell() {
		return shell;
	}

	protected void pasteClipboardToKeyboard() {
		Clipboard clip = new Clipboard(Display.getDefault());
		String contents = (String) clip.getContents(TextTransfer.getInstance());
		if (contents == null) {
			contents = (String) clip.getContents(RTFTransfer.getInstance());
		}
		if (contents != null) {
			machine.getClient().getKeyboardHandler().pasteText(contents);
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
		dialog.setFilterPath(directory);
		dialog.setFileName(fileName);
		String filename = dialog.open();
		return filename;
	}
}
