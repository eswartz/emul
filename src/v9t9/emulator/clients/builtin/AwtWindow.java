/**
 * 
 */
package v9t9.emulator.clients.builtin;

import java.awt.Canvas;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.video.AwtVideoRenderer;
import v9t9.emulator.runtime.Executor;
import v9t9.engine.settings.ISettingListener;
import v9t9.engine.settings.Setting;

/**
 * Provide the emulator in an SWT window
 * @author ejs
 *
 */
public class AwtWindow extends BaseEmulatorWindow {
	
	protected Frame frame;
	protected Canvas videoControl;
	private Container controlsContainer;
	private BufferedImage icons;
	private Shell shell;
	private BufferStrategy bufferStrategy;
	private GridBagLayout controlsLayout;
	public AwtWindow(final Machine machine) {
		super(machine);
		
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = env.getDefaultScreenDevice();
		GraphicsConfiguration gc = device.getDefaultConfiguration();
		frame = new Frame(gc);
		
		
		AwtVideoRenderer renderer = new AwtVideoRenderer(this);
		setVideoRenderer(renderer);
		
		//frame.setIgnoreRepaint(true);
		frame.setTitle("V9t9");
		
		BoxLayout toplevelLayout = new BoxLayout(frame, BoxLayout.X_AXIS);
		frame.setLayout(toplevelLayout);
		
		frame.add(renderer.getAwtCanvas());
		
		frame.setFocusCycleRoot(true);
		frame.setFocusTraversalKeysEnabled(false);
		
		File iconsFile = new File("icons/icons.png");
		try {
			icons = ImageIO.read(iconsFile);
		} catch (IOException e1) {
			icons = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		}
		
		controlsContainer = new Container();
		controlsLayout = new GridBagLayout();
		controlsContainer.setLayout(controlsLayout);
		controlsContainer.setFocusable(false);
		controlsContainer.setFocusTraversalKeysEnabled(false);

		frame.add(Box.createGlue());
		frame.add(controlsContainer);
		
		createButton(icons, 
				new Rectangle(0, 64, 64, 64), "Send a NMI interrupt",
				new ButtonPressHandler() {
					public void pressed() {
						sendNMI();
					}
				});

		createButton(icons, 
				new Rectangle(0, 256, 64, 64), "Reset the computer",
				new ButtonPressHandler() {
					public void pressed() {
						sendReset();
					}
				});

		createStateButton(Executor.settingDumpFullInstructions,
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
				new ButtonPressHandler() {
					public void pressed() {
						pasteClipboardToKeyboard();
					}
			});
		
		createButton(icons, new Rectangle(0, 384, 64, 64),
				"Save machine state",
				new ButtonPressHandler() {
					public void pressed() {
						saveMachineState();
					}

			});
		
		createButton(icons, new Rectangle(0, 448, 64, 64),
				"Load machine state",
				new ButtonPressHandler() {
					public void pressed() {
						loadMachineState();
					}
			});

		controlsContainer.setMinimumSize(new Dimension(64, 64));
		controlsContainer.setPreferredSize(new Dimension(64, 64 * controlsContainer.getComponentCount()));
		
		//controlsContainer.add(Box.createVerHorizontalStrut(64));
		//createStateButton(Machine.settingPauseMachine, icons, new Rectangle(0, 512, 64, 64),
		//		new Rectangle(0, 0, 64, 64),
		//		"Pause machine");

		//screenContainer.setFocus();


		frame.setVisible(true);
		//frame.setBounds(800, 800, 256 * 3, 192 * 3);
		frame.setLocation(800, 800);
		
		frame.createBufferStrategy(1);
		bufferStrategy = frame.getBufferStrategy();

	}

	@Override
	public void dispose() {
		super.dispose();
		frame.dispose();
		
	}
	public BufferStrategy getBufferStrategy() {
		return bufferStrategy;
	}
	class BasicButton extends LightweightButton {

		/**
		 * 
		 */
		private static final long serialVersionUID = -47717612024496339L;
		private final Rectangle bounds;
		private BufferedImage icon;
		private Rectangle overlayBounds;

		public BasicButton(BufferedImage icon_, Rectangle bounds_, String tooltip) {
			super(tooltip, new Dimension(bounds_.width, bounds_.height));
			this.icon = icon_;
			this.bounds = bounds_;
		}

		public void setOverlayBounds(Rectangle overlayBounds) {
			this.overlayBounds = overlayBounds;
		}
		
		@Override
		public void paint(Graphics g) {
			
			g.drawImage(icon, 0, 0, getWidth(), getHeight(), bounds.x, bounds.y, bounds.x+bounds.width, bounds.y+bounds.height, frame);
			if (overlayBounds != null) {
				g.drawImage(icon, 0, 0, getWidth(), getHeight(), 
						overlayBounds.x, overlayBounds.y, overlayBounds.x+overlayBounds.width, overlayBounds.y+overlayBounds.height, frame);
			}
			super.paint(g);
		}
	}

	private BasicButton createButton(final BufferedImage icon, final Rectangle bounds,
			String tooltip, final ButtonPressHandler handler) {
		BasicButton button = new BasicButton(icon, bounds, tooltip);
		controlsContainer.add(button);
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				handler.pressed();
			}
			
		});
		button.setPreferredSize(new Dimension(64, 64));
		
		button.setFocusTraversalKeysEnabled(false);
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.anchor = GridBagConstraints.EAST;
		controlsLayout.setConstraints(button, constraints);

		return button;
	}
	
	
	private BasicButton createStateButton(final Setting setting,
			BufferedImage icons, Rectangle bounds, final Rectangle checkBounds,
			String tooltip) {
		
		final BasicButton button = createButton(icons, bounds, tooltip, new ButtonPressHandler() {

			public void pressed() {
				setting.setBoolean(!setting.getBoolean());
			}
			
		});
		setting.addListener(new ISettingListener() {

			public void changed(final Setting setting, final Object oldValue) {
				if (setting.getBoolean()) {
					button.setOverlayBounds(checkBounds);
				} else {
					button.setOverlayBounds(null);
				}
				button.repaint();
			}
			
		});
		return button;
	}

	protected void restoreFocus() {
	}

	
	public void setDesiredScreenSize(int desiredWidth, int desiredHeight) {
		//Dimension sz = frame.getLayout().preferredLayoutSize(frame);
		//frame.setSize(sz);
		frame.pack();
	}

	public AwtVideoRenderer getVideoRenderer() {
		return (AwtVideoRenderer) videoRenderer;
	}
	
	public Frame getFrame() {
		return frame;
	}


	protected void pasteClipboardToKeyboard() {
		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable t = clip.getContents(null);
		try {
			if (t == null) {
				throw new Exception("No data on clipboard");
			}
		
			if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				String contents = t.getTransferData(DataFlavor.stringFlavor).toString();
				machine.getClient().getKeyboardHandler().pasteText(contents);
			} else {
				throw new Exception("Cannot convert clipboard to text");
			}
		} catch (Exception e) {
			showErrorMessage("Paste Error", 
					e.getMessage());
		}
	}

	@Override
	protected String openFileSelectionDialog(String title, String directory,
			String fileName, boolean isSave) {
		JFileChooser chooser = new JFileChooser(directory);
		chooser.setDialogTitle(title);
		chooser.setSelectedFile(new File(directory, fileName));
		if (isSave)
			chooser.showSaveDialog(frame);
		else
			chooser.showOpenDialog(frame);
		return chooser.getSelectedFile() != null ? chooser.getSelectedFile().getAbsolutePath() : null;
	}
	
	@Override
	protected void showErrorMessage(String title, String msg) {
		
	}
	
}
