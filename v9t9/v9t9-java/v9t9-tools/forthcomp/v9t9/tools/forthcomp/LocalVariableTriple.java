/*
  LocalVariableTriple.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp;

import v9t9.tools.forthcomp.words.LocalVariable;

/**
 * @author ejs
 *
 */
public class LocalVariableTriple {
	public LocalVariableTriple(LocalVariable local) {
		var = local;
	}
	public LocalVariable var;
}
