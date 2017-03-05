/*
  IImageButtonAreaHandler.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.bars;

import org.eclipse.swt.graphics.Point;

/**
 * This allows extending the behavior of an image button given a particular
 * area of the button.
 * <p/>
 * The handler's methods are called whenever mouse events are detected or refresh() is invoked.
 * @author ejs
 *
 */
public interface IImageButtonAreaHandler {

	/**
	 * Attach self to the button -- for instance, to register image overlay(s)
	 * or property listener(s)
	 * @param button
	 */
	void attach(ImageButton button);
	/**
	 * Detach self from the button, removing image overlay(s) and property listener(s)
	 * @param button
	 */
	void detach(ImageButton button);
	
	/**
	 * @param x
	 * @param y
	 * @param size
	 * @return
	 */
	boolean isInBounds(int x, int y, Point size);
	
	/** Tell whether the area is currently active.  If so, other
	 * queries may be performed.  If not, control passes
	 * to the handler underneath this one, or to the button itself.
	 * @return
	 */
	boolean isActive();

	/**
	 * Tell whether the area acts as a menu.  If true, then button-1 events will
	 * trigger a MenuDetect event.
	 */
	boolean isMenu();

	/**
	 * Get the tooltip for the area
	 * @return string or <code>null</code>
	 */
	String getTooltip();
	
	/**
	 * Called when the area is active and the mouse enters the area
	 */
	void mouseEnter();
	/**
	 * Called when the area is active and the mouse hovers over the area
	 */
	void mouseHover();
	/**
	 * Called when the area is active and the mouse exits the area
	 */
	void mouseExit();
	/**
	 * Called when the area is active and the mouse clicks in the area 
	 * @return true if handled 
	 */
	boolean mouseDown(int button);
	/**
	 * Called when the area is active and the mouse button is released in the area 
	 * @return true if handled 
	 */
	boolean mouseUp(int button);
	
}
