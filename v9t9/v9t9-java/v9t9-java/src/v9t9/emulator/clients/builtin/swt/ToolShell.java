/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.ejs.coffee.core.utils.PrefUtils;

import v9t9.emulator.common.EmulatorSettings;

public class ToolShell {
	public enum Centering {
		INSIDE,
		OUTSIDE,
	}
	public static class Behavior {
		public Centering centering;
		public boolean dismissOnClickOutside;
		public String boundsPref;
		public Control centerOverControl;
	}

	private Point desiredLocation; 
	private Shell shell;
	private final IFocusRestorer focusRestorer;
	private Behavior behavior;
	private long clickOutsideCheckTime;
	private final boolean isHorizontal;
	private Control toolControl;
	
	public ToolShell(Shell parentShell, 
			IFocusRestorer focusRestorer_,
			boolean isHorizontal,
			Behavior behavior) {
		this.shell = new Shell(parentShell, SWT.TOOL | SWT.RESIZE | SWT.CLOSE | SWT.TITLE);
		this.focusRestorer = focusRestorer_;
		this.behavior = behavior;
		this.isHorizontal = isHorizontal;
		this.clickOutsideCheckTime = System.currentTimeMillis() + 1500;	// let it show up first, so click on the button that created it doesn't kill it
	}
	
	public void init(Control tool) {
		this.toolControl = tool;
		shell.setImage(((Shell)shell.getParent()).getImage());
		shell.setLayout(GridLayoutFactory.fillDefaults().create());
		
		final GridData data = GridDataFactory.fillDefaults().grab(true, true).hint(400, 300).create();
		toolControl.setLayoutData(data);
		
		shell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				toolControl.dispose();
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						focusRestorer.restoreFocus();
					}
				});
			}
		});

		String boundsStr = EmulatorSettings.INSTANCE.getSettings().get(behavior.boundsPref);
		if (boundsStr != null) {
			final Rectangle savedBounds = PrefUtils.readBoundsString(boundsStr);
			if (savedBounds != null) {
				if (behavior.centering != null && behavior.centerOverControl != null)
					shell.setSize(savedBounds.width, savedBounds.height);
				else {
					SwtWindow.adjustRectVisibility(shell, savedBounds);
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							shell.setBounds(savedBounds);
							shell.setLocation(savedBounds.x, savedBounds.y);
						}
					});
				}
			}
		} else {
			shell.pack();
		}
		
		if (behavior.centering != null && behavior.centerOverControl != null) {
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
				data.heightHint = toolControl.getSize().y;
			}
		});
		
		shell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				Rectangle bounds = shell.getBounds();
				String boundsStr = PrefUtils.writeBoundsString(bounds);
				EmulatorSettings.INSTANCE.getSettings().put(behavior.boundsPref, boundsStr);
			}
		});
	}
	
	/**
	 * @return the toolControl
	 */
	public Control getToolControl() {
		return toolControl;
	}
	
	public boolean isKeepCentered() {
		return behavior.centerOverControl != null && behavior.centering != null;
	}
	public boolean isDismissOnClickOutside() {
		return behavior.dismissOnClickOutside;
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
		if (shell.isDisposed() || behavior.centerOverControl == null || behavior.centerOverControl.isDisposed())
			return;
		
		Rectangle sbounds = shell.getBounds();
		Rectangle bbounds = behavior.centerOverControl.getBounds();
		
		Point pt;
		if (behavior.centerOverControl.getParent() != null)
			pt = behavior.centerOverControl.getParent().toDisplay(bbounds.x, bbounds.y);
		else
			pt = new Point(bbounds.x, bbounds.y);
		
		if (isHorizontal) {
			pt = new Point(pt.x + (bbounds.width - sbounds.width) / 2,
					pt.y + (behavior.centering == Centering.INSIDE ? -sbounds.height: bbounds.height));
		}
		else
			pt = new Point(pt.x + (behavior.centering == Centering.INSIDE ? -sbounds.width: bbounds.width),
					pt.y + (bbounds.height - sbounds.height) / 2);
		
		Rectangle dbounds = shell.getDisplay().getBounds();
		if (pt.x < 0)
			pt.x = 0;
		else if (pt.x + sbounds.width > dbounds.x + dbounds.width)
			pt.x = dbounds.x + dbounds.width - sbounds.width;

		if (pt.y < 0)
			pt.y = 0;
		else if (pt.y + sbounds.height > dbounds.y + dbounds.height)
			pt.y = dbounds.y + dbounds.height - sbounds.height;
		
		//System.out.println(sbounds + " / " + bbounds + " / " + pt);
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
		if (!shell.isVisible()) {
			shell.setVisible(true);
			shell.setFocus();
		}
		
		clickOutsideCheckTime = System.currentTimeMillis() + 500;
	}

	/**
	 * 
	 */
	public void toggle() {
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