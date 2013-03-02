/*
  ISoundRangeSelection.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.sound;

import org.eclipse.jface.viewers.ISelection;


/**
 * @author ejs
 *
 */
public interface ISoundRangeSelection extends ISelection, ISoundView {

	ISoundView getSoundView();
	
	int getSelectedStartFrame();
	int getSelectedFrameCount();
}
