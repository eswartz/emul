/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.ejs.coffee.core.utils.PrefUtils;

import v9t9.emulator.common.EmulatorSettings;

public class ToolShell {
	private boolean keepCentered;
	private boolean dismissOnClickOutside;
	private Point desiredLocation; 
	private Shell shell;
	private final String boundsPref;
	private final IFocusRestorer focusRestorer;
	private Control centerOverControl;
	private long clickOutsideCheckTime;
	private final boolean isHorizontal;
	
	public ToolShell(Shell shell_, 
			IFocusRestorer focusRestorer_,
			String boundsPref_, 
			Control centerOverControl_, 
			boolean isHorizontal,
			boolean dismissOnClickOutside) {
		this.shell = shell_;
		this.focusRestorer = focusRestorer_;
		this.boundsPref = boundsPref_;
		this.centerOverControl = centerOverControl_;
		this.isHorizontal = isHorizontal;
		this.dismissOnClickOutside = dismissOnClickOutside;
		this.keepCentered = centerOverControl_ != null;
		this.clickOutsideCheckTime = System.currentTimeMillis() + 1500;	// let it show up first, so click on the button that created it doesn't kill it
	}
	
	public void init(final Control tool) {
		shell.setImage(((Shell)shell.getParent()).getImage());
		shell.setLayout(new GridLayout(1, false));
		
		final GridData data = GridDataFactory.fillDefaults().grab(true, true).hint(400, 300).create();
		tool.setLayoutData(data);
		
		shell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				tool.dispose();
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						focusRestorer.restoreFocus();
					}
				});
			}
		});

		String boundsStr = EmulatorSettings.INSTANCE.getSettings().get(boundsPref);
		if (boundsStr != null) {
			Rectangle savedBounds = PrefUtils.readBoundsString(boundsStr);
			if (savedBounds != null) {
				if (keepCentered)
					shell.setSize(savedBounds.width, savedBounds.height);
				else {
					SwtWindow.adjustRectVisibility(shell, savedBounds);
					shell.setBounds(savedBounds);
				}
			}
		} else {
			shell.pack();
		}
		
		if (keepCentered) {
			shell.addControlListener(new ControlAdapter() {
				@Override
				public void controlResized(ControlEvent e) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							if (shell.isDisposed())
								return;
							recenterTo(null);
							centerShell();
						}
					});
				}
				/* (non-Javadoc)
				 * @see org.eclipse.swt.events.ControlAdapter#controlMoved(org.eclipse.swt.events.ControlEvent)
				 */
				@Override
				public void controlMoved(ControlEvent e) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							if (shell.isDisposed())
								return;
							if (desiredLocation != null && desiredLocation.equals(shell.getLocation())) {
								desiredLocation = null;
							} else {
								centerShell();
							}
						}
					});
				}
			});
		}
		

		shell.open();
		
		shell.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				// try to stay the same (user controlled) size and not 
				// grow to full screen when next packed
				data.heightHint = tool.getSize().y;
			}
		});
		
		shell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				Rectangle bounds = shell.getBounds();
				String boundsStr = PrefUtils.writeBoundsString(bounds);
				EmulatorSettings.INSTANCE.getSettings().put(boundsPref, boundsStr);
			}
		});
	}
	
	public boolean isKeepCentered() {
		return keepCentered;
	}
	public boolean isDismissOnClickOutside() {
		return dismissOnClickOutside;
	}
	/**
	 * @return the clickOutsideCheckTime
	 */
	public long getClickOutsideCheckTime() {
		return clickOutsideCheckTime;
	}
	
	public void recenterTo(Point pt) {
		if (pt != null) {
			Rectangle sbounds = shell.getBounds();
			if (pt.x != sbounds.x || pt.y != sbounds.y) {
				if (desiredLocation != null && desiredLocation.x == pt.x && desiredLocation.y == pt.y) {
					// already tried, and it failed; just accept this fact
					desiredLocation = new Point(sbounds.x, sbounds.y);
				} else {
					desiredLocation = pt;
					shell.setLocation(pt);
				}
			}
		} else {
			desiredLocation = null;
		}
	}
	

	public void centerShell() {
		if (shell.isDisposed() || centerOverControl.isDisposed())
			return;
		
		Rectangle sbounds = shell.getBounds();
		Rectangle bbounds = centerOverControl.getBounds();
		
		Point pt = centerOverControl.getParent().toDisplay(bbounds.x, bbounds.y);
		if (isHorizontal)
			pt = new Point(pt.x + (bbounds.width - sbounds.width) / 2,
					pt.y - sbounds.height);
		else
			pt = new Point(pt.x - sbounds.width,
					pt.y + (bbounds.height - sbounds.height) / 2);
		
		recenterTo(pt);
	}

	/**
	 * 
	 */
	public void dispose() {
		shell.dispose();
	}

	/**
	 * @return
	 */
	public Shell getShell() {
		return shell;
	}

	/**
	 * 
	 */
	public void restore() {
		if (shell.isVisible()) {
			shell.setVisible(false);
			focusRestorer.restoreFocus();
		} else {
			shell.setVisible(true);
			shell.setFocus();
		}
		clickOutsideCheckTime = System.currentTimeMillis() + 500;
	}

}