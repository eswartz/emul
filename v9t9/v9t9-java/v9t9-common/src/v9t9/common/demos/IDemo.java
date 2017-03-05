/*
  IDemo.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.demos;

import java.net.URI;

/**
 * @author ejs
 *
 */
public interface IDemo {

	/** get the full location of the *.dem */
	URI getURI();

	/** get the path holding the *.dem */
	URI getParentURI();

	/** get the filename */
	String getName();
	
	/** get a description */
	String getDescription();

}
