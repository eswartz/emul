/*
  IGromTargetContext.java

  (c) 2014 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp;

import v9t9.engine.memory.MemoryDomain;

/**
 * @author ejs
 *
 */
public interface IGromTargetContext extends ITargetContext {

	/**
	 * @return
	 */
	int getGP();

	/**
	 * @param gp
	 */
	void setGP(int gp);

	/**
	 * @param grom
	 */
	void setGrom(MemoryDomain grom);

	/**
	 * @return
	 */
	MemoryDomain getGrom();

	/**
	 * @param b
	 */
	void setUseGromDictionary(boolean b);

	/**
	 * @return
	 */
	boolean useGromDictionary();

	/**
	 * 
	 */
	void finishDict();

}
