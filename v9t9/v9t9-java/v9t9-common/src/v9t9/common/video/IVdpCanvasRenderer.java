/*
  IVdpCanvasRenderer.java

  (c) 2011-2014 Edward Swartz

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
public interface IVdpCanvasRenderer {

	void dispose();
	
	boolean update();
	
	void forceRedraw();

	IVdpCanvas getCanvas();
	
	void refresh();
	
	public interface CanvasListener {
		void modeChanged();
	}
	
	void addListener(CanvasListener listener);
	void removeListener(CanvasListener listener);
}
