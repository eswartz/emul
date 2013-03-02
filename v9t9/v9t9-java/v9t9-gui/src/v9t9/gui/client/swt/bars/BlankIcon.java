/*
  BlankIcon.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.bars;


/**
 * @author ejs
 *
 */
public class BlankIcon extends ImageBarChild {

	public BlankIcon(final IImageBar parentDrawer, int style) {
		super(parentDrawer, style);
		
		setCursor(null);
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.bars.ImageIconCanvas#isIconMouseable()
	 */
	@Override
	protected boolean isIconMouseable() {
		return false;
	}
	

}
