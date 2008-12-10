/**
 * 
 */
package v9t9.emulator.clients.builtin;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.video.SwtVideoRenderer;
import v9t9.emulator.clients.builtin.video.VideoRenderer;
import v9t9.emulator.clients.builtin.video.VdpCanvas.ICanvasListener;
import v9t9.emulator.runtime.Cpu;
import v9t9.emulator.runtime.Executor;
import v9t9.engine.settings.ISettingListener;
import v9t9.engine.settings.Setting;

/**
 * Provide the emulator in an SWT window
 * @author ejs
 *
 */
public class SwtWindow {
	
	protected Shell shell;
	protected Control videoControl;
	protected SwtVideoRenderer videoRenderer;
	
	private final Machine machine;
	private Composite controlsComposite;
	
	public SwtWindow(Display display, SwtVideoRenderer renderer, Machine machine) {
		this.machine = machine;
		shell = new Shell(display);
		
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		shell.setLayout(layout);
		
		this.videoRenderer = renderer;

		Composite mainComposite = shell;
		//mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		//mainComposite.setLayout(new GridLayout(2, false));
		
		final Composite screenComposite = new Composite(mainComposite, SWT.BORDER);
		//layout = new GridLayout();
		//layout.marginHeight = layout.marginWidth = 0;
		//screenComposite.setLayout(layout);
		screenComposite.setLayout(new FillLayout());
		GridData screenLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		screenLayoutData.minimumHeight = 256;
		screenLayoutData.minimumWidth = 192;
		screenLayoutData.widthHint = 256 * 3;
		screenLayoutData.heightHint = 192 * 3;
		screenComposite.setLayoutData(screenLayoutData);
		
		this.videoControl = renderer.createControl(screenComposite);
		//GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		//videoControl.setLayoutData(gridData);
		
		File iconsFile = new File("icons/icons.png");
		Image icons = new Image(getShell().getDisplay(), iconsFile.getAbsolutePath());
		
		/*
		ExpandBar expandingButtons = new ExpandBar(mainComposite, SWT.V_SCROLL);
		layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		expandingButtons.setLayout(layout);
		expandingButtons.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, true));
		*/
		
		//controlsComposite = new Composite(expandingButtons, SWT.NONE);
		controlsComposite = new Composite(mainComposite, SWT.NO_RADIO_GROUP);
		layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		controlsComposite.setLayout(layout);
		controlsComposite.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, true));
		
		/*
		ExpandItem expandingButton = new ExpandItem (expandingButtons, SWT.NONE, 0);
		expandingButton.setControl(controlsComposite);
		expandingButton.setText("Commands");
		*/
		//controlsComposite.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
		/*
		mainComposite.setLayout(new Layout() {

			@Override
			protected Point computeSize(Composite composite, int whint,
					int hhint, boolean flushCache) {
				// get, don't compute, the size
				Point screenSize = screenComposite.getSize();
				Point buttonSize = controlsComposite.computeSize(whint, hhint);
				return new Point(screenSize.x + buttonSize.x, Math.max(screenSize.y, buttonSize.y));
			}

			@Override
			protected void layout(Composite composite, boolean flushCache) {
				Point sz = composite.getSize();
				Point screenSize = screenComposite.getSize();
				if (screenSize.x < 256) screenSize.x = 256;
				if (screenSize.y < 192) screenSize.y = 192;
				screenComposite.setSize(screenSize);
				controlsComposite.setSize(sz.x - screenSize.x, Math.max(sz.y, screenSize.y));
			}
			
		});
*/
		
		BasicButton abortButton = createButton(icons, 
				new Rectangle(0, 64, 64, 64), "Send a NMI interrupt",
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						SwtWindow.this.machine.getCpu().holdpin(Cpu.INTPIN_LOAD);
						restoreFocus();
					}
				});

		BasicButton logButton = createStateButton(Executor.settingDumpFullInstructions,
				icons, new Rectangle(0, 128, 64, 64),
				new Rectangle(0, 0, 64, 64),
				"Toggle CPU logging");
		

		shell.open();
		shell.setBounds(800, 800, shell.getSize().x, shell.getSize().y);
		shell.pack();
	}

	class BasicButton extends Composite {

		private final Button button;
		private final Rectangle bounds;
		private Image icon;
		private int offset;
		private Rectangle overlayBounds;

		public BasicButton(Composite parent, int style, Image icon_, Rectangle bounds_, String tooltip) {
			super(parent, SWT.NO_FOCUS);
			this.icon = icon_;
			this.bounds = bounds_;
			
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
				public void widgetSelected(SelectionEvent e) {
					restoreFocus();
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

			public void changed(Setting setting, Object oldValue) {
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

}
