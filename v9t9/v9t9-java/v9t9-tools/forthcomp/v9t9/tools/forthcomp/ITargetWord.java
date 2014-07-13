/*
  ITargetWord.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp;


/**
 * @author ejs
 *
 */
public interface ITargetWord extends IWord {
	
	DictEntry getEntry();

	/**
	 * @param localDP
	 */
	void setHostDp(int localDP);

	int getHostDp();
}
