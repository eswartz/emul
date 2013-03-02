/*
  GraphLayout.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package org.ejs.gui.properties;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * @author ejs
 *
 */
public class GraphLayout extends Layout {

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite, int, int, boolean)
	 */
	@Override
	protected Point computeSize(Composite composite, int whint, int hhint,
			boolean flushCache) {
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;

		for (Control kid : composite.getChildren()) {
			Point kidSize = kid.computeSize(-1, -1);
			Point kidOffs = kid.getLocation();
			if (kidOffs.x < minX) minX = kidOffs.x;
			if (kidOffs.y < minY) minY = kidOffs.y;
			if (kidOffs.x + kidSize.x > maxX) maxX = kidOffs.x + kidSize.x;
			if (kidOffs.y + kidSize.y > maxY) maxY = kidOffs.y + kidSize.y;
		}
		
		if (whint > 0) {
			if (whint + minX > maxX)
				maxX = whint + minX;
		}
		if (hhint > 0) {
			if (hhint + minY > maxY)
				maxY = hhint + minY;
		}
		
		return new Point(maxX - minX, maxY - minY);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite, boolean)
	 */
	@Override
	protected void layout(Composite composite, boolean flushCache) {
		
	}

}
