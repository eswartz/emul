/**
 * 
 */
package v9t9.gui.client.swt.shells;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import v9t9.common.keyboard.IKeyboardListener;
import v9t9.common.keyboard.IKeyboardMapping;
import v9t9.common.keyboard.IKeyboardMapping.PhysKey;
import v9t9.common.keyboard.IKeyboardMode;
import v9t9.common.keyboard.IKeyboardModeListener;
import v9t9.common.keyboard.KeyboardConstants;
import v9t9.common.machine.IMachine;
import v9t9.gui.client.swt.IFocusRestorer;
import v9t9.gui.client.swt.bars.Gradient;
import v9t9.gui.client.swt.bars.IImageCanvas;
import v9t9.gui.client.swt.bars.ImageButton;
import v9t9.gui.client.swt.bars.ImageCanvas;
import v9t9.gui.client.swt.bars.ImageProvider;
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

		/**
		 * @param parentBar
		 * @param style
		 * @param key 
		 * @param imageProvider
		 * @param iconIndex
		 * @param tooltip
		 */
		public KeyboardButton(IImageCanvas parentBar, int style, PhysKey key) {
			super(parentBar, style, KeyboardDialog.this.imageProvider, -1, "");
			this.key = key;
		}
		
		public void setText(String text) {
			if (text != this.text || text != null && !text.equals(this.text)) {
				this.text = text;
				redraw();
			}
		}

		/* (non-Javadoc)
		 * @see v9t9.gui.client.swt.bars.ImageIconCanvas#doPaint(org.eclipse.swt.events.PaintEvent)
		 */
		@Override
		protected void doPaint(PaintEvent e) {
			super.doPaint(e);
			
			if (getSelection()) {
				e.gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
				e.gc.fillRectangle(e.x, e.y, e.width, e.height);
			}
			
			e.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
			Point tw = e.gc.stringExtent(text);
			e.gc.drawString(text, (getSize().x-tw.x)/2, (getSize().y-tw.y)/2, true);
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
		
		/* (non-Javadoc)
		 * @see v9t9.gui.client.swt.bars.ImageButton#doMouseHover(org.eclipse.swt.events.MouseEvent)
		 */
		@Override
		protected void doMouseHover(MouseEvent e) {
			super.doMouseHover(e);
//			if (nextChangeTime > 0) {
//				if (System.currentTimeMillis() >= nextChangeTime ) {
//					fireKeypress(getSelection());
//					//setSelection(!getSelection());
//					nextChangeTime = System.currentTimeMillis() + 100;
//				}
//			}
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
	private int zoom = 1;
	private boolean buttonUpdateWaiting;
	private ImageCanvas imageCanvas;
	private ImageProvider imageProvider;
	
	public KeyboardDialog(Shell shell, IMachine machine_, IFocusRestorer focusRestorer, ImageProvider imageProvider) {
		
		super(shell, SWT.NONE);
		
		this.machine = machine_;
		this.imageProvider = imageProvider;
		
		shell.setText("Keyboard");

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
				new Gradient(true, new int[] { 0, -1, 0}, new float[] { 0.33f, 0.67f }),
				focusRestorer, true);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(imageCanvas);
		
		createKeys(imageCanvas);

		machine.addKeyboardModeListener(this);
		machine.getKeyboardState().addKeyboardListener(this);
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				machine.removeKeyboardModeListener(KeyboardDialog.this);
			}
		});
		
		keyboardModeChanged(machine.getKeyboardMode());
		updateButtons();

		layoutKeys();
		
	}

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
		final KeyboardButton button = new KeyboardButton(parent, SWT.BORDER | SWT.PUSH, key);
		
//		GridDataFactory.fillDefaults().span(key.width, key.height)
//			.minSize(BASE_X_SIZE*key.width, BASE_Y_SIZE*key.height)
//			.applyTo(label);
		//.indent(key.x * BASE_X_SIZE, key.y * BASE_Y_SIZE).		
		keyButtons.put(key, button);
		
//		button.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				byte shiftLockMask = (byte) (machine.getKeyboardState().getShiftMask()
//						| machine.getKeyboardState().getLockMask());
//				
//				int keycode = currentMode.getKeycode(shiftLockMask, key);
//				if (keycode != KeyboardConstants.KEY_UNKNOWN)
//					machine.getKeyboardState().stickyApplyKey(keycode, button.getSelection());
//			}
//			
//		});
		return button;
	}
	
	protected void layoutKeys() {
		for (PhysKey key : physKeys) {
			Control control = keyButtons.get(key);
			
			control.setLocation(key.x * BASE_X_SIZE * zoom, key.y * BASE_Y_SIZE * zoom);
			control.setSize(key.width * BASE_X_SIZE * zoom, key.height * BASE_Y_SIZE * zoom);

		}
	}

}
