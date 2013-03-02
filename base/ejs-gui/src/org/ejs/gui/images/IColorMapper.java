/*
  IColorMapper.java

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
public interface IColorMapper {

	/** Return a color index from mapping the RGB pixel 
	 * 
	 * @param pixel pixel in X8R8G8B8 format
	 * @param dist array for receiving distance² from the returned pixel
	 * @return the color index
	 */
	public abstract int mapColor(int pixel, int[] dist);

}