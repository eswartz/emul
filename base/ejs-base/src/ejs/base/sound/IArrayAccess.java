/*
  IArrayAccess.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.sound;

/**
 * @author ejs
 *
 */
public interface IArrayAccess {

	/**
	 * Get the sample (interleaving channels)
	 * @param absOffs
	 * @return sample, or 0 if out of range
	 */
	float at(int absOffs);

	/**
	 * @return
	 */
	int size();


}