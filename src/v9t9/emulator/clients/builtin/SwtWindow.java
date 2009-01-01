/**
 * 
 */
package v9t9.emulator.clients.builtin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.video.ISwtVideoRenderer;
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
	public SwtWindow(Display display, ISwtVideoRenderer renderer, Machine machine) {
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
		
		buttonBar = new ButtonBar(mainComposite, SWT.HORIZONTAL);
		buttonBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		/*
		controlsComposite = new Composite(mainComposite, SWT.NO_RADIO_GROUP | SWT.NO_FOCUS);
		layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		controlsComposite.setLayout(layout);
		controlsComposite.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, true));
		controlsComposite.addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				paintButtonBar(e.gc, e.widget, new Point(0, 0), controlsComposite.getSize());
			}
			
		});
		*/
		/*BasicButton abortButton =*/ createButton(buttonBar, 
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

		/*BasicButton logButton =*/ createStateButton(buttonBar,
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


		shell.open();
		shell.pack();
		Rectangle displaySize = shell.getDisplay().getBounds();
		Point shellSize = shell.getSize();
		shell.setBounds(displaySize.width - shellSize.x, displaySize.height - shellSize.y, shellSize.x, shellSize.y);
		
		renderer.setFocus();

	}

	class ButtonBar extends Composite {

		private GridLayout layout;
		private boolean isHorizontal;

		public ButtonBar(Composite parent, int style) {
			super(parent, style & ~(SWT.HORIZONTAL + SWT.VERTICAL) | SWT.NO_RADIO_GROUP | SWT.NO_FOCUS);
			this.isHorizontal = (style & SWT.HORIZONTAL) != 0;
			layout = new GridLayout(1, true);
			layout.marginHeight = layout.marginWidth = 0;
			setLayout(layout);
			
			addPaintListener(new PaintListener() {

				public void paintControl(PaintEvent e) {
					paintButtonBar(e.gc, e.widget, new Point(0, 0), getSize());
				}
				
			});
		}

		protected void paintButtonBar(GC gc, Widget w, Point offset,  Point size) {
			gc.setForeground(w.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			gc.setBackground(w.getDisplay().getSystemColor(SWT.COLOR_GRAY));
			if (isHorizontal) {
				gc.fillGradientRectangle(offset.x, offset.y + size.y / 2, size.x, size.y / 2, true);
				gc.fillGradientRectangle(offset.x, offset.y + size.y / 2, size.x, -size.y / 2, true);
			} else {
				gc.fillGradientRectangle(offset.x + size.x / 2, offset.y, size.x, size.y, false);
				gc.fillGradientRectangle(offset.x + size.x / 2, offset.y, -size.x / 2, size.y, false);
				
			}
		}
	}
	class BasicButton extends Canvas {

		private final Rectangle bounds;
		private Image icon;
		private Rectangle overlayBounds;
		private List<SelectionListener> listeners;
		private boolean selected;

		public BasicButton(ButtonBar buttonBar, int style, Image icon_, Rectangle bounds_, String tooltip) {
			super(buttonBar, SWT.NO_FOCUS | SWT.NO_RADIO_GROUP /*| SWT.NO_BACKGROUND*/);
			
			buttonBar.layout.numColumns++;
			
			this.icon = icon_;
			this.bounds = bounds_;
			this.listeners = new ArrayList<SelectionListener>();
			addKeyListener(new KeyListener() {
				
				public void keyPressed(KeyEvent e) {
					e.doit = false;
				}
	
				public void keyReleased(KeyEvent e) {
					e.doit = false;
				}
				
			});
			
			GridData data = new GridData(bounds.width, bounds.height);
			data.minimumHeight = 8;
			data.grabExcessHorizontalSpace = false;
			data.grabExcessVerticalSpace = false;
			setLayoutData(data);
			setLayout(new FillLayout());
			
			setToolTipText(tooltip);
			
			addPaintListener(new PaintListener() {
	
				public void paintControl(PaintEvent e) {
					Point size = getSize();
					((ButtonBar)getParent()).paintButtonBar(e.gc, BasicButton.this, new Point(0, 0), size);
					e.gc.drawImage(icon, bounds.x, bounds.y, bounds.width, bounds.height, 
							0, 0, size.x, size.y);
					if (overlayBounds != null)
						e.gc.drawImage(icon, overlayBounds.x, overlayBounds.y, overlayBounds.width, overlayBounds.height, 
								0, 0, size.y, size.y);
				}
				
			});
			addTraverseListener(new TraverseListener() {
	
				public void keyTraversed(TraverseEvent e) {
					e.doit = false;
				}
				
			});
			
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseUp(MouseEvent e) {
					SelectionListener[] array = (SelectionListener[]) listeners.toArray(new SelectionListener[listeners.size()]);
					Event event = new Event();
					event.widget = BasicButton.this;
					SelectionEvent selEvent = new SelectionEvent(event);
					for (SelectionListener listener : array) {
						listener.widgetSelected(selEvent);
					}
					videoRenderer.setFocus();
				}
			});
			
		}

		public void setOverlayBounds(Rectangle overlayBounds) {
			this.overlayBounds = overlayBounds;
		}

		public void addSelectionListener(SelectionListener listener) {
			listeners.add(listener);
		}

		public boolean getSelection() {
			return selected;
		}

		public void setSelection(boolean flag) {
			if (flag != selected) {
				this.selected = flag;
				redraw();
			}
		}
	}

	/*
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
	*/

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
