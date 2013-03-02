/*
  KeyboardDialog.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.ejs.gui.common.FontUtils;

import v9t9.common.keyboard.IKeyboardListener;
import v9t9.common.keyboard.IKeyboardMapping;
import v9t9.common.keyboard.IKeyboardMapping.PhysKey;
import v9t9.common.keyboard.IKeyboardMode;
import v9t9.common.keyboard.IKeyboardModeListener;
import v9t9.common.keyboard.KeyboardConstants;
import v9t9.common.machine.IMachine;
import v9t9.gui.EmulatorGuiData;
import v9t9.gui.client.swt.IFocusRestorer;
import v9t9.gui.client.swt.bars.Gradient;
import v9t9.gui.client.swt.bars.IImageCanvas;
import v9t9.gui.client.swt.bars.ImageButton;
import v9t9.gui.client.swt.bars.ImageCanvas;
import v9t9.gui.client.swt.bars.ImageProvider;
import v9t9.gui.client.swt.bars.MultiImageSizeProvider;
import ejs.base.utils.Pair;

/**
 * Shows a keyboard which allows input and shows converted keystrokes
 * @author ejs
 *
 */
public class KeyboardDialog extends Composite implements IKeyboardModeListener, IKeyboardListener {

	private static final int BASE_Y_SIZE = 8;
	private static final int BASE_X_SIZE = 8;
	
	public static final String KEYBOARD_TOOL_ID = "keyboard";

	class KeyboardButton extends ImageButton {

		private String text;
		private PhysKey key;
		private int ioy;

		/**
		 * @param parentBar
		 * @param style
		 * @param key 
		 * @param imageProvider
		 * @param iconIndex
		 * @param tooltip
		 */
		public KeyboardButton(IImageCanvas parentBar, int style, PhysKey key) {
			super(parentBar, style, keyImageProvider, 0, "");
			this.key = key;
		}
		
		public void setText(String text) {
			if (text != this.text || text != null && !text.equals(this.text)) {
				this.text = text;
				redraw();
			}
		}

		/* (non-Javadoc)
		 * @see v9t9.gui.client.swt.bars.ImageButton#updateDrawRect(org.eclipse.swt.graphics.Rectangle)
		 */
		@Override
		protected void updateDrawRect(Rectangle drawRect) {
			ioy = 0;
			if (getSelection()) {
				ioy = getSize().y / 16;
			}
			drawRect.y += ioy;
		}
		/* (non-Javadoc)
		 * @see v9t9.gui.client.swt.bars.ImageIconCanvas#doPaint(org.eclipse.swt.events.PaintEvent)
		 */
		@Override
		protected void doPaint(PaintEvent e) {
			super.doPaint(e);
			
//			int offs = 0;
//			if (getSelection()) {
//				offs = getSize().y / 8;
////				e.gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
////				e.gc.fillRectangle(e.x, e.y, e.width, e.height);
//			}
			
			e.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
			
			Point sz = getSize();
			Point tw = null;
			FontDescriptor fdesc = FontUtils.getFontDescriptor(e.gc.getFont());
			Font newFont = null;
			
			int fh = (int) fdesc.getFontData()[0].height;
			String measText = text;
			int nlIdx = text.indexOf('\n');
			if (nlIdx >= 0) 
				measText = text.substring(0, nlIdx);
			while (fh > 0) {
				tw = e.gc.stringExtent(measText);
				if (tw.x * 3 / 2 <= sz.x)
					break;
				fdesc = fdesc.increaseHeight(-1);
				fh--;
				newFont = textFonts.get(fh);
				if (newFont == null) {
					newFont = fdesc.createFont(getDisplay());
					textFonts.put(fh, newFont);
				}
				
				e.gc.setFont(newFont);
			}
			
			if (measText != text)
				tw.y *= 2;
			
			e.gc.drawString(measText, (sz.x - tw.x) / 2, sz.y * 1 / 3 - tw.y / 2
					+ ioy, true);

			if (measText != text) {
				measText = text.substring(measText.length()+1);
				tw = e.gc.stringExtent(measText);
				e.gc.drawString(measText, 
						(sz.x - tw.x) / 2, sz.y * 1 / 3 - tw.y + e.gc.getFontMetrics().getHeight() + ioy, true);
			}

			e.gc.setFont(getFont());
		}
		
		/* (non-Javadoc)
		 * @see v9t9.gui.client.swt.bars.ImageButton#doClickStart()
		 */
		@Override
		protected void doClickStart() {
			super.doClickStart();
			fireKeypress(true);
			
		}
		
		/* (non-Javadoc)
		 * @see v9t9.gui.client.swt.bars.ImageButton#doClickStop(org.eclipse.swt.widgets.Event)
		 */
		@Override
		protected void doClickStop(Event e) {
			super.doClickStop(e);
			fireKeypress(false);
		}
		
	
		/**
		 * 
		 */
		protected void fireKeypress(boolean set) {
			setSelection(set);
			redraw();
			
			byte shiftLockMask = (byte) (machine.getKeyboardState().getShiftMask()
					| machine.getKeyboardState().getLockMask());
			
			int keycode = currentMode.getKeycode(shiftLockMask, key);
			if (keycode != KeyboardConstants.KEY_UNKNOWN)
				machine.getKeyboardState().stickyApplyKey(keycode, set);
			
			
		}
	}
	
	public static IToolShellFactory getToolShellFactory(final IMachine machine, final ImageCanvas buttonBar, final ImageProvider imageProvider) {
		return new IToolShellFactory() {
			Behavior behavior = new Behavior();
			{
				behavior.boundsPref = "KeyboardWindowBounds";
				behavior.centering = null;
				behavior.centerOverControl = buttonBar;
				behavior.dismissOnClickOutside = false;
			}
			public Control createContents(Shell shell) {
				return new KeyboardDialog(shell, machine, buttonBar.getFocusRestorer(), imageProvider);
			}
			public Behavior getBehavior() {
				return behavior;
			}
		};
	}

	
	private final IMachine machine;
	private IKeyboardMapping keyboardMapping;
	private PhysKey[] physKeys;
	private Map<PhysKey, KeyboardButton> keyButtons;
	private Set<Integer> pressedPhysKeyIds = new HashSet<Integer>();
	private IKeyboardMode currentMode;
	private float zoom = 1.5f;
	private boolean buttonUpdateWaiting;
	private ImageCanvas imageCanvas;
	private MultiImageSizeProvider keyImageProvider;
	private Map<Integer, Font> textFonts = new HashMap<Integer, Font>();
	
	public KeyboardDialog(Shell shell, IMachine machine_, IFocusRestorer focusRestorer, ImageProvider imageProvider) {
		
		super(shell, SWT.NONE);
		
		this.machine = machine_;
//		this.imageProvider = imageProvider;
		
		shell.setText("Keyboard");

		TreeMap<Integer, Image> iconSizeMap = new TreeMap<Integer, Image>();
		for (int size : new int[] { 32, 64, 128, 256, 512 }) {
			Image iconsImage = EmulatorGuiData.loadImage(shell.getDisplay(), "icons/key_" + size + ".png");
			if (iconsImage != null) {
				iconSizeMap.put(size, iconsImage);
			}
		}
		
		keyImageProvider = new MultiImageSizeProvider(iconSizeMap);
		
		//renderIcon(40);
		
		GridLayoutFactory.fillDefaults().applyTo(this);
		
		keyboardMapping = machine.getKeyboardMapping();
		
		if (keyboardMapping == null) {
			Label label = new Label(this, SWT.WRAP);
			label.setText("Sorry, no keyboard mapping known for this machine");
			
			GridDataFactory.fillDefaults().grab(true, true).align(SWT.CENTER, SWT.CENTER).applyTo(label);
			return;
		}
		
		physKeys = keyboardMapping.getPhysicalLayout();
		
		keyButtons = new HashMap<PhysKey, KeyboardButton>();

		imageCanvas = new ImageCanvas(this, SWT.HORIZONTAL, 
				new Gradient(true, new int[] { 0x7f7f7f, -1, 0x7f7f7f}, new float[] { 0.33f, 0.67f }),
				focusRestorer, true);

		GridDataFactory.fillDefaults().grab(true, true).applyTo(imageCanvas);
		
		createKeys(imageCanvas);

		machine.addKeyboardModeListener(this);
		machine.getKeyboardState().addKeyboardListener(this);
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				machine.removeKeyboardModeListener(KeyboardDialog.this);
				
				for (Font font : textFonts.values())
					font.dispose();
				textFonts.clear();
//				keyImage.dispose();
			}
		});
		
		keyboardModeChanged(machine.getKeyboardMode());
		updateButtons();

		layoutKeys();
		
	}
//
//	/**
//	 * @param sz
//	 */
//	private void renderIcon(int sz) {
//		BufferedImage img = new BufferedImage(sz, sz, BufferedImage.TYPE_4BYTE_ABGR);
//		Graphics2D g2d = img.createGraphics(); 
//		try {
//
//	    	svgDiagram.setDeviceViewport(new java.awt.Rectangle(0, 0, 32, 32));
//	    	
//	        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//	        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//
//	        g2d.setClip(0, 0, 32, 32);
//	    	
//			svgDiagram.render(g2d);
//			g2d.dispose();
//			
//			if (keyImage != null)
//				keyImage.dispose();
//			
//			keyImage = ImageUtils.convertAwtImage(getDisplay(), img);
//					
//		} catch (SVGException e1) {
//			e1.printStackTrace();
//		}
//		
//	}

	/* (non-Javadoc)
	 * @see v9t9.common.keyboard.IKeyboardModeListener#keyboardModeChanged(java.lang.String)
	 */
	@Override
	public synchronized void keyboardModeChanged(String modeId) {
		IKeyboardMode newMode = keyboardMapping.getMode(modeId);
		if (newMode != currentMode) {
			currentMode = newMode; 
			scheduleButtonUpdate();
		}
	}
		
	/* (non-Javadoc)
	 * @see v9t9.common.keyboard.IKeyboardListener#joystickChangeEvent(int, byte)
	 */
	@Override
	public void joystickChangeEvent(int num, byte mask) {
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.keyboard.IKeyboardListener#physKeyEvent(java.util.Collection, boolean)
	 */
	@Override
	public synchronized void physKeyEvent(Collection<Integer> keys, boolean pressed) {
		if (pressed)
			pressedPhysKeyIds.addAll(keys);
		else
			pressedPhysKeyIds.removeAll(keys);

		scheduleButtonUpdate();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.keyboard.IKeyboardListener#keyEvent(java.util.Collection, boolean)
	 */
	@Override
	public void keyEvent(Collection<Integer> keys, boolean pressed) {
		
	}
	/**
	 * 
	 */
	private synchronized void scheduleButtonUpdate() {
		if (!buttonUpdateWaiting && !isDisposed()) {
			buttonUpdateWaiting = true;
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (!isDisposed())
						updateButtons();
					synchronized (KeyboardDialog.this) {
						buttonUpdateWaiting = false;
					}
				}
			});
		}
		
	}

	/**
	 * 
	 */
	protected void updateButtons() {
		if (currentMode == null) {
			for (KeyboardButton button : keyButtons.values()) {
				button.setText("");
			}
			return;
		}
		
		byte shiftLockMask = (byte) (machine.getKeyboardState().getShiftMask()
				| machine.getKeyboardState().getLockMask());
		
		Map<PhysKey, Pair<Integer, String>> shiftMap = currentMode.getShiftLockMaskMap(shiftLockMask);
		Map<PhysKey, Pair<Integer, String>> normalMap = currentMode.getShiftLockMaskMap((byte) 0);
		
		for (Map.Entry<PhysKey, KeyboardButton> ent : keyButtons.entrySet()) {
			
			Pair<Integer, String> info = shiftMap.get(ent.getKey());
			if (info == null) // && (shiftLockMask & KeyboardConstants.MASK_SHIFT+(1<<KeyboardConstants.KEY_SHIFT)) != 0)
				info = normalMap.get(ent.getKey());
			KeyboardButton button = ent.getValue();
			if (info == null) {
				button.setText("");
			} else {
				boolean selected = pressedPhysKeyIds.contains(ent.getKey().keyId);
				button.setSelection(selected);
				button.setText(info.second);
			}
		}
	}
	
	/**
	 */
	private void createKeys(IImageCanvas composite) {
		//Composite composite = new Composite(parent, SWT.NONE);
		//GridLayoutFactory.fillDefaults().applyTo(composite);
		//composite.setLayout(new FillLayout());
		for (PhysKey key : physKeys) {
			Control control = createKey(composite, key);
			control.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
		}
	}

	/**
	 * @param composite
	 * @param key
	 * @return
	 */
	private Control createKey(IImageCanvas parent, final PhysKey key) {
		final KeyboardButton button = new KeyboardButton(parent, SWT.PUSH, key);
		keyButtons.put(key, button);
		return button;
	}
	
	protected void layoutKeys() {
		for (PhysKey key : physKeys) {
			Control control = keyButtons.get(key);
			
			control.setLocation((int) (key.x * BASE_X_SIZE * zoom), (int) (key.y * BASE_Y_SIZE * zoom));
			control.setSize((int) (key.width * BASE_X_SIZE * zoom), (int) (key.height * BASE_Y_SIZE * zoom));

		}
	}

}
