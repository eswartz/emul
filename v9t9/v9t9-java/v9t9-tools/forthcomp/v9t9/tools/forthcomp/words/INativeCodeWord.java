/*
  INativeCodeWord.java

  (c) 2014 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.ITargetWord;

/**
 * This marks a word that is implemented in native code.
 * Such words are built using the ITargetContext#compile...
 * methods.  Others are built using #buildCell() and #buildChar(). 
 * @author ejs
 *
 */
public interface INativeCodeWord extends ITargetWord {
}
