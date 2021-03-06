/*
  IVdpRealtimeCanvasRenderer.java

  (c) 2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.video;

/**
 * @author ejs
 *
 */
public interface IVdpRealtimeCanvasRenderer extends IVdpCanvasRenderer {

	/**
	 * @param row
	 */
	void renderRow(int row);

}
