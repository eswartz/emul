/*
  ImageProvider.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.bars;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;


public interface ImageProvider {
	void drawImage(GC gc, Rectangle drawRect, Rectangle imgRect);
	Rectangle imageIndexToBounds(int iconIndex);
}