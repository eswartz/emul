/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.io.File;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import v9t9.emulator.hardware.V9t9;

/**
 * This tests the model of having a window in which a subcomponent has a known and fixed
 * size.
 * @author ejs
 *
 */
public class TestSWTWindow {

	static class VideoControl extends Composite {

		private FixedAspectLayout layout;

		public VideoControl(Composite parent, int style) {
			super(parent, style);
			
			layout = new FixedAspectLayout(256, 192, 1.0, 1.0);
			setLayout(layout);
			
			//canvas = new Canvas(this, SWT.NONE);
			//canvas.setLayout(layout);
			
			addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					System.out.println(e.character);
					int w = layout.getWidth();
					int h = layout.getHeight();
					if (e.character == 'w') {
						if (w == 512)
							w = 256;
						else if (w == 256)
							w = 240;
					} else if (e.character == 'W') {
						if (w == 240)
							w = 256;
						else if (w == 256)
							w = 512;
					}
					if (e.character == 'h') {
						if (h == 424)
							h =(384);
						else if (h == 384)
							h =(212);
						else if (h == 212)
							h =(192);
					} else if (e.character == 'H') {
						if (h == 192)
							h =(212);
						else if (h == 212)
							h =(384);
						else if (h == 384)
							h = (424);
					} 
					layout.setSize(w, h);
					
					
					getParent().layout(true);
				}
			});
		}
		
	}


	private VideoControl videoControl;
	private ButtonBar buttonBar;
	private Image mainIcons;
	private Shell shell;
	
	
	protected TestSWTWindow(Display display) {
		
		shell = new Shell(display, SWT.SHELL_TRIM| SWT.RESIZE);
		shell.setText("V9t9");

		Composite mainComposite = shell;
		GridLayoutFactory.fillDefaults().margins(2, 2).applyTo(mainComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(mainComposite);
		
		Composite topComposite = new Composite(mainComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(topComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(topComposite);
		
		videoControl = new VideoControl(topComposite, SWT.BORDER);
		
		final GridData rendererLayoutData = GridDataFactory.swtDefaults()
			.indent(0, 0)
			.align(SWT.CENTER, SWT.CENTER)
			.grab(true, true)
			.create();
		videoControl.setLayoutData(rendererLayoutData);
		
		createButtons(mainComposite);
		
		shell.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						shell.layout(true);
;					}
				});
			}
		});
	}
	

	private BasicButton createButton(ButtonBar buttonBar, int iconIndex, String tooltip) {
		Rectangle bounds = mainIconIndexToBounds(iconIndex);
		BasicButton button = new BasicButton(buttonBar, SWT.PUSH, mainIcons, bounds, tooltip);
		return button;
	}
	
	private Rectangle mainIconIndexToBounds(int iconIndex) {
		Rectangle bounds = mainIcons.getBounds();
		int unit = bounds.width;
		return new Rectangle(0, unit * iconIndex, unit, unit); 
	}

	private void createButtons(Composite parent) {
		File iconsFile = V9t9.getDataFile("icons/icons.png");
		mainIcons = new Image(shell.getDisplay(), iconsFile.getAbsolutePath());
		
		buttonBar = new ButtonBar(parent, SWT.HORIZONTAL);
		
		GridLayoutFactory.swtDefaults().margins(0, 0).applyTo(buttonBar);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.BOTTOM).applyTo(buttonBar);
		
		createButton(buttonBar, 1,
				"Send a NMI interrupt");

		createButton(buttonBar, 4,
				"Reset the computer");

		createButton(buttonBar,
				7, "Create debugger window"
		);
		
		createButton(buttonBar, 6,
				"Load or save machine state");

		createButton(buttonBar, 10,
				"Take screenshot");

		createButton(buttonBar, 11,
				"Zoom the screen");
		
		createButton(buttonBar, 12,
				"Accelerate execution");
		
	}


	public static void main(String[] args) {
		Display display = new Display();
		
		TestSWTWindow window = new TestSWTWindow(display);
		window.shell.open();
		
		while (!window.shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
}
