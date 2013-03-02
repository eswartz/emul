/*
  StorageException.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.utils;

/**
 * @author Ed
 *
 */
public class StorageException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5894450788658608707L;

	public StorageException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
