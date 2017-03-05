/*
  IDither.java

  (c) 2016 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video.imageimport;

import java.awt.image.BufferedImage;

import org.ejs.gui.images.IPaletteMapper;

/**
 * @author ejs
 *
 */
public interface IDither {

	void run(BufferedImage img, IPaletteMapper mapColor);

}
