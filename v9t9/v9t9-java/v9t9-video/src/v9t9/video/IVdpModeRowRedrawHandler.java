/*
  IVdpModeRowRedrawHandler.java

  (c) 2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video;

/**
 * This interface represents redrawing the screen row-by-row. 
 * @author ejs
 *
 */
public interface IVdpModeRowRedrawHandler extends IVdpModeRedrawHandler {
	void updateCanvasRow(int row, int col);
	void updateCanvasBlock(int screenOffs, int col, int row);

}
