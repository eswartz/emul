/*
  ExitException.java

  (c) 2010-2013 Edward Swartz

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
public class ExitException extends AbortException {

	/**
	 * @param string
	 */
	public ExitException() {
		super("Exit");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 6849411457765574521L;

}
