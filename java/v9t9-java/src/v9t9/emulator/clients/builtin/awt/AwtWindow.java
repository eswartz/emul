/**
 * 
 */
package v9t9.emulator.clients.builtin.awt;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Paint;
import java.awt.Point;
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

import org.ejs.coffee.core.utils.ISettingListener;
import org.ejs.coffee.core.utils.Setting;

import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.BaseEmulatorWindow;
import v9t9.emulator.clients.builtin.ButtonPressHandler;
import v9t9.emulator.hardware.V9t9;
import v9t9.emulator.runtime.Executor;

/**
 * Provide the emulator in an SWT window
 * @author ejs
 *
 */
public class AwtWindow extends BaseEmulatorWindow implements IAwtVideoRendererContainer {
	
	protected Frame frame;
	private Container controlsContainer;
	private BufferedImage icons;
	private BufferStrategy bufferStrategy;
	private GridBagLayout controlsLayout;
	public AwtWindow(final Machine machine) {
		super(machine);
		
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = env.getDefaultScreenDevice();
		GraphicsConfiguration gc = device.getDefaultConfiguration();
		frame = new Frame(gc);
		
		AwtVideoRenderer renderer = new AwtVideoRenderer();
		setVideoRenderer(renderer);
		
		//frame.setIgnoreRepaint(true);
		frame.setTitle("V9t9");
		
		BoxLayout toplevelLayout = new BoxLayout(frame, BoxLayout.X_AXIS);
		frame.setLayout(toplevelLayout);
		
		frame.add(renderer.getAwtCanvas());
		
		frame.setFocusCycleRoot(true);
		frame.setFocusTraversalKeysEnabled(false);
		
		File iconsFile = V9t9.getDataFile("icons/icons.png");
		try {
			icons = ImageIO.read(iconsFile);
		} catch (IOException e1) {
			icons = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		}
		
		controlsContainer = new Container() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void paint(Graphics g) {
				Graphics2D g2d = (Graphics2D) g;
				Paint oldPaint = g2d.getPaint();
				GradientPaint paint = new GradientPaint(new Point(0, 0), new Color(0xf0f0f0),
						new Point(getWidth() / 2, 0), new Color(0xc0c0c0), true);
				g2d.setPaint(paint);
				g2d.fillRect(0, 0, getWidth(), getHeight());
				g2d.setPaint(oldPaint);
				super.paint(g);
			}
		};
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
				new Rectangle(0, 128, 64, 64), new Rectangle(0, 0, 64, 64),
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
		createStateButton(Machine.settingPauseMachine, new Rectangle(0, 512, 64, 64), new Rectangle(0, 0, 64, 64),
				"Pause machine");

		createStateButton(V9t9.settingMonitorDrawing, new Rectangle(0, 576, 64, 64), new Rectangle(0, 0, 64, 64), 
				"Apply monitor effect to video");


		//screenContainer.setFocus();


		frame.setVisible(true);
		//frame.setBounds(800, 800, 256 * 3 + 96, 200 * 3);
		frame.setLocation(800, 800);
		frame.pack();
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
			Rectangle bounds, final Rectangle checkBounds, String tooltip) {
		
		final BasicButton button = createButton(icons, bounds, tooltip, new ButtonPressHandler() {

			public void pressed() {
				machine.asyncExec(new Runnable() {
					public void run() {
						setting.setBoolean(!setting.getBoolean());
					}
				});
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
		
		if (setting.getBoolean())
			button.setOverlayBounds(checkBounds);
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
				machine.getKeyboardState().pasteText(contents);
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
			String fileName, boolean isSave, String[] extensions) {
		JFileChooser chooser = new JFileChooser(directory);
		chooser.setDialogTitle(title);
		chooser.setSelectedFile(new File(directory, fileName));
		int ret;
		if (isSave)
			ret = chooser.showSaveDialog(frame);
		else
			ret = chooser.showOpenDialog(frame);
		if (ret == JFileChooser.CANCEL_OPTION)
			return null;
		return chooser.getSelectedFile().getAbsolutePath();
	}
	
	@Override
	protected String openDirectorySelectionDialog(String title, String directory) {
		JFileChooser chooser = new JFileChooser(directory);
		chooser.setDialogTitle(title);
		chooser.setSelectedFile(new File(directory));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int ret;
		ret = chooser.showOpenDialog(frame);
		if (ret == JFileChooser.CANCEL_OPTION)
			return null;
		return chooser.getSelectedFile().getAbsolutePath();
	}
	
	@Override
	protected void showErrorMessage(String title, String msg) {
		
	}
	
}
