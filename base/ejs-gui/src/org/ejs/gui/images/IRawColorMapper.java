/*
  IRawColorMapper.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package org.ejs.gui.images;

/**
 * @author ejs
 *
 */
public interface IRawColorMapper {

	int getDistance(int rgb1, int rgb2);
	int getRGBPixel(int rgb);
}
